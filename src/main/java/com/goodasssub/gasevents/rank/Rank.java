package com.goodasssub.gasevents.rank;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum Rank {

    // TODO: Store ranks in mongodb



    OWNER(
        "Owner",
        "1209578826573549588",
        "red",
        false,
        100,
        "<%color%>",
        List.of("*")
    ),
    SYNCED(
        "Synced",
        null,
        "green",
        false,
        100,
        "<%color%>",
        List.of()
    ),
    DEFAULT(
        "Default",
        null,
        "white",
        false,
        0,
        "<gray>",
        List.of()
    );

    private final String name;
    private final String roleId;
    private final String color;
    private final boolean staff;
    private final int weight;
    private final String prefix;
    private List<String> permissions;

    Rank(String name, String roleId, String color, Boolean staff, int weight, String prefix, List<String> permissions) {
        this.name = name;
        this.roleId = roleId;
        this.color = color;
        this.staff = staff;
        this.weight = weight;
        this.prefix = prefix.replace("%color%", color);
        this.permissions = permissions;
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
