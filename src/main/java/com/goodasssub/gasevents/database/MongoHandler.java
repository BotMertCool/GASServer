package com.goodasssub.gasevents.database;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.io.Closeable;
import java.util.UUID;

public class MongoHandler implements Closeable {
    private final MongoClient client;
    private final MongoCollection<Document> profiles;
    private final MongoCollection<Document> punishments;

    public MongoHandler(String uri, String dbName) {
        client = MongoClients.create(uri);

        var database = client.getDatabase(dbName);
        profiles = database.getCollection("profiles");
        punishments = database.getCollection("punishments");

        Main.getInstance().getLogger().info("Connected to the mongodb database: {}", dbName);
    }

    public Document getProfile(UUID uuid) {
        return this.profiles.find(Filters.eq("uuid", String.valueOf(uuid))).first();
    }

    public Document getProfileBySyncCode(String syncCode) {
        return this.profiles.find(Filters.eq("syncCode", String.valueOf(syncCode))).first();
    }

    public void upsertProfile(UUID uuid, Document document) {
        this.profiles.replaceOne(Filters.eq("uuid", String.valueOf(uuid)), document,
            new ReplaceOptions().upsert(true)
        );
    }

    public Document getPunishment(UUID uuid) {
        return this.punishments.find(Filters.eq("uuid", String.valueOf(uuid))).first();
    }

    public MongoCursor<Document> getPunishmentsByTarget(UUID uuid) {
        return this.punishments.find(Filters.eq("uuid", String.valueOf(uuid))).iterator();
    }

    public void upsertPunishment(UUID uuid, Document document) {
        this.punishments.replaceOne(Filters.eq("target_uuid", String.valueOf(uuid)), document,
            new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }
}
