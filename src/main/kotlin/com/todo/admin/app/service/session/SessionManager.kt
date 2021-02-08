package com.todo.admin.app.service.session

import com.squareup.moshi.Moshi
import com.todo.admin.config.SessionKey
import com.todo.admin.domain.session.UsernamePasswordSession
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class SessionManager(
    private val redisTemplate: StringRedisTemplate
) {

    private val adapter = Moshi.Builder().build().adapter(UsernamePasswordSession::class.java)

    fun getLoginUser(): UsernamePasswordSession =
        adapter.fromJson(redisTemplate.opsForValue().get(SessionKey.USER.key)!!)
            ?: throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("再ログインしてください"))

}