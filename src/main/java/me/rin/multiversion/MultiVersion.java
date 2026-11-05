package me.rin.multiversion;

import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public interface MultiVersion {
   String getEnchantmentKey(Enchantment var1);

   int getEnchantmentLevel(ItemStack var1, Enchantment var2);

   Map<String, Double> getAttributes(ItemStack var1);

   Map<String, Double> getAttributes(Player var1);

   int getPotionLevel(LivingEntity var1, PotionEffectType var2);

   boolean isGliding(Player var1);

   boolean isGlidingToggled(Player var1);

   boolean isRiptiding(Player var1);

   boolean isSwimming(Player var1);

   boolean isClimbing(Player var1);

   boolean isInWater(Player var1);

   ItemStack getItemInMainHand(Player var1);

   ItemStack getItemInOffHand(Player var1);

   Block getTargetBlockExact(Player var1, int var2);

   void sendBlockData(Player var1, Location var2, Block var3);

   boolean sendHoverMessage(Player var1, List<String> var2, boolean var3);

   double getWidth(Entity var1);

   double getHeight(Entity var1);

   ProjectileSource getSpectralArrowShooter(Entity var1);

   boolean isPassable(Block var1);

   boolean isWatterLoggedSlab(Block var1);

   int getSnowLayers(Block var1);
}
