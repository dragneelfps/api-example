package com.example.web

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.lang.RuntimeException


class InvalidCredentialsException(message: String): RuntimeException(message)


open class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}