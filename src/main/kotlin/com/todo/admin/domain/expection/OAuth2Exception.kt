package com.todo.admin.domain.expection

import org.slf4j.LoggerFactory

@Suppress("ClassOrdering")
class OAuth2Exception : RuntimeException {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    constructor(
        message: String
    ) : super(
        message.apply { log.debug(this) }
    )
}
