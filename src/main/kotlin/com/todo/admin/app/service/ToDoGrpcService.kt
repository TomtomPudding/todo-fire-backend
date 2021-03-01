package com.todo.admin.app.service

import com.grpc.api.Client
import com.grpc.api.DeleteResponse
import com.grpc.api.ToDoServiceCoroutineGrpc
import com.grpc.api.ToDoUpdateResponse
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
class ToDoGrpcService(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) : ToDoServiceCoroutineGrpc.ToDoServiceImplBase() {

    val failedSearchProject = "Project 情報がありません"
    val failedSearchToDo = "ToDo 情報がありません"

    override suspend fun update(request: Client.ToDoUpdateRequest): Client.ToDoUpdateResponse {
        val userId = AuthInterceptor.USER_IDENTITY.get()

        val targetProject = getWritableProject(userId, request.projectId, request.todoId).apply {
            val contents = this.contents.map { todo ->
                if (request.todoId != todo.id) return@map todo
                todo.of(request, userId)
            }
            if (contents == this.contents) throw GrpcException.runtimeInvalidArgument(failedSearchToDo)
            this.contents = contents
        }

        val savedProject = projectRepository.save(targetProject).contents.first { it.id == request.todoId }
        return ToDoUpdateResponse {
            projectId = targetProject.projectId
            groupId = savedProject.groupId
            todoId = savedProject.id
            title = savedProject.title
            content = savedProject.content
        }
    }


    override suspend fun register(request: Client.ToDoRegisterRequest): Client.ToDoUpdateResponse {
        val userId = AuthInterceptor.USER_IDENTITY.get()
        val newTodoId = UUID.randomUUID().toString()

        val targetProject = getWritableProject(userId, request.projectId).apply {
            val addContent = ProjectEntity.ToDo.create(newTodoId, request, userId)
            this.contents = contents.plus(addContent)
        }

        val savedProject = projectRepository.save(targetProject).contents.first { it.id == newTodoId }
        return ToDoUpdateResponse {
            projectId = targetProject.projectId
            groupId = savedProject.groupId
            todoId = savedProject.id
            title = savedProject.title
            content = savedProject.content
        }
    }

    override suspend fun delete(request: Client.ToDoDeleteRequest): Client.DeleteResponse {
        val userId = AuthInterceptor.USER_IDENTITY.get()

        val targetProject = getWritableProject(userId, request.projectId, request.todoId).apply {
            val contents = this.contents.filter { todo -> request.todoId != todo.id }
            if (this.contents == contents) throw GrpcException.runtimeInvalidArgument(failedSearchToDo)
            this.contents = contents
        }

        val savedProject = projectRepository.save(targetProject).contents.first { it.id == request.todoId }
        return DeleteResponse {
            message = "ToDo ${savedProject.id}を削除しました。"
        }
    }

    private fun getWritableProject(userId: String, projectId: String): ProjectEntity {
        return userRepository.findByIdOrNull(userId)?.projectIds?.let { ids ->
            val id = ids.firstOrNull { it == projectId } ?: return@let null
            val project = projectRepository.findByIdOrNull(id) ?: return@let null
            if (userId in project.writer) return@let project else null
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchProject)
    }

    private fun getWritableProject(userId: String, projectId: String, todoId: String): ProjectEntity {
        return getWritableProject(userId, projectId).let { project ->
            if (todoId in project.contents.map { it.id } && userId in project.writer) {
                project
            } else null
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchToDo)
    }
}
