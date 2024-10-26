package com.goodasssub.gasevents.commands.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class SetBlockCommand extends Command {
    final String PERMISSION = "core.setblock";

    public SetBlockCommand() {
        super("setblock");

        // Define the arguments: x, y, z coordinates and block type
        var xArg = ArgumentType.Integer("x");
        var yArg = ArgumentType.Integer("y");
        var zArg = ArgumentType.Integer("z");
        var blockArg = ArgumentType.Word("block").from(Block.values().stream().map(Block::name).toArray(String[]::new));

        // Add syntax and execution
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }

            if (!player.hasPermission(PERMISSION)) {
                sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }

            // Parse the arguments
            int x = context.get(xArg);
            int y = context.get(yArg);
            int z = context.get(zArg);
            String blockName = context.get(blockArg);

            // Find the block
            Block block = Block.fromNamespaceId("minecraft:" + blockName.toLowerCase());
            if (block == null) {
                player.sendMessage("Invalid block type: " + blockName);
                return;
            }

            // Get the player's instance
            Instance instance = player.getInstance();
            if (instance == null) {
                player.sendMessage("You are not in a valid instance.");
                return;
            }

            // Set the block at the specified position
            Pos blockPosition = new Pos(x, y, z);
            instance.setBlock(blockPosition, block);
            player.sendMessage("Set block at " + blockPosition + " to " + blockName);
        }, xArg, yArg, zArg, blockArg);
    }
}
