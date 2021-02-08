package com.todo.admin.domain.entity.enum

import com.grpc.api.Client

enum class ProjectStatus {
    OPEN,
    CLOSE;

    fun equalsStatus(status: Client.ProjectStatus) =
        this.name == status.name
}