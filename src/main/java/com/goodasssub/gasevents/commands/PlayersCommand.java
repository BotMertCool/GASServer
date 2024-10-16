package com.goodasssub.gasevents.commands;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.profile.Rank;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayersCommand extends Command {
    public PlayersCommand() {
        super("players", "list");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        CompletableFuture.runAsync(() -> {
            final Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            final List<Rank> ranks = Rank.sortedByWeight();

            StringBuilder stringBuilder = new StringBuilder("(%s/%s): ".formatted(
                players.size(),
                Main.getInstance().getConfig().getMaxPlayers())
            );

            for (int i = 0; i < ranks.size(); i++) {
                Rank rank = ranks.get(i);

                stringBuilder.append("<").append(rank.getColor()).append(">")
                    .append(rank.getName())
                    .append("</").append(rank.getColor()).append(">");

                if (i == Rank.values().length - 1) continue;
                stringBuilder.append(", ");
            }

            stringBuilder.append("\n");

            List<Component> playerNames = players.stream()
                .sorted(Comparator.comparingInt((Player player) ->
                    Profile.getOrCreateProfileByUUID(player.getUuid().toString()).getRank().getWeight()).reversed()
                )
                .map(player -> {
                    Component playerName = player.getName();
                    if (player.getDisplayName() != null) {
                        playerName = player.getDisplayName();
                    }

                    return playerName;
                })
                .toList();

            TextComponent.Builder builder = Component.text()
                .append(Main.getInstance().getMiniMessage().deserialize(stringBuilder.toString()));

            for (int i = 0; i < playerNames.size(); i++) {

                builder.append(playerNames.get(i)).append();
                if (i == players.size() - 1) continue;
                builder.append(Main.getInstance().getMiniMessage().deserialize("<gray>, </gray>"));
            }

            sender.sendMessage(builder.build());
        });
    }
}
