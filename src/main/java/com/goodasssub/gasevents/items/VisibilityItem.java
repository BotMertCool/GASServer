package com.goodasssub.gasevents.items;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VisibilityItem {
    @Getter private static final Map<UUID, Boolean> playerVisibilityMap = new HashMap<>();
    @Getter private static final int inventorySlot = 4;

    public static ItemStack getEnabledItem() {
        return ItemStack.builder(Material.LIME_DYE)
            .customName(MiniMessage.miniMessage()
                .deserialize("<gold>Player Visibility: <green>Enabled")
                .decoration(TextDecoration.ITALIC, false))
            .lore(Component.text("Right click to toggle player visibility.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false))
            .glowing(true)
            .build();
    }

    public static ItemStack getDisabledItem() {
        return ItemStack.builder(Material.GRAY_DYE)
            .customName(MiniMessage.miniMessage()
                .deserialize("<gold>Player Visibility: <red>Disabled")
                .decoration(TextDecoration.ITALIC, false))
            .lore(Component.text("Right click to toggle player visibility.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false))
            .glowing(true)
            .build();
    }

    public static void toggleVisibility(Player player) {
        boolean currentVisibility = playerVisibilityMap.getOrDefault(player.getUuid(), true);
        boolean newVisibility = !currentVisibility;
        playerVisibilityMap.put(player.getUuid(), newVisibility);

        if (newVisibility) {
            player.getInventory().setItemStack(inventorySlot, getEnabledItem());
        } else {
            player.getInventory().setItemStack(inventorySlot, getDisabledItem());
        }

        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(target -> {
            if (target.hasPermission("core.staff")) return;

            target.addViewer(player);
            if (!newVisibility) {
                target.removeViewer(player);
            }
        });

        Component enabled = MiniMessage.miniMessage()
            .deserialize("<gold>Player Visibility: <green>Enabled");

        Component disabled = MiniMessage.miniMessage()
            .deserialize("<gold>Player Visibility: <red>Disabled");

        Component message = newVisibility ? enabled : disabled;
        player.sendMessage(message);
    }
}
