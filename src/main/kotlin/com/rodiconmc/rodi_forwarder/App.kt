package com.rodiconmc.rodi_forwarder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val hostMap = mutableMapOf<String, String>()

//Example env: RODIFORWARD="alpha.rodiconmc.com;10.0.0.1:25565 beta.rodiconmc.com;10.0.0.2:25565"

val logger: Logger = LoggerFactory.getLogger("RodiForwarder")

fun main() {
    val assignmentString = System.getenv("RODIFORWARD") ?: throw NullPointerException("Environment variable RODIFORWARD is not set")
    for (pair in assignmentString.split(" ")) {
        val split = pair.split(";")
        hostMap[split[0]] = split[1]
    }

    var port = 25565
    if (System.getenv("LISTENPORT")?.toInt() ?: 0 > 0) {
        port = System.getenv("LISTENPORT").toInt()
    }
    ForwarderServer.run(port)
}
