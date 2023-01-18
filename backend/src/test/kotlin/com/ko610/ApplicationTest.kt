package com.ko610

import com.ko610.plugins.Login
import com.ko610.plugins.UpdateUser
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    val username = "高専太郎"
    val password = "1jfc21#fc"

    @Test
    fun testLogin() = testApplication {
        init()

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val responseLogin = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(Login(username, password))
        }
        assertEquals(HttpStatusCode.OK, responseLogin.status)

        val body: Token = responseLogin.body()

        val responseHello = client.get("/hello") {
            bearerAuth(body.token)
        }
        assertEquals(HttpStatusCode.OK, responseHello.status)
        println(responseHello.bodyAsText())
    }

    @Test
    fun testUpdateUser() = testApplication {
        init()

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val responseLogin = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(Login(username, password))
        }

        val body = UpdateUser("2004-12-27", 0, "こんにちは", "kosentr", "dummy", "example@example.com", "富山商船", 10)
        val token: Token = responseLogin.body()

        val responsePutUser = client.put("/user") {
            contentType(ContentType.Application.Json)
            setBody(body)
            bearerAuth(token.token)
        }
        assertEquals(responsePutUser.status, HttpStatusCode.OK)
    }
}

@Serializable
data class Token(val token: String)