package com.goodasssub.gasevents.profile;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.discordbot.DiscordBot;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.rank.Rank;
import com.goodasssub.gasevents.util.SyncUtil;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Profile {
    @Getter private final static Map<UUID, Profile> cache = new ConcurrentHashMap<>();

    @Getter private final UUID uuid;
    @Getter @Setter private String name;
    @Getter @Setter private String ipAddress;
    @Getter private Rank rank;
    @Getter @Setter private List<Punishment> punishments;
    @Getter private String discordId;
    @Getter @Setter private String syncCode;

    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.name = null;
        this.ipAddress = null;
        this.rank = Rank.DEFAULT;
        this.punishments = new ArrayList<>();
        this.discordId = null;
        this.syncCode = null;

        this.load();
    }

    public void load() {
        Document document = Main.getInstance().getMongoHandler().getProfile(this.uuid);

        if (document == null) {
            this.save();
            return;
        }

        if (this.getName() == null) {
            this.name = document.getString("name");
        }

        this.ipAddress = document.getString("ipAddress");

        this.discordId = document.getString("discordId");
        this.syncCode = document.getString("syncCode");

        this.checkAndUpdateRank();
    }

    public void save() {
        Document document = Main.getInstance().getMongoHandler().getProfile(this.uuid);

        if (document == null) document = new Document();

        document.put("uuid", String.valueOf(uuid));
        document.put("name", this.name);
        document.put("ipAddress", this.ipAddress);
        document.put("rank", this.rank);
        document.put("punishments", this.punishments);
        document.put("discordId", this.discordId);
        document.put("syncCode", this.syncCode);

        Main.getInstance().getMongoHandler().upsertProfile(this.uuid, document);
    }

    public static boolean profileExists(UUID uuid) {
        if (Profile.getCache().containsKey(uuid)) {
            return true;
        }

        Document document = Main.getInstance().getMongoHandler().getProfile(uuid);

        return document != null;
    }

    public static Profile fromUuid(UUID uuid) {
        if (Profile.getCache().containsKey(uuid)) {
            return Profile.getCache().get(uuid);
        }

        return new Profile(uuid);
    }

    public static Profile fromSyncCode(String syncCode) {
        Document document = Main.getInstance().getMongoHandler().getProfileBySyncCode(syncCode);

        if (document == null) return null;

        UUID uuid = UUID.fromString(document.getString("uuid"));

        if (Profile.getCache().containsKey(uuid)) {
            return Profile.getCache().get(uuid);
        }

        return new Profile(uuid);
    }

    public static Profile fromDiscordId(String discordId) {
        Document document = Main.getInstance().getMongoHandler().getProfileByDiscordId(discordId);

        if (document == null) return null;

        UUID uuid = UUID.fromString(document.getString("uuid"));

        if (Profile.getCache().containsKey(uuid)) {
            return Profile.getCache().get(uuid);
        }

        return new Profile(uuid);
    }


    public void setDiscordId(String discordId) {
        this.discordId = discordId;
        this.syncCode = null;

        this.save();
    }

    public void setRank(Rank rank) {
        this.rank = rank;

        Player player = this.getPlayer();
        if (player != null) {
            Component playerName = Main.getInstance().getMiniMessage().deserialize(String.format("<%s>", rank.getColor()))
                .append(Component.text(player.getUsername()));

            player.setDisplayName(playerName);
        }

        this.save();
    }

    public void checkAndUpdateRank() {
        if (this.discordId == null) return;
        Rank newRank = Rank.PERFORMER;

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

        this.setRank(newRank);
    }

    public Component getRankPrefix() {
        return Main.getInstance().getMiniMessage().deserialize(this.rank.getPrefix());
    }

    public Component getFormattedName() {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(this.uuid);
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

    public Player getPlayer() {
        // make so can get offline?
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(this.uuid);
    }
}