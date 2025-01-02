package me.hiencao.provider

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.hiencao.models.MangaScraperData
import me.hiencao.models.type.ProviderType
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming
import java.io.StringReader

abstract class Scraper {

    protected val client: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 3000
            socketTimeoutMillis = 5000
        }
        install(HttpCache)
        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
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
}