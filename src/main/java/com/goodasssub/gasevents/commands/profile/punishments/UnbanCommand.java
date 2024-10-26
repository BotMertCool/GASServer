package com.goodasssub.gasevents.commands.profile.punishments;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
import com.goodasssub.gasevents.util.PlayerUtil;
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

import java.util.List;
import java.util.UUID;

public class UnbanCommand extends Command {
    private final String PERMISSION = "core.punish";

    public UnbanCommand() {
        super("unban");

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

        UUID uuid = UUIDUtil.uuidFromName(playerName);
        if (uuid == null) {
            sender.sendMessage(Component.text("No player found!", NamedTextColor.RED));
            return;
        }

        if (!Main.getInstance().getProfileHandler().isPlayerPunishmentType(uuid, PunishmentType.BAN)) {
            sender.sendMessage(Component.text(playerName + " is not banned.", NamedTextColor.RED));
            return;
        }

        List<Punishment> punishments = Main.getInstance().getProfileHandler().getActivePlayerPunishments(uuid);

        UUID executorUuid = sender instanceof Player player ? player.getUuid() : Punishment.SYSTEM_UUID;
        for (Punishment punishment : punishments) {
            if (punishment.getPunishmentType() != PunishmentType.BAN) continue;

            punishment.removePunishment(false, playerName, executorUuid);
        }

    }
}
