package com.todo.admin.app.service

import com.grpc.api.FirebaseAdmin
import com.grpc.api.FirebaseAdminUserServiceCoroutineGrpc
import com.grpc.api.UserResponse
import com.squareup.moshi.Moshi
import com.todo.admin.app.repository.UserRepository
import com.todo.admin.domain.session.UsernamePasswordSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.redis.core.StringRedisTemplate


@GRpcService
@ExperimentalCoroutinesApi
class AdminUserGrpcService(
    private val userRepository: UserRepository,
    private val redisTemplate: StringRedisTemplate
) : FirebaseAdminUserServiceCoroutineGrpc.FirebaseAdminUserServiceImplBase() {

    private val adapter = Moshi.Builder().build().adapter(UsernamePasswordSession::class.java)
    private val invalidUserMessage = "入力が不正です。内容をご確認ください。"

    // ToDo 未実装
    override suspend fun registerUser(request: FirebaseAdmin.UserRequest): FirebaseAdmin.UserResponse {
        return UserResponse {
            uid = "uid"
            email = request.email
            userName = request.userName
        }
    }

    // ToDo 未実装
    override suspend fun updateUser(request: FirebaseAdmin.UserRequest): FirebaseAdmin.UserResponse {
        return UserResponse {
            uid = "uid"
            email = request.email
            userName = request.userName
        }
    }

}
