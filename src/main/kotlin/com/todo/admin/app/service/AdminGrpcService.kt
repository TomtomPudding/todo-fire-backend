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

    private val invalidUserMessage = "入力が不正です。内容をご確認ください。"
    private val invalidEmptyUserMessage = "ユーザ名を入力してください"

    override suspend fun login(request: Admin.LoginRequest): Admin.LoginResponse {
        if (request.uid.isEmpty()) {
            throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(invalidEmptyUserMessage))
        }
        val user = userRepository.findByIdOrNull(request.uid)
        if (user == null || user.password != request.password) {
            throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(invalidUserMessage))
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

}
