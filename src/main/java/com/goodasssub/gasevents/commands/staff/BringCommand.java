package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.location.RelativeVec;

public class BringCommand extends Command {
    private final String PERMISSION = "core.teleport";

    public BringCommand() {
        super("bring", "tphere");

        setDefaultExecutor((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

            String commandName = context.getCommandName();
            sender.sendMessage(Component.text("Usage: /" + commandName + " <player>", NamedTextColor.RED));
        });

        //TODO: entity finder?
        var playerArg = ArgumentType.String("player")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });

        addSyntax(this::executePlayer, playerArg);
    }

    private void executePlayer(CommandSender sender, CommandContext context) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;
        final Player player = (Player) sender;

        final String playerName = context.get("player");

        Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);
        if (target == null) {
            sender.sendMessage(Component.text("No player found!", NamedTextColor.RED));
            return;
        }

        target.teleport(player.getPosition());
        sender.sendMessage(Component.text("Brought ", NamedTextColor.GOLD)
                .append(Component.text(playerName, NamedTextColor.WHITE))
                .append(Component.text(" to you.", NamedTextColor.GOLD)));
    }
}