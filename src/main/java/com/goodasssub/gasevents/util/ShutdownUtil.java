package com.goodasssub.gasevents.util;

import com.goodasssub.gasevents.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ShutdownUtil {
    public static void stopServer() {
        Main.getInstance().getDiscordBot().stopBot();
        Main.getInstance().getMongoHandler().close();

        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers())
            player.kick(Component.text("Server is shutting down", NamedTextColor.RED));

        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS)
            .execute(MinecraftServer::stopCleanly);
    }
}
