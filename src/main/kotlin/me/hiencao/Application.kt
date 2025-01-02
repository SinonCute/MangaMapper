package me.hiencao

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

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8083,
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
    val updateInterval = System.getenv("UPDATE_INTERVAL")?.toLong()
    task.scheduleUpdate(updateInterval ?: 1440)
}
