package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

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

        ArgumentWord durationArg = ArgumentType.Word("duration");

        durationArg.setSuggestionCallback((sender, context, suggestion) -> {
            for (String spawnType : spawnTypes) {
                suggestion.addEntry(new SuggestionEntry(spawnType));
            }
        });

        addSyntax((sender, context) -> {
            String type = context.get("type");

            if (!spawnTypes.contains(type)) {
                Component message = Component.text("Not a valid spawn type.")
                    .appendNewline()
                    .append(Component.text("Types: " + String.join(", ", spawnTypes)));

                sender.sendMessage(message);
                return;
            }


        });

    }
}
