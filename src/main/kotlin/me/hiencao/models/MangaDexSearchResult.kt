package me.hiencao.models

data class MangaDexSearchResult (
    val mangaDexId: String,
    val aniListId: String? = null,
    val malId: String? = null,
    val title: String,
    val altTitles: List<String>,
    val tags: List<String>,
    val yearRelease: Int? = null
)