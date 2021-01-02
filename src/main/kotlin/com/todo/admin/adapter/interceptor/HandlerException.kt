package com.todo.admin.adapter.interceptor

import com.todo.admin.domain.expection.OAuth2Exception
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.Status

@Suppress("UtilityClassWithPublicConstructor")
class HandlerException {

    companion object {

        fun <ReqT : Any?, RespT : Any?> handleException(
            exception: Exception,
            serverCall: ServerCall<ReqT, RespT>,
            metadata: Metadata
        ) {
            when (exception) {
                is IllegalAccessException ->
                    serverCall.close(Status.INVALID_ARGUMENT.withDescription(exception.message), metadata)
                is OAuth2Exception ->
                    serverCall.close(Status.UNAUTHENTICATED.withDescription(exception.message), metadata)
                else -> serverCall.close(Status.UNKNOWN, metadata)
            }.run { ->
                throw exception
            }
        }
    }

}
