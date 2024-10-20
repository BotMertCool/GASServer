package com.goodasssub.gasevents.anticheat;

import com.fasterxml.jackson.databind.util.Named;
import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.PlayerUtil;
import discord4j.core.object.entity.channel.TextChannel;
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
    // TODO: into mongo too?

    // K: Player UUID V: Alert Message
    private final ConcurrentHashMap<Component, UUID> newFlaggedPlayers = new ConcurrentHashMap<>();

    public AntiCheat() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerFlagEvent.class, event -> {
            Component alert = Component.text("[AC] ", NamedTextColor.GOLD)
                .append(Component.text(event.player().getUsername(), NamedTextColor.RED))
                .append(Component.text(" has flagged check ", NamedTextColor.GOLD))
                .append(Component.text(event.checkName(), NamedTextColor.RED))
                .append(Component.text(" [%sms]".formatted(event.player().getLatency()), NamedTextColor.GRAY));

            newFlaggedPlayers.put(alert, event.player().getUuid());
        });

        var disabledChecks = List.of(
            ReachCheck.class,
            BasicSpeedCheck.class,
            HitConsistencyCheck.class,
            KillauraManualCheck.class,
            CpsCheck.class,
            FastBreakCheck.class,
            KillauraManualCheck.class,
            TeleportSpamCheck.class
        );

        MangoAC.Config config = new MangoAC.Config(true, disabledChecks, List.of());
        MangoAC ac = new MangoAC(config);
        ac.start();
    }

    public void sendFlaggedAlerts() {
        if (newFlaggedPlayers.isEmpty()) return;

        List<Player> onlineStaff = PlayerUtil.getOnlineStaff();
        if (onlineStaff.isEmpty()) return;

        TextComponent.Builder alertBatch = Component.text();

        int i = 0;
        final int lastIndex = newFlaggedPlayers.entrySet().size() - 1;
        for (Map.Entry<Component, UUID> entry : newFlaggedPlayers.entrySet()) {
            alertBatch.append(entry.getKey());

            if (i++ != lastIndex) alertBatch.appendNewline();
        }

        newFlaggedPlayers.clear();

        for (Player player : onlineStaff) {
            player.sendMessage(alertBatch);
        }
    }
}
