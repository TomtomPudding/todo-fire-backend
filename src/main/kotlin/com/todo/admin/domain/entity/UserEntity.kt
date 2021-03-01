package com.todo.admin.domain.entity

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedTimestamp
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedTimestamp
import com.todo.admin.domain.entity.enum.UserType
import java.util.*

/**
 * User 管理テーブル
 */
@DynamoDBTable(tableName = "User")
data class UserEntity(
    @get: DynamoDBHashKey(attributeName = "user_id")
    var userId: String = "",

    @get: DynamoDBAttribute(attributeName = "firebase_id")
    var firebaseId: String = "",

    @get: DynamoDBAttribute(attributeName = "name")
    var name: String = "",

    @get: DynamoDBAttribute(attributeName = "type")
    @get: DynamoDBTypeConvertedJson
    var type: UserType = UserType.GUEST,

    @get: DynamoDBAttribute(attributeName = "email")
    var email: String = "",

    @get: DynamoDBAttribute(attributeName = "password")
    var password: String = "",

    @get: DynamoDBAttribute(attributeName = "project_ids")
    @get: DynamoDBTypeConvertedJson
    var projectIds: List<String> = listOf(),

    @get: DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
    @get: DynamoDBTypeConvertedTimestamp(timeZone = "JST")
    @get: DynamoDBAttribute(attributeName = "created_at")
    var createdAt: Date? = null,

    @get: DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.ALWAYS)
    @get: DynamoDBTypeConvertedTimestamp(timeZone = "JST")
    @get: DynamoDBAttribute(attributeName = "updated_at")
    var updatedAt: Date? = null
)
