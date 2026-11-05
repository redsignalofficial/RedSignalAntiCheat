package me.rin.redsignalanticheat.listener.unloadedchunk;

import me.rin.redsignalanticheat.Main;
import me.rin.redsignalanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.rin.redsignalanticheat.event.playermove.LACPlayerMoveEvent;
import me.rin.redsignalanticheat.util.async.AsyncUtil;
import me.rin.redsignalanticheat.util.config.ConfigManager;
import me.rin.redsignalanticheat.util.detection.CheckUtil;
import me.rin.redsignalanticheat.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UnloadedChunkListener implements Listener {

    private static final Set<UUID> CHECKABLE_PLAYERS = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> FROZEN_PLAYERS = ConcurrentHashMap.newKeySet();

    public static void handleUnloadedChunks() {
        Bukkit.getPluginManager().registerEvents(new UnloadedChunkListener(), Main.getInstance());
        Scheduler.runTaskTimer(() -> {
            Scheduler.runTaskAsynchronously(true, () -> {
                CHECKABLE_PLAYERS.clear();
            });
        }, 10, 2);
        Scheduler.runTaskTimer(() -> {
            Scheduler.runTaskAsynchronously(true, () -> {
                FROZEN_PLAYERS.clear();
            });
        }, 10, 5);
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        if (!ConfigManager.Config.LagProtection.preventEnteringIntoUnloadedChucks)
            return;
        UUID uuid = event.getPlayer().getUniqueId();

        if (FROZEN_PLAYERS.contains(uuid)) {
            CHECKABLE_PLAYERS.remove(uuid);
            event.setCancelled(true);
            return;
        }

        if (!event.isPlayerRiptiding() && !event.isPlayerGliding() &&
                CheckUtil.distanceHorizontal(event.getFrom(), event.getTo()) < 0.35)
            return;

        CHECKABLE_PLAYERS.add(uuid);
    }

    @EventHandler
    public void onMovement(LACPlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!CHECKABLE_PLAYERS.contains(player.getUniqueId()))
            return;
        Location location = player.getLocation();
        World world = AsyncUtil.getWorld(player);
        if (world == null) world = player.getWorld();
        if (world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
            return;
        FROZEN_PLAYERS.add(player.getUniqueId());
    }

}
