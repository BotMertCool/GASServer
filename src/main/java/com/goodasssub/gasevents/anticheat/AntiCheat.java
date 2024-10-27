package com.goodasssub.gasevents.anticheat;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.profile.punishments.Punishment;
import com.goodasssub.gasevents.profile.punishments.PunishmentType;
import com.goodasssub.gasevents.util.PlayerUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.time.TimeUnit;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AntiCheat {
    private static final int MAX_PING = 1000;
    private static final int MAX_FLAGS_BEFORE_BAN = 150;

    private final Map<UUID, List<String>> newFlaggedPlayers = new ConcurrentHashMap<>();
    @Getter private final Map<UUID, Integer> playerFlagCount = new ConcurrentHashMap<>();

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
                    "[AC] Cheating.",
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

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            CompletableFuture.runAsync(() -> {
                Profile.getCache().entrySet().removeIf(entry -> entry.getValue().getPlayer() == null);
                Main.getInstance().getAntiCheat().sendFlaggedAlerts();
            });
        }).repeat(1, TimeUnit.SERVER_TICK).schedule();
    }

    public void sendFlaggedAlerts() {
        List<Player> onlineStaff = PlayerUtil.getOnlineStaff();
        if (onlineStaff.isEmpty()) return;
        if (newFlaggedPlayers.isEmpty()) return;

        Map<UUID, List<String>> flaggedPlayersCopy = new HashMap<>(newFlaggedPlayers);

        newFlaggedPlayers.clear();

        List<Component> alerts = new ArrayList<>();
        for (Map.Entry<UUID, List<String>> entry : flaggedPlayersCopy.entrySet()) {
            var player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(entry.getKey());

            if (player == null) return;

            for (int i = 0; i < entry.getValue().size(); i++) {
                String string = entry.getValue().get(i);

                alerts.add(formatAlertMessage(
                    player.getUsername(),
                    string,
                    player.getLatency()
                ));
            }
        }

        JoinConfiguration config = JoinConfiguration.builder()
            .separator(Component.newline())
            .build();

        for (Player staff : onlineStaff) {
            staff.sendMessage(Component.join(config, alerts));
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
