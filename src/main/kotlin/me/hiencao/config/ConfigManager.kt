package me.hiencao.config

import me.hiencao.models.TagMapping
import me.hiencao.utils.getDirPath
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration
import java.io.File

object ConfigManager {
    private lateinit var config: YamlConfiguration
    private lateinit var tagsMapping: List<TagMapping>

    fun init() {
        saveDefaultConfig()
        loadConfig()
    }

    private fun loadConfig() {
        val configFile = File(getDirPath(), "config.yml")
        val tagsMappingFile = File(getDirPath(), "data/tags_mapping.yml")
        config = YamlConfiguration.loadConfiguration(configFile)
        tagsMapping = loadTagMapping(tagsMappingFile)
    }

    private fun saveDefaultConfig() {
        val configFile = File(getDirPath(), "config.yml")
        if (!configFile.exists()) {
            configFile.createNewFile()
            val defaultConfig = ConfigManager::class.java.getResourceAsStream("/config.yml")
            if (defaultConfig != null) {
                configFile.writeBytes(defaultConfig.readAllBytes())
            }
        }

        val genresMappingFile = File(getDirPath(), "data/tags_mapping.yml")
        if (!genresMappingFile.exists()) {
            genresMappingFile.parentFile.mkdirs()
            genresMappingFile.createNewFile()
            val defaultGenresMapping = ConfigManager::class.java.getResourceAsStream("/data/tags_mapping.yml")
            if (defaultGenresMapping != null) {
                genresMappingFile.writeBytes(defaultGenresMapping.readAllBytes())
            }
        }
    }

    fun getConfig(): YamlConfiguration {
        return config
    }

    fun getTagsMapping(): List<TagMapping> {
        return tagsMapping
    }


    private fun loadTagMapping(tagsMappingFile: File): List<TagMapping> {
        val yamlConfig = YamlConfiguration.loadConfiguration(tagsMappingFile)
        val tagsSection = yamlConfig.getConfigurationSection("tags") ?: return emptyList()

        val tagMappings = mutableListOf<TagMapping>()

        for (tag in tagsSection.getKeys(false)) {
            val mappings = tagsSection.getStringList(tag)
            tagMappings.add(TagMapping(tag, mappings))
        }

        return tagMappings
    }
}