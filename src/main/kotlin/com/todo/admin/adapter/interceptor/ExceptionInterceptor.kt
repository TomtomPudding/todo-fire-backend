package com.todo.admin.adapter.interceptor

import com.todo.admin.domain.expection.OAuth2Exception
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.lognet.springboot.grpc.GRpcGlobalInterceptor
import org.springframework.core.annotation.Order

@Suppress("TooGenericExceptionCaught")
@GRpcGlobalInterceptor
@Order(20)
class ExceptionInterceptor : ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        serverCall: ServerCall<ReqT, RespT>?,
        metadata: Metadata?,
        serverCallHandler: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val listener = serverCallHandler.startCall(serverCall, metadata)
        return ExceptionHandlingServerCallListener<ReqT, RespT>(listener, serverCall!!, metadata!!)
    }

    private inner class ExceptionHandlingServerCallListener<ReqT, RespT> constructor(
        listener: ServerCall.Listener<ReqT>?,
        private val serverCall: ServerCall<ReqT, RespT>,
        private val metadata: Metadata
    ) : SimpleForwardingServerCallListener<ReqT>(listener) {
        override fun onHalfClose() {
            try {
                super.onHalfClose()
            } catch (ex: RuntimeException) {
                handleException(ex, serverCall, metadata)
                throw ex
            }
        }

        override fun onReady() {
            try {
                super.onReady()
            } catch (ex: RuntimeException) {
                handleException(ex, serverCall, metadata)
                throw ex
            }
        }

        private fun handleException(
            exception: RuntimeException,
            serverCall: ServerCall<ReqT, RespT>,
            metadata: Metadata
        ) = when (exception) {
            is IllegalAccessException ->
                serverCall.close(Status.INVALID_ARGUMENT.withDescription(exception.message), metadata)
            is OAuth2Exception ->
                serverCall.close(Status.UNAUTHENTICATED.withDescription(exception.message), metadata)
            else -> serverCall.close(Status.UNKNOWN, metadata)
        }
    }
}
