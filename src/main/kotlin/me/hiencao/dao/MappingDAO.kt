package me.hiencao.dao

import me.hiencao.models.entities.Mapping
import me.hiencao.models.type.ProviderType

interface MappingDAO {
    suspend fun getMappingByMangaId(mangaId: String): Mapping?
    suspend fun getMappingByProvider(providerType: ProviderType, sourceId: String): Mapping?
    suspend fun getMappings(): List<Mapping>
    suspend fun getMappingsCount(): Long
    suspend fun upsertMapping(mangaId: String, providerType: ProviderType, sourceId: String)
    suspend fun deleteMapping(providerType: ProviderType, sourceId: String)
}
