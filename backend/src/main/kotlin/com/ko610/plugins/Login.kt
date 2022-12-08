package com.ko610.plugins

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.client.engine.cio.*

fun Application.loginRouting() {
    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    install(Authentication) {
        oauth("auth-oauth") {
            urlProvider = { "http://localhost:8081/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "microsoft",
                    authorizeUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
                    accessTokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("MICROSOFT_CLIENT_ID"),
                    clientSecret = System.getenv("MICROSOFT_CLIENT_SECRET"),
                    defaultScopes = listOf("https://graph.microsoft.com/User.Read"),
                )
            }
            client = HttpClient(CIO)
        }
    }

    routing {
        authenticate("auth-oauth") {
            get("/login") {}

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondText(principal?.accessToken.toString())
            }
        }
    }
}

data class UserSession(val accessToken: String)