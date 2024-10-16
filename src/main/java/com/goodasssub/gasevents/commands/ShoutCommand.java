package com.goodasssub.gasevents.commands;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.profile.Rank;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ShoutCommand extends Command {
    public ShoutCommand() {
        super("shout");

        ArgumentString message =  ArgumentType.String("msg");

        setDefaultExecutor((sender, context) -> {
            String commandName = context.getCommandName();

            sender.sendMessage(Component.text("Usage: /" + commandName + " <msg>", NamedTextColor.RED));
        });

        addSyntax((sender, context) -> {
            CompletableFuture.runAsync(() -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Please run this command in-game.", NamedTextColor.RED));
                    return;
                }

                if (player.getPermissionLevel() < 2) {
                    sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return;
                }

                Profile profile = Profile.getOrCreateProfileByUUID(player.getUuid().toString());

                if (profile == null) {
                    Main.getInstance().getLogger().error("Error profile null: {}", player.getUsername());
                    player.sendMessage(Component.text("A fatal error has occurred.", NamedTextColor.RED));
                    return;
                }

                if(!profile.getRank().equals(Rank.OWNER)) {
                    player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                    return;
                }

                String messageString = context.get(message);

                player.sendMessage(Component.text("Message sent.", NamedTextColor.GREEN));

                Component fullMessage = Component.newline()
                    .append(profile.getFormattedName())
                    .append(Main.getInstance().getMiniMessage().deserialize(" <gold>shouted a message<gray>:"))
                    .appendNewline()
                    .append(Component.text(messageString, NamedTextColor.WHITE))
                    .appendNewline();

                Audiences.players().sendMessage(fullMessage);
            });
        }, message);
    }

}