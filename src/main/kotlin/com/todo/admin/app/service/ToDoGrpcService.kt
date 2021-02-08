package com.todo.admin.app.service

import com.grpc.api.Client
import com.grpc.api.DeleteResponse
import com.grpc.api.ToDoServiceCoroutineGrpc
import com.grpc.api.ToDoUpdateResponse
import com.todo.admin.app.repository.ProjectRepository
import com.todo.admin.app.repository.UserRepository
import com.todo.admin.app.service.session.SessionManager
import com.todo.admin.domain.entity.ProjectEntity
import com.todo.admin.domain.expection.GrpcException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.repository.findByIdOrNull

@GRpcService
@ExperimentalCoroutinesApi
class ToDoGrpcService(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ToDoServiceCoroutineGrpc.ToDoServiceImplBase() {

    val failedSearchProject = "Project 情報がありません"
    val failedSearchToDo = "ToDo 情報がありません"

    override suspend fun update(request: Client.ToDoUpdateRequest): Client.ToDoUpdateResponse {
        val updateUserId = sessionManager.getLoginUser().id

        val targetProject = getWritableProject(updateUserId, request.id).apply {
            val contents = this.contents.map { todo ->
                if (request.id != todo.id) return@map todo
                todo.of(request, updateUserId)
            }
            if (contents == this.contents) throw GrpcException.runtimeInvalidArgument(failedSearchToDo)
            this.contents = contents
        }

        val savedProject = projectRepository.save(targetProject).contents.first { it.id == request.id }
        return ToDoUpdateResponse {
            id = savedProject.id
            title = savedProject.title
            content = savedProject.content
            groupId = savedProject.groupId
        }
    }


    override suspend fun register(request: Client.ToDoUpdateRequest): Client.ToDoUpdateResponse {
        val updateUserId = sessionManager.getLoginUser().id

        val targetProject = getWritableProject(updateUserId, request.id).apply {
            val addContent = ProjectEntity.ToDo.create(request, updateUserId)
            this.contents = contents.plus(addContent)
        }

        val savedProject = projectRepository.save(targetProject).contents.first { it.id == request.id }
        return ToDoUpdateResponse {
            id = savedProject.id
            title = savedProject.title
            content = savedProject.content
            groupId = savedProject.groupId
        }
    }

    override suspend fun delete(request: Client.ToDoDeleteRequest): Client.DeleteResponse {
        val updateUserId = sessionManager.getLoginUser().id

        val targetProject = getWritableProject(updateUserId, request.id).apply {
            val contents = this.contents.filter { todo -> request.id != todo.id }
            if (this.contents == contents) throw GrpcException.runtimeInvalidArgument(failedSearchToDo)
            this.contents = contents
        }

        val savedProject = projectRepository.save(targetProject).contents.first { it.id == request.id }
        return DeleteResponse {
            message = "ToDo ${savedProject.id}を削除しました。"
        }
    }

    private fun getWritableProject(userId: String, todoId: String): ProjectEntity {
        val project = userRepository.findByIdOrNull(userId)?.projectIds?.let { ids ->
            projectRepository.findAllById(ids)
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchProject)

        return project.firstOrNull {
            (todoId in it.contents.map(ProjectEntity.ToDo::id)) && (userId in it.writer)
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchToDo)
    }
}
