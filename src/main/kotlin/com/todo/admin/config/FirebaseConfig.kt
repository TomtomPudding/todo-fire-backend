package com.todo.admin.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.FileInputStream
import java.io.IOException


@Configuration
class FireBaseConfig {

    @Bean
    fun createFireBaseApp(): FirebaseApp {
        val serviceAccount = ClassPathResource("key/todo-app-959e2-firebase-adminsdk.json").inputStream
        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("url")
                .build()
        return FirebaseApp.initializeApp(options)
    }
}