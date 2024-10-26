package com.goodasssub.gasevents.commands.profile.whitelist;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class WhitelistOffCommand extends Command {
    final String PERMISSION = "core.whitelist";

    public WhitelistOffCommand() {
        super("off");

        setDefaultExecutor((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;
            if (!(sender instanceof Player player)) return;

            Main.getInstance().getProfileHandler().setWhitelistMode(false);
            player.sendMessage(Component.text("Disabled whitelists.", NamedTextColor.RED));
        });

    }
}
