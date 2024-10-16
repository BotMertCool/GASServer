package com.goodasssub.gasevents.commands;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
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

            Profile profile = Profile.getOrCreateProfileByUUID(player.getUuid().toString());

            if (profile == null) {
                Main.getInstance().getLogger().error("Error profile null: {}", player.getUsername());
                sender.sendMessage(Component.text("A fatal error has occurred.", NamedTextColor.RED));
                return;
            }

            if (profile.getDiscordId() != null) {
                sender.sendMessage(Component.text("Your account is already synced to a discord.", NamedTextColor.RED));
                return;
            }

            final String syncChannelName = "#sync";

            sender.sendMessage(Component.text("Enter the code ", NamedTextColor.GREEN)
                .append(Component.text(profile.getSyncCode(), NamedTextColor.WHITE))
                .append(Component.text(" in "))
                .append(Component.text(syncChannelName, NamedTextColor.WHITE))
                .append(Component.text(" to sync your account.", NamedTextColor.GREEN)));
        });
    }
}