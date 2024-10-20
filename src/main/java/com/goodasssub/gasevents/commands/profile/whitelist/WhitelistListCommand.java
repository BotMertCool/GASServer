package com.goodasssub.gasevents.commands.profile.whitelist;

import com.goodasssub.gasevents.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

import java.util.Map;

public class WhitelistListCommand extends Command {
    public WhitelistListCommand() {
        super("list");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) return;

        if (player.getPermissionLevel() < 2) {
            player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        var players = Main.getInstance().getProfileHandler().getWhitelistList();

        if (players.isEmpty()) {
            player.sendMessage("None.");
            return;
        }

        StringBuilder message = new StringBuilder();

        int i = 0;
        for (Map.Entry<String, String> entry : players.entrySet()) {
            message.append("%s. %s".formatted(++i, entry.getValue()));
            if (i != players.size()) message.append("\n");
        }

        player.sendMessage(message.toString());
    }
}
