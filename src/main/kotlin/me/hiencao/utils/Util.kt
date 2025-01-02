package me.hiencao.utils

import me.hiencao.models.type.ProviderType
import me.hiencao.provider.Scraper
import me.hiencao.scrapers

fun getDirPath(): String {
    return System.getProperty("user.dir")
}

fun getScraper(providerType: ProviderType): Scraper? {
    return scrapers.find { it.provider == providerType }
}