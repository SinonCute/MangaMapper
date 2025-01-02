package me.hiencao.models.entities

import me.hiencao.models.type.ProviderType

data class MangaMapping(
    val providerType: ProviderType,
    val sourceId: String,
)