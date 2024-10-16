package com.goodasssub.gasevents.anticheat;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.mangolise.anticheat.MangoAC;
import net.mangolise.anticheat.checks.combat.CpsCheck;
import net.mangolise.anticheat.checks.combat.HitConsistencyCheck;
import net.mangolise.anticheat.checks.combat.KillauraManualCheck;
import net.mangolise.anticheat.checks.combat.ReachCheck;
import net.mangolise.anticheat.events.PlayerFlagEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AntiCheat {
    // TODO: into mongo too?
    static Map<UUID, List<Flag>> flagsList = new ConcurrentHashMap<>();

    private static int getFlags(String checkName, UUID uuid) {
        // TODO: idek what im coding
        List<Flag> flags = flagsList.get(uuid);

        final int ONE_MINUTE = 60_000;

        // TODO: violations go away after mins but stay if reactivated within time.
        List<Flag> nameCheckedFlags = flags.stream()
            .filter((flag) -> flag.getCheckName().equals(checkName))
            .sorted(Comparator.comparingLong(Flag::getTimestamp).reversed())
            .toList();

        float violations = nameCheckedFlags.size();

/*        for (Flag flag : flags) {
            if (flag.getTimestamp() + ONE_MINUTE < System.currentTimeMillis()) {
                flags.remove(flag);
                flagsList.put(uuid, flags);
                continue;
            }
            if (!flag.getCheckName().equals(checkName)) continue;
            violations++;
        }*/

        Audiences.players().sendMessage(Component.newline());
        flags.stream()
            .filter((flag) -> flag.getCheckName().equals(checkName))
            .sorted(Comparator.comparingLong(Flag::getTimestamp).reversed())
            .toList()
            .forEach((f) -> Audiences.players().sendMessage(Component.text(f.getCheckName() + " " + f.getTimestamp())));
        Audiences.players().sendMessage(Component.newline());

        if (checkName.equals("BasicSpeed") ) {
            if (violations % 3 != 0)
                return 0;
            return (int) (violations / 3);
        }

        return (int) violations;
    }

    public static void init() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerFlagEvent.class, event -> {
            List<Player> onlineStaff = PlayerUtil.getOnlineStaff();

            Player target = event.player();

            List<Flag> checksFlagged = flagsList.get(target.getUuid());
            if (checksFlagged == null) {
                checksFlagged = new ArrayList<>();
            }
            checksFlagged.add(new Flag(event.checkName(), System.currentTimeMillis()));

            flagsList.put(target.getUuid(), checksFlagged);
            int flags = getFlags(event.checkName(), target.getUuid());
            if (flags == 0) return;

            String alertString = ("<gray>[<red>AC<gray>] <green>%s<green> <reset>has flagged check %s <gray>[<red>%s<gray>] (%s) (Ping %sms)").formatted(
                target.getUsername(),
                event.checkName(),
                flags,
                checksFlagged.size(),
                target.getLatency()
            );

            Component alert = Main.getInstance().getMiniMessage().deserialize(alertString);

            for (Player player : onlineStaff) {
                player.sendMessage(alert);
            }
        });

        var disabledChecks = List.of(
            ReachCheck.class,
            HitConsistencyCheck.class,
            KillauraManualCheck.class,
            CpsCheck.class,
            KillauraManualCheck.class
        );

        MangoAC.Config config = new MangoAC.Config(false, disabledChecks, List.of());
        MangoAC ac = new MangoAC(config);
        ac.start();
    }
}
