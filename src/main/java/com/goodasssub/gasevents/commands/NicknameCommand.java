package com.goodasssub.gasevents.commands;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.entities.NametagEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class NicknameCommand extends Command {
    public static HashSet<UUID> nickedPlayer = new HashSet<>();

    public NicknameCommand() {
        super("setnick", "nick", "nickname");

        ArgumentStringArray nickname = ArgumentType.StringArray("nickname");

        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            String commandName = context.getCommandName();

            player.sendMessage(Component.text("Usage: /" + commandName + " <nickname>", NamedTextColor.RED));
        });

        addSyntax((sender, context) -> {
            Player player = (Player) sender;

            if (player.getPermissionLevel() < 2) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            Profile profile = Profile.getOrCreateProfileByUUID(String.valueOf(player.getUuid()));

            String[] nicknameStringArray = context.get(nickname);
            String nicknameString = String.join(" ", nicknameStringArray);

            player.setDisplayName(Main.getInstance().getMiniMessage().deserialize(String.format("<%s>", profile.getRank().getColor()))
                .append(Component.text(nicknameString)));

            if (player.hasTag(NametagEntity.NAMETAG_TAG)) {
                NametagEntity nametag = player.getTag(NametagEntity.NAMETAG_TAG);
                nametag.remove();
            }

            new NametagEntity(player);

            if (nicknameString.equals(player.getUsername())) {
                player.sendMessage(Component.text("Your nickname has been reset.", NamedTextColor.GREEN));
                nickedPlayer.remove(player.getUuid());
                return;
            }

            nickedPlayer.add(player.getUuid());

            player.sendMessage(Component.text("You nickname has been changed to ", NamedTextColor.GREEN)
                .append(Component.text(nicknameString, NamedTextColor.WHITE)));
        }, nickname);
    }
}