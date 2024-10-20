package com.goodasssub.gasevents.profile.punishments;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.PlayerUtil;
import com.goodasssub.gasevents.util.TimeUtil;
import com.mongodb.client.MongoCursor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import org.bson.Document;

import java.util.Objects;
import java.util.UUID;

@Getter
public class Punishment {
    public static final long PERMANENT = 0L;
    public static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID ANTICHEAT_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");

    private final UUID uuid;
    private PunishmentType punishmentType;
    private UUID executor;
    private UUID target;
    private String reason;
    private long expireTime;
    private boolean removed;
    private UUID removedBy;

    public Punishment(PunishmentType punishmentType, UUID executor, UUID target, String reason, long duration) {
        this.uuid = UUID.randomUUID();
        this.punishmentType = punishmentType;
        this.executor = executor;
        this.target = target;
        this.reason = reason;
        this.expireTime = duration;
        this.removed = false;
        this.removedBy = null;

        this.save();
    }

    public Punishment(UUID uuid) {
        this.uuid = uuid;
        this.punishmentType = null;
        this.executor = null;
        this.target = null;
        this.expireTime = 0L;
        this.removed = false;
        this.removedBy = null;

        this.load();
    }

    public void load() {
        Document document = Main.getInstance().getMongoHandler().getPunishment(this.uuid);

        if (document == null) {
            throw new RuntimeException("punishment null idk why");
        }

        this.punishmentType = PunishmentType.valueOf(document.getString("type"));
        this.executor = UUID.fromString(document.getString("executor_uuid"));
        this.target = UUID.fromString(document.getString("target_uuid"));
        this.reason = document.getString("reason");
        this.expireTime = document.getLong("expire_time");
        this.removed = document.getBoolean("removed");
        this.removedBy = UUID.fromString(document.getString("removed_uuid"));
    }

    public void save() {
        Document document = Main.getInstance().getMongoHandler().getPunishment(this.uuid);

        if (document == null) document = new Document();

        document.put("uuid", String.valueOf(this.uuid));
        document.put("type", String.valueOf(this.punishmentType));
        document.put("reason", Objects.requireNonNullElse(this.reason, "None"));
        document.put("executor_uuid", String.valueOf(this.executor));
        document.put("target_uuid", String.valueOf(this.target));
        document.put("expire_time", this.expireTime);
        document.put("removed", this.removed);
        document.put("removed_uuid", String.valueOf(this.removedBy));

        Main.getInstance().getMongoHandler().upsertProfile(this.uuid, document);
    }

    public boolean isPermanent() {
        return expireTime == 0;
    }

    public boolean isBanned() {
        return !removed && (isPermanent() || System.currentTimeMillis() < expireTime);
    }

    public void removePunishment(UUID removedBy) {
        this.removed = true;
        this.removedBy = removedBy;
    }

    public String getTimeLeft() {
        if (isPermanent()) return "Permanent";

        try {
            long timeLeft = System.currentTimeMillis() - this.expireTime;
            return TimeUtil.formatTime(timeLeft);
        } catch (IllegalArgumentException ignored) {
            return "Expired";
        }
    }

    public void execute(boolean silent, String playerName) {
        Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(this.target);
        this.broadcast(silent, playerName);
        if (target != null) target.kick("test");

        this.save();
    }

    private void broadcast(boolean silent, String playerName) {
        var connectionManager = MinecraftServer.getConnectionManager();

        Component executorDisplayName = Component.text("Error", NamedTextColor.RED);

        if (this.executor.equals(SYSTEM_UUID)) {
            executorDisplayName = Component.text("System", NamedTextColor.GREEN);
        } else if (this.executor.equals(ANTICHEAT_UUID)) {
            executorDisplayName = Component.text("AntiCheat", NamedTextColor.RED);
        } else {
            Player player = connectionManager.getOnlinePlayerByUuid(this.executor);

            if (player != null && player.getDisplayName() != null) {
                executorDisplayName = player.getDisplayName();
            }
        }

        Component targetDisplayName;

        Player player = connectionManager.getOnlinePlayerByUuid(this.executor);
        if (player == null || player.getDisplayName() != null) {
            targetDisplayName = Component.text(playerName, NamedTextColor.GRAY);
        } else {
            targetDisplayName = player.getDisplayName();
        }

        TextComponent.Builder broadcastMsg = Component.text();

        String punishmentTypeString = "Error";

        if (this.punishmentType == PunishmentType.BAN) {
            punishmentTypeString = "banned";
        } else if (this.punishmentType == PunishmentType.MUTE) {
            punishmentTypeString = "muted";
        }

        if (silent) {
            broadcastMsg.append(Component.text("[Silent] ", NamedTextColor.GRAY));
        }

        broadcastMsg.append(targetDisplayName)
            .append(Component.text(" was %s by ".formatted(punishmentTypeString), NamedTextColor.WHITE))
            .append(executorDisplayName);

        if (expireTime != PERMANENT) {
            broadcastMsg.append(Component.text(" for ", NamedTextColor.WHITE));
            broadcastMsg.append(Component.text(this.getTimeLeft(), NamedTextColor.GREEN));
        }

        if (!reason.equals("None")) {
            broadcastMsg.append(Component.text(" for ", NamedTextColor.WHITE));
            broadcastMsg.append(Component.text(this.reason, NamedTextColor.GREEN));
        }

        if (!silent) {
            Audiences.players().sendMessage(broadcastMsg);
            return;
        }

        for (Player staff : PlayerUtil.getOnlineStaff()) {
            staff.sendMessage(broadcastMsg);
        }
    }
}
