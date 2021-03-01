package com.todo.admin.domain.entity.enum

import com.grpc.api.Client

enum class ProjectType {
    PRIVATE,
    COMMUNITY;

    fun getType() =
        if (this == COMMUNITY) {
            Client.ProjectType.COMMUNITY
        } else {
            Client.ProjectType.PRIVATE
        }


    fun equalsType(type: Client.ProjectType) =
        type.name == this.name
}
