package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

import java.util.Collection;
import java.util.List;

public class SetSpawnCommand extends Command {
    private final String PERMISSION = "core.setspawn";

    final List<String> spawnTypes = List.of(
        "normal",
        "staff"
    );

    public SetSpawnCommand() {
        super("setspawn");

        setDefaultExecutor((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

            String commandName = context.getCommandName();

            sender.sendMessage(Component.text("Usage: /" + commandName + " <type>", NamedTextColor.RED));
        });

        ArgumentWord typeArg = ArgumentType.Word("type");

        typeArg.setSuggestionCallback((sender, context, suggestion) -> {
            for (String spawnType : spawnTypes) {
                suggestion.addEntry(new SuggestionEntry(spawnType));
            }
        });

        addSyntax((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

            String type = context.get("type");

            if (!spawnTypes.contains(type)) {
                Component message = Component.text("Not a valid spawn type.")
                    .appendNewline()
                    .append(Component.text("Types: " + String.join(", ", spawnTypes)));

                sender.sendMessage(message);
                return;
            }

            Pos pos = ((Player) sender).getPosition();
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();

            if (type.equals("normal")) {
                Main.getInstance().getConfigManager().getConfig().setNormalSpawnX(pos.x());
                Main.getInstance().getConfigManager().getConfig().setNormalSpawnY(pos.y());
                Main.getInstance().getConfigManager().getConfig().setNormalSpawnZ(pos.z());

                for (Player player : players) {
                    if (player.hasPermission("core.staff")) return;

                    player.setRespawnPoint(pos);
                }
            }

            if (type.equals("staff")) {
                Main.getInstance().getConfigManager().getConfig().setStaffSpawnX(pos.x());
                Main.getInstance().getConfigManager().getConfig().setStaffSpawnY(pos.y());
                Main.getInstance().getConfigManager().getConfig().setStaffSpawnZ(pos.z());

                for (Player player : players) {
                    if (!player.hasPermission("core.staff")) return;

                    player.setRespawnPoint(pos);
                }
            }

            Main.getInstance().getConfigManager().saveConfig();
        }, typeArg);

    }
}
