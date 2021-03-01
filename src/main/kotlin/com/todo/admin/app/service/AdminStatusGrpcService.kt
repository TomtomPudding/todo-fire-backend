package com.todo.admin.app.service

import com.grpc.api.Admin
import com.grpc.api.AdminStatusServiceCoroutineGrpc
import com.grpc.api.HttpGrpcStatus
import com.todo.admin.adapter.interceptor.AuthInterceptor
import com.todo.admin.app.repository.TokenRepository
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

@GRpcService(interceptors = [AuthInterceptor::class])
@ExperimentalCoroutinesApi
class AdminStatusGrpcService(
    private val tokenRepository: TokenRepository
) : AdminStatusServiceCoroutineGrpc.AdminStatusServiceImplBase() {

    override suspend fun logout(request: Admin.Empty): Admin.HttpGrpcStatus {
        val userId = AuthInterceptor.USER_IDENTITY.get()
        tokenRepository.deleteById(userId)

        return HttpGrpcStatus {
            code = HttpStatus.OK.value()
            message = HttpStatus.OK.reasonPhrase
        }
    }

    override suspend fun authState(
        request: Admin.Empty,
        responseChannel: SendChannel<Admin.LoginStatusResponse>
    ) {
        val userId = AuthInterceptor.USER_IDENTITY.get()
        val authStateChannel = authStateChanel(userId).openSubscription()
        authStateChannel.consumeEach {
            responseChannel.send(it)
        }
    }

    private fun authStateChanel(
        userId: String
    ) = GlobalScope.broadcast<Admin.LoginStatusResponse> {
        while (isActive) {
            try {
                tokenRepository.findByIdOrNull(userId) ?: cancel()
                send { status = true }
            } catch (e: StatusRuntimeException) {
                send { status = false }
                cancel(CancellationException(e.message, e))
            }
            delay(SEARCH_DELAY)
        }
    }

    companion object {
        private const val SEARCH_DELAY: Long = 1000L
    }
}
