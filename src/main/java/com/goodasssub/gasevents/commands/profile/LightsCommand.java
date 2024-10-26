package com.goodasssub.gasevents.commands.profile;

import com.goodasssub.gasevents.Main;
import com.goodasssub.gasevents.util.PlayerUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.light.Light;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.Map;

public class LightsCommand extends Command {
    private final String PERMISSION = "core.lights";
    private boolean lights = false;

    public LightsCommand() {
        super("lights");

        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        if (!PlayerUtil.hasPermission(sender, PERMISSION)) return;
        if (!(sender instanceof Player player)) return;

        var players = MinecraftServer.getConnectionManager().getOnlinePlayers();

        lights = !lights;

        if (lights) {
            for (Player player1 : players) {
                player1.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, 99999));
            }
        } else {
            for (Player player1 : players) {
                player1.clearEffects();
            }
        }




        player.sendMessage("lights -> " + (lights ? "enabled" : "disabled"));
    }
}
