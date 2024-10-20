package com.goodasssub.gasevents.commands.profile.whitelist;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class WhitelistCommand extends Command {
    public WhitelistCommand() {
        super("whitelist");

        addSubcommand(new WhitelistOnCommand());
        addSubcommand(new WhitelistOffCommand());
        addSubcommand(new WhitelistListCommand());
        addSubcommand(new WhitelistAddCommand());
        addSubcommand(new WhitelistRemoveCommand());

        setDefaultExecutor((sender, context) -> {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < this.getSubcommands().size(); i++) {
                Command command = this.getSubcommands().get(i);
                stringBuilder.append("/")
                    .append(context.getCommandName())
                    .append(" ")
                    .append(command.getName())
                    .append(" ")
                    .append(command.getSyntaxesStrings().stream().map((string) -> " <" + string + ">"));

                if (i != this.getSubcommands().size() - 1) {
                    stringBuilder.append("\n");
                }
            }

            sender.sendMessage(stringBuilder.toString());
        });
    }
}
