package me.rin.multiversion;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public class V1_8Color {
   private static boolean legacyColor = false;
   private static final Map<Color, ChatColor> BUKKIT_COLORS = new HashMap<>();

   public static String colorize(String text, boolean customColor) {
      if (!text.contains("&")) {
         return text;
      } else {
         Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");

         for (Matcher matcher = pattern.matcher(text); matcher.find(); matcher = pattern.matcher(text)) {
            String color = text.substring(matcher.start(), matcher.end());
            text = customColor
               ? text.replace(color, String.valueOf(chatColorOf(color.substring(1))))
               : text.replace(color, String.valueOf(getBukkitColor(color.substring(1))));
         }

         return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', text);
      }
   }

   protected static net.md_5.bungee.api.ChatColor chatColorOf(String hex) {
      if (legacyColor) {
         return getBukkitColor(hex);
      } else {
         for (Method method : net.md_5.bungee.api.ChatColor.class.getDeclaredMethods()) {
            if (method.getName().equals("of")) {
               Class<?>[] parameterTypes = method.getParameterTypes();
               if (parameterTypes.length == 1 && parameterTypes[0].getTypeName().equals("java.lang.String")) {
                  try {
                     Object result = method.invoke(net.md_5.bungee.api.ChatColor.class, hex);
                     if (result instanceof net.md_5.bungee.api.ChatColor) {
                        return (net.md_5.bungee.api.ChatColor)result;
                     }

                     return getBukkitColor(hex);
                  } catch (InvocationTargetException | IllegalAccessException var7) {
                     return getBukkitColor(hex);
                  }
               }
            }
         }

         legacyColor = true;
         return getBukkitColor(hex);
      }
   }

   protected static net.md_5.bungee.api.ChatColor getBukkitColor(String hex) {
      Color hexColor;
      try {
         hexColor = Color.decode(hex);
      } catch (IllegalArgumentException var10) {
         return net.md_5.bungee.api.ChatColor.BLACK;
      }

      ChatColor closestColor = ChatColor.BLACK;
      double closesDistance = 1000.0;

      for (Color color : BUKKIT_COLORS.keySet()) {
         ChatColor chatColor = BUKKIT_COLORS.get(color);
         double distance = colorDistance(hexColor, color);
         if (!(distance >= closesDistance)) {
            closestColor = chatColor;
            closesDistance = distance;
         }
      }

      return closestColor.asBungee();
   }

   private static double colorDistance(Color c1, Color c2) {
      int red1 = c1.getRed();
      int red2 = c2.getRed();
      int rmean = red1 + red2 >> 1;
      int r = red1 - red2;
      int g = c1.getGreen() - c2.getGreen();
      int b = c1.getBlue() - c2.getBlue();
      return Math.sqrt((double)(((512 + rmean) * r * r >> 8) + 4 * g * g + ((767 - rmean) * b * b >> 8)));
   }

   static {
      BUKKIT_COLORS.put(Color.decode("#000000"), ChatColor.BLACK);
      BUKKIT_COLORS.put(Color.decode("#0000AA"), ChatColor.DARK_BLUE);
      BUKKIT_COLORS.put(Color.decode("#00AA00"), ChatColor.DARK_GRAY);
      BUKKIT_COLORS.put(Color.decode("#00AAAA"), ChatColor.DARK_AQUA);
      BUKKIT_COLORS.put(Color.decode("#AA0000"), ChatColor.DARK_RED);
      BUKKIT_COLORS.put(Color.decode("#AA00AA"), ChatColor.DARK_PURPLE);
      BUKKIT_COLORS.put(Color.decode("#FFAA00"), ChatColor.GOLD);
      BUKKIT_COLORS.put(Color.decode("#AAAAAA"), ChatColor.GRAY);
      BUKKIT_COLORS.put(Color.decode("#555555"), ChatColor.DARK_GRAY);
      BUKKIT_COLORS.put(Color.decode("#5555FF"), ChatColor.BLUE);
      BUKKIT_COLORS.put(Color.decode("#55FF55"), ChatColor.GREEN);
      BUKKIT_COLORS.put(Color.decode("#55FFFF"), ChatColor.AQUA);
      BUKKIT_COLORS.put(Color.decode("#FF5555"), ChatColor.RED);
      BUKKIT_COLORS.put(Color.decode("#FF55FF"), ChatColor.LIGHT_PURPLE);
      BUKKIT_COLORS.put(Color.decode("#FFFF55"), ChatColor.YELLOW);
      BUKKIT_COLORS.put(Color.decode("#FFFFFF"), ChatColor.WHITE);
   }
}
