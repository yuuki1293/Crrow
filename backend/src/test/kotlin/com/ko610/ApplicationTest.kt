package com.ko610

import com.ko610.dao.DatabaseFactory
import com.ko610.plugins.Login
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.*
import kotlin.test.*

class ApplicationTest {
    val username = "高専太郎"
    val password = "1jfc21#fc"
    @Test
    fun testLogin() = testApplication {
        DatabaseFactory.init()

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val responseLogin = client.post("/login"){
            contentType(ContentType.Application.Json)
            setBody(Login(username, password))
        }
        assertEquals(HttpStatusCode.OK, responseLogin.status)

        @Serializable
        data class Token(val token: String)
        val body: Token = responseLogin.body()

        val responseHello = client.get("/hello"){
            bearerAuth(body.token)
        }
        assertEquals(HttpStatusCode.OK, responseHello.status)
        println(responseHello.bodyAsText())
    }
}