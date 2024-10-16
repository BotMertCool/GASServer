package com.goodasssub.gasevents.profile;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.discordbot.DiscordBot;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.bson.Document;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public class Profile {
    private static final ConcurrentMap<String, Profile> profiles = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Profile> onlineProfiles = new ConcurrentHashMap<>();

    private final String uniqueId;
    private Rank rank;
    private String discordId;
    private String syncCode;

    public Profile(String uniqueId, Rank rank, String discordId, String syncCode) {
        this.uniqueId = uniqueId;
        this.rank = rank;
        this.discordId = discordId;
        this.syncCode = syncCode;
    }

    public static Profile getOrCreateProfileByUUID(String uniqueId) {
        return profiles.computeIfAbsent(uniqueId, Profile::loadOrCreateProfile);
    }

    private static Profile loadOrCreateProfile(String uniqueId) {
        try {
            MongoCollection<Document> profilesCollection = Main.getInstance().getMongoDB().profilesCollection;
            Document query = new Document("uniqueId", uniqueId);
            Document profileDocument = profilesCollection.find(query).first();

            if (profileDocument == null) {
                profileDocument = new Document("uniqueId", uniqueId)
                    .append("rank", Rank.DEFAULT.getName())
                    .append("discordId", null)
                    .append("syncCode", generateSyncCode());

                profilesCollection.insertOne(profileDocument);
            }

            return new Profile(
                profileDocument.getString("uniqueId"),
                Rank.getRankByName(profileDocument.getString("rank")),
                profileDocument.getString("discordId"),
                profileDocument.getString("syncCode")
            );
        } catch (MongoException e) {
            throw new RuntimeException("Could not load profile", e);
        }
    }

    public static Profile getProfileBySyncCode(String syncCode) {
        try {
            MongoCollection<Document> profilesCollection = Main.getInstance().getMongoDB().profilesCollection;
            Document query = new Document("syncCode", syncCode);
            Document profileDocument = profilesCollection.find(query).first();

            if (profileDocument == null) return null;

            Profile profile = new Profile(
                profileDocument.getString("uniqueId"),
                Rank.getRankByName(profileDocument.getString("rank")),
                profileDocument.getString("discordId"),
                profileDocument.getString("syncCode")
            );

            profiles.put(profile.getUniqueId(), profile);
            return profile;
        } catch (MongoException e) {
            Main.getInstance().getLogger().error("An error occurred: {}", e.getMessage());
        }

        return null;
    }

    public void setDiscordId(String discordId) {
        MongoCollection<Document> profilesCollection = Main.getInstance().getMongoDB().profilesCollection;

        this.discordId = discordId;
        this.syncCode = null;

        Document query = new Document("uniqueId", this.uniqueId);
        Document update = new Document("$set", new Document("discordId", this.discordId)
            .append("syncCode", null));

        profilesCollection.updateOne(query, update);
        profiles.put(this.uniqueId, this);
    }

    public void setRank(Rank rank) {
        MongoCollection<Document> profilesCollection = Main.getInstance().getMongoDB().profilesCollection;

        this.rank = rank;

        Document query = new Document("uniqueId", this.uniqueId);
        Document update = new Document("$set", new Document("rank", this.rank.getName()));

        profilesCollection.updateOne(query, update);
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(UUID.fromString(this.uniqueId));

        if (player != null) {
            Component playerName = Main.getInstance().getMiniMessage().deserialize(String.format("<%s>", rank.getColor()))
                .append(Component.text(player.getUsername()));

            player.setDisplayName(playerName);
        }

        profiles.put(this.uniqueId, this);
    }

    public void checkAndUpdateRank() {
        if (this.discordId == null) return;
        Rank newRank = Rank.SYNCED;

        DiscordBot discord = Main.getInstance().getDiscordBot();
        Member member = discord.getMemberById(this.discordId);
        if (member == null) {
            // TODO: Way to unset discord id for this and so you can do unsync command.
            Main.getInstance().getLogger().error("Member is equal to null. {}", this.discordId);
            return;
        }

        List<Role> roles = member.getRoles().collectList().block();
        if (roles == null) {
            Main.getInstance().getLogger().error("Discord roles null. DiscordId: {}", this.discordId);
            return;
        }

        for (Role role : roles) {
            Rank rank = Rank.getRankByRoleId(role.getId().asString());
            if (rank == null) continue;
            if (newRank.getWeight() > rank.getWeight()) continue;
            newRank = rank;
        }

        if (newRank == this.rank) return;

        setRank(newRank);
    }

    public Component getRankPrefix() {
        return Main.getInstance().getMiniMessage().deserialize(this.rank.getPrefix());
    }

    public Component getFormattedName() {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(UUID.fromString(this.uniqueId));
        if (player == null) {
            return Component.text("A fatal error has occured.");
        }

        Component playerName = player.getName();
        if (player.getDisplayName() != null) {
            playerName = player.getDisplayName();
        }

        return this.getRankPrefix()
            .append(Main.getInstance().getMiniMessage().deserialize(String.format("<%s>", rank.getColor())).append(playerName));

    }

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generateSyncCode() {
        final int LENGTH = 6;

        StringBuilder result = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }

}