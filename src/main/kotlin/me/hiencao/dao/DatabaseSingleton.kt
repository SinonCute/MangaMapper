package me.hiencao.dao

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import me.hiencao.dotenv
import org.litote.kmongo.reactivestreams.KMongo

object DatabaseSingleton {
    private lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    fun init() {
        val mongoUri = dotenv["MONGO_URI"]
            ?: throw IllegalStateException("Environment variable MONGO_URI is not set")
        val collectionName = dotenv["MONGO_COLLECTION_NAME"]
            ?: throw IllegalStateException("Environment variable MONGO_COLLECTION_NAME is not set")

        client = KMongo.createClient(mongoUri)
        database = client.getDatabase(collectionName)
    }
}
