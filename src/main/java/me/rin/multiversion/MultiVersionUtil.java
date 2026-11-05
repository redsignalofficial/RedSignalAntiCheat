package me.rin.multiversion;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class MultiVersionUtil {
   public static final String PLUGIN_NAME = "LightAntiCheat";
   private static final Map<UUID, Boolean> PLAYERS = new ConcurrentHashMap<>();

   public static boolean isInWater(Player player) {
      World world = player.getWorld();
      Location location = player.getLocation();
      Location startPoint = location.clone().subtract(0.3, 0.0, 0.3);
      Location endPoint = location.clone().add(0.3, 1.8, 0.3);
      Set<Block> blocks = new HashSet<>();

      for (int x = startPoint.getBlockX(); x <= (endPoint.getX() % 1.0 == 0.0 ? endPoint.getBlockX() - 1 : endPoint.getBlockX()); x++) {
         for (int z = startPoint.getBlockZ(); z <= (endPoint.getZ() % 1.0 == 0.0 ? endPoint.getBlockZ() - 1 : endPoint.getBlockZ()); z++) {
            blocks.add(world.getBlockAt(x, startPoint.getBlockY(), z));
         }
      }

      Set<Block> higherBlocks = new HashSet<>();

      for (int y = startPoint.getBlockY() + 1; y <= (endPoint.getY() % 1.0 == 0.0 ? endPoint.getBlockY() - 1 : endPoint.getBlockY()); y++) {
         for (Block block : blocks) {
            higherBlocks.add(block.getRelative(BlockFace.UP, y - startPoint.getBlockY()));
         }
      }

      blocks.addAll(higherBlocks);
      Set<Material> materials = new HashSet<>();

      for (Block block : blocks) {
         materials.add(block.getType());
      }

      return materials.contains(Material.WATER);
   }

   public static double getWidth(Entity entity) {
      if (entity.getType() == EntityType.PLAYER) {
         return 0.6;
      } else if (entity.getType() == EntityType.BOAT) {
         return 1.375;
      } else if (entity.getType() == EntityType.ZOMBIE) {
         return 0.6;
      } else if (entity.getType() == EntityType.SKELETON) {
         return 0.6;
      } else if (entity.getType() == EntityType.SPIDER) {
         return 1.4;
      } else if (entity.getType() == EntityType.PIG) {
         return 0.9;
      } else if (entity.getType() == EntityType.COW) {
         return 0.9;
      } else if (entity.getType() == EntityType.SHEEP) {
         return 0.9;
      } else if (entity.getType() == EntityType.CHICKEN) {
         return 0.4;
      } else if (entity.getType() == EntityType.CREEPER) {
         return 0.6;
      } else if (entity.getType() == EntityType.CAVE_SPIDER) {
         return 0.7;
      } else if (entity.getType() == EntityType.SILVERFISH) {
         return 0.4;
      } else if (entity.getType() == EntityType.BLAZE) {
         return 0.6;
      } else if (entity.getType() == EntityType.WITCH) {
         return 0.6;
      } else if (entity.getType() == EntityType.VILLAGER) {
         return 0.6;
      } else {
         return ("PIG_ZOMBIE".equals(entity.getType().name()) || "ZOMBIFIED_PIGLIN".equals(entity.getType().name())) ? 0.6 : 10.0;
      }
   }

   public static double getHeight(Entity entity) {
      if (entity.getType() == EntityType.PLAYER) {
         return 1.8;
      } else if (entity.getType() == EntityType.BOAT) {
         return 0.5625;
      } else if (entity.getType() == EntityType.ZOMBIE) {
         return 1.95;
      } else if (entity.getType() == EntityType.SKELETON) {
         return 2.0;
      } else if (entity.getType() == EntityType.SPIDER) {
         return 0.9;
      } else if (entity.getType() == EntityType.PIG) {
         return 0.9;
      } else if (entity.getType() == EntityType.COW) {
         return 1.4;
      } else if (entity.getType() == EntityType.SHEEP) {
         return 1.3;
      } else if (entity.getType() == EntityType.CHICKEN) {
         return 0.7;
      } else if (entity.getType() == EntityType.CREEPER) {
         return 1.7;
      } else if (entity.getType() == EntityType.CAVE_SPIDER) {
         return 0.5;
      } else if (entity.getType() == EntityType.SILVERFISH) {
         return 0.3;
      } else if (entity.getType() == EntityType.BLAZE) {
         return 1.8;
      } else if (entity.getType() == EntityType.WITCH) {
         return 1.95;
      } else if (entity.getType() == EntityType.VILLAGER) {
         return 1.95;
      } else {
         return ("PIG_ZOMBIE".equals(entity.getType().name()) || "ZOMBIFIED_PIGLIN".equals(entity.getType().name())) ? 1.95 : 10.0;
      }
   }

   public static void setToggledGliding(Player player, Boolean value) {
      PLAYERS.put(player.getUniqueId(), value);
   }

   public static boolean isToggledGliding(Player player) {
      return PLAYERS.getOrDefault(player.getUniqueId(), false);
   }

   public static void removeToggledGliding(Player player) {
      PLAYERS.remove(player.getUniqueId());
   }
}
