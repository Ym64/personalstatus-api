package me.m64diamondstar.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer

fun Application.configureAuthentication() {
    install(Authentication) {
        // Permission level 999: highest access possible, only used for the bot (where the key is stored securely)
        bearer("auth-level-999") {
            authenticate { tokenCredential ->
                if(tokenCredential.token == System.getenv("POST_API_KEY")) {
                    UserIdPrincipal(tokenCredential.token)
                } else null
            }
        }
    }
}