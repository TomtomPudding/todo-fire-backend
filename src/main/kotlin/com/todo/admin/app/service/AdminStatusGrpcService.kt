package com.todo.admin.app.service

import com.grpc.api.Admin
import com.grpc.api.AdminStatusServiceCoroutineGrpc
import com.grpc.api.HttpGrpcStatus
import com.grpc.api.LoginStatusResponse
import com.todo.admin.adapter.interceptor.AuthInterceptor
import com.todo.admin.app.repository.TokenRepository
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.util.concurrent.ConcurrentHashMap

@GRpcService(interceptors = [AuthInterceptor::class])
@ExperimentalCoroutinesApi
class AdminStatusGrpcService(
    private val tokenRepository: TokenRepository
) : AdminStatusServiceCoroutineGrpc.AdminStatusServiceImplBase() {

    private val channels = ConcurrentHashMap.newKeySet<SendChannel<Admin.LoginStatusResponse>>()


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
        channels.forEach {
            try {
                tokenRepository.findByIdOrNull(userId) ?: channels.remove(responseChannel)
                it.send(LoginStatusResponse { status = true })
            } catch (e: StatusRuntimeException) {
                channels.remove(responseChannel)
            }
            delay(10_00)
        }
    }

}
