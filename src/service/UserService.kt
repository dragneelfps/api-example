package com.example.service

import com.example.models.LoginRegister
import com.example.models.User
import com.example.models.Users
import com.example.service.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class UserService {
    suspend fun getOrInsertUser(loginRegister: LoginRegister): User {
        return dbQuery {
            var user = Users.select {
                Users.name eq loginRegister.user
            }.firstOrNull()?.let {
                return@let toUser(it)
            }
            if(user == null) {
                Users.insert {
                    it[name] = loginRegister.user
                    it[password] = loginRegister.password
                }
                user = User(loginRegister.user, loginRegister.password)
            }
            return@dbQuery user
        }
    }

    fun toUser(row: ResultRow): User = User(
        name = row[Users.name],
        password = row[Users.password]
    )
}