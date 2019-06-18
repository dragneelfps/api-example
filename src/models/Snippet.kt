package com.example.models

import org.jetbrains.exposed.sql.Table

object Snippets : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val user = varchar("user", 255)
    val text = varchar("text", 255)
}

data class Snippet(val user: String, val text: String)


data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)
}