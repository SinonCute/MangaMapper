package me.hiencao.provider

import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.hiencao.dotenv
import me.hiencao.models.MangaScraperData
import me.hiencao.models.type.ProviderType
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming
import java.io.StringReader

abstract class Scraper {

    open val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    open val client: HttpClient by lazy {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
                connectTimeoutMillis = 3000
                socketTimeoutMillis = 5000
            }
            install(HttpCache)
        }
    }

    abstract val provider: ProviderType
    abstract val baseUrl: String

    /**
     * Get id from URL.
     * @param url The URL to extract the id from.
     */
    abstract fun getIdFromUrl(url: String): String

    /**
     * Fetch all available sitemap URLs for the website.
     * @return A list of sitemap URLs as strings.
     */
    abstract suspend fun fetchSitemapUrls(): List<String>

    /**
     * Scrape manga data from a given URL.
     * @param url The URL to scrape.
     * @return A MangaData object representing the manga data.
     */
    abstract suspend fun scrapeMangaData(url: String): MangaScraperData.MangaInfo?

    /**
     * Extract list of chapters info from a given source ID.
     * @param sourceId The source ID of the manga.
     * @return A list of MangaChapterInfo objects representing the chapters info.
     */
    abstract suspend fun extractChapterInfo(sourceId: String): List<MangaScraperData.MangaChapterInfo>

    /**
     * Extract chapter content from a given chapter ID.
     * @param chapterId The chapter ID of the manga.
     * @return A MangaChapterContent object representing the chapter content.
     */
    abstract suspend fun extractChapterContent(chapterId: String): MangaScraperData.MangaChapterContent


    /**
     * Close the client.
     */
    open fun closeClient() {
        client.close()
    }

    /**
     * Extract URLs from a sitemap XML content.
     * @param xmlContent The XML content of the sitemap.
     * @return A list of URLs as strings.
     */
    fun extractUrlsFromSitemap(xmlContent: String): List<String> {
        val urls = mutableListOf<String>()
        val reader: XmlReader = xmlStreaming.newReader(StringReader(xmlContent))

        while (reader.hasNext()) {
            val eventType = reader.next()
            if (eventType == EventType.START_ELEMENT && reader.localName == "loc") {
                if (reader.next() == EventType.TEXT) {
                    urls.add(reader.text.trim())
                }
            }
        }

        reader.close()
        return urls
    }

    suspend fun getCloudflareCookie(url: String): Cookie {
        val proxyEndpoint = dotenv["PROXY_ENDPOINT"]
            ?: throw IllegalStateException("Missing 'PROXY_ENDPOINT' in .env file")
        val rawResponse = HttpClient().get("$proxyEndpoint/cookies?url=$url")
        val jsonObject = JsonParser.parseString(rawResponse.bodyAsText()).asJsonObject
        val cfClearance = jsonObject["cookies"]?.asJsonPrimitive?.asString
            ?: throw IllegalStateException("Missing 'cookies' field in response")

        return Cookie("cf_clearance", cfClearance)
    }
}