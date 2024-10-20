package com.goodasssub.gasevents.util;

import net.minestom.server.coordinate.Vec;

public class VecUtil {
    public static Vec at(double x, double y, double z) {
        int yTrunc = (int) y;
        switch (yTrunc) {
            case 0:
                if (x == 0 && y == 0 && z == 0) {
                    return new Vec(0, 0, 0);
                }
                break;
            case 1:
                if (x == 1 && y == 1 && z == 1) {
                    return new Vec(1, 1, 1);
                }
                break;
            default:
                break;
        }
        return new Vec(x, y, z);
    }
}
