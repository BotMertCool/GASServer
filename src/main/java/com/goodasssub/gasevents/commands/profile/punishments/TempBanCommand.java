package com.goodasssub.gasevents.commands.profile.punishments;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
import com.goodasssub.gasevents.util.TimeUtil;
import com.goodasssub.gasevents.util.UUIDUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.UUID;

public class TempBanCommand extends Command {
    final String PERMISSION = "core.punish";

    public TempBanCommand() {
        super("tempban");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;

            String commandName = context.getCommandName();

            if (!player.hasPermission(PERMISSION)) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            sender.sendMessage(Component.text("Usage: /" + commandName + " <player> <duration> [reason]", NamedTextColor.RED));
        });

        // FIXME: isnt working
        var playerArg = ArgumentType.Word("player")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            });

        ArgumentWord durationArg = ArgumentType.Word("duration");

        durationArg.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("1y"));
            suggestion.addEntry(new SuggestionEntry("1d"));
            suggestion.addEntry(new SuggestionEntry("1h"));
            suggestion.addEntry(new SuggestionEntry("30m"));
        });

        ArgumentStringArray reasonArg = ArgumentType.StringArray("reason");

        String[] none = {"None"};

        addSyntax((sender, context) -> execute(sender, context, none), playerArg, durationArg);
        addSyntax((sender, context) -> execute(sender, context, context.get("reason")), playerArg, durationArg, reasonArg);
    }
    private void execute(CommandSender sender, CommandContext context, String[] reason) {
        final Player player = (Player) sender;
        final String reasonString = String.join(" ", reason);

        if (!player.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        final String playerName = context.get("player");
        final long duration = TimeUtil.convertToTimeInMillis(context.get("duration"));

        UUID uuid = UUIDUtil.uuidFromName(playerName);
        if (uuid == null) {
            sender.sendMessage(Component.text("No player found!", NamedTextColor.RED));
            return;
        }

        if (player.getUuid().equals(uuid)) {
            sender.sendMessage(Component.text("You cant ban yourself!", NamedTextColor.RED));
            return;
        }

        if (Main.getInstance().getProfileHandler().isPlayerPunishmentType(uuid, PunishmentType.BAN)) {
            sender.sendMessage(Component.text(playerName + " is already banned.", NamedTextColor.RED));
            return;
        }

        Punishment punishment = new Punishment(
            PunishmentType.BAN,
            player.getUuid(),
            uuid,
            reasonString,
            duration
        );

        punishment.execute(false, playerName);

        Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);

        if (target != null) {
            target.kick(punishment.getMessage());
        }
    }
}
