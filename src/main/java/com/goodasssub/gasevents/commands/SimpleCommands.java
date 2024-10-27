package com.goodasssub.gasevents.commands;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.profile.Profile;
import com.goodasssub.gasevents.profile.Rank;
import com.goodasssub.gasevents.util.PlayerUtil;
import com.goodasssub.gasevents.util.ShutdownUtil;
import com.goodasssub.gasevents.util.TPSUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.monitoring.BenchmarkManager;

import java.util.List;

public class SimpleCommands {
    public static void register() {
        for (Command command : commands())
            Main.getInstance().getCommandManager().register(command);
    }

    private static List<Command> commands() {
        Command stop = new Command("stop");
        stop.setCondition((sender, commandString) -> PlayerUtil.hasPermission(sender, "*"));
        stop.setDefaultExecutor((sender, context) -> {
            ShutdownUtil.stopServer();
        });

        Command ping = new Command("ping", "latency");
        ping.setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            player.sendMessage(Component.text("Your ping is " + player.getLatency() + "ms", NamedTextColor.GREEN));
        });


        Command debug = new Command("debug");

        debug.setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;

            if (player.getPermissionLevel() < 4) {
                player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                return;
            }
            Pos pos = player.getPosition();

            player.sendMessage(Component.text("""
                x: %f
                y: %f
                z: %f
                ya/: %f
                pitch: %f
                """.formatted(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch())));
        });

        ArgumentString test = ArgumentType.String("test");
        ArgumentString arg = ArgumentType.String("arg");

        debug.addSyntax((sender, context) -> {
            Player player = (Player) sender;

            if (player.getPermissionLevel() < 4) {
                //player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
                //return;
            }

            String testString = context.get(test);
            String argString = context.get(arg);

            switch (testString.toLowerCase()) {
                case "rank": {
                    Profile profile = Profile.fromUuid(player.getUuid());
                    argString = argString.toUpperCase();

                    Rank rank = Rank.valueOf(argString);
                    try {
                        profile.setRank(rank);
                        player.sendMessage(Component.text("[debug] rank set " + argString +  " -> ", NamedTextColor.LIGHT_PURPLE)
                            .append(profile.getFormattedName())
                        );
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("[debug] no rank with name: " + argString);
                    }
                    break;
                }
                case "discordid": {
                    Profile profile = Profile.fromUuid(player.getUuid());
                    profile.setDiscordId(argString);
                    player.sendMessage("[debug] discordid set -> " + argString);
                    break;
                }
                default: {
                    break;
                }

            }
        }, test, arg);

        Command metrics = new Command("metrics");
        metrics.setDefaultExecutor((sender, context) -> {
            BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6;

            String text = String.format("""
                TPS: %.2f
                Tick Time: %.2fms
                Memory: %dMB""", TPSUtil.get(), Main.LAST_TICK.get().getTickTime(), ramUsage);

            sender.sendMessage(Component.text(text));
        });

        Command tps = new Command("tps");
        tps.setDefaultExecutor((sender, context) -> sender.sendMessage(
            Component.text(String.format("TPS: %.2f", TPSUtil.get()), NamedTextColor.GOLD))
        );

        Command time = new Command("time");
        ArgumentInteger inputTime = ArgumentType.Integer("time");

        time.setCondition((sender, commandString) -> PlayerUtil.hasPermission(sender, "core.time"));

        time.setDefaultExecutor((sender, context) ->
            sender.sendMessage(Component.text("Usage: /" +  context.getCommandName() + " <time>", NamedTextColor.RED)));

        time.addSyntax((sender, context) -> {
            InstanceContainer instance = Main.getInstance().getInstanceContainer();
            instance.setTime(context.get(inputTime));
            sender.sendMessage(Component.text("Time set to " + context.get(inputTime), NamedTextColor.GREEN));
        }, inputTime);

        Command spawn = new Command("spawn");

        spawn.setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.teleport(player.getRespawnPoint());
            }
        });

        return List.of(stop, ping, spawn, debug, metrics, tps, time);
    }
}
