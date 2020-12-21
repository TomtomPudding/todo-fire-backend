package com.todo.admin.app.service

import com.grpc.api.FirebaseAdmin
import com.grpc.api.FirebaseAdminServiceCoroutineGrpc
import com.grpc.api.HttpGrpcStatus
import com.grpc.api.LoginResponse
import com.grpc.api.UserResponse
import com.todo.admin.adapter.interceptor.LoggingInterceptor
import com.todo.admin.repository.UserRepository
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import org.lognet.springboot.grpc.GRpcService
import org.springframework.http.HttpStatus
import java.util.concurrent.ConcurrentHashMap

@GRpcService(interceptors = [LoggingInterceptor::class])
@ExperimentalCoroutinesApi
class AdminGrpcService(
    val userRepository: UserRepository
) : FirebaseAdminServiceCoroutineGrpc.FirebaseAdminServiceImplBase() {

    private val channels = ConcurrentHashMap.newKeySet<SendChannel<FirebaseAdmin.LoginResponse>>()

    override suspend fun login(request: FirebaseAdmin.LoginRequest): FirebaseAdmin.LoginResponse {
//        try {
//            userRepository.save(
//                UserEntity(
//                    id = request.uid,
//                    firebaseId = request.uid,
//                    name = "aaa",
//                    email = request.email,
//                    password = Random.toString()
//                )
//            )
//        } catch (e: Exception) {
//            print(e)
//        }

        return LoginResponse {
            uid = request.uid
            email = request.email
        }
    }

    override suspend fun logout(request: FirebaseAdmin.Empty): FirebaseAdmin.HttpGrpcStatus {
        return HttpGrpcStatus {
            code = HttpStatus.OK.value()
            message = HttpStatus.OK.reasonPhrase
        }
    }

    override suspend fun authState(
        request: FirebaseAdmin.Empty,
        responseChannel: SendChannel<FirebaseAdmin.LoginResponse>
    ) {
        val res = LoginResponse {
//            uid = request.uid
//            email = request.email
        }
        channels.forEach {
            try {
                it.send(res)
            } catch (e: StatusRuntimeException) {
                channels.remove(responseChannel)
            }
        }
    }

    override suspend fun registerUser(request: FirebaseAdmin.UserRequest): FirebaseAdmin.UserResponse {
        return UserResponse {
            uid = "uid"
            email = request.email
            userName = request.userName
        }
    }

    override suspend fun updateUser(request: FirebaseAdmin.UserRequest): FirebaseAdmin.UserResponse {
        return UserResponse {
            uid = "uid"
            email = request.email
            userName = request.userName
        }
    }

}
