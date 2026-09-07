package me.m64diamondstar

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.bodylimit.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import me.m64diamondstar.plugins.configureAuthentication
import me.m64diamondstar.plugins.configureRouting
import me.m64diamondstar.status.StatusManager
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val status = StatusManager()

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(CORS) {
        // Allow your website
        val hosts = System.getenv("ALLOWED_HOSTS")?.split(",") ?: emptyList()
        hosts.forEach { host ->
            allowHost(host, schemes = listOf("http", "https"))
        }

        // Allow GET for the public endpoint
        allowMethod(HttpMethod.Get)

        // If you ever call authenticated endpoints from the browser
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowCredentials = false
    }

    install(RequestBodyLimit) {
        bodyLimit { 12 * 1024 } // Allows up to 12KB per request
    }

    install(RateLimit) {
        register(RateLimitName("contact")) {
            rateLimiter(
                limit = 3,
                refillPeriod = 10.minutes,
            )

            requestKey {
                "global-contact"
            }
        }

        register(RateLimitName("status")) {
            rateLimiter(
                limit = 3,
                refillPeriod = 5.seconds,
            )
        }
    }

    configureAuthentication()
    configureRouting()
}
