package com.todo.admin.domain.session

data class UsernamePasswordSession(
    val id: String,
    val password: String,
    val role: String
)