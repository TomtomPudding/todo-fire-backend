package com.todo.admin.app.service

import com.grpc.api.Admin
import com.grpc.api.AdminServiceCoroutineGrpc
import com.grpc.api.LoginResponse
import com.todo.admin.app.repository.TokenRepository
import com.todo.admin.app.repository.UserRepository
import com.todo.admin.domain.entity.TokenEntity
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@GRpcService
@ExperimentalCoroutinesApi
class AdminGrpcService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository
) : AdminServiceCoroutineGrpc.AdminServiceImplBase() {

    override suspend fun login(request: Admin.LoginRequest): Admin.LoginResponse {
        if (request.uid.isEmpty()) {
            throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(INVALID_EMPTY_USER_MESSAGE))
        }
        val user = userRepository.findByIdOrNull(request.uid)
        if (user == null || user.password != request.password) {
            throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(INVALID_USER_MESSAGE))
        }

        // save token
        val tokenEntity = tokenRepository.findByIdOrNull(user.userId)
            ?: tokenRepository.save(TokenEntity(user.userId, UUID.randomUUID().toString()))

        return LoginResponse {
            uid = request.uid
            email = request.email
            userName = user.name
            token = tokenEntity.token
        }
    }

    companion object {
        private const val INVALID_USER_MESSAGE = "入力が不正です。内容をご確認ください。"
        private const val INVALID_EMPTY_USER_MESSAGE = "ユーザ名を入力してください"
    }
}
