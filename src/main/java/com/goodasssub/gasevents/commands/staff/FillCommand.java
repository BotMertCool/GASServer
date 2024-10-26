package com.goodasssub.gasevents.commands.staff;

import com.goodasssub.gasevents.util.PlayerUtil;
import io.netty.util.concurrent.CompleteFuture;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FillCommand extends Command {
    private final String PERMISSION = "core.fill";

    public enum FillMode {
        DESTROY,
        HOLLOW,
        KEEP,
        OUTLINE,
        REPLACE
    }

    public FillCommand() {
        super("fill");

        // Register arguments
        var fromPosArg = ArgumentType.RelativeBlockPosition("from");
        var toPosArg = ArgumentType.RelativeBlockPosition("to");
        var blockArg = ArgumentType.BlockState("block");
        var modeArg = ArgumentType.Enum("mode", FillMode.class)
            .setDefaultValue(FillMode.REPLACE);


        setDefaultExecutor((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;
            sender.sendMessage("Usage: /setblock <pos> <block> [destroy|keep|replace]");
        });

        // Add syntax
        addSyntax((sender, context) -> {
            if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;

            CompletableFuture.runAsync(() -> {
                Player player = (Player) sender;
                // Get positions
                Point from = context.get(fromPosArg).fromSender(sender);
                Point to = context.get(toPosArg).fromSender(sender);
                Block block = context.get(blockArg);
                FillMode mode = context.get(modeArg);

                Instance instance = player.getInstance();
                if (instance == null) return;

                // Get bounds
                int minX = Math.min((int) from.x(), (int) to.x());
                int minY = Math.min((int) from.y(), (int) to.y());
                int minZ = Math.min((int) from.z(), (int) to.z());
                int maxX = Math.max((int) from.x(), (int) to.x());
                int maxY = Math.max((int) from.y(), (int) to.y());
                int maxZ = Math.max((int) from.z(), (int) to.z());

                // Check volume limits (32768 blocks is vanilla limit)
                int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                if (volume > 32768) {
                    sender.sendMessage("Too many blocks! Maximum volume is 32768 blocks");
                    return;
                }

                int blocksAffected = 0;

                switch (mode) {
                    case REPLACE -> {
                        // Simply replace all blocks in the area
                        blocksAffected = fillArea(instance, minX, minY, minZ, maxX, maxY, maxZ, block, null);
                    }
                    case KEEP -> {
                        // Only replace air blocks
                        blocksAffected = fillArea(instance, minX, minY, minZ, maxX, maxY, maxZ, block,
                            (pos, currentBlock) -> currentBlock.isAir());
                    }
                    case DESTROY -> {
                        // Replace all blocks and trigger block break effects
                        blocksAffected = fillArea(instance, minX, minY, minZ, maxX, maxY, maxZ, block,
                            (pos, currentBlock) -> {
                                // You could add particle effects and sounds here
                                return true;
                            });
                    }
                    case HOLLOW -> {
                        // Fill only the shell, leaving the inside empty
                        blocksAffected = fillHollow(instance, minX, minY, minZ, maxX, maxY, maxZ, block);
                    }
                    case OUTLINE -> {
                        // Fill only the edges
                        blocksAffected = fillOutline(instance, minX, minY, minZ, maxX, maxY, maxZ, block);
                    }
                }

                sender.sendMessage("Filled " + blocksAffected + " blocks");
            });

        }, fromPosArg, toPosArg, blockArg, modeArg);
    }

    @FunctionalInterface
    interface BlockPlacementCondition {
        boolean shouldPlace(Point pos, Block currentBlock);
    }

    private int fillArea(Instance instance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                         Block block, BlockPlacementCondition condition) {
        int blocksAffected = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Point pos = new Vec(x, y, z);
                    Block currentBlock = instance.getBlock(pos);

                    if (condition == null || condition.shouldPlace(pos, currentBlock)) {
                        instance.setBlock(pos, block);
                        blocksAffected++;
                    }
                }
            }
        }

        return blocksAffected;
    }

    private int fillHollow(Instance instance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block) {
        int blocksAffected = 0;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // Check if this position is on the shell
                    boolean isShell = x == minX || x == maxX ||
                        y == minY || y == maxY ||
                        z == minZ || z == maxZ;

                    if (isShell) {
                        Point pos = new Vec(x, y, z);
                        instance.setBlock(pos, block);
                        blocksAffected++;
                    }
                }
            }
        }

        return blocksAffected;
    }

    private int fillOutline(Instance instance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block) {
        int blocksAffected = 0;
        List<Point> edges = new ArrayList<>();

        // Add all 12 edges of the cube
        // Horizontal edges along X
        for (int x = minX; x <= maxX; x++) {
            edges.add(new Vec(x, minY, minZ));
            edges.add(new Vec(x, minY, maxZ));
            edges.add(new Vec(x, maxY, minZ));
            edges.add(new Vec(x, maxY, maxZ));
        }

        // Vertical edges along Y
        for (int y = minY; y <= maxY; y++) {
            edges.add(new Vec(minX, y, minZ));
            edges.add(new Vec(minX, y, maxZ));
            edges.add(new Vec(maxX, y, minZ));
            edges.add(new Vec(maxX, y, maxZ));
        }

        // Horizontal edges along Z
        for (int z = minZ; z <= maxZ; z++) {
            edges.add(new Vec(minX, minY, z));
            edges.add(new Vec(minX, maxY, z));
            edges.add(new Vec(maxX, minY, z));
            edges.add(new Vec(maxX, maxY, z));
        }

        // Place blocks along edges
        for (Point pos : edges) {
            instance.setBlock(pos, block);
            blocksAffected++;
        }

        return blocksAffected;
    }
}