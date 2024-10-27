package com.goodasssub.gasevents.commands.anticheat;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;

public class AntiCheatCommand extends Command {
    private final String PERMISSION = "core.whitelist";

    public AntiCheatCommand() {
        super("ac", "anticheat");

        addSubcommand(new ACBanCommand());
        addSubcommand(new ACTopCommand());

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

            if (i != this.getSubcommands().size() - 1) {
                stringBuilder.append("\n");
            }
        }

        sender.sendMessage(Component.text(stringBuilder.toString(), NamedTextColor.GREEN));
    }
}