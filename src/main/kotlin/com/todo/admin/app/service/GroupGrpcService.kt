package com.todo.admin.app.service

import com.grpc.api.Client
import com.grpc.api.DeleteResponse
import com.grpc.api.GroupServiceCoroutineGrpc
import com.grpc.api.GroupUpdateResponse
import com.todo.admin.adapter.interceptor.AuthInterceptor
import com.todo.admin.app.repository.ProjectRepository
import com.todo.admin.app.repository.UserRepository
import com.todo.admin.domain.entity.ProjectEntity
import com.todo.admin.domain.expection.GrpcException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@GRpcService(interceptors = [AuthInterceptor::class])
@ExperimentalCoroutinesApi
class GroupGrpcService(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) : GroupServiceCoroutineGrpc.GroupServiceImplBase() {

    override suspend fun update(request: Client.GroupUpdateRequest): Client.GroupUpdateResponse {
        val updateUserId = AuthInterceptor.USER_IDENTITY.get()

        val targetProject = getWritableProject(updateUserId, request.projectId, request.groupId).apply {
            val targetGroup = group.map { group ->
                if (group.id != request.groupId) return@map group
                group.copy(name = request.name, content = request.content)
            }.run {
                val addGroup = firstOrNull {
                    it.id == request.groupId
                } ?: throw GrpcException.runtimeInvalidArgument(FAILED_SEARCH_GROUP)
                val position = if (request.position < size - 1) request.position else size - 1
                val movedGroup = filter { it.id != request.groupId }.toMutableList()
                movedGroup.add(position, addGroup)
                movedGroup.toList()
            }
            this.group = targetGroup
        }

        val savedProject = projectRepository.save(targetProject).group.first { it.id == request.groupId }
        return GroupUpdateResponse {
            projectId = request.projectId
            groupId = savedProject.id
            name = savedProject.name
            content = savedProject.content
        }
    }

    override suspend fun register(request: Client.GroupRegisterRequest): Client.GroupUpdateResponse {
        val updateUserId = AuthInterceptor.USER_IDENTITY.get()
        val newGroupId = UUID.randomUUID().toString()

        val targetProject = getWritableProject(updateUserId, request.projectId).apply {
            val targetGroup = group.run {
                val addGroup = ProjectEntity.Group(id = newGroupId, name = request.name, content = request.content)
                val position = if (request.position < size - 1) request.position else size - 1
                val movedGroup = toMutableList()
                movedGroup.add(position, addGroup)
                movedGroup.toList()
            }
            this.group = targetGroup
        }

        val savedProject = projectRepository.save(targetProject).group.first { it.id == newGroupId }
        return GroupUpdateResponse {
            projectId = request.projectId
            groupId = savedProject.id
            name = savedProject.name
            content = savedProject.content
        }
    }


    override suspend fun delete(request: Client.GroupDeleteRequest): Client.DeleteResponse {
        val updateUserId = AuthInterceptor.USER_IDENTITY.get()
        val targetProject = getWritableProject(updateUserId, request.projectId, request.groupId).apply {
            this.group = group.filter { it.id != request.groupId }
            this.contents = contents.filter { it.groupId != request.groupId }
        }

        projectRepository.save(targetProject)
        return DeleteResponse {
            message = "Group ${request.groupId}を削除しました。"
        }
    }

    private fun getWritableProject(userId: String, projectId: String): ProjectEntity {
        return userRepository.findByIdOrNull(userId)?.projectIds?.let { ids ->
            val id = ids.firstOrNull { it == projectId } ?: return@let null
            val project = projectRepository.findByIdOrNull(id) ?: return@let null
            if (userId in project.writer) return@let project else null
        } ?: throw GrpcException.runtimeInvalidArgument(FAILED_SEARCH_PROJECT)
    }

    private fun getWritableProject(userId: String, projectId: String, groupId: String): ProjectEntity {
        return getWritableProject(userId, projectId).let { project ->
            if (project.group.any { group -> group.id == groupId } && (userId in project.writer)) {
                return@let project
            } else null
        } ?: throw GrpcException.runtimeInvalidArgument(FAILED_SEARCH_PROJECT)
    }

    companion object {
        private const val FAILED_SEARCH_PROJECT = "Project 情報がありません"
        private const val FAILED_SEARCH_GROUP = "Group 情報がありません"
    }
}
