package com.goodasssub.gasevents.handlers;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.commands.NicknameCommand;
import com.goodasssub.gasevents.items.VisibilityItem;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.profile.Rank;
import com.goodasssub.gasevents.entities.NametagEntity;
import discord4j.core.object.entity.Member;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerHandler {
    private static Component tabHeader;
    private static Component tabFooter;

    public static EventNode<?> getEventNode(Instance spawnInstance) {
        var eventNode = EventNode.type("players-node", EventFilter.PLAYER);

        eventNode.addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(PlayerSwapItemEvent.class, event -> event.setCancelled(true));
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(spawnInstance);
            player.setRespawnPoint(new Pos(434.5, 4.5, 1036.5, -90, -.5f));
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

            player.sendMessage(Main.getInstance().getMiniMessage().deserialize(joinMessage));

            CompletableFuture.runAsync(() -> {
                long profileStartTime = System.currentTimeMillis();
                Profile profile = Profile.getOrCreateProfileByUUID(player.getUuid().toString());

                if (profile == null) {
                    Main.getInstance().getLogger().error("Error profile null: {}", player.getUsername());
                    player.sendMessage(Component.text("A fatal error has occurred whilst loading your profile.", NamedTextColor.RED));
                    return;
                }

                if (profile.getDiscordId() == null) {
                    player.sendMessage(Component.text("Please sync your minecraft account to your discord account.\n" +
                        "You can do this with the /sync command.", NamedTextColor.RED));
                } else {
                    profile.checkAndUpdateRank();
                }

                Component playerName = Main.getInstance().getMiniMessage().deserialize(String.format("<%s>", profile.getRank().getColor()))
                    .append(Component.text(player.getUsername()));

                player.setDisplayName(playerName);
                new NametagEntity(player);

                player.sendMessage((System.currentTimeMillis() - profileStartTime) + "ms");

                // TODO: use permissions?
                if (profile.getRank().equals(Rank.OWNER)) {
                    player.setPermissionLevel(4);
                }
            });
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

            Profile profile = Profile.getOrCreateProfileByUUID(player.getUuid().toString());
            Component formattedName = profile.getFormattedName();

            event.setChatFormat(chatEvent -> {
                Component discordName = Component.empty();
                if (profile.getDiscordId() != null) {
                    Member member = Main.getInstance().getDiscordBot().getMemberById(profile.getDiscordId());
                    String discordUsername = member.getUsername();

                    if (!discordUsername.equals(player.getUsername()) &&
                        !NicknameCommand.nickedPlayer.contains(player.getUuid())) {
                        discordName = Component.space().append(Main.getInstance().getMiniMessage().deserialize(
                            String.format("<gray>(<#3E7CF7>%s<gray>)", discordUsername)
                        ));
                    }
                }

                return formattedName
                    .append(discordName)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(Component.text(event.getMessage(), NamedTextColor.WHITE));
            });
        });
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            Material materialInHand = player.getItemInMainHand().material();

            if (materialInHand == VisibilityItem.getEnabledItem().material() ||
                materialInHand == VisibilityItem.getDisabledItem().material()) {
                VisibilityItem.toggleVisibility(player);
                event.setCancelled(true);
            }
        });
        eventNode.addListener(PlayerStartSneakingEvent.class, event -> {
            NametagEntity nametag = event.getPlayer().getTag(NametagEntity.NAMETAG_TAG);
            nametag.setSneaking(true);
        });
        eventNode.addListener(PlayerStartSneakingEvent.class, event -> {
            NametagEntity nametag = event.getPlayer().getTag(NametagEntity.NAMETAG_TAG);
            nametag.setSneaking(false);
        });

        return eventNode;
    }

    public static void init() {
        var eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addChild(getEventNode(Main.getInstance().getInstanceContainer()));

        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        MinestomAdventure.COMPONENT_TRANSLATOR = (c, l) -> c;

        ScoreboardHandler sidebar = new ScoreboardHandler();

        TextComponent.Builder headerBuilder = Component.text();
        TextComponent.Builder footerBuilder = Component.text();

        final List<String> headerStrings = Main.getInstance().getConfig().getTabHeader();

        for (int i = 0; i < headerStrings.size(); i++)
            headerBuilder.append(Main.getInstance().getMiniMessage().deserialize(headerStrings.get(i) + (i < headerStrings.size() - 1 ? "\n" : "")));
        tabHeader = headerBuilder.build();

        final List<String> footerStrings = Main.getInstance().getConfig().getTabFooter();
        for (int i = 0; i < footerStrings.size(); i++)
            footerBuilder.append(Main.getInstance().getMiniMessage().deserialize(footerStrings.get(i) + (i < footerStrings.size() - 1 ? "\n" : "")));
        tabFooter = footerBuilder.build();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty())
                return;

            Audiences.players().sendPlayerListHeaderAndFooter(tabHeader, tabFooter);

            sidebar.update(players.size());
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

}
