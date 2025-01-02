package me.hiencao.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.hiencao.dao.impl.MappingDAOImpl
import me.hiencao.models.type.ProviderType
import me.hiencao.utils.getScraper

private val mappingDAO = MappingDAOImpl()

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText(
                """
                Welcome to the Manga API! Below are the available endpoints and examples.
                
                We indexed ${mappingDAO.getMappingsCount()} manga titles from various sources.

                Example API Usage:

                1. To get a mapping by manga ID:
                   - GET /api/mangas/{mangaId}/mapping
                   - Example: /api/mangas/bc3dc29d-9046-485f-9e2a-006056aaa65e/mapping
                   - Response: Returns the mapping for the specified manga ID.

                2. To get a mapping by provider and provider ID:
                   - GET /api/providers/{providerType}/{providerId}/mapping
                   - Example: /api/providers/CMANGA/33/mapping
                   - Response: Returns the mapping for the specified provider and provider ID.

                3. To get chapter information from a source:
                   - GET /api/sources/{sourceId}/{providerType}/chapter-info
                   - Example: /api/sources/33/CMANGA/chapter-info
                   - Response: Returns chapter information from the specified source.

                4. To get chapter content from a provider:
                   - GET /api/sources/{sourceId}/{providerType}/chapter-content
                   - Example: /api/sources/1109549/CMANGA/chapter-content
                   - Response: Returns chapter content from the specified provider.

                Feel free to interact with the API using these endpoints.
                """.trimIndent()
            )
        }

        route("/api") {
            get("/{mangaId}/mapping") {
                val mangaId = call.parameters["mangaId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "mangaId is missing")

                val mapping = mappingDAO.getMappingByMangaId(mangaId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Mapping not found")

                call.respond(mapping)
            }

            get("/providers/{providerType}/{providerId}/mapping") {
                val providerType = call.parameters["providerType"]?.let { ProviderType.valueOf(it.uppercase()) }
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "providerType is missing")

                val providerId = call.parameters["providerId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "providerId is missing")

                val mapping = mappingDAO.getMappingByProvider(providerType, providerId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Mapping not found")

                call.respond(mapping)
            }

            route("/sources") {
                get("/{sourceId}/{providerType}/chapter-info") {
                    val sourceId = call.parameters["sourceId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "sourceId is missing")

                    val providerType = call.parameters["providerType"]?.let { ProviderType.valueOf(it.uppercase()) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "providerType is missing")

                    val scraper = getScraper(providerType)
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Scraper not found for provider type: $providerType")

                    val chapterInfo = scraper.extractChapterInfo(sourceId)
                    call.respond(chapterInfo)
                }

                get("/{chapterId}/{providerType}/chapter-content") {
                    val chapterId = call.parameters["chapterId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "chapterId is missing")

                    val providerType = call.parameters["providerType"]?.let { ProviderType.valueOf(it.uppercase()) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "providerType is missing")

                    val scraper = getScraper(providerType)
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Scraper not found for provider type: $providerType")

                    val chapterContent = scraper.extractChapterContent(chapterId)
                    call.respond(chapterContent)
                }
            }
        }
    }
}
