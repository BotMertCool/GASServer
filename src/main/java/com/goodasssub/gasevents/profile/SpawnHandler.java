package com.goodasssub.gasevents.profile;

import com.goodasssub.gasevents.Main;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

@Getter
public class SpawnHandler {
    private Pos normalSpawn;
    private Pos staffSpawn;

    public SpawnHandler(Pos normalSpawn, Pos staffSpawn) {
        this.normalSpawn = normalSpawn;
        this.staffSpawn = staffSpawn;
    }

    public void setStaffSpawn(Pos staffSpawn) {
        this.staffSpawn = staffSpawn;
        this.refreshSpawns();

        Main.getInstance().getConfigManager().getConfig().setStaffSpawnX(staffSpawn.x());
        Main.getInstance().getConfigManager().getConfig().setStaffSpawnY(staffSpawn.y());
        Main.getInstance().getConfigManager().getConfig().setStaffSpawnZ(staffSpawn.z());
        Main.getInstance().getConfigManager().saveConfig();
    }

    public void setNormalSpawn(Pos normalSpawn) {
        this.normalSpawn = normalSpawn;
        this.refreshSpawns();

        Main.getInstance().getConfigManager().getConfig().setNormalSpawnX(normalSpawn.x());
        Main.getInstance().getConfigManager().getConfig().setNormalSpawnY(normalSpawn.y());
        Main.getInstance().getConfigManager().getConfig().setNormalSpawnZ(normalSpawn.z());
        Main.getInstance().getConfigManager().saveConfig();
    }

    public void refreshSpawns() {
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            boolean isStaff = p.hasPermission("core.staff");
            p.setRespawnPoint(isStaff ? staffSpawn : normalSpawn);
        }

    }
}
