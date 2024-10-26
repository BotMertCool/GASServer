package com.goodasssub.gasevents.commands.profile.punishments;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
import com.goodasssub.gasevents.util.PlayerUtil;
import com.goodasssub.gasevents.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class KickCommand extends Command {
    final String PERMISSION = "core.punish";

    public KickCommand() {
        super("kick");

        setDefaultExecutor((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

            String commandName = context.getCommandName();
            sender.sendMessage(Component.text("Usage: /" + commandName + " <player>", NamedTextColor.RED));
        });

        var playerArg = ArgumentType.String("player")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });

        addSyntax(this::execute, playerArg);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

        final String playerName = context.get("player");

        Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
        if (target == null) {
            sender.sendMessage(Component.text("Player is not online!", NamedTextColor.RED));
            return;
        }

        if (sender instanceof Player player && player.getUuid().equals(target.getUuid())) {
            sender.sendMessage(Component.text("You cant kick yourself!", NamedTextColor.RED));
            return;
        }

        Audiences.players().sendMessage(Component.text(playerName, NamedTextColor.GREEN)
            .append(Component.text(" has been kicked from the game.", NamedTextColor.WHITE)));

        target.kick(Component.text("You have been kicked!", NamedTextColor.RED));
    }
}
