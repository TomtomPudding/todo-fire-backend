package com.todo.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdminApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<AdminApplication>(*args)
}
