package me.rin.multiversion;

import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Snow;
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

public abstract class V1_18 implements MultiVersion, Listener {
   private final JavaPlugin pluginInstance;

   public V1_18(JavaPlugin pluginInstance) {
      this.pluginInstance = pluginInstance;
      Bukkit.getPluginManager().registerEvents(this, pluginInstance);
   }

   @Override
   public String getEnchantmentKey(Enchantment enchantment) {
      return enchantment.getKey().getKey();
   }

   @Override
   public int getEnchantmentLevel(ItemStack itemStack, Enchantment enchantment) {
      return itemStack.getEnchantmentLevel(enchantment);
   }

   @Override
   public Map<String, Double> getAttributes(ItemStack itemStack) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         Multimap<Attribute, AttributeModifier> attributeModifiers = itemStack.getItemMeta().getAttributeModifiers();
         if (attributeModifiers != null && !attributeModifiers.isEmpty()) {
            Map<String, Double> result = new HashMap<>();
            attributeModifiers.forEach((attribute, attributeModifier) -> {
               Double var10000 = result.put(attribute.name(), attributeModifier.getAmount());
            });
            return result;
         } else {
            return Collections.emptyMap();
         }
      } else {
         return Collections.emptyMap();
      }
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
      PotionEffect potionEffect = entity.getPotionEffect(effectType);
      return potionEffect == null ? 0 : potionEffect.getAmplifier() + 1;
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
      if (Bukkit.isPrimaryThread()) {
         return player.isRiptiding();
      } else if (player.isRiptiding()) {
         return true;
      } else if (!Bukkit.getServer().getPluginManager().isPluginEnabled("LightAntiCheat")) {
         return false;
      } else {
         CompletableFuture<Boolean> result = new CompletableFuture<>();
         Bukkit.getScheduler().runTask(this.pluginInstance, () -> result.complete(player.isRiptiding()));

         try {
            return result.get(125L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException | TimeoutException | ExecutionException var4) {
            return player.isRiptiding();
         }
      }
   }

   @Override
   public boolean isSwimming(Player player) {
      return player.isSwimming();
   }

   @Override
   public boolean isClimbing(Player player) {
      return player.isClimbing();
   }

   @Override
   public boolean isInWater(Player player) {
      return player.isInWater();
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
      return player.getTargetBlockExact(distance);
   }

   @Override
   public void sendBlockData(Player player, Location location, Block block) {
      player.sendBlockChange(location, block.getBlockData());
   }

   @Override
   public boolean sendHoverMessage(Player player, List<String> lines, boolean hexColor) {
      player.spigot().sendMessage(V1_16Text.generateTextComponent(lines, hexColor));
      return false;
   }

   @Override
   public double getWidth(Entity entity) {
      return entity.getWidth();
   }

   @Override
   public double getHeight(Entity entity) {
      return entity.getHeight();
   }

   @Override
   public ProjectileSource getSpectralArrowShooter(Entity spectralArrow) {
      return !(spectralArrow instanceof SpectralArrow) ? null : ((SpectralArrow)spectralArrow).getShooter();
   }

   @Override
   public boolean isPassable(Block block) {
      return block.isPassable();
   }

   @Override
   public boolean isWatterLoggedSlab(Block block) {
      return block.getBlockData() instanceof Slab ? ((Slab)block.getBlockData()).isWaterlogged() : false;
   }

   @Override
   public int getSnowLayers(Block block) {
      return !(block.getBlockData() instanceof Snow) ? -1 : ((Snow)block.getBlockData()).getLayers();
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
