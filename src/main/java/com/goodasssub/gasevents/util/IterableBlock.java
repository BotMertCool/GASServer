package com.goodasssub.gasevents.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

// https://github.com/EngineHub/WorldEdit/

public class IterableBlock {
    private final Instance instance;

    private int maxDistance;
    private double checkDistance;
    private double curDistance;
    private Vec targetPos;
    private Vec targetPosDouble;
    private Vec prevPos;
    private Vec offset;

    public IterableBlock(Player player, int maxDistance, double checkDistance) {
        this.instance = player.getInstance();
        this.setValues(
            player.getPosition(),
            player.getPosition().yaw(),
            player.getPosition().pitch(),
            maxDistance,
            1.65,
            checkDistance
        );
    }

    private void setValues(Pos loc, double rotationX, double rotationY, int maxDistance, double viewHeight, double checkDistance) {
        this.maxDistance = maxDistance;
        this.checkDistance = checkDistance;
        this.curDistance = 0;

        Vec locVec = new Vec(loc.x(), loc.y(), loc.z());

        rotationX = (rotationX + 90) % 360;
        rotationY *= -1;

        double h = (checkDistance * Math.cos(Math.toRadians(rotationY)));

        offset = VecUtil.at((h * Math.cos(Math.toRadians(rotationX))),
            (checkDistance * Math.sin(Math.toRadians(rotationY))),
            (h * Math.sin(Math.toRadians(rotationX))));

        targetPosDouble = locVec.add(0.0, viewHeight, 0.0);
        targetPos = VecUtil.at(targetPosDouble.x(), targetPosDouble.y(), targetPosDouble.z());
        prevPos = targetPos;
    }

    public Vec getCurrentBlock() {
        if (curDistance > maxDistance) {
            return null;
        } else {
            return targetPos;
        }
    }

    public Point getNextBlock() {
        prevPos = targetPos;
        do {
            curDistance += checkDistance;

            targetPosDouble = offset.add(targetPosDouble.x(),
                targetPosDouble.y(),
                targetPosDouble.z());
            targetPos = VecUtil.at(targetPosDouble.x(), targetPosDouble.y(), targetPosDouble.z());
        } while (curDistance <= maxDistance
            && targetPos.x() == prevPos.x()
            && targetPos.y() == prevPos.y()
            && targetPos.z() == prevPos.z());

        if (curDistance > maxDistance) {
            return null;
        }

        return targetPos;
    }


}
