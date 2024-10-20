package com.goodasssub.gasevents.commands.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class FlyCommand extends Command {
    public FlyCommand() {
        super("fly");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) return;

        if (player.getPermissionLevel() < 2) {
            player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        player.setAllowFlying(!player.isAllowFlying());

        Component flightMessage = Component.text("Flight: ", NamedTextColor.GOLD);

        if (player.isAllowFlying()) {
            flightMessage = flightMessage.append(Component.text("Enabled", NamedTextColor.GREEN));
        } else {
            player.setFlying(false);
            flightMessage = flightMessage.append(Component.text("Disabled", NamedTextColor.RED));
        }
        
        sender.sendMessage(flightMessage);
    }
}
