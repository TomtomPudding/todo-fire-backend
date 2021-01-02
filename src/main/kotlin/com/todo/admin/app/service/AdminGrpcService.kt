package com.todo.admin.app.service

import com.grpc.api.FirebaseAdmin
import com.grpc.api.FirebaseAdminServiceCoroutineGrpc
import com.grpc.api.HttpGrpcStatus
import com.grpc.api.LoginResponse
import com.grpc.api.LoginStatusResponse
import com.grpc.api.UserResponse
import com.todo.admin.app.repository.UserRepository
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.security.auth.message.AuthException

@GRpcService
@ExperimentalCoroutinesApi
class AdminGrpcService(
    private val userRepository: UserRepository
) : FirebaseAdminServiceCoroutineGrpc.FirebaseAdminServiceImplBase() {

    private val channels = ConcurrentHashMap.newKeySet<SendChannel<FirebaseAdmin.LoginStatusResponse>>()

    override suspend fun login(request: FirebaseAdmin.LoginRequest): FirebaseAdmin.LoginResponse {
        val user = userRepository.findByIdOrNull(request.uid) ?: throw AuthException("入力が不正です。内容をご確認ください。")
        if (user.password != request.password) throw AuthException("入力が不正です。内容をご確認ください。")

        val securityContext: SecurityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken(
            user.id,
            user.password,
            AuthorityUtils.createAuthorityList("ROLE_USER")
        )
        SecurityContextHolder.setContext(securityContext)

        return LoginResponse {
            uid = request.uid
            email = request.email
            token = user.token
        }
    }

    override suspend fun logout(request: FirebaseAdmin.Empty): FirebaseAdmin.HttpGrpcStatus {
        SecurityContextHolder.clearContext()

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
            val userId = SecurityContextHolder.getContext().authentication?.principal
            val currentStatus = userId?.let { id ->
                if (id !is String) return@let false
                (userRepository.findByIdOrNull(id) != null)
            } ?: false

            try {
                it.send(LoginStatusResponse { status = currentStatus })
            } catch (e: StatusRuntimeException) {
                channels.remove(responseChannel)
            }
        }
    }

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
