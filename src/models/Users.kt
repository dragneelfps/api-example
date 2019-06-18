package com.example.models

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val password = varchar("password", 255)
}

class User(val name: String, val password: String)

class LoginRegister(val user: String, val password: String)