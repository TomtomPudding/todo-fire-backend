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

    val failedSearchProject = "Project 情報がありません"
    val failedSearchGroup = "Group 情報がありません"

    override suspend fun update(request: Client.GroupUpdateRequest): Client.GroupUpdateResponse {
        val updateUserId = AuthInterceptor.USER_IDENTITY.get()

        val targetProject = getWritableProject(updateUserId, request.id).apply {
            val targetGroup = group.map { group ->
                if (group.id != request.id) return@map group
                group.copy(name = request.name, content = request.content)
            }.run {
                val addGroup = firstOrNull {
                    it.id == request.id
                } ?: throw GrpcException.runtimeInvalidArgument(failedSearchGroup)
                val position = if (request.position < size - 1) request.position else size - 1
                val movedGroup = filter { projectId != request.id }.toMutableList()
                movedGroup.add(position, addGroup)
                movedGroup.toList()
            }
            this.group = targetGroup
        }

        val savedProject = projectRepository.save(targetProject).group.first { it.id == request.id }
        return GroupUpdateResponse {
            id = savedProject.id
            name = savedProject.name
            content = savedProject.content
            color = Client.Color.BLACK // とりあえず
        }
    }

    override suspend fun register(request: Client.GroupRegisterRequest): Client.GroupUpdateResponse {
        val updateUserId = AuthInterceptor.USER_IDENTITY.get()

        val targetProject = getWritableProject(updateUserId, request.id).apply {
            val targetGroup = group.run {
                val groupId = UUID.randomUUID().toString()
                val addGroup = ProjectEntity.Group(id = groupId, name = request.name, content = request.content)
                val position = if (request.position < size - 1) request.position else size - 1
                val movedGroup = toMutableList()
                movedGroup.add(position, addGroup)
                movedGroup.toList()
            }
            this.group = targetGroup
        }

        val savedProject = projectRepository.save(targetProject).group.first { it.id == request.id }
        return GroupUpdateResponse {
            id = savedProject.id
            name = savedProject.name
            content = savedProject.content
            color = Client.Color.BLACK // とりあえず
        }
    }


    override suspend fun delete(request: Client.GroupDeleteRequest): Client.DeleteResponse {
        val updateUserId = AuthInterceptor.USER_IDENTITY.get()
        val targetProject = getWritableProject(updateUserId, request.id).apply {
            this.group = group.filter { it.id != request.id }
        }

        val savedProject = projectRepository.save(targetProject).group.first { it.id == request.id }
        return DeleteResponse {
            message = "Group ${savedProject.id}を削除しました。"
        }
    }

    private fun getWritableProject(userId: String, groupId: String): ProjectEntity {
        val project = userRepository.findByIdOrNull(userId)?.projectIds?.let { ids ->
            projectRepository.findAllById(ids)
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchProject)

        return project.firstOrNull {
            it.group.any { group -> group.id == groupId } && (userId in it.writer)
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchGroup)
    }
}
