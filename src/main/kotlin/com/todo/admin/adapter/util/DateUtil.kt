package com.todo.admin.adapter.util

import java.time.LocalDate
import java.time.ZoneId
import java.util.*

object DateUtil {

    fun Date.toLocalDate(): LocalDate {
        return toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun LocalDate.toDate(): Date =
        Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

    // true 入力値の方が前、 false 入力値の方が後
    fun Date.beforeNow(): Boolean = before(Date())
}