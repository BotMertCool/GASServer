package com.goodasssub.gasevents.profile.punishments;

import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class Punishment {
    @Getter private PunishmentType punishmentType;
    @Getter private UUID target;
    @Getter private boolean ban;

    public void broadcast(Component displayName) {

    }
}
