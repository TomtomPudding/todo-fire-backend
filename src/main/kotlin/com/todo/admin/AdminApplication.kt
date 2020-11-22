package com.todo.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.awt.SystemTray
import kotlin.system.exitProcess

@SpringBootApplication
class AdminApplication

fun main(args: Array<String>) {
	runApplication<AdminApplication>(*args)
}
