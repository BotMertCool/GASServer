package com.goodasssub.gasevents.commands.profile.whitelist;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class WhitelistCommand extends Command {
    final String PERMISSION = "core.whitelist";

    public WhitelistCommand() {
        super("whitelist");

        addSubcommand(new WhitelistOnCommand());
        addSubcommand(new WhitelistOffCommand());
        addSubcommand(new WhitelistListCommand());
        addSubcommand(new WhitelistAddCommand());
        addSubcommand(new WhitelistRemoveCommand());

        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

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
