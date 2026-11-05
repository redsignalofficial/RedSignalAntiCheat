package me.rin.redsignalanticheat.check.checks.packet.morepackets;

import me.rin.redsignalanticheat.check.CheckName;
import me.rin.redsignalanticheat.check.buffer.Buffer;
import me.rin.redsignalanticheat.check.checks.packet.PacketCheck;
import me.rin.redsignalanticheat.event.packetrecive.LACAsyncPacketReceiveEvent;
import me.rin.redsignalanticheat.player.LACPlayer;
import me.rin.redsignalanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Packet limiter
 */
public class MorePacketsA extends PacketCheck implements Listener {
    public MorePacketsA() {
        super(CheckName.MOREPACKETS_A);
    }

    private static final int LONG_LIMIT = (int) Math.ceil(20 * 1.6 * 5.5);
    private static final int SHORT_LIMIT = (int) Math.ceil(20 * 0.8 * 4.5);

    //      Long interval:

    @EventHandler
    public void onAsyncPacketReceiveA(LACAsyncPacketReceiveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('A', buffer, 1600L, (int) Math.ceil(LONG_LIMIT * 1.9), 3))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 1500)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

    //      Short interval:

    @EventHandler
    public void onAsyncPacketReceiveB(LACAsyncPacketReceiveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!limitPackets('B', buffer, 800L, (int) Math.ceil(SHORT_LIMIT * 1.9), 5))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlagTime") < 700)
            return;
        buffer.put("lastFlagTime", System.currentTimeMillis());

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

}
