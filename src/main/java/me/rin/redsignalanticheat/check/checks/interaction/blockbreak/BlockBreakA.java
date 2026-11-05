package me.rin.redsignalanticheat.check.checks.interaction.blockbreak;

import me.rin.redsignalanticheat.check.CheckName;
import me.rin.redsignalanticheat.check.buffer.Buffer;
import me.rin.redsignalanticheat.check.checks.interaction.InteractionCheck;
import me.rin.redsignalanticheat.event.playerbreakblock.LACAsyncPlayerBreakBlockEvent;
import me.rin.redsignalanticheat.event.playerbreakblock.LACPlayerBreakBlockEvent;
import me.rin.redsignalanticheat.player.LACPlayer;
import me.rin.redsignalanticheat.player.cache.history.HistoryElement;
import me.rin.redsignalanticheat.player.cache.history.PlayerCacheHistory;
import me.rin.redsignalanticheat.util.hook.plugin.simplehook.AureliumSkillsHook;
import me.rin.redsignalanticheat.util.hook.plugin.simplehook.EnchantsSquaredHook;
import me.rin.redsignalanticheat.util.hook.plugin.simplehook.McMMOHook;
import me.rin.redsignalanticheat.util.hook.plugin.simplehook.VeinMinerHook;
import me.rin.redsignalanticheat.util.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Head rotation
 */
public class BlockBreakA extends InteractionCheck implements Listener {
    public BlockBreakA() {
        super(CheckName.BLOCKBREAK_A);
    }

    @EventHandler
    public void onAsyncBlockBreak(LACAsyncPlayerBreakBlockEvent event) {
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastAsyncResult", flag(event.getPlayer(), event.getLacPlayer(),
                event.getBlock(), event.getEyeLocation(), true));
    }

    @EventHandler
    public void onBlockBreak(LACPlayerBreakBlockEvent event) {
        Buffer buffer = getBuffer(event.getPlayer(), true);
        if (!buffer.getBoolean("lastAsyncResult"))
            return;
        if (!flag(event.getPlayer(), event.getLacPlayer(),
                event.getBlock(), event.getPlayer().getEyeLocation(), false))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("lastFlag") < 500)
            return;
        buffer.put("lastFlag", currentTime);

        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 1)
            return;

        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        Block block = event.getBlock();

        Scheduler.runTaskLater(player, () -> {
            if (getYawChange(player.getEyeLocation(), lacPlayer) > 30.0)
                return;

            if (AureliumSkillsHook.isPrevented(player) ||
                    VeinMinerHook.isPrevented(player) ||
                    McMMOHook.isPrevented(block.getType()))
                return;

            if (EnchantsSquaredHook.hasEnchantment(player, "Excavation", "Deforestation", "Harvesting"))
                return;

            callViolationEvent(player, lacPlayer, null);
        }, 1);
    }

    private boolean flag(Player player, LACPlayer lacPlayer, Block block, Location eyeLocation, boolean async) {
        if (!isCheckAllowed(player, lacPlayer, async))
            return false;

        boolean flag = true;
        Location blockLocation = block.getLocation();
        Block targetBlock = lacPlayer.getTargetBlockExact(10);
        if (targetBlock != null)
            if (distanceHorizontal(blockLocation, targetBlock.getLocation()) <= 3.5)
                flag = false;

        if (flag) {
            Set<Material> transparent = new HashSet<>();
            transparent.add(Material.AIR);
            transparent.add(Material.WATER);
            transparent.add(Material.LAVA);
            transparent.add(block.getType());
            if (targetBlock != null)
                transparent.add(targetBlock.getType());

            List<Block> lineOfSight = player.getLineOfSight(transparent, 10);
            for (Block block1 : lineOfSight) {
                if (distanceHorizontal(blockLocation, block1.getLocation()) <= 3.0) {
                    flag = false;
                    break;
                }
            }
        }

        Vector vector = blockLocation.toVector().setY(0.0D).subtract(eyeLocation.toVector().setY(0.0D));
        float angle = eyeLocation.getDirection().setY(0.0D).angle(vector) * 57.2958F;
        if (angle > 120 && eyeLocation.getPitch() < 60 && eyeLocation.getPitch() > -40)
            flag = true;
        return flag;
    }

    private static double getYawChange(Location eyeLocation, LACPlayer lacPlayer) {
        float yaw = yaw(eyeLocation.getYaw());
        PlayerCacheHistory<Location> eventHistory = lacPlayer.cache.history.onEvent.location;
        PlayerCacheHistory<Location> packetHistory = lacPlayer.cache.history.onPacket.location;
        return Math.max(
                Math.min(Math.abs(yaw - yaw(eventHistory.get(HistoryElement.FROM).getYaw())),
                        Math.abs(yaw - yaw(packetHistory.get(HistoryElement.FROM).getYaw()))),
                Math.min(Math.abs(yaw - yaw(eventHistory.get(HistoryElement.FIRST).getYaw())),
                        Math.abs(yaw - yaw(packetHistory.get(HistoryElement.FIRST).getYaw())))
        );
    }

    private static float yaw(float yaw) {
        yaw = yaw % 360;
        return yaw >= 0 ? yaw : 360 - yaw;
    }

}
