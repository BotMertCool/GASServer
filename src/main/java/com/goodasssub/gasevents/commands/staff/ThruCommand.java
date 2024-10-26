package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class ThruCommand extends Command {
    private final String PERMISSION = "core.thru";

    public ThruCommand() {
        super("thru", "through");
        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) return;
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

        if (!PlayerUtil.passThroughForwardWall(player, 6)) {
            sender.sendMessage(Component.text("No free spot ahead of you found.", NamedTextColor.RED));
            return;
        };

        sender.sendMessage(Component.text("Woosh!", NamedTextColor.GREEN));
    }
}
