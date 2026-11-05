package me.rin.redsignalanticheat.check.checks.packet.badpackets;

import me.rin.redsignalanticheat.check.CheckName;
import me.rin.redsignalanticheat.check.checks.packet.PacketCheck;
import me.rin.redsignalanticheat.event.packetrecive.LACAsyncPacketReceiveEvent;
import me.rin.redsignalanticheat.event.packetrecive.packettype.PacketType;
import me.rin.redsignalanticheat.player.LACPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Impossible entity ID
 */
public class BadPacketsB extends PacketCheck implements Listener {
    public BadPacketsB() {
        super(CheckName.BADPACKETS_B);
    }

    @EventHandler
    public void onAsyncPacketReceive(LACAsyncPacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.USE_ENTITY)
            return;

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (event.getEntityId() < 0)
            flag(player, lacPlayer);
    }

}
