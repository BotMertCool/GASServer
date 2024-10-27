package com.goodasssub.gasevents.profile;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.TPSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.*;

public class ScoreboardHandler {

    private final Sidebar sidebar;

    private final Map<Integer, Sidebar.ScoreboardLine> lines = new HashMap<>();

    public ScoreboardHandler() {
        var titleString = Main.getInstance().getConfigManager().getConfig().getScoreboardTitle();
        var titleComponent = Main.getInstance().getMiniMessage().deserialize(titleString);

        sidebar = new Sidebar(titleComponent);
    }

    private void updateLines(int playerCount) {
        List<String> stringLines = Main.getInstance().getConfigManager().getConfig().getSidebarLines();

        Set<String> currentLines = new HashSet<>();
        sidebar.getLines().forEach(line -> currentLines.add(line.getId()));

        for (String lineId : currentLines) {
            sidebar.removeLine(lineId);
        }

        for (int i = stringLines.size() - 1; i >= 0; i--) {
            String line = stringLines.get(i);
            int score = stringLines.size() - 1 - i;

            line = line.replace("%player%", String.valueOf(playerCount));
            line = line.replace("%tps%", String.format("%.2f", TPSUtil.get()));

            Component content = Main.getInstance().getMiniMessage().deserialize(line);

            sidebar.createLine(new Sidebar.ScoreboardLine(
                score + "_line",
                content,
                score
            ));
        }
    }

    public void update(int playerCount) {
        Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();

        Set<Player> currentPlayers = new HashSet<>(sidebar.getPlayers());

        for (Player player : players) {
            if (!currentPlayers.contains(player)) {
                sidebar.addViewer(player);
            }
        }

        for (Player player : currentPlayers) {
            if (!players.contains(player)) {
                sidebar.removeViewer(player);
            }
        }

        updateLines(playerCount);
    }
}
