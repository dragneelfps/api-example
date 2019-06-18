package com.example.web

import com.example.models.LoginRegister
import com.example.service.UserService
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.user(userService: UserService, simpleJWT: SimpleJWT) {
    post("/login-register") {
        val loginRegister = call.receive<LoginRegister>()
        val user = userService.getOrInsertUser(loginRegister)
        if(user.password != loginRegister.password) throw InvalidCredentialsException("Invalid credentials")
        call.respond(mapOf("token" to simpleJWT.sign(user.name)))
    }
}