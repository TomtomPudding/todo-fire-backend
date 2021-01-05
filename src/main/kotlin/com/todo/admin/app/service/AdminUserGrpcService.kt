package com.todo.admin.app.service

import com.grpc.api.Admin
import com.grpc.api.UserResponse
import com.grpc.api.UserServiceCoroutineGrpc
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lognet.springboot.grpc.GRpcService

@GRpcService
@ExperimentalCoroutinesApi
class AdminUserGrpcService : UserServiceCoroutineGrpc.UserServiceImplBase() {

    // ToDo 未実装
    override suspend fun registerUser(request: Admin.UserRequest): Admin.UserResponse {
        return UserResponse {
            uid = "uid"
            email = request.email
            userName = request.userName
        }
    }

    // ToDo 未実装
    override suspend fun updateUser(request: Admin.UserRequest): Admin.UserResponse {
        return UserResponse {
            uid = "uid"
            email = request.email
            userName = request.userName
        }
    }

}
