package com.rin.rsInjector;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class RSInjector {
   private static final String COMPLETE_VERSION;
   private static final int VERSION;
   private static final Class<?> SERVER_CLASS;
   private static final Class<?> SERVER_CONNECTION_CLASS;
   private static final Class<?> NETWORK_MANAGER_CLASS;
   private static final Class<?> ENTITY_PLAYER_CLASS;
   private static final Class<?> PLAYER_CONNECTION_CLASS;
   private static final Class<?> PACKET_LOGIN_OUT_SUCCESS_CLASS;
   private static final Field NMS_SERVER;
   private static final Field NMS_SERVER_CONNECTION;
   private static final Field NMS_NETWORK_MANAGERS_LIST;
   @Nullable
   private static final Field NMS_PENDING_NETWORK_MANAGERS;
   private static final Field NMS_CHANNEL_FROM_NM;
   private static final Field GAME_PROFILE_FROM_PACKET;
   private static final Field GET_PLAYER_CONNECTION;
   private static final Field GET_NETWORK_MANAGER;
   private static final Method GET_PLAYER_HANDLE;
   private static int ID;
   private final Plugin plugin;
   private final String identifier;
   private final List<?> networkManagers;
   @Nullable
   private final Iterable<?> pendingNetworkManagers;
   private final RSInjector.EventListener listener = new RSInjector.EventListener();
   private final AtomicBoolean closed = new AtomicBoolean(false);
   private final Map<UUID, Player> playerCache = Collections.synchronizedMap(new HashMap<>());
   private final Set<Channel> injectedChannels = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

   public RSInjector(@NotNull Plugin plugin) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("RSInjector must be constructed on the main thread.");
      } else if (!Objects.requireNonNull(plugin, "Plugin is null.").isEnabled()) {
         throw new IllegalArgumentException("Plugin " + plugin.getName() + " is not enabled");
      } else {
         this.plugin = plugin;
         this.identifier = Objects.requireNonNull(this.getIdentifier(), "getIdentifier() returned a null value.") + '-' + ID++;

         try {
            Object conn = NMS_SERVER_CONNECTION.get(NMS_SERVER.get(Bukkit.getServer()));
            if (conn == null) {
               throw new RuntimeException("[RSInjector] ServerConnection is null.");
            }

            this.networkManagers = (List<?>)NMS_NETWORK_MANAGERS_LIST.get(conn);
            if (NMS_PENDING_NETWORK_MANAGERS != null) {
               this.pendingNetworkManagers = (Iterable<?>)NMS_PENDING_NETWORK_MANAGERS.get(conn);
            } else {
               this.pendingNetworkManagers = null;
            }
         } catch (ReflectiveOperationException var6) {
            throw new RuntimeException("[RSInjector] An error occurred while injecting.", var6);
         }

         Bukkit.getPluginManager().registerEvents(this.listener, plugin);

         for (Player p : Bukkit.getOnlinePlayers()) {
            try {
               this.injectPlayer(p);
            } catch (Exception var5) {
            }
         }
      }
   }

   @Nullable
   protected abstract Object onPacketReceiveAsync(@Nullable Player var1, @NotNull Channel var2, @NotNull Object var3);

   @Nullable
   protected abstract Object onPacketSendAsync(@Nullable Player var1, @NotNull Channel var2, @NotNull Object var3);

   public final void sendPacket(@NotNull Player receiver, @NotNull Object packet) {
      Objects.requireNonNull(receiver, "Player is null.");
      Objects.requireNonNull(packet, "Packet is null.");
      this.sendPacket(this.getChannel(receiver), packet);
   }

   public final void sendPacket(@NotNull Channel channel, @NotNull Object packet) {
      Objects.requireNonNull(channel, "Channel is null.");
      Objects.requireNonNull(packet, "Packet is null.");
      channel.pipeline().writeAndFlush(packet);
   }

   public final void receivePacket(@NotNull Player sender, @NotNull Object packet) {
      Objects.requireNonNull(sender, "Player is null.");
      Objects.requireNonNull(packet, "Packet is null.");
      this.receivePacket(this.getChannel(sender), packet);
   }

   public final void receivePacket(@NotNull Channel channel, @NotNull Object packet) {
      Objects.requireNonNull(channel, "Channel is null.");
      Objects.requireNonNull(packet, "Packet is null.");
      ChannelHandlerContext encoder = channel.pipeline().context("encoder");
      Objects.requireNonNull(encoder, "Channel is not a player channel").fireChannelRead(packet);
   }

   @NotNull
   protected String getIdentifier() {
      return "rs-injector-" + this.plugin.getName();
   }

   public final void close() {
      if (!this.closed.getAndSet(true)) {
         this.listener.unregister();
         synchronized (this.networkManagers) {
            for (Object manager : this.networkManagers) {
               try {
                  Channel channel = this.getChannel(manager);
                  channel.eventLoop().submit(() -> channel.pipeline().remove(this.identifier));
               } catch (Exception var6) {
               }
            }
         }

         this.playerCache.clear();
         this.injectedChannels.clear();
      }
   }

   public final boolean isClosed() {
      return this.closed.get();
   }

   @NotNull
   public final Plugin getPlugin() {
      return this.plugin;
   }

   private void injectPlayer(Player player) {
      this.injectChannel(this.getChannel(player)).player = player;
   }

   private RSInjector.PacketHandler injectChannel(Channel channel) {
      RSInjector.PacketHandler handler = new RSInjector.PacketHandler();
      channel.eventLoop().submit(() -> {
         if (!this.isClosed()) {
            if (this.injectedChannels.add(channel)) {
               try {
                  channel.pipeline().addBefore("packet_handler", this.identifier, handler);
               } catch (IllegalArgumentException var4) {
               }
            }
         }
      });
      return handler;
   }

   private void injectNetworkManager(Object networkManager) {
      Channel channel = this.getChannel(networkManager);
      if (!this.injectedChannels.contains(channel)) {
         this.injectChannel(channel);
      }
   }

   private Object getNetworkManager(Player player) {
      try {
         return GET_NETWORK_MANAGER.get(GET_PLAYER_CONNECTION.get(GET_PLAYER_HANDLE.invoke(player)));
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException("[RSInjector] Couldn't get player's network manager.", var3);
      }
   }

   private Channel getChannel(Player player) {
      return this.getChannel(this.getNetworkManager(player));
   }

   private Channel getChannel(Object networkManager) {
      try {
         return (Channel)NMS_CHANNEL_FROM_NM.get(networkManager);
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException("[RSInjector] Couldn't get network manager's channel.", var3);
      }
   }

   private static Class<?> getNMSClass(String name, String mcPackage) {
      String path = COMPLETE_VERSION != null
         ? "net.minecraft." + (VERSION >= 17 ? mcPackage : "server." + COMPLETE_VERSION) + '.' + name
         : "net.minecraft." + (VERSION >= 17 ? mcPackage : "server") + '.' + name;

      try {
         return Class.forName(path);
      } catch (ClassNotFoundException var4) {
         throw new RuntimeException("[RSInjector] Cannot find NMS Class! (" + path + ')', var4);
      }
   }

   private static Class<?> getCBClass(String name) {
      String clazz = COMPLETE_VERSION != null ? "org.bukkit.craftbukkit." + COMPLETE_VERSION + "." + name : "org.bukkit.craftbukkit." + name;

      try {
         return Class.forName(clazz);
      } catch (ClassNotFoundException var3) {
         throw new RuntimeException("[RSInjector] Cannot find CB Class! (" + clazz + ')', var3);
      }
   }

   private static Field getField(Class<?> clazz, String name) {
      try {
         Field f = clazz.getDeclaredField(name);
         f.setAccessible(true);
         return f;
      } catch (ReflectiveOperationException var3) {
         throw new RuntimeException("[RSInjector] Cannot find field! (" + clazz.getName() + '.' + name + ')', var3);
      }
   }

   private static Field getField(Class<?> clazz, Class<?> type, @Range(from = 1L,to = 2147483647L) int index) {
      return getField(clazz, type, index, 0);
   }

   private static Field getField(
      Class<?> clazz, Class<?> type, @Range(from = 1L,to = 2147483647L) int index, @Range(from = 0L,to = 2147483647L) int superClassesToTry
   ) {
      Class<?> savedClazz = clazz;
      int savedIndex = index;

      for (int i = 0; i <= superClassesToTry; i++) {
         Field[] fields = clazz.getDeclaredFields();

         for (Field f : fields) {
            if (type.equals(f.getType())) {
               if (--index <= 0) {
                  f.setAccessible(true);
                  return f;
               }
            }
         }

         index = savedIndex;

         for (Field fx : fields) {
            if (type.isAssignableFrom(fx.getType())) {
               if (--index <= 0) {
                  fx.setAccessible(true);
                  return fx;
               }
            }
         }

         clazz = clazz.getSuperclass();
         if (clazz == null || clazz == Object.class) {
            break;
         }

         index = savedIndex;
      }

      String errorMsg = "[RSInjector] Cannot find field! (" + savedIndex + getOrdinal(savedIndex) + type.getName() + " in " + savedClazz.getName();
      if (superClassesToTry > 0) {
         errorMsg = errorMsg + " and in its " + superClassesToTry + (superClassesToTry == 1 ? " super class" : " super classes");
      }

      errorMsg = errorMsg + ')';
      throw new RuntimeException(errorMsg);
   }

   @Nullable
   private static Field getPendingNetworkManagersFieldOrNull(Class<?> serverConnectionClass) {
      try {
         Field pending = getField(serverConnectionClass, "pending");
         if (pending.getType() == Queue.class || pending.getType() == List.class) {
            return pending;
         }
      } catch (Exception var4) {
      }

      try {
         return getField(serverConnectionClass, Queue.class, 1);
      } catch (Exception var3) {
         try {
            return getField(serverConnectionClass, List.class, 3);
         } catch (Exception var2) {
            return null;
         }
      }
   }

   private static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
      try {
         Method m = clazz.getDeclaredMethod(name, parameters);
         m.setAccessible(true);
         return m;
      } catch (ReflectiveOperationException var9) {
         StringJoiner params = new StringJoiner(", ");

         for (Class<?> p : parameters) {
            params.add(p.getName());
         }

         throw new RuntimeException("[RSInjector] Cannot find method! (" + clazz.getName() + '.' + name + '(' + params.toString() + ')', var9);
      }
   }

   private static Method getMethod(Class<?> clazz, Class<?> returnType, @Range(from = 1L,to = 2147483647L) int index) {
      int savedIndex = index;
      Method[] methods = clazz.getDeclaredMethods();

      for (Method m : methods) {
         if (returnType.equals(m.getReturnType())) {
            if (--index <= 0) {
               m.setAccessible(true);
               return m;
            }
         }
      }

      index = savedIndex;

      for (Method mx : methods) {
         if (returnType.isAssignableFrom(mx.getReturnType())) {
            if (--index <= 0) {
               mx.setAccessible(true);
               return mx;
            }
         }
      }

      throw new RuntimeException(
         "[RSInjector] Cannot find method! (" + savedIndex + getOrdinal(savedIndex) + " returning " + returnType.getName() + " in " + clazz.getName() + ')'
      );
   }

   private static String getOrdinal(int i) {
      switch (i) {
         case 1:
            return "st ";
         case 2:
            return "nd ";
         case 3:
            return "rd ";
         default:
            return "th ";
      }
   }

   static {
      if (Bukkit.getServer().getClass().getName().split("\\.").length >= 5) {
         COMPLETE_VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
         VERSION = Integer.parseInt(COMPLETE_VERSION.split("_")[1]);
      } else {
         COMPLETE_VERSION = null;
         VERSION = 17;
      }

      SERVER_CLASS = getNMSClass("MinecraftServer", "server");
      SERVER_CONNECTION_CLASS = getNMSClass("ServerConnection", "server.network");
      NETWORK_MANAGER_CLASS = getNMSClass("NetworkManager", "network");
      ENTITY_PLAYER_CLASS = getNMSClass("EntityPlayer", "server.level");
      PLAYER_CONNECTION_CLASS = getNMSClass("PlayerConnection", "server.network");
      PACKET_LOGIN_OUT_SUCCESS_CLASS = getNMSClass("PacketLoginOutSuccess", "network.protocol.login");
      NMS_SERVER = getField(getCBClass("CraftServer"), SERVER_CLASS, 1);
      NMS_SERVER_CONNECTION = getField(SERVER_CLASS, SERVER_CONNECTION_CLASS, 1);
      NMS_NETWORK_MANAGERS_LIST = getField(SERVER_CONNECTION_CLASS, List.class, 2);
      NMS_PENDING_NETWORK_MANAGERS = getPendingNetworkManagersFieldOrNull(SERVER_CONNECTION_CLASS);
      NMS_CHANNEL_FROM_NM = getField(NETWORK_MANAGER_CLASS, Channel.class, 1);
      GAME_PROFILE_FROM_PACKET = getField(PACKET_LOGIN_OUT_SUCCESS_CLASS, GameProfile.class, 1);
      GET_PLAYER_CONNECTION = getField(ENTITY_PLAYER_CLASS, PLAYER_CONNECTION_CLASS, 1);
      GET_NETWORK_MANAGER = getField(PLAYER_CONNECTION_CLASS, NETWORK_MANAGER_CLASS, 1, 1);
      GET_PLAYER_HANDLE = getMethod(getCBClass("entity.CraftPlayer"), "getHandle");
      ID = 0;
   }

   private final class EventListener implements Listener {
      private EventListener() {
      }

      @EventHandler(
         priority = EventPriority.LOWEST
      )
      private void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
         if (!RSInjector.this.isClosed()) {
            synchronized (RSInjector.this.networkManagers) {
               if (RSInjector.this.networkManagers instanceof RandomAccess) {
                  for (int i = RSInjector.this.networkManagers.size() - 1; i >= 0; i--) {
                     Object networkManager = RSInjector.this.networkManagers.get(i);
                     RSInjector.this.injectNetworkManager(networkManager);
                  }
               } else {
                  for (Object networkManager : RSInjector.this.networkManagers) {
                     RSInjector.this.injectNetworkManager(networkManager);
                  }
               }

               if (RSInjector.this.pendingNetworkManagers != null) {
                  synchronized (RSInjector.this.pendingNetworkManagers) {
                     for (Object networkManager : RSInjector.this.pendingNetworkManagers) {
                        RSInjector.this.injectNetworkManager(networkManager);
                     }
                  }
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOWEST
      )
      private void onPlayerLoginEvent(PlayerLoginEvent event) {
         if (!RSInjector.this.isClosed()) {
            RSInjector.this.playerCache.put(event.getPlayer().getUniqueId(), event.getPlayer());
         }
      }

      @EventHandler(
         priority = EventPriority.LOWEST
      )
      private void onPlayerJoinEvent(PlayerJoinEvent event) {
         if (!RSInjector.this.isClosed()) {
            Player player = event.getPlayer();
            Object networkManager = RSInjector.this.getNetworkManager(player);
            Channel channel = RSInjector.this.getChannel(networkManager);
            ChannelHandler channelHandler = channel.pipeline().get(RSInjector.this.identifier);
            if (channelHandler != null) {
               if (channelHandler instanceof RSInjector.PacketHandler) {
                  ((RSInjector.PacketHandler)channelHandler).player = player;
                  RSInjector.this.playerCache.remove(player.getUniqueId());
               }
            } else {
               RSInjector.this.injectChannel(channel).player = player;
            }
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      private void onPluginDisableEvent(PluginDisableEvent event) {
         if (RSInjector.this.plugin.equals(event.getPlugin())) {
            RSInjector.this.close();
         }
      }

      private void unregister() {
         AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
         PlayerLoginEvent.getHandlerList().unregister(this);
         PlayerJoinEvent.getHandlerList().unregister(this);
         PluginDisableEvent.getHandlerList().unregister(this);
      }
   }

   private final class PacketHandler extends ChannelDuplexHandler {
      private volatile Player player;

      private PacketHandler() {
      }

      public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
         RSInjector.this.injectedChannels.remove(ctx.channel());
         super.channelUnregistered(ctx);
      }

      public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
         if (this.player == null && RSInjector.PACKET_LOGIN_OUT_SUCCESS_CLASS.isInstance(packet)) {
            try {
               Player player = RSInjector.this.playerCache.remove(((GameProfile)RSInjector.GAME_PROFILE_FROM_PACKET.get(packet)).getId());
               if (player != null) {
                  this.player = player;
               }
            } catch (ReflectiveOperationException var8) {
            }
         }

         Object newPacket;
         try {
            newPacket = RSInjector.this.onPacketSendAsync(this.player, ctx.channel(), packet);
         } catch (OutOfMemoryError var6) {
            throw var6;
         } catch (Throwable var7) {
            super.write(ctx, packet, promise);
            return;
         }

         if (newPacket != null) {
            super.write(ctx, newPacket, promise);
         }
      }

      public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
         Object newPacket;
         try {
            newPacket = RSInjector.this.onPacketReceiveAsync(this.player, ctx.channel(), packet);
         } catch (OutOfMemoryError var5) {
            throw var5;
         } catch (Throwable var6) {
            super.channelRead(ctx, packet);
            return;
         }

         if (newPacket != null) {
            super.channelRead(ctx, newPacket);
         }
      }
   }
}
