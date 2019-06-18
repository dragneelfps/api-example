package com.example.service

import com.example.models.PostSnippet
import com.example.models.Snippet
import com.example.models.Snippets
import com.example.service.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*

class SnippetService {

    suspend fun getAllSnippets(): List<Snippet> = dbQuery {
        Snippets.selectAll().map { toSnippet(it) }
    }

    suspend fun getSnippetsByUser(user: String): List<Snippet> = dbQuery {
        Snippets.select {
            (Snippets.user eq user)
        }.mapNotNull { toSnippet(it) }
    }

    suspend fun addSnippet(username: String, postSnippet: PostSnippet) {
        dbQuery {
            Snippets.insert {
                it[user] = username
                it[text] = postSnippet.snippet.text
            }
        }
    }

    suspend fun deleteSnippetsByUser(user: String): List<Snippet> {
        val deletedSnippets = getSnippetsByUser(user)
        dbQuery {
            Snippets.deleteWhere { (Snippets.user eq user) }
        }
        return deletedSnippets
    }

    private fun toSnippet(row: ResultRow): Snippet = Snippet(
        user = row[Snippets.user],
        text = row[Snippets.text]
    )

}