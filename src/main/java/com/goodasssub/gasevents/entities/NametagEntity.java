package com.goodasssub.gasevents.entities;

import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.tag.Tag;

import java.util.Map;
import java.util.UUID;

public class NametagEntity extends Entity {

    // Custom nametags in minestom https://gist.github.com/cosrnic/14508868147c347ccfdc15fce09fb8d8

    public static final Tag<NametagEntity> NAMETAG_TAG = Tag.Transient("nametag");
    private final UUID owner;

    public NametagEntity(Player player) {
        super(EntityType.INTERACTION);

        this.owner = player.getUuid();

        InteractionMeta meta = (InteractionMeta) this.getEntityMeta();
        meta.setCustomNameVisible(true);
        if (player.getDisplayName() != null) {
            meta.setCustomName(player.getDisplayName().color(NamedTextColor.WHITE));
        } else {
            meta.setCustomName(Component.text("you should probably set this to something..."));
        }

        final float HEIGHT = -0.125F;
        final float WIDTH = 0.1F;

        meta.setHeight(HEIGHT);
        meta.setWidth(WIDTH);
        meta.setPose(Pose.SNIFFING);

        player.setTag(NAMETAG_TAG, this);
        player.addPassenger(this);
    }

    @Override
    public void updateNewViewer(Player viewer) {
        if (viewer.getUuid().equals(this.owner)) {
            return;
        }

        super.updateNewViewer(viewer);

        // a tick after the initial meta is sent, send a new meta packet with a large height to stop it from disappearing, you can read why in Owen1212055's gist.
        this.scheduler().scheduleNextTick(() -> {
            if (viewer.isDead()) return;
            viewer.sendPacket(new EntityMetaDataPacket(this.getEntityId(), Map.of(9, Metadata.Float(99999999))));
        });
    }
}
