package me.hiencao.models

import kotlinx.serialization.Serializable

class MangaScraperData {
    @Serializable
    data class MangaInfo(
        val id: String,
        val title: String,
        val altTitles: List<String>,
        val tags: List<String>,
        val status: String = "",
        val chapterNum: Int = 0,
    )

    @Serializable
    data class MangaChapterInfo(
        val chapterNum: String,
        val id: String,
    )

    @Serializable
    data class MangaChapterContent(
        val proxyRequired: Boolean,
        val headers: Map<String, String>,
        val images: List<String>,
    )
}
