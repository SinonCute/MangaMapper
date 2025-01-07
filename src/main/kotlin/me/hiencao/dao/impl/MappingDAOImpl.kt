package me.hiencao.dao.impl

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates.*
import me.hiencao.dao.DatabaseSingleton
import me.hiencao.dao.MappingDAO
import me.hiencao.models.entities.MangaMapping
import me.hiencao.models.entities.Mapping
import me.hiencao.models.type.ProviderType
import me.hiencao.utils.LogUtil
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq

class MappingDAOImpl: MappingDAO {
    private val collection: CoroutineCollection<Mapping> = DatabaseSingleton.database.getCollection("mappings", Mapping::class.java).coroutine

    override suspend fun getMappingByMangaId(mangaId: String): Mapping? {
        return collection.findOne(
            Filters.eq("mangaId", mangaId)
        )
    }

    override suspend fun getMappingByProvider(providerType: ProviderType, sourceId: String): Mapping? {
        return collection.findOne(
            and(
                Filters.eq("providers.providerType", providerType.name),
                Filters.eq("providers.sourceId", sourceId)
            )
        )
    }

    override suspend fun getMappings(): List<Mapping> {
        return collection.find().toList()
    }

    override suspend fun getMappingsCount(): Long {
        return collection.countDocuments()
    }

    override suspend fun upsertMapping(mangaId: String, providerType: ProviderType, sourceId: String) {
        val existingMapping = collection.findOne(
            Filters.and(
                Filters.eq("providers.providerType", providerType),
                Filters.eq("providers.sourceId", sourceId)
            )
        )

        if (existingMapping != null) {
            LogUtil.info("Mapping already exists for providerType: $providerType and sourceId: $sourceId")
            return
        }

        val newMangaMapping = MangaMapping(
            providerType = providerType,
            sourceId = sourceId
        )

        val mangaMapping = collection.findOne(Mapping::mangaId eq mangaId)

        if (mangaMapping == null) {
            collection.insertOne(
                Mapping(
                    mangaId = mangaId,
                    providers = listOf(newMangaMapping),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            collection.updateOne(
                Filters.eq("mangaId", mangaId),
                push("providers", newMangaMapping)
            )

            collection.updateOne(
                Filters.eq("mangaId", mangaId),
                set("updatedAt", System.currentTimeMillis())
            )
        }
    }


    override suspend fun deleteMapping(providerType: ProviderType, sourceId: String) {
        collection.updateOne(
            Filters.and(
                Filters.eq("providers.sourceId", sourceId),
                Filters.eq("providers.providerType", providerType.name)
            ),
            pullByFilter(Filters.eq("providers.providerType", providerType))
        )
    }
}