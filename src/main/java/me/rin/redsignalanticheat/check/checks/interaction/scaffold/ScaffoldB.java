package me.rin.redsignalanticheat.check.checks.interaction.scaffold;

import me.rin.redsignalanticheat.check.CheckName;
import me.rin.redsignalanticheat.check.buffer.Buffer;
import me.rin.redsignalanticheat.check.checks.interaction.InteractionCheck;
import me.rin.redsignalanticheat.event.playerplaceblock.LACAsyncPlayerPlaceBlockEvent;
import me.rin.redsignalanticheat.player.LACPlayer;
import me.rin.redsignalanticheat.player.cache.PlayerCache;
import me.rin.redsignalanticheat.player.cache.history.HistoryElement;
import me.rin.redsignalanticheat.util.hook.plugin.FloodgateHook;
import me.rin.redsignalanticheat.util.scheduler.Scheduler;
import me.rin.redsignalanticheat.version.VerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

/**
 * Sprint
 */
public class ScaffoldB extends InteractionCheck implements Listener {
    public ScaffoldB() {
        super(CheckName.SCAFFOLD_B);
    }

    @EventHandler
    public void onAsyncBlockPlace(LACAsyncPlayerPlaceBlockEvent event) {
        Player player = event.getPlayer();
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!isScaffoldPlacement(player, event.getBlock(), event.getBlockAgainst()))
            return;

        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        for (Block withinBlock : getWithinBlocks(player)) {
            if (withinBlock.getType() != Material.AIR)
                return;
        }

        if (getEffectAmplifier(player, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(player, PotionEffectType.SPEED) > 5)
            return;

        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++) {
            if (!cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsFalse ||
                    !cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsFalse)
                return;
        }

        if (!player.isSprinting())
            return;

        Buffer buffer = getBuffer(player, true);
        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 2)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

}
