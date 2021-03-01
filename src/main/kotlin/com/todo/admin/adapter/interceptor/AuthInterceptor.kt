package com.todo.admin.adapter.interceptor

import com.google.common.base.Strings.nullToEmpty
import com.todo.admin.adapter.interceptor.HandlerException.Companion.handleException
import com.todo.admin.app.repository.TokenRepository
import com.todo.admin.domain.expection.OAuth2Exception
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(30)
@Component
class AuthInterceptor(
    private val tokenRepository: TokenRepository
) : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        headers: Metadata?,
        next: ServerCallHandler<ReqT, RespT>?
    ): ServerCall.Listener<ReqT> {
        val authHeader = nullToEmpty(headers!![Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)])

        if (!(authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer "))) {
            handleException(OAuth2Exception("トークンを設定してください"), call!!, headers)
        }
        val token = authHeader.substring(BEARER_SIZE)
        if (token.isEmpty()) {
            handleException(OAuth2Exception("トークンを設定してください"), call!!, headers)
        }
        val userId = tokenRepository.findByToken(token)?.userId
        if (userId == null) {
            handleException(OAuth2Exception("トークンが不正です"), call!!, headers)
        }

        val context = Context.current().withValue(USER_IDENTITY, userId)
        return Contexts.interceptCall<ReqT, RespT>(context, call, headers, next)
    }

    companion object {
        val USER_IDENTITY: Context.Key<String> = Context.key("identity") // "identity" is just for debugging
        private const val BEARER_SIZE: Int = 7
    }
}
