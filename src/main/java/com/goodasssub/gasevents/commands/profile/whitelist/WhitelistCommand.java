package com.goodasssub.gasevents.commands.profile.whitelist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class WhitelistCommand extends Command {
    public WhitelistCommand() {
        super("whitelist");

        addSubcommand(new WhitelistOnCommand());
        addSubcommand(new WhitelistOffCommand());
        addSubcommand(new WhitelistListCommand());
        addSubcommand(new WhitelistAddCommand());
        addSubcommand(new WhitelistRemoveCommand());

        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) return;

        if (player.getPermissionLevel() < 2) {
            player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < this.getSubcommands().size(); i++) {
            Command command = this.getSubcommands().get(i);
            stringBuilder.append("/")
                .append(context.getCommandName())
                .append(" ")
                .append(command.getName());
            //.append(" ")
            //.append(command.getSyntaxes().stream().map((string) -> " <" + string + ">"));

            if (i != this.getSubcommands().size() - 1) {
                stringBuilder.append("\n");
            }
        }

        sender.sendMessage(Component.text(stringBuilder.toString(), NamedTextColor.GREEN));
    }
}
