package com.example

import com.example.service.DatabaseFactory
import com.example.service.SnippetService
import com.example.service.UserService
import com.example.web.InvalidCredentialsException
import com.example.web.SimpleJWT
import com.example.web.snippet
import com.example.web.user
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val simpleJWT = SimpleJWT("my-super-secret-for-jwt")

    install(DefaultHeaders)
    install(CallLogging)


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

    DatabaseFactory.init()

    val snippetService = SnippetService()
    val userService = UserService()
    routing {

        snippet(snippetService)

        user(userService, simpleJWT)

        route("echo/{text}") {
            get {
                call.respond(mapOf("echo" to call.parameters["text"]!!))
            }
        }

    }
}