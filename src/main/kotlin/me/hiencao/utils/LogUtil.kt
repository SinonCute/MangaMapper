package me.hiencao.utils

object LogUtil {

    // ANSI color codes
    private const val RESET = "\u001B[0m"
    private const val BLACK = "\u001B[30m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"
    private const val PURPLE = "\u001B[35m"
    private const val CYAN = "\u001B[36m"
    private const val WHITE = "\u001B[37m"

    // Log levels
    fun debug(message: String) = log(message, BLUE, "DEBUG")
    fun info(message: String) = log(message, GREEN, "INFO")
    fun warning(message: String) = log(message, YELLOW, "WARNING")
    fun error(message: String) = log(message, RED, "ERROR")
    fun custom(message: String, colorCode: String = WHITE) = log(message, colorCode, "CUSTOM")

    // Log function
    private fun log(message: String, color: String, level: String) {
        val timestamp = java.time.LocalDateTime.now()
        println("$color[$timestamp] [$level]: $message$RESET")
    }
}
