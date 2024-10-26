package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.List;

public class GamemodeCommand extends Command {
    private final String PERMISSION = "core.gamemode";

    public GamemodeCommand() {
        super("gamemode", "gm");

        ArgumentEnum<GameMode> gamemode = ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        gamemode.setCallback((sender, exception) -> {
            sender.sendMessage(
                Component.text("Invalid gamemode ", NamedTextColor.RED)
                    .append(Component.text(exception.getInput(), NamedTextColor.WHITE))
                    .append(Component.text("!")));
        });

        ArgumentEntity target = ArgumentType.Entity("targets").onlyPlayers(true);

        setDefaultExecutor((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

            String commandName = context.getCommandName();
            sender.sendMessage(Component.text("Usage: /" + commandName + " <gamemode> [targets]", NamedTextColor.RED));
        });

        addSyntax((sender, context) -> {
            GameMode mode = context.get(gamemode);

            executeSelf((Player) sender, mode);
        }, gamemode);
        
        addSyntax((sender, context) -> {
            EntityFinder finder = context.get(target);
            GameMode mode = context.get(gamemode);

            executeOthers(sender, mode, finder.find(sender));
        }, gamemode, target);
    }

    private void executeOthers(CommandSender sender, GameMode mode, List<Entity> entities) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

        if (entities.isEmpty()) {
            sender.sendMessage(Component.text("No player was found.", NamedTextColor.RED));
            return;
        }

        for (Entity entity : entities) {
            if (!(entity instanceof Player player)) return;

            if (player == sender) {
                executeSelf((Player) sender, mode);
                return;
            }

            player.setGameMode(mode);
            String gamemode = mode.name().toUpperCase();

            Component senderComponent = Component.text("You set ", NamedTextColor.GOLD)
                .append(player.getName()).color(NamedTextColor.WHITE)
                .append(Component.text("'s gamemode to ", NamedTextColor.GOLD))
                .append(Component.text(gamemode, NamedTextColor.WHITE));

            Component targetComponent = Component.text("Your gamemode has been set to ", NamedTextColor.GOLD)
                .append(Component.text(gamemode, NamedTextColor.WHITE));

            player.sendMessage(targetComponent);
            sender.sendMessage(senderComponent);
        }
    }

    private void executeSelf(Player sender, GameMode mode) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;
        sender.setGameMode(mode);

        String gamemode = mode.name().toUpperCase();

        Component gamemodeComponent = Component.text("Gamemode: ", NamedTextColor.GOLD)
            .append(Component.text(gamemode));

        sender.sendMessage(gamemodeComponent);
    }
}