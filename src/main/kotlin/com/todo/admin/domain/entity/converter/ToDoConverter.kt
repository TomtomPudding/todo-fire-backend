package com.todo.admin.domain.entity.converter

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.todo.admin.domain.entity.ProjectEntity
import java.text.SimpleDateFormat
import java.util.*

class ToDoConverter : DynamoDBTypeConverter<String?, List<ProjectEntity.ToDo>> {

    class DateJsonAdapter : JsonAdapter<Date>() {

        private val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private val sdFormat = SimpleDateFormat(dateFormat, Locale.JAPAN)

        @Synchronized
        @Throws(Exception::class)
        override fun fromJson(reader: JsonReader): Date {
            val string = reader.nextString()
            return sdFormat.parse(string)
        }

        @Synchronized
        @Throws(Exception::class)
        override fun toJson(writer: JsonWriter, value: Date?) {
            writer.value(sdFormat.format(value))
        }

    }

    override fun convert(todo: List<ProjectEntity.ToDo>?): String? {
        val moshi = Moshi.Builder().add(Date::class.java, DateJsonAdapter()).build()
        val type = Types.newParameterizedType(
            List::class.java,
            ProjectEntity.ToDo::class.java
        )
        val listAdapter: JsonAdapter<List<ProjectEntity.ToDo>> = moshi.adapter(type)
        return listAdapter.toJson(todo)
    }

    override fun unconvert(line: String?): List<ProjectEntity.ToDo> {
        val moshi = Moshi.Builder().add(Date::class.java, DateJsonAdapter()).build()
        val type = Types.newParameterizedType(
            MutableList::class.java,
            ProjectEntity.ToDo::class.java
        )
        val adapter: JsonAdapter<List<ProjectEntity.ToDo>> = moshi.adapter(type)
        return line?.let {
            adapter.fromJson(line) ?: listOf()
        } ?: listOf()
    }
}