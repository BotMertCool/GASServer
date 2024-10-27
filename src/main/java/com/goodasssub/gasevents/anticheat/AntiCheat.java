package com.goodasssub.gasevents.anticheat;

import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mangolise.anticheat.MangoAC;
import net.mangolise.anticheat.checks.combat.CpsCheck;
import net.mangolise.anticheat.checks.combat.HitConsistencyCheck;
import net.mangolise.anticheat.checks.combat.KillauraManualCheck;
import net.mangolise.anticheat.checks.combat.ReachCheck;
import net.mangolise.anticheat.checks.movement.BasicSpeedCheck;
import net.mangolise.anticheat.checks.movement.TeleportSpamCheck;
import net.mangolise.anticheat.checks.other.FastBreakCheck;
import net.mangolise.anticheat.events.PlayerFlagEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AntiCheat {
    private static final int MAX_PING = 1000;
    private static final int MAX_FLAGS_BEFORE_BAN = 100;

    private final Map<UUID, List<String>> newFlaggedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerFlagCount = new ConcurrentHashMap<>();

    public AntiCheat() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerFlagEvent.class, (event) -> {
            Player player = event.player();

            int ping = player.getLatency();
            if (ping >= MAX_PING) {
                player.kick(Component.text("Ping too high", NamedTextColor.RED));
                return;
            }

            int currentFlags = playerFlagCount.merge(player.getUuid(), 1, Integer::sum);

            if (currentFlags >= MAX_FLAGS_BEFORE_BAN) {
                Punishment punishment = new Punishment(
                    PunishmentType.BAN,
                    Punishment.ANTICHEAT_UUID,
                    player.getUuid(),
                    "AntiCheat Ban",
                    Punishment.PERMANENT
                );

                playerFlagCount.remove(player.getUuid());

                punishment.execute(false, event.player().getUsername());
                return;
            }

            int flags = playerFlagCount.get(player.getUuid());

            if (flags % 9 == 0) {
                newFlaggedPlayers.computeIfAbsent(player.getUuid(), i -> new ArrayList<>()).add(event.checkName());
            }
        });

        var disabledChecks = List.of(
            ReachCheck.class, BasicSpeedCheck.class, HitConsistencyCheck.class,
            KillauraManualCheck.class, CpsCheck.class, FastBreakCheck.class, TeleportSpamCheck.class
        );
        MangoAC.Config config = new MangoAC.Config(true, disabledChecks, List.of());
        MangoAC ac = new MangoAC(config);
        ac.start();
    }

    public void sendFlaggedAlerts() {
        if (newFlaggedPlayers.isEmpty()) return;

        List<Player> onlineStaff = PlayerUtil.getOnlineStaff();
        if (onlineStaff.isEmpty()) return;

        Map<UUID, List<String>> flaggedPlayersCopy = new HashMap<>(newFlaggedPlayers);

        newFlaggedPlayers.clear();

        TextComponent.Builder alertBatch = Component.text();

        for (Map.Entry<UUID, List<String>> entry : flaggedPlayersCopy.entrySet()) {
            var player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(entry.getKey());

            if (player == null) return;

            for (int i = 0; i < entry.getValue().size(); i++) {
                String string = entry.getValue().get(i);

                Component alert = formatAlertMessage(
                    player.getUsername(),
                    string,
                    player.getLatency()
                );

                alertBatch.append(alert).appendNewline();
            }
        }

        for (Player staff : onlineStaff) {
            staff.sendMessage(alertBatch);
        }
    }

    private Component formatAlertMessage(String playerName, String cheatType, int ping) {
        return Component.text("[AC] ", NamedTextColor.GOLD)
            .append(Component.text(playerName, NamedTextColor.RED))
            .append(Component.text(" has flagged check ", NamedTextColor.GOLD))
            .append(Component.text(cheatType, NamedTextColor.RED))
            .append(Component.text(" [%sms]".formatted(ping), NamedTextColor.GRAY));
    }
}
