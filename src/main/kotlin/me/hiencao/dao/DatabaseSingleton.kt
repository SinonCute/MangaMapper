package me.hiencao.dao

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import org.litote.kmongo.reactivestreams.KMongo

object DatabaseSingleton {
    private lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    fun init() {
        client = KMongo.createClient(
            System.getenv("MONGO_URI")
        )
        database = client.getDatabase(
            System.getenv("MONGO_COLLECTION_NAME")
        )
    }
}