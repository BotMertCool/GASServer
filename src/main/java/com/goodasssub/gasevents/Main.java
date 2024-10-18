package com.goodasssub.gasevents;

import com.goodasssub.gasevents.anticheat.AntiCheat;
import com.goodasssub.gasevents.commands.*;
import com.goodasssub.gasevents.commands.profile.NicknameCommand;
import com.goodasssub.gasevents.commands.profile.PlayersCommand;
import com.goodasssub.gasevents.commands.profile.SyncCommand;
import com.goodasssub.gasevents.commands.staff.TeleportCommand;
import com.goodasssub.gasevents.commands.staff.GamemodeCommand;
import com.goodasssub.gasevents.config.Config;
import com.goodasssub.gasevents.config.ConfigManager;
import com.goodasssub.gasevents.database.MongoHandler;
import com.goodasssub.gasevents.discordbot.DiscordBot;
import com.goodasssub.gasevents.profile.ProfileHandler;
import com.goodasssub.gasevents.util.ShutdownUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.CommandManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.monitoring.TickMonitor;

import net.minestom.server.ping.ResponseData;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.identity.NamedAndIdentified;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    @Getter public static Main instance;

    //todo: AsdffdasasfdafdsfdsaafsdafsdASasdf
    public static final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    @Getter private final Config config;
    @Getter private final Logger logger;
    @Getter private final MongoHandler mongoHandler;
    @Getter private final ProfileHandler profileHandler;
    @Getter private final DiscordBot discordBot;
    @Getter private final InstanceContainer instanceContainer;
    @Getter private final MiniMessage miniMessage;

    // TODO: Figure out how to do unit tests.

    // TODO: maybe take in args in constructor.
    public Main() {
        long startTime = System.currentTimeMillis();

        instance = this;
        logger = LoggerFactory.getLogger("com.goodasssub.gasevents");
        miniMessage = MiniMessage.builder().build();
        config = new ConfigManager().getConfig();

        if (config.getDiscordToken().equals("token")) {
            // TODO: custom exceptions?
            throw new RuntimeException("Please set the discord bot token, guild id, channel id in the config.json file.");
        }

        System.setProperty("minestom.chunk-view-distance", config.getViewDistance());
        System.setProperty("minestom.entity-view-distance", config.getViewDistance());

        MinecraftServer minecraftServer = MinecraftServer.init();
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new GamemodeCommand());
        commandManager.register(new PlayersCommand());
        commandManager.register(new SyncCommand());
        commandManager.register(new NicknameCommand());
        commandManager.register(new TeleportCommand());

        SimpleCommands.register(commandManager);

        commandManager.setUnknownCommandCallback((sender, command) ->
            sender.sendMessage(Component.text("Unknown command.", NamedTextColor.RED))
        );

        DynamicRegistry<DimensionType> registry = MinecraftServer.getDimensionTypeRegistry();
        DimensionType fullBright = DimensionType.builder()
            .ambientLight(0.3f)
            .build();
        registry.register(NamespaceID.from("minestorm:custom"), fullBright);

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // TODO: use polar?
        instanceContainer = instanceManager.createInstanceContainer(registry.getKey(fullBright));
        instanceContainer.setChunkLoader(new AnvilLoader(config.getWorldPath()));
        instanceContainer.setTimeRate(0);
        instanceContainer.setTime(18000);

        MinecraftServer.getBenchmarkManager().enable(Duration.of(5, TimeUnit.SECOND));

        var eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(ServerListPingEvent.class, event -> {
            // TODO: add to config, desc, server name, etc
            ResponseData responseData = event.getResponseData();

            responseData.addEntry(NamedAndIdentified.named(Component.text("Good Ass Sub Minecraft", NamedTextColor.RED)));
            String desc = "<red><bold>GoodAssSub <reset><gray>| <white>discord.gg/GoodAssSub\n" +
                "Offical Event Server.";

            responseData.setDescription(Main.getInstance().getMiniMessage().deserialize(desc));
            responseData.setMaxPlayer(config.getMaxPlayers());
        });

        // TODO: change last tick
        eventHandler.addListener(ServerTickMonitorEvent.class, event -> LAST_TICK.set(event.getTickMonitor()));
        eventHandler.addListener(PickupItemEvent.class, event -> event.setCancelled(true));
        eventHandler.addListener(ItemDropEvent.class, event -> event.setCancelled(true));
        eventHandler.addListener(InventoryPreClickEvent.class, event -> event.setCancelled(true));

        this.profileHandler = new ProfileHandler(this);
        AntiCheat.init();

        if (config.getMojangAuth()) {
            MojangAuth.init();
            logger.info("Mojang auth initialized.");
        } else {
            logger.info("Mojang auth disabled.");
        }

        mongoHandler = new MongoHandler(config.getMongoUri(), config.getMongoDatabase());
        discordBot = new DiscordBot(config.getDiscordToken());
        discordBot.startBot();

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            ShutdownUtil.stopServer();
            logger.info("Stopping...");
        });

        minecraftServer.start("0.0.0.0", 25565);

        logger.info("Server started after {}ms.", (System.currentTimeMillis() - startTime));
    }

    public static void main(String[] args) {
        new Main();
    }
}