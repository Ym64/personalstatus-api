package me.m64diamondstar.routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.m64diamondstar.status

@Serializable
data class StatusRequest(
    val status: Int
)

fun Route.statusRoutes() {
    route("/status") {

        rateLimit(RateLimitName("status")) {
            get("/get") {
                call.respond(StatusRequest(status.getStatus()))
            }
        }

        authenticate("auth-level-999") {

            post("/set") {
                val request = call.receive<StatusRequest>()
                status.setStatus(request.status)
                call.respond(HttpStatusCode.OK)
            }

        }
    }
}