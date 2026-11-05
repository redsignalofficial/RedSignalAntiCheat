package me.rin.multiversion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public abstract class V1_9 implements MultiVersion, Listener {
   private final JavaPlugin pluginInstance;

   public V1_9(JavaPlugin pluginInstance) {
      this.pluginInstance = pluginInstance;
      Bukkit.getPluginManager().registerEvents(this, pluginInstance);
   }

   @Override
   public String getEnchantmentKey(Enchantment enchantment) {
      return enchantment.getName();
   }

   @Override
   public int getEnchantmentLevel(ItemStack itemStack, Enchantment enchantment) {
      return itemStack.getEnchantmentLevel(enchantment);
   }

   @Override
   public Map<String, Double> getAttributes(ItemStack itemStack) {
      return Collections.emptyMap();
   }

   @Override
   public Map<String, Double> getAttributes(Player player) {
      Map<String, Double> result = new HashMap<>();

      for (Attribute attribute : Attribute.values()) {
         AttributeInstance attributeInstance = player.getAttribute(attribute);
         if (attributeInstance != null && attributeInstance.getValue() != attributeInstance.getBaseValue()) {
            result.put(attribute.name(), attributeInstance.getValue());
         }
      }

      return result;
   }

   @Override
   public int getPotionLevel(LivingEntity entity, PotionEffectType effectType) {
      for (PotionEffect potionEffect : entity.getActivePotionEffects()) {
         if (potionEffect.getType() == effectType) {
            return potionEffect.getAmplifier() + 1;
         }
      }

      return 0;
   }

   @Override
   public boolean isGliding(Player player) {
      if (Bukkit.isPrimaryThread()) {
         return player.isGliding();
      } else if (player.isGliding()) {
         return true;
      } else if (!Bukkit.getServer().getPluginManager().isPluginEnabled("LightAntiCheat")) {
         return false;
      } else {
         CompletableFuture<Boolean> result = new CompletableFuture<>();
         Bukkit.getScheduler().runTask(this.pluginInstance, () -> result.complete(player.isGliding()));

         try {
            return result.get(125L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException | TimeoutException | ExecutionException var4) {
            return player.isGliding();
         }
      }
   }

   @Override
   public boolean isGlidingToggled(Player player) {
      return MultiVersionUtil.isToggledGliding(player);
   }

   @Override
   public boolean isRiptiding(Player player) {
      return false;
   }

   @Override
   public boolean isSwimming(Player player) {
      return false;
   }

   @Override
   public boolean isClimbing(Player player) {
      if (Bukkit.isPrimaryThread()) {
         return player.getLocation().getBlock().getType() == Material.LADDER;
      } else {
         CompletableFuture<Boolean> result = new CompletableFuture<>();
         Bukkit.getScheduler().runTask(this.pluginInstance, () -> result.complete(player.getLocation().getBlock().getType() == Material.LADDER));

         try {
            return result.get(100L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException | TimeoutException | ExecutionException var4) {
            return false;
         }
      }
   }

   @Override
   public boolean isInWater(Player player) {
      if (Bukkit.isPrimaryThread()) {
         return MultiVersionUtil.isInWater(player);
      } else {
         CompletableFuture<Boolean> result = new CompletableFuture<>();
         Bukkit.getScheduler().runTask(this.pluginInstance, () -> result.complete(MultiVersionUtil.isInWater(player)));

         try {
            return result.get(125L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException | TimeoutException | ExecutionException var4) {
            return MultiVersionUtil.isInWater(player);
         }
      }
   }

   @Override
   public ItemStack getItemInMainHand(Player player) {
      return player.getInventory().getItemInMainHand();
   }

   @Override
   public ItemStack getItemInOffHand(Player player) {
      return player.getInventory().getItemInOffHand();
   }

   @Override
   public Block getTargetBlockExact(Player player, int distance) {
      return player.getTargetBlock(new HashSet<>(Arrays.asList(Material.AIR, Material.WATER, Material.LAVA)), distance);
   }

   @Override
   public void sendBlockData(Player player, Location location, Block block) {
      player.sendBlockChange(location, block.getType(), block.getData());
   }

   @Override
   public boolean sendHoverMessage(Player player, List<String> lines, boolean hexColor) {
      player.sendMessage(V1_8Color.colorize(lines.get(0), false));
      return false;
   }

   @Override
   public double getWidth(Entity entity) {
      return entity.getType() == EntityType.SHULKER ? 1.0 : MultiVersionUtil.getWidth(entity);
   }

   @Override
   public double getHeight(Entity entity) {
      return entity.getType() == EntityType.SHULKER ? 1.0 : MultiVersionUtil.getHeight(entity);
   }

   @Override
   public ProjectileSource getSpectralArrowShooter(Entity spectralArrow) {
      return !(spectralArrow instanceof SpectralArrow) ? null : ((SpectralArrow)spectralArrow).getShooter();
   }

   @Override
   public boolean isPassable(Block block) {
      return block.isLiquid() ? true : !block.getType().isSolid() && !block.getType().isOccluding();
   }

   @Override
   public boolean isWatterLoggedSlab(Block block) {
      return false;
   }

   @Override
   public int getSnowLayers(Block block) {
      return block.getType() == Material.SNOW ? 0 : -1;
   }

   public abstract void onBlockPlace(BlockPlaceEvent var1);

   public abstract void onBlockBreak(BlockBreakEvent var1);

   public abstract void onMovement(PlayerMoveEvent var1);

   public abstract void onEntityDamage(EntityDamageByEntityEvent var1);

   @EventHandler
   public void blockPlaceListener(BlockPlaceEvent event) {
      this.onBlockPlace(event);
   }

   @EventHandler
   public void blockBreakListener(BlockBreakEvent event) {
      this.onBlockBreak(event);
   }

   @EventHandler
   public void movementListener(PlayerMoveEvent event) {
      this.onMovement(event);
   }

   @EventHandler
   public void entityDamageListener(EntityDamageByEntityEvent event) {
      this.onEntityDamage(event);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void glidingToggleListener(EntityToggleGlideEvent event) {
      if (event.getEntityType() == EntityType.PLAYER) {
         Player player = (Player)event.getEntity();
         MultiVersionUtil.setToggledGliding(player, event.isGliding());
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void quitListener(PlayerQuitEvent event) {
      MultiVersionUtil.removeToggledGliding(event.getPlayer());
   }
}
