package me.hiencao

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

object RateLimiter {
    private const val MAX_REQUESTS_PER_MINUTE = 300
    private const val INTERVAL_MS = 60000L / MAX_REQUESTS_PER_MINUTE

    private val requestChannel = Channel<Unit>(capacity = MAX_REQUESTS_PER_MINUTE)
    private val rateLimiterJob = CoroutineScope(Dispatchers.Default).launch {
        while (isActive) {
            requestChannel.send(Unit)
            delay(INTERVAL_MS)
        }
    }

    suspend fun acquire() {
        requestChannel.receive()
    }

    fun stop() {
        rateLimiterJob.cancel()
        requestChannel.close()
    }
}