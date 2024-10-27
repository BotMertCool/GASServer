package com.goodasssub.gasevents.commands.anticheat;

import com.goodasssub.gasevents.Main;
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
import java.util.concurrent.CompletableFuture;

public class ACTopCommand extends Command {
    private final String PERMISSION = "core.ac";

    public ACTopCommand() {
        super("top");

        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

        CompletableFuture.runAsync(() -> {
            // TODO: VVV
        });
    }
}
