package com.todo.admin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class Environments {

    enum class EnvProfile(name: String) {
        LOCAL("Local"),
        DEV("Develop"),
        STG("Staging"),
        PRD("Product");

        companion object {
            fun isLocal(env: String): Boolean =
                env.equals((LOCAL.name), ignoreCase = true)

            fun findName(name: String): String =
                when (name) {
                    LOCAL.name -> "Local"
                    DEV.name -> "Dev"
                    STG.name -> "Stg"
                    PRD.name -> "Prd"
                    else -> "Local"
                }
        }
    }

    @Value("\${environments.profile}")
    lateinit var profile: String

    val isLocal: Boolean get() = EnvProfile.isLocal(profile)

    val dynamoDBPrefix: String get() = EnvProfile.findName(profile)
}
