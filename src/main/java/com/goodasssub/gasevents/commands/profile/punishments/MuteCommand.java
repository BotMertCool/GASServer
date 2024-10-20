package com.goodasssub.gasevents.commands.profile.punishments;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
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

public class MuteCommand extends Command {
    public MuteCommand() {
        super("mute");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String commandName = context.getCommandName();

            if (player.getPermissionLevel() < 2) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            sender.sendMessage(Component.text("Usage: /" + commandName + " <player> [reason]", NamedTextColor.RED));
        });

        var playerArg = ArgumentType.String("player")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });
        var reasonArg = ArgumentType.Word("reason");

        addSyntax((sender, context) -> execute(sender, context, "None"), playerArg);
        addSyntax((sender, context) -> execute(sender, context, context.get("reason")), playerArg, reasonArg);
    }

    private void execute(CommandSender sender, CommandContext context, String reason) {
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

        if (player.getUuid().equals(uuid)) {
            sender.sendMessage(Component.text("You cant mute yourself!", NamedTextColor.RED));
            return;
        }

        if (Main.getInstance().getProfileHandler().isPlayerPunishmentType(uuid, PunishmentType.MUTE)) {
            sender.sendMessage(Component.text(playerName + " is already muted.", NamedTextColor.RED));
            return;
        }

        Punishment punishment = new Punishment(
            PunishmentType.MUTE,
            player.getUuid(),
            uuid,
            reason,
            Punishment.PERMANENT
        );

        punishment.execute(false, playerName);
    }
}
