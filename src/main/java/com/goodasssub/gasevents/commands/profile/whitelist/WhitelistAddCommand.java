package com.goodasssub.gasevents.commands.profile.whitelist;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class WhitelistAddCommand extends Command {
    public WhitelistAddCommand() {
        super("add");

//        setCondition((sender, ignored) -> sender.hasPermission("minestom.teleport"));

        setDefaultExecutor((source, context) -> source.sendMessage(
            Component.text("Usage: /" + context.getCommandName() + " <player>", NamedTextColor.RED)
        ));

        var playerArg = ArgumentType.Word("player");

        addSyntax(this::onPlayerTeleport, playerArg);
    }

    private void onPlayerTeleport(CommandSender sender, CommandContext context) {
        final Player player = (Player) sender;

        if (player.getPermissionLevel() < 2) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        final String playerName = context.get("player");

        UUID uuid = UUIDUtil.uuidFromName(playerName);
        if (uuid == null) {
            sender.sendMessage(Component.text("No player found!", NamedTextColor.RED));
            return;
        }

        Main.getInstance().getProfileHandler().addPlayerWhitelist(uuid, player.getUsername());
        sender.sendMessage(Component.text("%s added to whitelist.".formatted(playerName)));
    }
}
