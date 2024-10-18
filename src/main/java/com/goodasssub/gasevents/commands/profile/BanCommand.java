package com.goodasssub.gasevents.commands.profile;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BanCommand extends Command {
    private final Set<UUID> bannedPlayers = new HashSet<>();
    private final Path bannedPlayersFile = Paths.get("banned-players.txt");

    public BanCommand() {
        super("ban");

        ArgumentEntity target = ArgumentType.Entity("targets").onlyPlayers(true);

        addSyntax((sender, context) -> {
//            if (!(sender instanceof Player)) {
//                sender.sendMessage("This command can only be run by a player.");
//                return;
//            }
//
//            String targetName = context.get("player");
//            //Player targetPlayer = getPlayerByName(targetName);
//            if (targetPlayer == null) {
//                sender.sendMessage("Player not found!");
//                return;
//            }
//
//            UUID targetUUID = targetPlayer.getUuid();
//            if (bannedPlayers.contains(targetUUID)) {
//                sender.sendMessage(targetPlayer.getUsername() + " is already banned.");
//                return;
//            }
//
//            // Ban the player
//            //banPlayer(targetPlayer);
//            sender.sendMessage(targetPlayer.getUsername() + " has been banned.");
        });
    }
}
