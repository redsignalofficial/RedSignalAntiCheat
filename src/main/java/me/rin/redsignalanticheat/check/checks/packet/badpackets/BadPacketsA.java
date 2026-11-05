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
 * Self-Damage
 */
public class BadPacketsA extends PacketCheck implements Listener {
    public BadPacketsA() {
        super(CheckName.BADPACKETS_A);
    }

    @EventHandler
    public void onAsyncPacketReceive(LACAsyncPacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.USE_ENTITY)
            return;

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (event.getEntityId() == player.getEntityId())
            flag(player, lacPlayer);
    }

}
