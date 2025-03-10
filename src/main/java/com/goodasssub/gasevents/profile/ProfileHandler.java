package com.goodasssub.gasevents.profile;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.commands.profile.NicknameCommand;
import com.goodasssub.gasevents.config.Config;
import com.goodasssub.gasevents.items.VisibilityItem;
import com.goodasssub.gasevents.entities.NametagEntity;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
import com.goodasssub.gasevents.profile.whitelist.WhitelistHandler;
import com.google.gson.JsonSyntaxException;
import com.mongodb.client.MongoCursor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.permission.Permission;
import net.minestom.server.utils.time.TimeUnit;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileHandler {

    private final WhitelistHandler whitelistHandler;

    private Component tabHeader;
    private Component tabFooter;
    private final Main instance;

    public ProfileHandler(Main instance) {
        this.instance = instance;
        this.whitelistHandler = new WhitelistHandler();
        
        var eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addChild(this.getEventNode(instance.getInstanceContainer()));

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        MinestomAdventure.COMPONENT_TRANSLATOR = (c, l) -> c;

        this.setTabFooter();
        this.setTabHeader();

        ScoreboardHandler sidebar = new ScoreboardHandler();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty())
                return;

            Audiences.players().sendPlayerListHeaderAndFooter(tabHeader, tabFooter);

            CompletableFuture.runAsync(() -> {
                Profile.getCache().entrySet().removeIf(entry -> entry.getValue().getPlayer() == null);
                Main.getInstance().getAntiCheat().sendFlaggedAlerts();
            });

            sidebar.update(players.size());
        }).repeat(20, TimeUnit.SERVER_TICK).schedule();
    }

    public EventNode<?> getEventNode(Instance spawnInstance) {
        var eventNode = EventNode.type("players-node", EventFilter.PLAYER);

        eventNode.addListener(PlayerSkinInitEvent.class, event -> {
            if (Main.getInstance().getConfigManager().getConfig().getMojangAuth()) return;
            try {
                PlayerSkin player = PlayerSkin.fromUsername(event.getPlayer().getUsername());
                event.setSkin(player);

            } catch (JsonSyntaxException ignored) {}
        });

        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();

            if (this.whitelistEnabled() && !isPlayerWhitelisted(player.getUuid())) {
                player.kick(Component.text("You are not whitelisted."));
                return;
            }

            List<Punishment> punishments = getActivePlayerPunishments(player.getUuid());

            for (Punishment punishment : punishments) {
                if (punishment.getPunishmentType() != PunishmentType.BAN) continue;

                player.kick(punishment.getMessage());
                return;
            }

            event.setClearChat(true);
            event.setSpawningInstance(spawnInstance);

            Config config = Main.getInstance().getConfigManager().getConfig();

            Pos pos = new Pos(
                config.getNormalSpawnX(),
                config.getNormalSpawnY(),
                config.getNormalSpawnZ()
            );

            player.setRespawnPoint(pos);
        });
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();

            VisibilityItem.getPlayerVisibilityMap().forEach((uuid, bool) -> {
                Player viewer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
                if (viewer == null) return;
                player.addViewer(viewer);
                if (!bool) player.removeViewer(viewer);
            });

            player.getInventory().setItemStack(VisibilityItem.getInventorySlot(), VisibilityItem.getEnabledItem());

            String bar = "<strikethrough>" + " ".repeat(30) + "</strikethrough>";

            // TODO: change to component so you can welcome player

            String joinMessage = """
                <gray>%s</gray>
                <gold>Recommended Settings:</gold>
                <gold>View Distance: </gold><white>16+</white>
                <gold>Simulation Distance: </gold><white>16+</white>
                
                <gold>Coded By: </gold><white>BotMert</white>
                <gray>%s</gray>""".formatted(bar, bar);

            player.sendMessage(instance.getMiniMessage().deserialize(joinMessage));

            CompletableFuture.runAsync(() -> {
                Profile profile = Profile.fromPlayer(player);

                Component playerName = instance.getMiniMessage()
                    .deserialize(String.format("<%s>", profile.getRank().getColor()))
                    .append(Component.text(player.getUsername()));

                player.setDisplayName(playerName);

                new NametagEntity(player);

                // ranks inherit?
                List<Permission> permissions = profile.getRank().getPermissions()
                    .stream()
                    .map(Permission::new)
                    .toList();

                permissions.forEach(player::addPermission);

                if (player.hasPermission("*")) {
                    player.setPermissionLevel(4);
                }

                Config config = Main.getInstance().getConfigManager().getConfig();

                if (player.hasPermission("core.staff")) {
                    Pos pos = new Pos(
                        config.getStaffSpawnX(),
                        config.getStaffSpawnY(),
                        config.getStaffSpawnZ()
                    );

                    player.setRespawnPoint(pos);
                    player.teleport(pos);
                }
            });


        });
        eventNode.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();

            if (player.hasTag(NametagEntity.NAMETAG_TAG)) {
                NametagEntity nametag = player.getTag(NametagEntity.NAMETAG_TAG);
                nametag.remove();
            }

            VisibilityItem.getPlayerVisibilityMap().remove(player.getUuid());
            NicknameCommand.nickedPlayer.remove(player.getUuid());
        });
        eventNode.addListener(PlayerChatEvent.class, event -> {
            Player player = event.getPlayer();

            List<Punishment> punishments = getActivePlayerPunishments(player.getUuid());

            for (Punishment punishment : punishments) {
                if (punishment.getPunishmentType() != PunishmentType.MUTE) continue;

                player.sendMessage(Component.text().appendNewline().append(punishment.getMessage()));

                event.setCancelled(true);
                return;
            }

            Profile profile = Profile.fromUuid(player.getUuid());
            Component formattedName = profile.getFormattedName();

            event.setChatFormat(chatEvent -> formattedName
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(event.getMessage(), NamedTextColor.WHITE)));

//          1.12.2+
//            event.setFormattedMessage(formattedName
//                .append(Component.text(": ", NamedTextColor.GRAY))
//                .append(Component.text(event.getRawMessage(), NamedTextColor.WHITE)));
        });
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            Material materialInHand = player.getItemInMainHand().material();

            if (materialInHand == VisibilityItem.getEnabledItem().material() ||
                materialInHand == VisibilityItem.getDisabledItem().material()) {
                //TODO: change
                VisibilityItem.toggleVisibility(player);
                event.setCancelled(true);
            }
        });

        return eventNode;
    }

    public void setTabHeader() {
        TextComponent.Builder headerBuilder = Component.text();

        final List<String> headerStrings = instance.getConfigManager().getConfig().getTabHeader();

        for (int i = 0; i < headerStrings.size(); i++)
            headerBuilder.append(instance.getMiniMessage().deserialize(headerStrings.get(i) + (i < headerStrings.size() - 1 ? "\n" : "")));
        tabHeader = headerBuilder.build();
    }

    public void setTabFooter() {
        TextComponent.Builder footerBuilder = Component.text();

        final List<String> footerStrings = instance.getConfigManager().getConfig().getTabFooter();
        for (int i = 0; i < footerStrings.size(); i++)
            footerBuilder.append(instance.getMiniMessage().deserialize(footerStrings.get(i) + (i < footerStrings.size() - 1 ? "\n" : "")));
        tabFooter = footerBuilder.build();
    }

    public List<Punishment> getPlayerPunishments(UUID uuid) {
        List<Punishment> punishments = new ArrayList<>();

        var mongo = Main.getInstance().getMongoHandler();

        try (MongoCursor<Document> cursor = mongo.getPunishmentsByTarget(uuid)) {
            cursor.forEachRemaining(punishmentDocument -> {
                UUID punishmentUUID = UUID.fromString(punishmentDocument.getString("uuid"));
                Punishment punishment = new Punishment(punishmentUUID);

                punishments.add(punishment);
            });
        }

        return punishments;
    }

    public List<Punishment> getActivePlayerPunishments(UUID uuid) {
        List<Punishment> punishments = new ArrayList<>();

        var mongo = Main.getInstance().getMongoHandler();

        try (MongoCursor<Document> cursor = mongo.getPunishmentsByTarget(uuid)) {
            cursor.forEachRemaining(punishmentDocument -> {
                UUID punishmentUUID = UUID.fromString(punishmentDocument.getString("uuid"));
                Punishment punishment = new Punishment(punishmentUUID);

                if (punishment.isActive()) {
                    punishments.add(punishment);
                }
            });
        }

        return punishments;
    }

    public boolean isPlayerPunishmentType(UUID uuid, PunishmentType type) {
        var mongo = Main.getInstance().getMongoHandler();

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        try (MongoCursor<Document> cursor = mongo.getPunishmentsByTarget(uuid)) {
            cursor.forEachRemaining(punishmentDocument -> {
                if (atomicBoolean.get()) return;

                UUID punishmentUUID = UUID.fromString(punishmentDocument.getString("uuid"));
                Punishment punishment = new Punishment(punishmentUUID);

                if (punishment.isActive() && punishment.getPunishmentType() == type) {
                    atomicBoolean.set(true);
                }
            });
        }

        return atomicBoolean.get();
    }

    public boolean isPlayerWhitelisted(UUID uuid) {
        String uuidString = String.valueOf(uuid);
        var players = this.whitelistHandler.getWhitelist().getPlayers();

        Main.getInstance().getLogger().info(uuidString);

        return players.entrySet().stream().anyMatch((playerUUID) -> playerUUID.getKey().equals(uuidString));
    }

    public void addPlayerWhitelist(UUID uuid, String playerName) {
        Map<String, String> players = this.whitelistHandler.getWhitelist().getPlayers();
        players.put(String.valueOf(uuid), playerName);
        this.whitelistHandler.getWhitelist().setPlayers(players);
        this.whitelistHandler.saveJson();
    }

    public void removePlayerWhitelist(UUID uuid) {
        Map<String, String> players = this.whitelistHandler.getWhitelist().getPlayers();
        players.remove(String.valueOf(uuid));
        this.whitelistHandler.getWhitelist().setPlayers(players);
        this.whitelistHandler.saveJson();
    }

    public Map<String, String> getWhitelistList() {
        return this.whitelistHandler.getWhitelist().getPlayers();
    }
    
    public void setWhitelistMode(boolean mode) {
        this.whitelistHandler.getWhitelist().setEnabled(mode);
        this.whitelistHandler.saveJson();
    }

    public boolean whitelistEnabled() {
        return this.whitelistHandler.getWhitelist().isEnabled();
    }
}
