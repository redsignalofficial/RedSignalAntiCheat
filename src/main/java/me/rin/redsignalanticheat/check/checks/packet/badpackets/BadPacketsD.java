package me.rin.redsignalanticheat.check.checks.packet.badpackets;

import me.rin.redsignalanticheat.check.CheckName;
import me.rin.redsignalanticheat.check.checks.packet.PacketCheck;
import me.rin.redsignalanticheat.event.packetrecive.LACAsyncPacketReceiveEvent;
import me.rin.redsignalanticheat.event.packetrecive.packettype.PacketType;
import me.rin.redsignalanticheat.player.LACPlayer;
import me.rin.redsignalanticheat.util.scheduler.Scheduler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Impassible SetCreativeSlot packet
 */
public class BadPacketsD extends PacketCheck implements Listener {
    public BadPacketsD() {
        super(CheckName.BADPACKETS_D);
    }

    @EventHandler
    public void onAsyncPacketReceive(LACAsyncPacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.SET_CREATIVE_SLOT)
            return;

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (player.getGameMode() == GameMode.CREATIVE ||
                System.currentTimeMillis() - lacPlayer.cache.lastGamemodeChange < 500)
            return;

        Scheduler.runTaskLater(() -> {
            if (!player.isOnline() || lacPlayer.leaveTime != 0)
                return;

            if (player.getGameMode() == GameMode.CREATIVE ||
                    System.currentTimeMillis() - lacPlayer.cache.lastGamemodeChange < 500)
                return;

            flag(player, lacPlayer);
        }, 1);
    }

}
