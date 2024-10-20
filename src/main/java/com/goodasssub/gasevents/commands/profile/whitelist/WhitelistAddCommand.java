package com.goodasssub.gasevents.commands.profile.whitelist;

import com.fasterxml.jackson.databind.util.Named;
import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class WhitelistAddCommand extends Command {
    public WhitelistAddCommand() {
        super("add");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String commandName = context.getCommandName();

            if (player.getPermissionLevel() < 2) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            sender.sendMessage(Component.text("Usage: /" + commandName + " <player>", NamedTextColor.RED));
        });

        var playerArg = ArgumentType.String("player")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });

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

        if (Main.getInstance().getProfileHandler().isPlayerWhitelisted(uuid)) {
            sender.sendMessage(Component.text("Player is already whitelisted.", NamedTextColor.RED));
            return;
        }

        var username = playerName;

        if (Main.getInstance().getConfigManager().getConfig().getMojangAuth()) {
            username += ":Online";
        } else {
            username += ":Offline";
        }

        Main.getInstance().getProfileHandler().addPlayerWhitelist(uuid, username);
        sender.sendMessage(Component.text("%s added to whitelist.".formatted(playerName), NamedTextColor.GREEN));
    }
}
