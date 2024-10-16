package com.goodasssub.gasevents.database;

import com.goodasssub.gasevents.Main;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDB {
    private final MongoClient client;
    public MongoCollection<Document> profilesCollection;

    public MongoDB(String uri, String dbName) {
        client = MongoClients.create(uri);

        profilesCollection = client.getDatabase(dbName).getCollection("profiles");

        Main.getInstance().getLogger().info("Connected to the mongodb database: {}", dbName);
    }

    public void close() {
        client.close();
    }

}
