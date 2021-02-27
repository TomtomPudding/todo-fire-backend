package com.todo.admin.app.service

import com.grpc.api.Client
import com.grpc.api.DeleteResponse
import com.grpc.api.ProjectAllTitle
import com.grpc.api.ProjectGroup
import com.grpc.api.ProjectServiceCoroutineGrpc
import com.grpc.api.ProjectUpdateResponse
import com.grpc.api.ToDo
import com.todo.admin.adapter.interceptor.AuthInterceptor
import com.todo.admin.app.repository.ProjectRepository
import com.todo.admin.app.repository.TokenRepository
import com.todo.admin.app.repository.UserRepository
import com.todo.admin.domain.entity.ProjectEntity
import com.todo.admin.domain.expection.GrpcException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.lognet.springboot.grpc.GRpcService
import org.springframework.data.repository.findByIdOrNull
import java.util.*


@GRpcService(interceptors = [AuthInterceptor::class])
@ExperimentalCoroutinesApi
class ProjectGrpcService(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository
) : ProjectServiceCoroutineGrpc.ProjectServiceImplBase() {

    val failedSearchProject = "Project 情報がありません"

    private fun searchChanel(
        userId: String,
        request: Client.ProjectSearchRequest
    ) = GlobalScope.broadcast<Client.ProjectSearchResponse> {
        while (isActive) {
            try {
                tokenRepository.findByIdOrNull(userId) ?: cancel()
                val project = getWritableProject(userId, request.id)
                send {
                    id = project.projectId
                    name = project.name
                    addAllGroups(createGroupResponse(project))
                    type = project.type.getType()
                }
            } catch (e: StatusRuntimeException) {
                cancel(CancellationException(e.message, e))
            }
            delay(30_00)
        }
    }

    private fun searchAllChanel(
        userId: String,
        request: Client.ProjectSearchAllRequest
    ) = GlobalScope.broadcast<Client.ProjectSearchAllResponse> {
        while (isActive) {
            try {
                tokenRepository.findByIdOrNull(userId) ?: cancel()
                val allTitle = getWritableProjects(userId).filterSearchAll(request).map { property ->
                    ProjectAllTitle {
                        id = property.projectId
                        name = property.name
                        type = property.type.getType()
                    }
                }
                send {
                    addAllProjects(allTitle)
                }
            } catch (e: StatusRuntimeException) {
                cancel(CancellationException(e.message, e))
            }
            delay(30_00)
        }
    }

    override suspend fun search(
        request: Client.ProjectSearchRequest,
        responseChannel: SendChannel<Client.ProjectSearchResponse>
    ) {
        val userId = AuthInterceptor.USER_IDENTITY.get()
        val searchChannel = searchChanel(userId, request).openSubscription()
        searchChannel.consumeEach {
            responseChannel.send(it)
        }
    }

    override suspend fun searchAll(
        request: Client.ProjectSearchAllRequest,
        responseChannel: SendChannel<Client.ProjectSearchAllResponse>
    ) {
        val userId = AuthInterceptor.USER_IDENTITY.get()
        val searchChannel = searchAllChanel(userId, request).openSubscription()
        searchChannel.consumeEach {
            responseChannel.send(it)
        }
    }


    override suspend fun update(request: Client.ProjectUpdateRequest): Client.ProjectUpdateResponse {
        val userId = AuthInterceptor.USER_IDENTITY.get()

        val project = getWritableProject(userId, request.id).apply {
            name = request.name
        }
        projectRepository.save(project)
        return ProjectUpdateResponse {
            id = project.projectId
            name = project.name
        }
    }

    override suspend fun register(request: Client.ProjectRegisterRequest): Client.ProjectUpdateResponse {
        val userId = AuthInterceptor.USER_IDENTITY.get()

        val project = ProjectEntity(
            projectId = UUID.randomUUID().toString(),
            name = request.name,
            writer = listOf(userId),
            createdUserId = userId
        )
        projectRepository.save(project)

        // ユーザ更新 所属プロジェクト追加
        val user = userRepository.findByIdOrNull(userId)?.apply {
            projectIds = projectIds.plus(project.projectId)
        }
        user?.run { userRepository.save(this) }
        return ProjectUpdateResponse {
            id = project.projectId
            name = project.name
        }
    }

    override suspend fun delete(request: Client.ProjectDeleteRequest): Client.DeleteResponse {
        val userId = AuthInterceptor.USER_IDENTITY.get()
        getWritableProject(userId, request.id)

        projectRepository.deleteById(request.id)
        return DeleteResponse {
            message = "Project ${request.id}を削除しました。"
        }
    }

    private fun List<ProjectEntity>.filterSearchAll(request: Client.ProjectSearchAllRequest) =
        filter { property ->
            (request.type == Client.ProjectType.UNKNOWN_TYPE ||
                    property.type.equalsType(request.type))
        }.filter { property ->
            (request.color == com.grpc.api.Client.Color.UNKNOWN_COLOR ||
                    property.status.equalsStatus(request.status))
        }.filter { property ->
            property.color.equalsColor(request.color)
        }


    private fun getWritableProjects(userId: String): List<ProjectEntity> =
        userRepository.findByIdOrNull(userId)?.projectIds?.let { ids ->
            projectRepository.findAllById(ids)
        }?.toList() ?: throw GrpcException.runtimeInvalidArgument(failedSearchProject)

    private fun getWritableProject(userId: String, projectId: String): ProjectEntity {
        val project = userRepository.findByIdOrNull(userId)?.projectIds?.let { ids ->
            projectRepository.findAllById(ids)
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchProject)

        return project.firstOrNull {
            it.projectId == projectId && (userId in it.writer)
        } ?: throw GrpcException.runtimeInvalidArgument(failedSearchProject)
    }

    private fun createGroupResponse(project: ProjectEntity): List<Client.ProjectGroup> {
        val allToDo = project.contents.groupBy(ProjectEntity.ToDo::groupId).mapValues { todos ->
            todos.value.map { todo ->
                ToDo {
                    id = todo.id
                    title = todo.title
                    content = todo.content
                }
            }
        }
        return project.group.map { group ->
            ProjectGroup {
                groupId = group.id
                name = group.name
                addAllTodos(allToDo.getOrDefault(groupId, listOf()))
            }
        }
    }

}
