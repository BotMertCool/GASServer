package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.RelativeVec;

public class SetBlockCommand extends Command {
    private final String PERMISSION = "core.setblock";

    public enum SetBlockMode {
        DESTROY,
        KEEP,
        REPLACE
    }

    public SetBlockCommand() {
        super("setblock");

        var positionArg = ArgumentType.RelativeBlockPosition("pos");
        var blockArg = ArgumentType.BlockState("block");
        var modeArg = ArgumentType.Enum("mode", SetBlockMode.class)
            .setDefaultValue(SetBlockMode.REPLACE);

        addSyntax((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;
            if (!(sender instanceof Player player)) return;

            RelativeVec position = context.get(positionArg);
            Block block = context.get(blockArg);
            SetBlockMode mode = context.get(modeArg);

            Point blockPos = position.from(player);

            var instance = player.getInstance();
            if (instance == null) return;

            Block currentBlock = instance.getBlock(blockPos);

            switch (mode) {
                case KEEP -> {
                    if (currentBlock.isAir()) {
                        instance.setBlock(blockPos, block);
                        sender.sendMessage("Block placed successfully");
                    } else {
                        sender.sendMessage("Block not placed - target location is not air");
                    }
                }
                case DESTROY -> {
                    instance.setBlock(blockPos, block);
                    sender.sendMessage("Block placed with destruction");
                }
                case REPLACE -> {
                    instance.setBlock(blockPos, block);
                    sender.sendMessage("Block replaced successfully");
                }
            }
        }, positionArg, blockArg, modeArg);
    }
}
