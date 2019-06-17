package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import com.fasterxml.jackson.databind.*
import io.ktor.auth.jwt.jwt
import io.ktor.jackson.*
import java.lang.RuntimeException
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

class InvalidCredentialsException(message: String): RuntimeException(message)

data class Snippet(val user: String, val text: String)

val snippets = Collections.synchronizedList(
    mutableListOf(
        Snippet("test", "One"),
        Snippet("test", "Two")
    )
)

data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)
}

class User(val name: String, val password: String)

val users = Collections.synchronizedMap(
    listOf(User("test", "test"))
        .associateBy { it.name }
        .toMutableMap()
)

class LoginRegister(val user: String, val password: String)

open class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

fun Application.module() {
    val simpleJWT = SimpleJWT("my-super-secret-for-jwt")
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(Authentication) {
        //        basic {
//            realm = "myrealm"
//            validate { if (it.name == "user" && it.password == "password") UserIdPrincipal("user") else null }
//        }
        jwt {
            verifier(simpleJWT.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }
    install(StatusPages) {
        exception<InvalidCredentialsException>{ exception ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("OK" to false, "error" to (exception.message ?: "")))
        }
    }
    routing {
        route("/snippets") {
            get {
                call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
            }
            authenticate {
                post {
                    val post = call.receive<PostSnippet>()
                    val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                    snippets += Snippet(principal.name, post.snippet.text)
                    call.respond(mapOf("OK" to true))
                }
                delete {
                    val principal = call.principal<UserIdPrincipal>() ?: throw InvalidCredentialsException("No principal")
                    val removedSnippets = snippets.filter { it.user == principal.name }
                    snippets.removeAll{ it.user == principal.name }
                    call.respond(mapOf("snippets" to removedSnippets))
                }
            }
        }

        post("/login-register") {
            val post = call.receive<LoginRegister>()
            val user = users.getOrPut(post.user) { User(post.user, post.password) }
            if(user.password != post.password) throw InvalidCredentialsException("Invalid credentials")
            call.respond(mapOf("token" to simpleJWT.sign(user.name)))
        }

        route("echo/{text}") {
            get {
                call.respond(mapOf("echo" to call.parameters["text"]!!))
            }
        }

    }
}