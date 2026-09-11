package me.m64diamondstar.routes

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ContactRequest (
    val name: String,
    val email: String,
    val discord: String?,
    val message: String,
    val turnstileToken: String,
)

@Serializable
data class TurnstileRequest (
    val secret: String,
    val response: String,
    val remoteIp: String
)

@Serializable
data class TurnstileResponse(
    val success: Boolean,

    @SerialName("error-codes")
    val errorCodes: List<String>? = null,

    val action: String? = null,
    val cdata: String? = null,
    val hostname: String? = null,

    @SerialName("challenge_ts")
    val challengeTs: String? = null
)

fun Route.contactRoutes() {
    route("/contact") {
        rateLimit(RateLimitName("contact")) {
            post("webhook") {
                val request = call.receive<ContactRequest>()
                val token = request.turnstileToken
                val ip = call.request.headers["CF-Connecting-IP"] ?: call.request.headers["X-Forwarded-For"] ?: "unknown"

                val validation = validateTurnstile(token, ip)

                if (!validation) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "turnstile_failed"))
                    return@post
                }

                if (request.name.length > 64 || request.email.length > 64
                    || (request.discord?.length ?: 0) > 64 || request.message.length > 2048
                ) {
                    call.respond(HttpStatusCode.BadRequest, "Input too long")
                    return@post
                }

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

suspend fun validateTurnstile(token: String, remoteIp: String): Boolean {
    try {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val response: TurnstileResponse = client.post("https://challenges.cloudflare.com/turnstile/v0/siteverify") {
            contentType(ContentType.Application.Json)
            setBody(
                TurnstileRequest(
                    secret = System.getenv("TURNSTILE_SECRET_KEY"),
                    response = token,
                    remoteIp = remoteIp
                )
            )
        }.body()

        if (!response.success) {
            throw Exception("Turnstile validation failed: ${response.errorCodes?.joinToString(", ")}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        throw Exception("Turnstile validation failed: ${e.message}")
    }
    return true
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