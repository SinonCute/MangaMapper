package me.hiencao.provider.websites

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import me.hiencao.models.MangaScraperData
import me.hiencao.provider.Scraper
import me.hiencao.models.type.ProviderType

class CMMangaScraper : Scraper() {

    override val provider = ProviderType.CMANGA

    override val baseUrl = "https://cmangam.com"

    override fun getIdFromUrl(url: String): String {
        return url.substringAfterLast("-")
    }

    override val userAgent: String = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"

    override val client: HttpClient by lazy {
        val cookie = runBlocking { getCloudflareCookie(baseUrl) }

        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
                connectTimeoutMillis = 3000
                socketTimeoutMillis = 5000
            }
            install(HttpCache)
            defaultRequest {
                header(
                    HttpHeaders.UserAgent,
                    userAgent
                )
                header(HttpHeaders.Cookie, cookie)
            }
        }
    }

    private val ignoreTags = listOf("manhwa", "manhua")

    override suspend fun fetchSitemapUrls(): List<String> {
        val sitemapContent = client.get("$baseUrl/sitemap_homepage.xml").body<String>()

        val albumUrls = extractUrlsFromSitemap(sitemapContent)
            .filter { it.startsWith("$baseUrl/sitemap_album") }

        val albumContent = albumUrls.map {
            client.get(it).body<String>()
        }

        val allUrls = albumContent.flatMap { extractUrlsFromSitemap(it) }

        return allUrls
    }

    override suspend fun scrapeMangaData(url: String): MangaScraperData.MangaInfo? {
        val id = getIdFromUrl(url)
        val rawData = client.get("$baseUrl/api/get_data_by_id") {
            parameter("id", id)
            parameter("table", "album")
            parameter("data", "info")
        }.body<String>()
        val jsonObject = Gson().fromJson(rawData, JsonObject::class.java)
        val infoObject = Gson().fromJson(jsonObject.get("info").asString, JsonObject::class.java)

        val title = infoObject.get("name").asString
        val altTitles = infoObject.getAsJsonArray("name_other")?.map { it.asString } ?: emptyList()
        val tags = infoObject.getAsJsonArray("tags")?.map { it.asString } ?: emptyList()
        val source = infoObject.get("source").asString

        if (tags.any { tag -> ignoreTags.any { it.equals(tag, ignoreCase = true) } }) {
            return null
        }

        return MangaScraperData.MangaInfo(
            id = id,
            title = title,
            altTitles = altTitles,
            tags = tags,
            additionalInfo = mapOf("source" to source),
        )
    }

    override suspend fun extractChapterInfo(sourceId: String): List<MangaScraperData.MangaChapterInfo> {
        val rawData = client.get("$baseUrl/api/chapter_list") {
            parameter("album", sourceId)
            parameter("page", 1)
            parameter("limit", 1000)
        }
        val chaptersInfo = mutableListOf<MangaScraperData.MangaChapterInfo>()
        val jsonArray = Gson().fromJson(rawData.bodyAsText(), JsonArray::class.java)

        jsonArray.forEach { chapter ->
            val chapterObject = chapter.asJsonObject
            val id = chapterObject.get("id_chapter").asString
            val infoObject = Gson().fromJson(chapterObject.get("info").asString, JsonObject::class.java)
            val chapterNumber = infoObject.get("num").asString

            chaptersInfo.add(
                MangaScraperData.MangaChapterInfo(
                    id = id,
                    chapterNum = chapterNumber,
                )
            )
        }

        return chaptersInfo
    }

    override suspend fun extractChapterContent(chapterId: String): MangaScraperData.MangaChapterContent {
        val rawData = client.get("$baseUrl/api/chapter_image") {
            parameter("chapter", chapterId)
        }
        val jsonObject = Gson().fromJson(rawData.bodyAsText(), JsonObject::class.java)
        val images = jsonObject.getAsJsonArray("image").map { it.asString }

        return MangaScraperData.MangaChapterContent(
            proxyRequired = false,
            headers = emptyMap(),
            images = images,
        )
    }
}