package com.goodasssub.gasevents.commands.profile.punishments;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
import com.goodasssub.gasevents.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.List;
import java.util.UUID;

public class UnbanCommand extends Command {
    public UnbanCommand() {
        super("unban");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String commandName = context.getCommandName();

            if (player.getPermissionLevel() < 2) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            sender.sendMessage(Component.text("Usage: /" + commandName + " <player>", NamedTextColor.RED));
        });

        var playerArg = ArgumentType.Word("player");

        addSyntax(this::execute, playerArg);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) return;

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
            sender.sendMessage(Component.text("You cant ban yourself!", NamedTextColor.RED));
            return;
        }

        if (!Profile.profileExists(uuid) ||
            !Main.getInstance().getProfileHandler().isPlayerPunishmentType(uuid, PunishmentType.BAN)) {
            sender.sendMessage(Component.text(playerName + " is not banned.", NamedTextColor.RED));
            return;
        }

        //Profile profile = Profile.fromUuid(uuid);

        List<Punishment> punishments = Main.getInstance().getProfileHandler().getActivePlayerPunishments(uuid);

        for (Punishment punishment : punishments) {
            if (punishment.getPunishmentType() != PunishmentType.BAN) continue;

            punishment.removePunishment(false, playerName, player.getUuid());
        }

    }
}
