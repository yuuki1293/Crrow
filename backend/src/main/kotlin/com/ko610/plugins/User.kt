package com.ko610.plugins

import arrow.core.None
import arrow.core.Some
import com.ko610.models.Setting
import com.ko610.models.User
import com.ko610.util.DBUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

fun Application.userRouting() {
    routing {
        post("/user") {
            try {
                val user = call.receive<PostUser>()
                transaction {
                    addLogger(StdOutSqlLogger)

                    val id = User.insertAndGetId {
                        it[name] = user.name
                        it[birthday] = LocalDate.parse(user.birthday)
                        it[sex] = user.sex
                        it[introduction] = user.introduction
                        it[type] = 1
                    }

                    Setting.insert {
                        it[userId] = id
                        it[nickname] = user.nickname
                        it[icon] = user.icon
                        it[email] = user.email
                        it[school] = user.school
                        it[password] = user.password
                    }
                }
                call.respond(HttpStatusCode.Created)
            } catch (ex: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: BadRequestException){
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        delete("/user") {
            val user = call.receive<Login>()
            when (val id = DBUtil.getIdFromName(user.name)) {
                is None -> call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
                is Some -> {
                    if (DBUtil.passwordValidate(id.value, user.password)) {
                        try {
                            val count = transaction {
                                addLogger(StdOutSqlLogger)

                                Setting.deleteWhere { Setting.userId eq id.value }
                                User.deleteWhere { User.id eq id.value }
                            }

                            if (count != 1)
                                call.respond(HttpStatusCode.NotFound)
                            else
                                call.respond(HttpStatusCode.ResetContent)
                        } catch (ex: Exception) {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
                    }
                }
            }
        }
        authenticate("auth-jwt") {
            get("/user") {
                val principal = call.principal<JWTPrincipal>()
                val id = principal!!.payload.getClaim("id").asInt()

                try {
                    val userProfile = transaction {
                        addLogger(StdOutSqlLogger)

                        val user = User.select { User.id eq id }.single()
                        val setting = Setting.select { Setting.userId eq id }.single()

                        GetUser(
                            name = user[User.name],
                            birthday = user[User.birthday].toString(),
                            sex = user[User.sex],
                            introduction = user[User.introduction],
                            type = user[User.type],
                            coin = user[User.coin],
                            nickname = setting[Setting.nickname],
                            icon = setting[Setting.icon],
                            email = setting[Setting.email],
                            school = setting[Setting.school],
                            range = setting[Setting.range],
                        )
                    }
                    call.respondText(Json.encodeToString(userProfile), ContentType.Text.Plain, HttpStatusCode.OK)
                } catch (ex: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound)
                } catch (ex: Exception) {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
            put("/user") {
                val principal = call.principal<JWTPrincipal>()
                val id = principal!!.payload.getClaim("id").asInt()

                try {
                    val user = call.receive<UpdateUser>()

                    val count = transaction {
                        addLogger(StdOutSqlLogger)

                        User.update({ User.id eq id })
                        {
                            it[birthday] = LocalDate.parse(user.birthday)
                            it[sex] = user.sex
                            it[introduction] = user.introduction
                        }

                        Setting.update({ Setting.userId eq id }) {
                            it[nickname] = user.nickname
                            it[icon] = user.icon
                            it[email] = user.email
                            it[school] = user.school
                            it[range] = user.range
                        }
                    }
                    if (count != 1)
                        call.respond(HttpStatusCode.NotFound)
                    else
                        call.respond(HttpStatusCode.OK)
                } catch (ex: ContentTransformationException) {
                    call.respond(HttpStatusCode.NotFound)
                } catch (ex: Exception) {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}


@Serializable
data class PostUser(
    val name: String,
    val birthday: String,
    val sex: Int,
    val introduction: String,
    val nickname: String,
    val icon: String,
    val email: String?,
    val school: String,
    val password: String
)

@Serializable
data class GetUser(
    val name: String,
    val birthday: String,
    val sex: Int,
    val introduction: String,
    val type: Int,
    val coin: Int,
    val nickname: String,
    val icon: String,
    val email: String?,
    val school: String,
    val range: Int,
)

@Serializable
data class UpdateUser(
    val birthday: String,
    val sex: Int,
    val introduction: String,
    val nickname: String,
    val icon: String,
    val email: String?,
    val school: String,
    val range: Int,
)

