package com.ko610

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
    @Test
    fun testLogin() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val responseLogin = client.post("/login"){
            contentType(ContentType.Application.Json)
            setBody(Login("hoge", "fuga"))
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