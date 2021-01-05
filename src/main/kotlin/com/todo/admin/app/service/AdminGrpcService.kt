package com.todo.admin.app.service

import com.grpc.api.FirebaseAdmin
import com.grpc.api.FirebaseAdminServiceCoroutineGrpc
import com.grpc.api.HttpGrpcStatus
import com.grpc.api.LoginResponse
import com.grpc.api.LoginStatusResponse
import com.squareup.moshi.Moshi
import com.todo.admin.app.repository.UserRepository
import com.todo.admin.config.SessionKey
import com.todo.admin.domain.session.UsernamePasswordSession
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.concurrent.ConcurrentHashMap

@GRpcService
@ExperimentalCoroutinesApi
class AdminGrpcService(
    private val userRepository: UserRepository,
    private val redisTemplate: StringRedisTemplate
) : FirebaseAdminServiceCoroutineGrpc.FirebaseAdminServiceImplBase() {

    private val adapter = Moshi.Builder().build().adapter(UsernamePasswordSession::class.java)
    private val invalidUserMessage = "入力が不正です。内容をご確認ください。"

    private val channels = ConcurrentHashMap.newKeySet<SendChannel<FirebaseAdmin.LoginStatusResponse>>()

    override suspend fun login(request: FirebaseAdmin.LoginRequest): FirebaseAdmin.LoginResponse {
        val user = userRepository.findByIdOrNull(request.uid)
        if (user == null || user.password != request.password) {
            throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(invalidUserMessage))
        }

        redisTemplate.opsForValue().set(
            SessionKey.USER.key,
            adapter.toJson(UsernamePasswordSession(user.id, user.password, "ROLE_USER"))
        )
        return LoginResponse {
            uid = request.uid
            email = request.email
            token = user.token
        }
    }

    override suspend fun logout(request: FirebaseAdmin.Empty): FirebaseAdmin.HttpGrpcStatus {
        redisTemplate.delete(SessionKey.USER.key)

        return HttpGrpcStatus {
            code = HttpStatus.OK.value()
            message = HttpStatus.OK.reasonPhrase
        }
    }

    override suspend fun authState(
        request: FirebaseAdmin.Empty,
        responseChannel: SendChannel<FirebaseAdmin.LoginStatusResponse>
    ) {
        channels.forEach {
            val user = adapter.fromJson(redisTemplate.opsForValue().get(SessionKey.USER.key)!!)
            val currentStatus = user?.id != null

            try {
                it.send(LoginStatusResponse { status = currentStatus })
            } catch (e: StatusRuntimeException) {
                channels.remove(responseChannel)
            }
        }
    }

}
