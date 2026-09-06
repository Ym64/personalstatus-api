package me.m64diamondstar.routes

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ContactRequest (
    val name: String,
    val email: String,
    val discord: String?,
    val message: String
)

fun Route.contactRoutes() {
    route("/contact") {
        rateLimit(RateLimitName("contact")) {
            post("webhook") {
                val request = call.receive<ContactRequest>()
                sendToWebhook(
                    System.getenv("CONTACT_WEBHOOK_URL"),
                    request.name,
                    request.email,
                    request.discord,
                    request.message
                )

                call.respondText("OK")
            }
        }
    }
}

@Serializable
data class Embed(
    val title: String,
    val description: String,
    val color: Int
)

@Serializable
data class WebhookPayload(
    val content: String? = null,
    val embeds: List<Embed>? = null
)

private suspend fun sendToWebhook(webhookUrl: String, name: String, email: String, discord: String?, message: String) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    try {
        client.post(webhookUrl) {
            contentType(ContentType.Application.Json)
            setBody(
                WebhookPayload(
                    embeds = listOf(
                        Embed(
                            title = "$name sent a message!",
                            description = "Email: $email\nDiscord: ${discord ?: "N/A"}\nMessage: $message",
                            color = 0x00FF00
                        )
                    )
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}