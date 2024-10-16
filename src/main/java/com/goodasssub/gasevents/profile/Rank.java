package com.goodasssub.gasevents.profile;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum Rank {

    // TODO: sum other way
    OWNER(
        "Owner",
        "1209578826573549588",
        "dark_red",
        true,
        1000,
        "<gray>[<%color%>Owner<gray>]</gray> "
    ),
    MANAGER(
        "Manager",
        "1209578826573549588",
        "red",
        true,
        900,
        "<gray>[<%color%>Manager<gray>]</gray> "
    ),
    ADMIN(
        "Admin",
        "1039916663665074256",
        "#FFFFFF",
        true,
        500,
        "<gray>[<%color%>Admin<gray>]</gray> "
    ),
    MOD(
        "Mod",
        "1030225908293971988",
        "#1F8B4C",
        true,
        400,
        "<gray>[<%color%>Mod<gray>]</gray> "
    ),
    CHAT_MOD(
        "Chat Mod",
        "1133504846230736917",
        "#FFCA99",
        true,
        300,
        "<gray>[<%color%>Chat Mod<gray>]</gray> "
    ),
    BOOSTER(
        "Booster",
        "1042182066139308193",
        "light_purple",
        false,
        200,
        "<%color%>"
    ),
    SYNCED(
        "Synced",
        null,
        "green",
        false,
        100,
        "<%color%>"
    ),
    DEFAULT(
        "Default",
        null,
        "white",
        false,
        0,
        "<gray>"
    );

    private final String name;
    private final String roleId;
    private final String color;
    private final boolean staff;
    private final int weight;
    private final String prefix;

    Rank(String name, String roleId, String color, Boolean staff, int weight, String prefix) {
        this.name = name;
        this.roleId = roleId;
        this.color = color;
        this.staff = staff;
        this.weight = weight;
        this.prefix = prefix.replace("%color%", color);
    }

    public static List<Rank> sortedByWeight() {
        return Arrays.stream(Rank.values())
            .sorted(Comparator.comparingInt(Rank::getWeight).reversed())
            .collect(Collectors.toList());
    }

    public static Rank getRankByName(String name) {
        for (Rank rank : Rank.values()) {
            if (rank.getName().equals(name))
                return rank;
        }
        return null;
    }
    public static Rank getRankByRoleId(String roleId) {
        for (Rank rank : Rank.values()) {
            if (rank.getRoleId() == null) continue;
            if (rank.getRoleId().equals(roleId))
                return rank;
        }
        return null;
    }
}
