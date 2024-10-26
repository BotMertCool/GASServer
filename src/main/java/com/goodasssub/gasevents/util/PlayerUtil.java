package com.goodasssub.gasevents.util;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.permission.Permission;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {
    public static List<Player> getOnlineStaff() {
        List<Player> onlineStaff = new ArrayList<>();

        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            Profile profile = Profile.fromUuid(player.getUuid());

            if (profile.getRank().isStaff())
                onlineStaff.add(player);


        }

        return onlineStaff;
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player player && !player.hasPermission(new Permission(permission))) {
            player.sendMessage(Component.text("No permission", NamedTextColor.RED));
            return false;
        }

        return true;
    }

    // https://github.com/EngineHub/WorldEdit/

    private static boolean advanceToWall(IterableBlock hitBlox) {
        Vec curBlock;
        while ((curBlock = hitBlox.getCurrentBlock()) != null) {
            if (!canPassThroughBlock(curBlock)) {
                return true;
            }

            hitBlox.getNextBlock();
        }

        return false;
    }

    private static boolean advanceToFree(IterableBlock hitBlox) {
        Vec curBlock;
        while ((curBlock = hitBlox.getCurrentBlock()) != null) {
            if (canPassThroughBlock(curBlock)) {
                return true;
            }

            hitBlox.getNextBlock();
        }

        return false;
    }

    private static boolean canPassThroughBlock(Point curBlock) {
        Block block = Main.getInstance().getInstanceContainer().getBlock(curBlock);
        return !isMovementBlock(block);
    }

    public static boolean passThroughForwardWall(Player player, int range) {
        IterableBlock block = new IterableBlock(player, range, 0.2);

        if (!advanceToWall(block)) {
            return false;
        }

        if (!advanceToFree(block)) {
            return false;
        }

        Vec foundBlock = block.getCurrentBlock();
        if (foundBlock != null) {

            setOnGround(foundBlock, player);
            return true;
        }

        return false;
    }

    public static void setOnGround(Vec searchPos, Player player) {
        int worldMinY = VecUtil.at(-30000000.0, 0.0, -30000000.0).blockY();

        int x = searchPos.blockX();
        int y = Math.max(worldMinY, searchPos.blockY());
        int z = searchPos.blockZ();
        int yLessSearchHeight = y - 256;
        int minY = Math.min(worldMinY, yLessSearchHeight) + 2;

        while (y >= minY) {
            final Vec pos = VecUtil.at(x, y, z);
            final Block block = player.getInstance().getBlock(pos);
            if (isMovementBlock(block)) {
                Vec teleportVec = VecUtil.at(x + 0.5, y + 1, z + 0.5);

                Pos newPos = new Pos(
                    teleportVec.x(),
                    teleportVec.y(),
                    teleportVec.z(),
                    player.getPosition().yaw(),
                    player.getPosition().pitch()
                );

                player.teleport(newPos);
                return;
            }

            --y;
        }
    }

    public static boolean isMovementBlock(Block block) {
        return block.registry().isSolid();
    }
}
