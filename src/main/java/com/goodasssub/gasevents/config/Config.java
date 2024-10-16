package com.goodasssub.gasevents.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Config {
    private String discordToken = "token";
    private String discordGuildId = "guild id";
    private String discordChannelId = "channel id";
    private String mongoUri = "mongodb://localhost:27017/";
    private String mongoDatabase = "gasevents";
    private String worldPath = "worlds/world";
    private Boolean mojangAuth = true;
    private String viewDistance = "24";
    private String simulationDistance = "24";
    private int maxPlayers = 1000;
    private List<String> sidebarLines = List.of(
        "<gray><strikethrough>" + " ".repeat(35),
        "<gold>Players: <white>%player%",
        "",
        "<gray>play.goodasssub.com",
        "<gray><strikethrough>" + " ".repeat(35)
    );
    private List<String> tabHeader = List.of(
        "<gray><strikethrough>" + " ".repeat(45),
        "<red><bold>GoodAssSub Network"
    );
    private List<String> tabFooter = List.of(
        "<gray>play.goodasssub.com",
        "<gray><strikethrough>" + " ".repeat(45)
    );
}
