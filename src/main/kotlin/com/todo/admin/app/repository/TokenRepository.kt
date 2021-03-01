package com.todo.admin.app.repository

import com.todo.admin.domain.entity.TokenEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface TokenRepository : CrudRepository<TokenEntity, String> {
    fun findByToken(token: String): TokenEntity?
}
