package com.ko610.util

import arrow.core.Option
import arrow.core.singleOrNone
import com.ko610.models.Setting
import com.ko610.models.User
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object DBUtil {
    fun getIdFromName(name: String): Option<Int> {
        return transaction {
            User.select {
                User.name eq name
            }.singleOrNone()
        }.map { it[User.id].value }
    }

    fun passwordValidate(id: Int, password: String): Boolean {
        val dbPassword = transaction {
            Setting.select {
                Setting.userId eq id
            }.single()[Setting.password]
        }

        return password == dbPassword
    }
}