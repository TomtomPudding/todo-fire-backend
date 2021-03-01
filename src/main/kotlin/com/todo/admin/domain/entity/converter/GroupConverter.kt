package com.todo.admin.domain.entity.converter

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.todo.admin.domain.entity.ProjectEntity

class GroupConverter : DynamoDBTypeConverter<String?, List<ProjectEntity.Group>> {

    override fun convert(groups: List<ProjectEntity.Group>?): String? {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(
            List::class.java,
            ProjectEntity.Group::class.java
        )
        val listAdapter: JsonAdapter<List<ProjectEntity.Group>> = moshi.adapter(type)
        return listAdapter.toJson(groups)
    }

    override fun unconvert(line: String?): List<ProjectEntity.Group> {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(
            MutableList::class.java,
            ProjectEntity.Group::class.java
        )
        val adapter: JsonAdapter<List<ProjectEntity.Group>> = moshi.adapter(type)
        return line?.let {
            adapter.fromJson(line) ?: listOf()
        } ?: listOf()
    }
}
