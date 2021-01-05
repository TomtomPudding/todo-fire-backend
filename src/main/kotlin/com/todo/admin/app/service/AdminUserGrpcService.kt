package com.todo.admin.app.service

import com.grpc.api.FirebaseAdmin
import com.grpc.api.FirebaseAdminUserServiceCoroutineGrpc
import com.grpc.api.UserResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lognet.springboot.grpc.GRpcService

@GRpcService
@ExperimentalCoroutinesApi
class AdminUserGrpcService : FirebaseAdminUserServiceCoroutineGrpc.FirebaseAdminUserServiceImplBase() {

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
