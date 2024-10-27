package com.goodasssub.gasevents.commands.anticheat;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.anticheat.AntiCheat;
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

import java.util.UUID;

public class ACBanCommand extends Command {
    private final String PERMISSION = "core.ac";

    public ACBanCommand() {
        super("ban");

        setDefaultExecutor((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

            String commandName = context.getCommandName();
            sender.sendMessage(Component.text("Usage: /" + commandName + " <player> [reason]", NamedTextColor.RED));
        });

        var playerArg = ArgumentType.String("player")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });

        var reasonArg = ArgumentType.StringArray("reason");

        String[] none = {"None"};

        addSyntax((sender, context) -> execute(sender, context, none), playerArg);
        addSyntax((sender, context) -> execute(sender, context, context.get("reason")), playerArg, reasonArg);
    }

    private void execute(CommandSender sender, CommandContext context, String[] reason) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

        final String reasonString = String.join(" ", reason);


        String playerName = context.get("player");

        Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);

        if (target != null) {
            playerName = target.getUsername();
        }

        UUID uuid = UUIDUtil.uuidFromName(playerName);
        if (uuid == null) {
            sender.sendMessage(Component.text("No player found!", NamedTextColor.RED));
            return;
        }

        if (sender instanceof Player player && player.getUuid().equals(uuid)) {
            sender.sendMessage(Component.text("You cant ban yourself!", NamedTextColor.RED));
            return;
        }

        if (Main.getInstance().getProfileHandler().isPlayerPunishmentType(uuid, PunishmentType.BAN)) {
            sender.sendMessage(Component.text(playerName + " is already banned.", NamedTextColor.RED));
            return;
        }

        Punishment punishment = new Punishment(
            PunishmentType.BAN,
            Punishment.ANTICHEAT_UUID,
            uuid,
            reasonString,
            Punishment.PERMANENT
        );

        punishment.execute(false, playerName);

        if (target != null) {
            target.kick(punishment.getMessage());
        }
    }
}
