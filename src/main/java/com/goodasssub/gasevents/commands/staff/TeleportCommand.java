package com.goodasssub.gasevents.commands.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.location.RelativeVec;

public class TeleportCommand extends Command {
    final String PERMISSION = "core.teleport";

    public TeleportCommand() {
        super("teleport", "tp");

        setCondition(Conditions::playerOnly);
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String commandName = context.getCommandName();
            
            if (!player.hasPermission(PERMISSION)) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            sender.sendMessage(Component.text("Usage: /" + commandName + " <player> | <x> <y> <z>", NamedTextColor.RED));
        });

        var posArg = ArgumentType.RelativeVec3("pos");
        var playerArg = ArgumentType.String("player")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });

        addSyntax(this::onPlayerTeleport, playerArg);
        addSyntax(this::onPositionTeleport, posArg);
    }

    private void onPlayerTeleport(CommandSender sender, CommandContext context) {
        final Player player = (Player) sender;

        if (!player.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        final String playerName = context.get("player");

        Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
        if (target == null) {
            sender.sendMessage(Component.text("No player found!", NamedTextColor.RED));
            return;
        }

        player.teleport(target.getPosition());
        sender.sendMessage(Component.text("Teleported to player " + playerName));
    }

    private void onPositionTeleport(CommandSender sender, CommandContext context) {
        //TODO: check if instanceof player
        final Player player = (Player) sender;

        if (!player.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        final RelativeVec relativeVec = context.get("pos");
        final Pos position = player.getPosition().withCoord(relativeVec.from(player));
        player.teleport(position);
        player.sendMessage(Component.text("You have been teleported to ", NamedTextColor.GOLD)
            .append(Component.text(position.toString(), NamedTextColor.WHITE)));
    }
}