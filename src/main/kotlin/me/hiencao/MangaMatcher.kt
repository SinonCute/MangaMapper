package me.hiencao

import me.hiencao.config.ConfigManager
import me.hiencao.models.MangaDexSearchResult
import me.hiencao.models.MangaMatch
import me.hiencao.models.MangaScraperData
import me.hiencao.utils.LogUtil
import me.hiencao.utils.searchMangaDex
import org.apache.commons.text.similarity.JaroWinklerSimilarity

class MangaMatcher {
    private val similarityAlgorithm = JaroWinklerSimilarity()

    suspend fun calculateScore(mangaInfo: MangaScraperData.MangaInfo): MangaMatch {
        val searchResults: MutableList<MangaDexSearchResult> = mutableListOf()

        try {
            searchResults.addAll(searchMangaDex(mangaInfo.title))

            if (searchResults.isEmpty() && mangaInfo.altTitles.isNotEmpty()) {
                for (altTitle in mangaInfo.altTitles) {
                    searchResults.addAll(searchMangaDex(altTitle))
                    if (searchResults.isNotEmpty()) break
                }
            }

            if (searchResults.isEmpty()) {
                return MangaMatch(null, 0.0)
            }

            var bestScore = 0.0
            var bestResult: MangaDexSearchResult? = null

            for (result in searchResults) {
                val scores = mutableMapOf<String, Double>()

                scores["Title"] = calculateTitleScore(mangaInfo, result)
                scores["Tags"] = calculateTagsScore(mangaInfo, result)

                val currentScore = scores.values.sum()

                if (currentScore > bestScore) {
                    bestScore = currentScore
                    bestResult = result
                }
            }

            return MangaMatch(bestResult, bestScore)
        } catch (e: Exception) {
            LogUtil.error("Error occurred while searching for: ${mangaInfo.title}")
            LogUtil.error(e.message ?: "Unknown error")
            return MangaMatch(null, 0.0)
        }
    }

    private fun calculateTitleScore(mangaInfo: MangaScraperData.MangaInfo, searchResult: MangaDexSearchResult): Double {
        val titlesToCompare = listOfNotNull(mangaInfo.title) + mangaInfo.altTitles
        val resultTitles = listOf(searchResult.title) + searchResult.altTitles

        var bestScore = 0.0

        for (animeTitle in titlesToCompare) {
            for (resultTitle in resultTitles) {
                val similarity = similarityAlgorithm.apply(animeTitle, resultTitle)
                bestScore = maxOf(bestScore, similarity)
            }
        }

        return minOf(bestScore * 60, 100.0)
    }

    private fun calculateTagsScore(mangaInfo: MangaScraperData.MangaInfo, searchResult: MangaDexSearchResult): Double {
        val mangaTags = mangaInfo.tags
        val resultTags = searchResult.tags

        if (mangaTags.isEmpty() || resultTags.isEmpty()) {
            return 0.0
        }

        val tagMapping = ConfigManager.getTagsMapping()

        val matchedGenres = mangaInfo.tags.filter { tag ->
            val mappedTag = tagMapping.firstOrNull { mapping ->
                mapping.mapping.any { mapped ->
                    similarityAlgorithm.apply(mapped, tag) > 0.8
                }
            }?.tag

            val tagToCompare = mappedTag ?: tag
            searchResult.tags.any { resultTag ->
                similarityAlgorithm.apply(tagToCompare, resultTag) > 0.8
            }
        }

        val score = (matchedGenres.size.toDouble() / mangaTags.size) * 40
        return minOf(score, 100.0)
    }
}