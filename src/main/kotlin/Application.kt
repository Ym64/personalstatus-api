package me.m64diamondstar

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
import me.m64diamondstar.plugins.configureAuthentication
import me.m64diamondstar.plugins.configureRouting
import me.m64diamondstar.status.StatusManager

val status = StatusManager()

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(CORS) {
        // Allow your website
        val hosts = System.getenv("ALLOWED_HOSTS")?.split(",") ?: emptyList()
        hosts.forEach { host ->
            allowHost(host, schemes = listOf("https"))
        }

        // Allow GET for the public endpoint
        allowMethod(HttpMethod.Get)

        // If you ever call authenticated endpoints from the browser
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowCredentials = false
    }

    configureAuthentication()
    configureRouting()
}
