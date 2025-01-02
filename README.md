# Manga Mapping API

This project serves as a mapping service between different manga databases. Currently, it maps manga entries between MangaDex and CManga, and it is designed to scale to support more sites in the future. The project provides an easy way to retrieve and match manga data from multiple sources, including various attributes like title, tags, and more.

If you have any other sites you'd like to integrate into this project, please create an issue and contribute.

The API currently supports mapping manga data between **MangaDex** and **CManga**. Below are examples of how to use the API:

## Future Features
* Support for additional manga providers (e.g., MyAnimeList, AniList, etc.).
* Enhanced caching mechanisms for faster data retrieval.
* Improved error handling and validation for incoming requests.

## Example Requests

#### 1. **Get mapping by MangaDex ID**
```http
GET /api/mangas/{mangaDexId}/mapping
```
Example: `/api/providers/CMANGA/source_123/mapping`

This will return the mapping for the specified provider and provider ID.
#### 2. **Get mapping by provider type and provider ID**
```http
GET /api/providers/{providerType}/{providerId}/mapping
```
Example: `/api/providers/CMANGA/source_123/mapping`

This will return the mapping for the specified provider and provider ID.
#### 3. **Get chapter information from a source**
```http
GET /api/sources/{chapterId}/{providerType}/chapter-content
```
Example: `/api/sources/source_123/CMANGA/chapter-info`

This will return chapter information from the specified source.
#### 4. **Get chapter content from a provider**
```http
GET /api/sources/{chapterId}/{providerType}/chapter-content
```
Example: `/api/sources/chapter_001/CMANGA/chapter-content`

This will return chapter content from the specified provider.

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |


## Contributing
If you want to add additional sites for mapping or have any suggestions, please open an issue or submit a pull request.

To contribute:

1. Fork the repository.
2. Create a new branch for your feature or fix.
3. Make your changes.
4. Submit a pull request with a clear description of what you've changed.