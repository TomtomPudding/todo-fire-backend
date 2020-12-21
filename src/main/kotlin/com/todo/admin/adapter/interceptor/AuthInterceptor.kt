package com.todo.admin.adapter.interceptor

import com.google.common.base.Strings.nullToEmpty
import com.todo.admin.app.repository.TokenRepository
import com.todo.admin.domain.expection.OAuth2Exception
import io.grpc.*
import org.lognet.springboot.grpc.GRpcGlobalInterceptor
import org.springframework.core.annotation.Order

@Order(30)
@GRpcGlobalInterceptor
class AuthInterceptor(
    private val tokenRepository: TokenRepository
) : ServerInterceptor {

    private val bearerSize: Int = 7

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        headers: Metadata?,
        next: ServerCallHandler<ReqT, RespT>?
    ): ServerCall.Listener<ReqT> {
        val authHeader = nullToEmpty(headers!![Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)])
        if (!(authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer "))) {
            handleException(OAuth2Exception("トークンを設定してください"), call!!, headers)
        }
        val token = authHeader.substring(bearerSize)

        if (token.isEmpty() || tokenRepository.findByToken(token).isEmpty()) {
            handleException(OAuth2Exception("トークンが不正です"), call!!, headers)
        }

        return next?.startCall(call, headers)!!
    }

    private fun <ReqT : Any?, RespT : Any?> handleException(
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
