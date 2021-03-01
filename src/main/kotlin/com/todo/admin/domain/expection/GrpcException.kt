package com.todo.admin.domain.expection

import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory

object GrpcException {
    private val log = LoggerFactory.getLogger(GrpcException::class.java)

    fun runtimeInvalidArgument(message: String): StatusRuntimeException {
        log.info(message)
        return StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(message))
    }
}
