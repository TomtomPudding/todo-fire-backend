package com.todo.admin.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.http.HttpRequest

@RestController
@RequestMapping("admin")
class AdminController {
    @GetMapping("/hello")
    fun hello(): String = "hello"
}