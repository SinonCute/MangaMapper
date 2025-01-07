package me.hiencao.task

import kotlinx.coroutines.*
import me.hiencao.MangaMatcher
import me.hiencao.RateLimiter
import me.hiencao.dao.impl.MappingDAOImpl
import me.hiencao.models.MangaScraperData
import me.hiencao.models.type.ProviderType
import me.hiencao.provider.Scraper
import me.hiencao.utils.LogUtil
import java.util.*
import java.util.concurrent.TimeUnit

private const val SIMILARITY_THRESHOLD = 80

private val mappingDAO = MappingDAOImpl()

class MangaUpdateTask(private val scrapers: List<Scraper>) {

    @OptIn(DelicateCoroutinesApi::class)
    fun scheduleUpdate(intervalMinutes: Long) {
        GlobalScope.launch {
            while (true) {
                try {
                    run()
                    delay(TimeUnit.MINUTES.toMillis(intervalMinutes))
                } catch (e: Exception) {
                    println("Periodic update failed: ${e.message}")
                }
            }
        }
    }

    private suspend fun run() {
        for (scraper in scrapers) {
            val results = scraper.fetchSitemapUrls()
            val scrapedResults = scrapeData(scraper, results)

            if (scrapedResults.isNotEmpty()) {
                LogUtil.info("Finished scraping ${scraper.provider} with ${scrapedResults.size} results.")
            }
        }
        RateLimiter.stop()
    }

    private suspend fun scrapeData(
        scraper: Scraper,
        sitemaps: List<String>
    ): List<MangaScraperData.MangaInfo> {
        val scrapedResults = mutableListOf<MangaScraperData.MangaInfo>()

        sitemaps.forEachIndexed { index, url ->
            LogUtil.info("Processing URL ${index + 1}/${sitemaps.size}: $url")
            val id = scraper.getIdFromUrl(url)

            if (mappingDAO.getMappingByProvider(scraper.provider, id) != null) {
                LogUtil.info("Skipping already existing URL: $url")
                return@forEachIndexed
            }

            try {
                val result = scraper.scrapeMangaData(url) ?: return@forEachIndexed
                scrapedResults.add(result)
                handleSpecialCase(result, scraper.provider)
                matchAndUpdate(result, scraper.provider)
            } catch (e: Exception) {
                LogUtil.error("Failed to scrape URL: $url - ${e.message}")
            }
        }

        LogUtil.info("Scraped ${scrapedResults.size} results.")
        return scrapedResults
    }

    private suspend fun handleSpecialCase(mangaInfo: MangaScraperData.MangaInfo, provider: ProviderType) {
        val additionalInfo = mangaInfo.additionalInfo
        when (provider) {
            ProviderType.CMANGA -> {
                val source = additionalInfo["source"] ?: return
                if (!source.startsWith("MangaDex")) return
                val sourceId = source.removePrefix("MangaDex ").trim()
                val mappingId = UUID.randomUUID().toString()
                mappingDAO.upsertMapping(mappingId, ProviderType.CMANGA, mangaInfo.id)
                mappingDAO.upsertMapping(mappingId, ProviderType.MANGADEX, sourceId)
                LogUtil.info("Special case: Matched ${mangaInfo.title} with MangaDex $sourceId")
            }
            else -> {}
        }
    }

    private suspend fun matchAndUpdate(mangaInfo: MangaScraperData.MangaInfo, provider: ProviderType) {
        val matcher = MangaMatcher()
        val matchData = matcher.calculateScore(mangaInfo)
        if (matchData.score >= SIMILARITY_THRESHOLD) {
            val mappingId = UUID.randomUUID().toString()
            mappingDAO.upsertMapping(mappingId, provider, mangaInfo.id)
            mappingDAO.upsertMapping(mappingId, ProviderType.MANGADEX, matchData.manga!!.mangaDexId)

            LogUtil.info("Matched: ${mangaInfo.title} with ${matchData.manga.title} (${matchData.manga.mangaDexId})")
        } else {
            LogUtil.warning("No match found for: ${mangaInfo.title}")
        }
    }
}