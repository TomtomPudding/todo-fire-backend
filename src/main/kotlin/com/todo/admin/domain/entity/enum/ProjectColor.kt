package com.todo.admin.domain.entity.enum

import com.grpc.api.Client

enum class ProjectColor {
    GRAY,
    LIGHT_GRAY,
    SKY_BLUE,
    CYAN,
    RED,
    PINK,
    MAGENTA,
    VIOLET,
    TURQUOISE,
    OLIVE,
    LIME,
    LIME_GREEN,
    BLACK;

    fun equalsColor(color: Client.Color) =
        color.name == this.name
}