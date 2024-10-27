package com.goodasssub.gasevents.profile;

import com.goodasssub.gasevents.Main;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum Rank {

    // TODO: Store ranks in mongodb



    ZAEL(
        "Zael",
        "1296573356614746334",
        "red",
        true,
        1000,
        "<%color%>",
        List.of("*")
    ),
    OWNER(
        "Owner",
        "1296572843408097393",
        "red",
        true,
        500,
        "<%color%>",
        List.of("*")
    ),
    PERFORMER(
        "Performer",
        null,
        "green",
        true,
        100,
        "<%color%>",
        List.of("*")
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
    private final List<String> permissions;

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
