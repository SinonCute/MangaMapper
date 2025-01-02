package me.hiencao.models.entities

data class Mapping(
    val mangaId: String,
    val providers: List<MangaMapping>,
    val createdAt: Long,
    val updatedAt: Long
)