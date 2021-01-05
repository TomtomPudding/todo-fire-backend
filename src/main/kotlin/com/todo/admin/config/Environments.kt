package com.todo.admin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class Environments {

    enum class EnvProfile(val env: String) {
        LOCAL("Local"),
        DEV("Develop"),
        STG("Staging"),
        PRD("Product");

        companion object {
            fun isLocal(env: String): Boolean =
                env.equals((LOCAL.env), ignoreCase = true)

            fun findName(name: String): String =
                when (name) {
                    LOCAL.env -> "Local"
                    DEV.env -> "Dev"
                    STG.env -> "Stg"
                    PRD.env -> "Prd"
                    else -> "Local"
                }
        }
    }

    @Value("\${environments.profile}")
    lateinit var profile: String

    val isLocal: Boolean get() = EnvProfile.isLocal(profile)

    val dynamoDBPrefix: String get() = EnvProfile.findName(profile)
}
