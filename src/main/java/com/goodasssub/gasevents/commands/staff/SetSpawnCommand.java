package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

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
            if (!(sender instanceof Player player)) return;


            String type = context.get("type");
            type = type.toLowerCase().trim();

            if (!spawnTypes.contains(type)) {
                Component message = Component.text("Not a valid spawn type.")
                    .appendNewline()
                    .append(Component.text("Types: " + String.join(", ", spawnTypes)));

                player.sendMessage(message);
                return;
            }

            Pos pos = player.getPosition();

            if (type.equals("normal")) {
                Main.getInstance().getSpawnHandler().setNormalSpawn(pos);
            }

            if (type.equals("staff")) {
                Main.getInstance().getSpawnHandler().setStaffSpawn(pos);
            }

            player.sendMessage(Component.text("Spawn set for \"%s\" at your location.".formatted(type), NamedTextColor.GREEN));
        }, typeArg);
    }
}
