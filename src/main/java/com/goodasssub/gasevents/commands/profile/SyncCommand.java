package com.goodasssub.gasevents.commands.profile;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.discordbot.DiscordBot;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.util.SyncUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SyncCommand extends Command {
    public SyncCommand() {
        super("sync");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        CompletableFuture.runAsync(() -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Please run this command in-game.", NamedTextColor.RED));
                return;
            }

            Profile profile = Profile.fromUuid(player.getUuid());

            if (profile.getDiscordId() != null) {
                sender.sendMessage(Component.text("Your account is already synced to a discord.", NamedTextColor.RED));
                return;
            }

            if (profile.getSyncCode() == null) {
                profile.setSyncCode(SyncUtil.generateSyncCode());
                profile.save();
            }

            final String syncChannelName = DiscordBot.syncChannelName;

            sender.sendMessage(Component.text("Enter the code ", NamedTextColor.GREEN)
                .append(Component.text(profile.getSyncCode(), NamedTextColor.WHITE))
                .append(Component.text(" in "))
                .append(Component.text("#" + syncChannelName, NamedTextColor.WHITE))
                .append(Component.text(" to sync your account.", NamedTextColor.GREEN)));
        });
    }
}