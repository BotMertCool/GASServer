package com.goodasssub.gasevents.util;

import com.goodasssub.gasevents.Main;
import net.minestom.server.monitoring.TickMonitor;

public class TPSUtil {
    public static double get() {
        TickMonitor tickMonitor = Main.LAST_TICK.get();
        double tickTime = tickMonitor.getTickTime();
        double rawTps = 1000.0 / tickTime;
        return Math.min(rawTps, 20);
    }
}
