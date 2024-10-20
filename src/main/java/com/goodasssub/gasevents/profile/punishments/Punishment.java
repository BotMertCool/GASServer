package com.goodasssub.gasevents.profile.punishments;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.util.PlayerUtil;
import com.goodasssub.gasevents.util.TimeUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import org.bson.Document;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Punishment {
    // TODO: ADD? : @Getter private final static Map<UUID, Profile> cache = new ConcurrentHashMap<>();

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
        this.executor = UUID.fromString(document.getString("uuid_executor"));
        this.target = UUID.fromString(document.getString("uuid_target"));
        this.reason = document.getString("reason");
        this.expireTime = document.getLong("expire_time");
        this.removed = document.getBoolean("removed");

        if (document.getString("uuid_remover") != null) {
            this.removedBy = UUID.fromString(document.getString("uuid_remover"));
        }
    }

    public void save() {
        Document document = Main.getInstance().getMongoHandler().getPunishment(this.uuid);

        if (document == null) document = new Document();

        document.put("uuid", String.valueOf(this.uuid));
        document.put("type", String.valueOf(this.punishmentType));
        document.put("reason", Objects.requireNonNullElse(this.reason, "None"));
        document.put("uuid_executor", String.valueOf(this.executor));
        document.put("uuid_target", String.valueOf(this.target));
        document.put("expire_time", this.expireTime);
        document.put("removed", this.removed);

        if (this.removedBy != null) {
            document.put("uuid_remover", this.removedBy.toString());
        }

        Main.getInstance().getMongoHandler().upsertPunishment(this.uuid, document);
    }

    public boolean isPermanent() {
        return expireTime == 0;
    }

    public boolean isActive() {
        return !removed && (isPermanent() || System.currentTimeMillis() < expireTime);
    }

    public void removePunishment(boolean silent, String playerName, UUID removedBy) {
        this.removed = true;
        this.removedBy = removedBy;
        this.broadcast(silent, playerName, true);

        this.save();
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
        this.broadcast(silent, playerName, false);
        if (target != null) target.kick("test");

        this.save();
    }

    public Component getMessage() {
        TextComponent.Builder builder = Component.text().color(NamedTextColor.RED);

        String punishmentTypeString = "Error";
        if (this.punishmentType == PunishmentType.BAN) {
            punishmentTypeString = "banned";
        } else if (this.punishmentType == PunishmentType.MUTE) {
            punishmentTypeString = "muted";
        }

        builder.append(Component.text("You are %s!" + punishmentTypeString, NamedTextColor.RED));
        builder.appendNewline();
        builder.appendNewline();

        if (!this.isPermanent()) {
            builder.append(Component.text("Remaining: ", NamedTextColor.RED));
            builder.append(Component.text(this.getTimeLeft(), NamedTextColor.WHITE));
        } else {
            builder.append(Component.text("Remaining: ", NamedTextColor.RED));
            builder.append(Component.text("Permanent", NamedTextColor.WHITE));
        }

        builder.appendNewline();
        builder.append(Component.text("Reason: ", NamedTextColor.RED));
        builder.append(Component.text(this.getReason(), NamedTextColor.WHITE));

        return builder.build();
    }

    private void broadcast(boolean silent, String playerName, boolean removal) {
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

        if (removal) {
            punishmentTypeString = "un" + punishmentTypeString;
        }

        if (silent) {
            broadcastMsg.append(Component.text("[Silent] ", NamedTextColor.GRAY));
        }

        broadcastMsg.append(targetDisplayName)
            .append(Component.text(" was %s by ".formatted(punishmentTypeString), NamedTextColor.WHITE))
            .append(executorDisplayName);

        if (!removal) {
            if (expireTime != PERMANENT) {
                broadcastMsg.append(Component.text(" for ", NamedTextColor.WHITE));
                broadcastMsg.append(Component.text(this.getTimeLeft(), NamedTextColor.GREEN));
            }

            if (!reason.equals("None")) {
                broadcastMsg.append(Component.text(" for ", NamedTextColor.WHITE));
                broadcastMsg.append(Component.text(this.reason, NamedTextColor.GREEN));
            }
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
