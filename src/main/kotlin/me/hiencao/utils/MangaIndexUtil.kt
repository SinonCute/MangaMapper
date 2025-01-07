package me.hiencao.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.hiencao.RateLimiter
import me.hiencao.models.MangaDexSearchResult

val client = HttpClient()

suspend fun searchMangaDex(query: String): List<MangaDexSearchResult> {
    RateLimiter.acquire()

    return try {
        val rawResponse = client.get("https://api.mangadex.org/manga") {
            parameter("title", query)
        }.body<String>()

        val jsonObject = Gson().fromJson(rawResponse, JsonObject::class.java)
        val results = jsonObject.getAsJsonArray("data")

        results.mapNotNull { result ->
            val data = result.asJsonObject
            val mangaDexId = data.get("id").asString
            val attributes = data.getAsJsonObject("attributes")
            val links = attributes.takeIf { it.has("links") && it.get("links").isJsonObject }
                ?.getAsJsonObject("links") ?: JsonObject()

            val aniListId = links.takeIf { it.has("al") }?.get("al")?.asString
            val malId = links.takeIf { it.has("mal") }?.get("mal")?.asString
            val title = attributes.getAsJsonObject("title").entrySet().first().value.asString
            val altTitles = attributes.getAsJsonArray("altTitles").map {
                it.asJsonObject.entrySet().first().value.asString
            }
            val tags = attributes.getAsJsonArray("tags").mapNotNull { tag ->
                val nameObj = tag.asJsonObject
                    .getAsJsonObject("attributes")?.getAsJsonObject("name")
                nameObj?.get("en")?.asString
            }
            val yearRelease = attributes.get("year").takeIf { !it.isJsonNull }?.asInt ?: 0

            MangaDexSearchResult(
                mangaDexId = mangaDexId,
                aniListId = aniListId,
                malId = malId,
                title = title,
                altTitles = altTitles,
                tags = tags,
                yearRelease = yearRelease
            )
        }
    } catch (e: Exception) {
        LogUtil.error("Error occurred while searching MangaDex: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}