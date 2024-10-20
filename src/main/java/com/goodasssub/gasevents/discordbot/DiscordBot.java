package com.goodasssub.gasevents.discordbot;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.notifications.Notification;
import net.minestom.server.advancements.notifications.NotificationCenter;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public class DiscordBot {

    // TODO: prolly separate this multiple classes if use for music bot.

    private final GatewayDiscordClient gateway;
    public static String syncChannelName = "None";

    public DiscordBot(String token) {
        DiscordClient client = DiscordClient.create(token);
        this.gateway = client.login().block();
    }

    public void startBot() {
        gateway.on(MessageCreateEvent.class)
            .flatMap(event -> Mono.fromRunnable(() -> {
                final String syncChannelId = Main.getInstance().getConfig().getDiscordChannelId();
                Message message = event.getMessage();

                if (!message.getChannelId().equals(Snowflake.of(syncChannelId))) return;
                if (message.getContent().length() != 5) return;
                if (message.getAuthor().isEmpty()) return;

                User author = message.getAuthor().get();
                if (author.isBot()) return;

                String syncCode = message.getContent();
                Profile profile = Profile.fromSyncCode(syncCode);

                if (profile == null) {
                    message.getChannel()
                        .flatMap(channel -> channel.createMessage("<@%s> This sync code is invalid."
                            .formatted(author.getId().asString())))
                        .subscribe();
                    return;
                }

                if (profile.getDiscordId() != null) {
                    message.getChannel()
                        .flatMap(channel -> channel.createMessage("<@%s> Your account is already synced."
                            .formatted(author.getId().asString())))
                        .subscribe();
                    return;
                }

                profile.setDiscordId(author.getId().asString());
                //profile.checkAndUpdateRank();

                message.getChannel()
                    .flatMap(channel -> channel.createMessage("<@%s> Your account has now been synced."
                        .formatted(author.getId().asString())))
                    .subscribe();

                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(profile.getUuid());

                if (player == null) return;

                Notification newNotification = new Notification(
                    Component.text("Account Synced"),
                    FrameType.CHALLENGE,
                    Material.EMERALD
                );

                NotificationCenter.send(newNotification, player);

                player.sendMessage(Component.text("Your account has been synced to ", NamedTextColor.GREEN)
                    .append(Component.text(author.getUsername(), NamedTextColor.WHITE))
                    .append(Component.text(" on discord.", NamedTextColor.GREEN)));
            }))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();


        Optional<Channel> channelOptional = gateway
            .getChannelById(Snowflake.of(Main.getInstance().getConfig().getDiscordChannelId()))
            .blockOptional();

        if (channelOptional.isPresent() && channelOptional.get() instanceof TextChannel textChannel) {
            syncChannelName = textChannel.getName();
        }

        Main.getInstance().getLogger().info("Discord bot started.");
    }

    public Member getMemberById(String userId) {
        final String guildId = Main.getInstance().getConfig().getDiscordGuildId();

        return gateway.getMemberById(Snowflake.of(guildId), Snowflake.of(userId))
            .block(Duration.ofSeconds(5));
    }

    public void stopBot() {
        gateway.logout().block();
    }
}
