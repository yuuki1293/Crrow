package com.ko610.plugins

import arrow.core.None
import arrow.core.Some
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ko610.util.DBUtil.getIdFromName
import com.ko610.util.DBUtil.passwordValidate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

fun Application.loginRouting(jwkProvider: JwkProvider) {
    val privateKeyString = environment.config.property("jwt.privateKey").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    fun genToken(id: Int): String {
        val publicKey = jwkProvider.get("2f85532c-aaeb-4f74-959b-49143ab1333c").publicKey
        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("id", id)
            .withExpiresAt(Date(System.currentTimeMillis() + 1800000))
            .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
    }

    routing {
        post("/login") {
            val user = call.receive<Login>()
            when (val id = getIdFromName(user.name)) {
                is None -> call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
                is Some -> {
                    if (passwordValidate(id.value, user.password)) {
                        val token = genToken(id.value)
                        call.respond(hashMapOf("token" to token))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
                    }
                }
            }
        }
        authenticate("auth-jwt") {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("id").asInt()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, Your id is $userId. Token is expired at $expiresAt ms.")
            }
        }
        static(".well-known") {
            staticRootFolder = File("certs")
            file("jwks.json")
        }
    }
}

@Serializable
data class Login(val name: String, val password: String)