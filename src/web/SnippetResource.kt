package com.example.web

import com.example.models.PostSnippet
import com.example.service.SnippetService
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.snippet(snippetService: SnippetService) {
    route("/snippets") {
        get {
            call.respond(snippetService.getAllSnippets())
        }
        authenticate {
            post {
                val post = call.receive<PostSnippet>()
                val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                snippetService.addSnippet(principal.name, post)
                call.respond(mapOf("OK" to true))
            }
            delete {
                val principal = call.principal<UserIdPrincipal>() ?: throw InvalidCredentialsException("No principal")
                val removedSnippets = snippetService.deleteSnippetsByUser(principal.name)
                call.respond(mapOf("snippets" to removedSnippets))
            }
        }
    }
}