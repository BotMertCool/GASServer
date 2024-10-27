package com.goodasssub.gasevents.prevention;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;

public class PreventionHandler {

    public PreventionHandler() {
        var eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addChild(this.getEventNode());
    }

    public EventNode<?> getEventNode() {
        var eventNode = EventNode.type("players-node", EventFilter.PLAYER);

        eventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            //TODO: move to prevention.
            if (event.getPlayer().hasPermission("*")) return;
            event.setCancelled(true);
        });
        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            //TODO: move to prevention.
            if (event.getPlayer().hasPermission("*")) return;
            event.setCancelled(true);
        });
        eventNode.addListener(PlayerSwapItemEvent.class, event -> {
            //TODO: move to prevention.
            if (event.getPlayer().hasPermission("*")) return;
            event.setCancelled(true);
        });

        return eventNode;
    }
}
