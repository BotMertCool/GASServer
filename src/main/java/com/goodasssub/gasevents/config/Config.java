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
    private String viewDistance = "16";
    private String simulationDistance = "16";
    private int maxPlayers = 1000;
    private double staffSpawnX = 0.5f;
    private double staffSpawnY = 0.5f;
    private double staffSpawnZ = 0.5f;
    private double normalSpawnX = 0.5f;
    private double normalSpawnY = 0.5f;
    private double normalSpawnZ = 0.5f;
    private String scoreboardTitle = "<red><bold>YZY CRAFT";
    private List<String> sidebarLines = List.of(
        "<gray><strikethrough>" + " ".repeat(35),
        "<gold>Players: <white>%player%",
        "",
        "<gray>see.kanye.live",
        "<gray><strikethrough>" + " ".repeat(35)
    );
    private List<String> tabHeader = List.of(
        "<gray><strikethrough>" + " ".repeat(45),
        "<red><bold>YZY CRAFT Network"
    );
    private List<String> tabFooter = List.of(
        "<gray>see.kanye.live",
        "<gray><strikethrough>" + " ".repeat(45)
    );
}
