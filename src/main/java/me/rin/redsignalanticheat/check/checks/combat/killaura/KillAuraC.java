package me.rin.redsignalanticheat.check.checks.combat.killaura;

import me.rin.redsignalanticheat.Main;
import me.rin.redsignalanticheat.check.CheckName;
import me.rin.redsignalanticheat.check.buffer.Buffer;
import me.rin.redsignalanticheat.check.checks.combat.CombatCheck;
import me.rin.redsignalanticheat.event.playerattack.LACAsyncPlayerAttackEvent;
import me.rin.redsignalanticheat.player.LACPlayer;
import me.rin.redsignalanticheat.player.cache.history.HistoryElement;
import me.rin.redsignalanticheat.event.playerattack.LACPlayerAttackEvent;
import me.rin.redsignalanticheat.util.scheduler.Scheduler;
import me.rin.redsignalanticheat.version.VerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

/**
 * Attack through blocks
 */
public class KillAuraC extends CombatCheck implements Listener {
    public KillAuraC() {
        super(CheckName.KILLAURA_C);
    }

    @EventHandler
    public void onAsyncHit(LACAsyncPlayerAttackEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (lacPlayer.isGliding() || lacPlayer.isRiptiding() || player.isInsideVehicle())
            return;

        boolean playerImmured = true;
        Set<Block> playerWithinBlocks = getWithinBlocks(player);
        for (Block block : playerWithinBlocks) {
            if (isNotHittableThrough(block))
                continue;
            playerImmured = false;
            break;
        }
        if (playerImmured && distance(lacPlayer.cache.history.onEvent.location.get(HistoryElement.FROM),
                lacPlayer.cache.history.onEvent.location.get(HistoryElement.FIRST)) >= 0.175)
            playerImmured = false;
        if (playerImmured && distance(lacPlayer.cache.history.onPacket.location.get(HistoryElement.FROM),
                lacPlayer.cache.history.onPacket.location.get(HistoryElement.FIRST)) >= 0.175)
            playerImmured = false;

        if (!playerImmured)
            return;

        Buffer buffer = getBuffer(player, true);
        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, null, buffer, 12 * 1000);
        });
    }

    @EventHandler
    public void onHit(LACPlayerAttackEvent event) {
        if (!event.isEntityAttackCause())
            return;
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer))
            return;

        if (lacPlayer.isGliding() || lacPlayer.isRiptiding() || player.isInsideVehicle())
            return;

        Entity entity = event.getEntity();
        boolean entityImmured = true;
        Set<Block> entityWithinBlocks = getWithinBlocks(entity);
        for (Block block : entityWithinBlocks) {
            if (isNotHittableThrough(block))
                continue;
            entityImmured = false;
            break;
        }
        if (entityImmured && entity instanceof Player) {
            Player damaged = (Player) entity;
            LACPlayer lacDamagedPlayer = LACPlayer.getLacPlayer(damaged);
            if (distance(lacDamagedPlayer.cache.history.onEvent.location.get(HistoryElement.FROM),
                    lacDamagedPlayer.cache.history.onEvent.location.get(HistoryElement.FIRST)) >= 0.175)
                entityImmured = false;
            if (distance(lacDamagedPlayer.cache.history.onPacket.location.get(HistoryElement.FROM),
                    lacDamagedPlayer.cache.history.onPacket.location.get(HistoryElement.FIRST)) >= 0.175)
                entityImmured = false;
        }

        if (!entityImmured)
            return;

        Buffer buffer = getBuffer(player, true);
        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, Main.getBufferDurationMils() - 1000L);
    }

    private static boolean isNotHittableThrough(Block block) {
        Material material = block.getType();
        String name = material.name().toLowerCase();
        return material.isOccluding() ||
                material == Material.GLASS || name.endsWith("_glass") ||
                material == VerUtil.material.get("TALL_GRASS") || material == VerUtil.material.get("LARGE_FERN") ||
                material == VerUtil.material.get("SUNFLOWER") || material == VerUtil.material.get("LILAC") ||
                material == VerUtil.material.get("ROSE_BUSH") || material == VerUtil.material.get("PEONY") ||
                material == Material.SUGAR_CANE || material == VerUtil.material.get("PITCHER_PLANT");
    }

}
