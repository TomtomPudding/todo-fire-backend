package com.todo.admin.service.interceptor

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.slf4j.LoggerFactory

class LoggingInterceptor : ServerInterceptor {
    companion object {
        private val log = LoggerFactory.getLogger(LoggingInterceptor::class.java)
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        log.info("method name=${call.methodDescriptor.fullMethodName}")
        return next.startCall(call, headers)
    }
}