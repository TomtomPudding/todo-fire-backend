package com.todo.admin.adapter.interceptor

import com.google.common.base.Strings.nullToEmpty
import com.todo.admin.adapter.interceptor.HandlerException.Companion.handleException
import com.todo.admin.app.repository.UserRepository
import com.todo.admin.domain.expection.OAuth2Exception
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.lognet.springboot.grpc.GRpcGlobalInterceptor
import org.springframework.core.annotation.Order
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder

@Order(30)
@GRpcGlobalInterceptor
class AuthInterceptor(
    private val userRepository: UserRepository
) : ServerInterceptor {

    private val bearerSize: Int = 7

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        headers: Metadata?,
        next: ServerCallHandler<ReqT, RespT>?
    ): ServerCall.Listener<ReqT> {
        val authHeader = nullToEmpty(headers!![Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)])
        val userId: Any? = SecurityContextHolder.getContext()?.authentication?.principal
        val token = userRepository.findByIdOrNull(userId.toString())?.token
            ?: handleException(OAuth2Exception("もう一度ログインしてください"), call!!, headers)

        if (!(authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer "))) {
            handleException(OAuth2Exception("トークンを設定してください"), call!!, headers)
        }
        val requestToken = authHeader.substring(bearerSize)

        if (requestToken != token) handleException(OAuth2Exception("トークンが不正です"), call!!, headers)

        return next?.startCall(call, headers)!!
    }
}
