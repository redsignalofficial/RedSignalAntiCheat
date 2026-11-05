package me.rin.redsignalanticheat.check.checks.interaction;

import me.rin.redsignalanticheat.check.Check;
import me.rin.redsignalanticheat.check.CheckName;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

public abstract class InteractionCheck extends Check {
    public InteractionCheck(CheckName name) {
        super(name);
    }

    public boolean isScaffoldPlacement(Player player, Block block, Block blockAgainst) {
        if (block.getRelative(0, -1, 0).getType() != Material.AIR ||
                block.getRelative(0, -2, 0).getType() != Material.AIR)
            return false;
        Set<Block> downBlocks = getDownBlocks(player, 0.45);
        if (!downBlocks.contains(block) || !downBlocks.contains(blockAgainst))
            return false;
        return true;
    }

}
