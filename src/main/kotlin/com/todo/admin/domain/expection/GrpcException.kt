package com.todo.admin.domain.expection

import io.grpc.Status
import io.grpc.StatusRuntimeException

class GrpcException {
    companion object {
        fun runtimeInvalidArgument(message: String) =
            StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(message))
    }
}