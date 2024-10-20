package com.goodasssub.gasevents.commands.profile.whitelist;

import com.goodasssub.gasevents.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import java.util.Map;

public class WhitelistListCommand extends Command {
    public WhitelistListCommand() {
        super("list");

//        setCondition((sender, ignored) -> sender.hasPermission("minestom.teleport"));

        setDefaultExecutor((source, context) -> {
            if (!(source instanceof Player player)) {
                // asdsfgg dsa fdsaf sda f
                return;
            }

            if (player.getPermissionLevel() < 2) {
                player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            var players = Main.getInstance().getProfileHandler().getWhitelistList();
            StringBuilder message = new StringBuilder();

            int i = 0;
            for (Map.Entry<String, String> entry : players.entrySet()) {
                message.append("%s. %s - %s".formatted(++i, entry.getValue(), entry.getKey()));
                if (i != players.size()) message.append("\n");
            }

            player.sendMessage(message.toString());
        });

    }
}
