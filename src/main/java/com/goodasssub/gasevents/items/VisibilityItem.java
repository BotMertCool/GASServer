package com.goodasssub.gasevents.items;

import com.goodasssub.gasevents.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VisibilityItem {
    public static final Map<UUID, Boolean> playerVisibilityMap = new HashMap<>();

    public static ItemStack getEnabledItem() {
        return ItemStack.builder(Material.LIME_DYE)
            .customName(MiniMessage.miniMessage()
                .deserialize("<reset><gold>Player Visibility: <green>Enabled"))
            .lore(Component.text("Right click to toggle player visibility", NamedTextColor.GRAY))
            .lore(MiniMessage.miniMessage()
                    .deserialize("<reset><gray>Right click to toggle player visibility"))
            .glowing(true)
            .build();
    }

    public static ItemStack getDisabledItem() {
        return ItemStack.builder(Material.GRAY_DYE)
            .customName(MiniMessage.miniMessage()
                .deserialize("<gold>Player Visibility: <red>Disabled"))
            .lore(Component.text("Right click to toggle player visibility", NamedTextColor.GRAY))
            .glowing(true)
            .build();
    }

    public static int inventorySlot = 0;

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
            if (target.getPermissionLevel() == 4) return;

            target.addViewer(player);
            if (!newVisibility) {
                target.removeViewer(player);
            }
        });

        String message = newVisibility ? "Players are now visible" : "Players are now invisible";
        player.sendMessage(message);
    }
}
