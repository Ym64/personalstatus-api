package me.m64diamondstar.routes

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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

                call.respond(HttpStatusCode.OK)
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
                            title = "_${escapeDiscordMarkdown(name)}_ sent a message!",
                            description =
                                "**Email**\n" +
                                        "```${escapeDiscordMarkdown(email)}```\n\n" +
                                        "**Discord**\n" +
                                        "```${escapeDiscordMarkdown(discord ?: "N/A")}```\n\n" +
                                        "**Message**\n" +
                                        escapeDiscordMarkdown(message),
                            color = 0xD980FF
                        )
                    )
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun escapeDiscordMarkdown(text: String): String {
    return text
        .replace("\\", "\\\\")
        .replace("*", "\\*")
        .replace("_", "\\_")
        .replace("`", "\\`")
        .replace("~", "\\~")
        .replace("|", "\\|")
        .replace(">", "\\>")
        .replace("[", "\\[")
        .replace("]", "\\]")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("#", "\\#")
}