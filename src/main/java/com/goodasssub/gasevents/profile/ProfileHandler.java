package com.goodasssub.gasevents.profile;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.commands.profile.NicknameCommand;
import com.goodasssub.gasevents.config.Config;
import com.goodasssub.gasevents.items.VisibilityItem;
import com.goodasssub.gasevents.entities.NametagEntity;
import com.goodasssub.gasevents.rank.Rank;
import com.goodasssub.gasevents.util.SyncUtil;
import com.goodasssub.gasevents.util.UUIDUtil;
import discord4j.core.object.entity.Member;
import lombok.Getter;
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
import net.minestom.server.listener.preplay.LoginListener;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.time.TimeUnit;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public class ProfileHandler {
    private Component tabHeader;
    private Component tabFooter;
    private final Main instance;

    public ProfileHandler(Main instance) {
        this.instance = instance;
        
        var eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addChild(getEventNode(instance.getInstanceContainer()));

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        MinestomAdventure.COMPONENT_TRANSLATOR = (c, l) -> c;

        this.getTabFooter();
        this.getTabHeader();

        ScoreboardHandler sidebar = new ScoreboardHandler();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty())
                return;

            Main.getInstance().getAntiCheat().sendFlaggedAlerts();
            Profile.getCache().entrySet().removeIf(entry -> entry.getValue().getPlayer() == null);
            Audiences.players().sendPlayerListHeaderAndFooter(tabHeader, tabFooter);

            sidebar.update(players.size());
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    public EventNode<?> getEventNode(Instance spawnInstance) {
        var eventNode = EventNode.type("players-node", EventFilter.PLAYER);

        eventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            //TODO: move to prevention.
            event.setCancelled(true);
        });
        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            //TODO: move to prevention.
            event.setCancelled(true);
        });
        eventNode.addListener(PlayerSwapItemEvent.class, event -> {
            //TODO: move to prevention.
            event.setCancelled(true);
        });



        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();


            event.setSpawningInstance(spawnInstance);

            Config config = Main.getInstance().getConfig();

            Pos pos = new Pos(
                config.getSpawnX(),
                config.getSpawnY(),
                config.getSpawnZ()
            );

            player.setRespawnPoint(pos);
        });
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();

            VisibilityItem.playerVisibilityMap.forEach((uuid, bool) -> {
                Player viewer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
                if (viewer != null) {
                    player.addViewer(viewer);
                    if (!bool) player.removeViewer(viewer);
                }
            });

            player.getInventory().setItemStack(0, VisibilityItem.getEnabledItem());

            String bar = "<strikethrough>" + " ".repeat(30) + "</strikethrough>";

            String joinMessage = """
                <gray>%s</gray>
                <gold>Recommended Settings:</gold>
                <gold>View Distance: </gold><white>24+</white>
                <gold>Simulation Distance: </gold><white>24+</white>
                <gray>%s</gray>""".formatted(bar, bar);

            player.sendMessage(instance.getMiniMessage().deserialize(joinMessage));

            Profile profile = Profile.fromUuid(player.getUuid());

            boolean save = false;

            if (profile.getIpAddress() == null) {
                InetSocketAddress address = (InetSocketAddress) player.getPlayerConnection().getRemoteAddress();
                profile.setIpAddress(address.getHostName());
                save = true;
            }

            String profileName = profile.getName();
            if (profileName == null || !profileName.equals(player.getUsername())) {
                profile.setName(player.getUsername());
                save = true;
            }

            if (profile.getDiscordId() != null) {
                profile.checkAndUpdateRank();
                save = true;
            } else {
                player.sendMessage(Component.text("Please sync your minecraft account to your discord account.\n" +
                    "You can do this with the /sync command.", NamedTextColor.RED));
            }

            // TODO: use permissions
            if (profile.getRank().equals(Rank.OWNER)) {
                player.setPermissionLevel(4);
            }

            if (save) profile.save();

            Component playerName = instance.getMiniMessage().deserialize(String.format("<%s>", profile.getRank().getColor()))
                .append(Component.text(player.getUsername()));

            player.setDisplayName(playerName);

            new NametagEntity(player);


        });
        eventNode.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();

            if (player.hasTag(NametagEntity.NAMETAG_TAG)) {
                NametagEntity nametag = player.getTag(NametagEntity.NAMETAG_TAG);
                nametag.remove();
            }

            VisibilityItem.playerVisibilityMap.remove(player.getUuid());
            NicknameCommand.nickedPlayer.remove(player.getUuid());
        });
        eventNode.addListener(PlayerChatEvent.class, event -> {
            Player player = event.getPlayer();

            Profile profile = Profile.fromUuid(player.getUuid());
            Component formattedName = profile.getFormattedName();

            event.setChatFormat(chatEvent -> formattedName
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(event.getMessage(), NamedTextColor.WHITE)));
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

    public void getTabHeader() {
        TextComponent.Builder headerBuilder = Component.text();

        final List<String> headerStrings = instance.getConfig().getTabHeader();

        for (int i = 0; i < headerStrings.size(); i++)
            headerBuilder.append(instance.getMiniMessage().deserialize(headerStrings.get(i) + (i < headerStrings.size() - 1 ? "\n" : "")));
        tabHeader = headerBuilder.build();
    }


    public void getTabFooter() {
        TextComponent.Builder footerBuilder = Component.text();

        final List<String> footerStrings = instance.getConfig().getTabFooter();
        for (int i = 0; i < footerStrings.size(); i++)
            footerBuilder.append(instance.getMiniMessage().deserialize(footerStrings.get(i) + (i < footerStrings.size() - 1 ? "\n" : "")));
        tabFooter = footerBuilder.build();
    }
}
