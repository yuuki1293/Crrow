package com.ko610

import com.ko610.dao.DatabaseFactory
import io.ktor.server.testing.*

fun init() = testApplication {
    DatabaseFactory.init()
}