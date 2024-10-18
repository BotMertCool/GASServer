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

    private final Sidebar sidebar = new Sidebar(Component.text("Good Ass Sub", NamedTextColor.RED)
        .decorate(TextDecoration.BOLD));


    // TODO: Nah..
    private List<Sidebar.ScoreboardLine> createLines(int playerCount) {
        List<Sidebar.ScoreboardLine> scoreboardLines = new ArrayList<>();

        List<String> stringLines = Main.getInstance().getConfig().getSidebarLines();
        for (int i = stringLines.size() - 1; i >= 0; i--) {
            String line = stringLines.get(i);

            line = line.replace("%player%", String.valueOf(playerCount));
            line = line.replace("%tps%", String.format("%.2f", TPSUtil.get()));

            scoreboardLines.add(new Sidebar.ScoreboardLine(
                (stringLines.size() - 1 - i) + "_line",
                Main.getInstance().getMiniMessage().deserialize(line),
                stringLines.size() - 1 - i)
            );
        }

        return scoreboardLines;
    }

    public void update(int playerCount) {
        Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();

        for (Sidebar.ScoreboardLine line : sidebar.getLines()) {
            sidebar.removeLine(line.getId());
        }

        Set<Player> toUpdate = new HashSet<>(players);
        toUpdate.retainAll(sidebar.getPlayers());

        Set<Player> toRemove = new HashSet<>(sidebar.getPlayers());
        toRemove.removeAll(toUpdate);
        for (Player player : toRemove) {
            sidebar.removeViewer(player);
        }

        for (Sidebar.ScoreboardLine line : createLines(playerCount)) {
            sidebar.createLine(line);
        }

        Set<Player> toAdd = new HashSet<>(players);
        toAdd.removeAll(sidebar.getPlayers());
        for (Player player : toAdd) {
            sidebar.addViewer(player);
        }
    }
}
