package me.rin.redsignalanticheat.check.checks.combat.autoclicker;

import me.rin.redsignalanticheat.check.CheckName;
import me.rin.redsignalanticheat.check.buffer.Buffer;
import me.rin.redsignalanticheat.check.checks.combat.CombatCheck;
import me.rin.redsignalanticheat.player.LACPlayer;
import me.rin.redsignalanticheat.util.player.cps.CPSListener;
import me.rin.redsignalanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Max CPS
 */
public class AutoClickerB extends CombatCheck implements Listener {
    public AutoClickerB() {
        super(CheckName.AUTOCLICKER_B);
    }

    @EventHandler
    public void onHit(PlayerInteractEvent event) {
        if (isExternalNPC(event)) return;
        if (event.getAction() == Action.PHYSICAL)
            return;
        Player player = event.getPlayer();
        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);

        Scheduler.entityThread(player, () -> {
            if (!isCheckAllowed(player, lacPlayer))
                return;

            if (CPSListener.getCurrentCps(player) < 47)
                return;

            Buffer buffer = getBuffer(player);
            long currentTime = System.currentTimeMillis();
            if (buffer.isExists("lastFlag") && currentTime - buffer.getLong("lastFlag") < 2000)
                return;
            buffer.put("lastFlag", currentTime);

            callViolationEvent(player, lacPlayer, event);
        });
    }

}
