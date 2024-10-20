package com.goodasssub.gasevents.commands.profile.whitelist;

import com.goodasssub.gasevents.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class WhitelistOnCommand extends Command {
    public WhitelistOnCommand() {
        super("on");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            if (player.getPermissionLevel() < 2) {
                player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            if (Main.getInstance().getProfileHandler().whitelistEnabled()) {
                player.sendMessage(Component.text("Whitelists are already enabled.", NamedTextColor.RED));
                return;
            }

            Main.getInstance().getProfileHandler().setWhitelistMode(true);
            player.sendMessage(Component.text("Enabled whitelists.", NamedTextColor.GREEN));
        });

    }
}
