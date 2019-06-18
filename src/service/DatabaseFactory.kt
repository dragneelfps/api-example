package com.example.service

import com.example.models.Snippets
import com.example.models.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(hikari())
        transaction {
            create(Users)
            create(Snippets)
            Users.insert {
                it[name] = "test1"
                it[password] = "test1"
            }
            Users.insert {
                it[name] = "test2"
                it[password] = "test2"
            }
            Snippets.insert {
                it[user] = "test1"
                it[text] = "hello world"
            }
            Snippets.insert {
                it[user] = "test1"
                it[text] = "hello world2"
            }
            Snippets.insert {
                it[user] = "test2"
                it[text] = "hakuna"
            }
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction { block() }
    }
}