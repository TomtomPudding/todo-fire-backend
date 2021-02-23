package com.todo.admin.domain.entity

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedTimestamp
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedTimestamp
import com.grpc.api.Client
import com.squareup.moshi.Json
import com.todo.admin.domain.entity.converter.GroupConverter
import com.todo.admin.domain.entity.converter.ToDoConverter
import com.todo.admin.domain.entity.enum.ProjectColor
import com.todo.admin.domain.entity.enum.ProjectStatus
import com.todo.admin.domain.entity.enum.ProjectType
import java.util.*


/**
 * ToDo テーブル
 */
@DynamoDBTable(tableName = "Project")
data class ProjectEntity(
    @get:DynamoDBHashKey(attributeName = "project_id")
    @get:DynamoDBAutoGeneratedKey
    var projectId: String = "",

    @get:DynamoDBAttribute(attributeName = "name")
    var name: String = "",

    @get:DynamoDBAttribute(attributeName = "type")
    @get:DynamoDBTypeConvertedJson
    var type: ProjectType = ProjectType.COMMUNITY,

    @get:DynamoDBAttribute(attributeName = "color")
    @get:DynamoDBTypeConvertedJson
    var color: ProjectColor = ProjectColor.BLACK,

    @get:DynamoDBAttribute(attributeName = "status")
    @get:DynamoDBTypeConvertedJson
    var status: ProjectStatus = ProjectStatus.OPEN,

    @get:DynamoDBTypeConverted(converter = ToDoConverter::class)
    @get:DynamoDBAttribute(attributeName = "contents")
    var contents: List<ToDo> = listOf(),

    @get:DynamoDBTypeConverted(converter = GroupConverter::class)
    @get:DynamoDBAttribute(attributeName = "group")
    var group: List<Group> = listOf(),

    @get:DynamoDBAttribute(attributeName = "writer")
    var writer: List<String> = listOf(),

    @get:DynamoDBAttribute(attributeName = "viewer")
    var viewer: List<String> = listOf(),

    @get:DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
    @get:DynamoDBTypeConvertedTimestamp(timeZone = "JST")
    @get:DynamoDBAttribute(attributeName = "created_at")
    var createdAt: Date = Date(),

    @get:DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.ALWAYS)
    @get:DynamoDBTypeConvertedTimestamp(timeZone = "JST")
    @get:DynamoDBAttribute(attributeName = "updated_at")
    var updatedAt: Date = Date()
) {
    data class Group(
        @get:DynamoDBAutoGeneratedKey
        @get:DynamoDBAttribute(attributeName = "id")
        @field:Json(name = "id") var id: String = "",

        @get:DynamoDBAttribute(attributeName = "name")
        @field:Json(name = "name") var name: String = "",

        @get:DynamoDBAttribute(attributeName = "content")
        @field:Json(name = "content") var content: String = ""
    )

    data class ToDo(
        @get:DynamoDBAutoGeneratedKey
        @get:DynamoDBAttribute(attributeName = "id")
        @field:Json(name = "id") var id: String = "",

        @get:DynamoDBAttribute(attributeName = "title")
        @field:Json(name = "title") var title: String = "",

        @get:DynamoDBAttribute(attributeName = "content")
        @field:Json(name = "content") var content: String = "",

        @get:DynamoDBAttribute(attributeName = "group_id")
        @field:Json(name = "group_id") var groupId: String = "",

        @get:DynamoDBAttribute(attributeName = "created_user_id")
        @field:Json(name = "created_user_id") var createdUserId: String = "",

        @get:DynamoDBAttribute(attributeName = "updated_user_id")
        @field:Json(name = "udpated_user_id") var updatedUserId: String = "",

        @get:DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
        @get:DynamoDBTypeConvertedTimestamp(timeZone = "JST")
        @field:Json(name = "created_at") var createdAt: Date = Date(),

        @get:DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.ALWAYS)
        @get:DynamoDBTypeConvertedTimestamp(timeZone = "JST")
        @field:Json(name = "udpated_at") var updatedAt: Date = Date()
    ) {
        companion object {

            @DynamoDBIgnore
            fun create(
                request: Client.ToDoUpdateRequest,
                updatedUserId: String
            ) =
                ToDo(
                    id = request.id,
                    title = request.title,
                    content = request.content,
                    groupId = request.groupId,
                    updatedUserId = updatedUserId
                )
        }

        @DynamoDBIgnore
        fun of(
            request: Client.ToDoUpdateRequest,
            updatedUserId: String
        ) =
            this.copy(
                id = request.id,
                title = request.title,
                content = request.content,
                groupId = request.groupId,
                updatedUserId = updatedUserId
            )
    }
}