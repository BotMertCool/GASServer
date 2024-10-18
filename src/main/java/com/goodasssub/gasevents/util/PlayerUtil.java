package com.goodasssub.gasevents.util;

import com.goodasssub.gasevents.profile.Profile;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

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
}
