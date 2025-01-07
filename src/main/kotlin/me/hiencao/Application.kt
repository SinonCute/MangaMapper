package me.hiencao

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.hiencao.config.ConfigManager
import me.hiencao.dao.DatabaseSingleton
import me.hiencao.plugins.configureHTTP
import me.hiencao.plugins.configureRouting
import me.hiencao.plugins.configureSerialization
import me.hiencao.provider.websites.CMMangaScraper
import me.hiencao.task.MangaUpdateTask

val dotenv = dotenv {
    directory = "./"
    ignoreIfMalformed = true
    ignoreIfMissing = true
}

fun main() {

    val port = dotenv["PORT"]?.toIntOrNull() ?: 8085
    val host = "0.0.0.0"

    println("Starting server on $host:$port")

    embeddedServer(
        Netty,
        port = port,
        host = host,
        module = Application::module
    ).start(wait = true)
}
val scrapers = listOf(
    CMMangaScraper()
)

fun Application.module() {
    ConfigManager.init()
    DatabaseSingleton.init()

    configureHTTP()
    configureRouting()
    configureSerialization()

    val task = MangaUpdateTask(scrapers)
    val updateInterval = dotenv["UPDATE_INTERVAL"]?.toLongOrNull()
    task.scheduleUpdate(updateInterval ?: 1440)
}
