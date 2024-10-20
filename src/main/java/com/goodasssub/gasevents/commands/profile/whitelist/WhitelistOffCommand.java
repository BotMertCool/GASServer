package com.goodasssub.gasevents.commands.profile.whitelist;

import com.goodasssub.gasevents.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class WhitelistOffCommand extends Command {
    public WhitelistOffCommand() {
        super("off");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            if (player.getPermissionLevel() < 2) {
                player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            if (!Main.getInstance().getProfileHandler().whitelistEnabled()) {
                player.sendMessage(Component.text("Whitelists are already disabled.", NamedTextColor.RED));
                return;
            }

            Main.getInstance().getProfileHandler().setWhitelistMode(false);
            player.sendMessage(Component.text("Disabled whitelists.", NamedTextColor.RED));
        });

    }
}
