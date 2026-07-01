package fun.slikdlc.api.utils.bot;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.player.AutoForest;
import fun.slikdlc.mixin.IMinecraftClientAccessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_10264;
import net.minecraft.class_1268;
import net.minecraft.class_243;
import net.minecraft.class_2535;
import net.minecraft.class_2561;
import net.minecraft.class_2661;
import net.minecraft.class_2670;
import net.minecraft.class_2708;
import net.minecraft.class_2720;
import net.minecraft.class_2749;
import net.minecraft.class_2793;
import net.minecraft.class_2827;
import net.minecraft.class_2856;
import net.minecraft.class_2879;
import net.minecraft.class_2886;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_3532;
import net.minecraft.class_412;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_6373;
import net.minecraft.class_6374;
import net.minecraft.class_638;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2830;
import net.minecraft.class_2828.class_5911;
import net.minecraft.class_2856.class_2857;
import net.minecraft.class_320.class_321;
import net.minecraft.class_642.class_8678;

public final class BotSessionManager {
   private static final List<BotSessionManager.BotConnection> connections = new CopyOnWriteArrayList<>();
   private static volatile boolean ignoreBotMessages;
   private static volatile boolean bypassResourcePacksDuringBotConnect;

   public BotSessionManager() {
   }

   public static List<BotSessionManager.BotConnection> getConnections() {
      pruneDeadConnections();
      return new ArrayList<>(connections);
   }

   public static boolean shouldBypassResourcePacks() {
      return bypassResourcePacksDuringBotConnect;
   }

   public static void finishBotConnectStage() {
      bypassResourcePacksDuringBotConnect = false;
   }

   public static String getCurrentSessionName() {
      class_310 mc = class_310.method_1551();
      return mc.method_1548() == null ? "" : mc.method_1548().method_1676();
   }

   public static List<String> getSessionNames(boolean includeCurrent) {
      pruneDeadConnections();
      Set<String> names = new LinkedHashSet<>();
      if (includeCurrent) {
         String currentName = getCurrentSessionName();
         if (!currentName.isBlank()) {
            names.add(currentName);
         }
      }

      for (BotSessionManager.BotConnection bot : connections) {
         if (bot.name() != null && !bot.name().isBlank()) {
            names.add(bot.name());
         }
      }

      return new ArrayList<>(names);
   }

   public static boolean toggleIgnoreBotMessages() {
      ignoreBotMessages = !ignoreBotMessages;
      return ignoreBotMessages;
   }

   public static boolean isIgnoreBotMessages() {
      return ignoreBotMessages;
   }

   public static void connect(String name, String address) {
      class_310 mc = class_310.method_1551();
      if (mc.method_1548() != null && name != null && !name.isBlank() && address != null && !address.isBlank()) {
         class_320 originalSession = mc.method_1548();
         class_642 originalServerInfo = mc.method_1558();
         pruneDeadConnections();
         disconnectSessionsByName(name, class_2561.method_43470("Replaced"));
         BotSessionManager.BotConnection previous = freezeCurrentSession();
         ModuleClass.autoForest.resetToDefaults();
         ((IMinecraftClientAccessor)mc).setSession(createSessionWithName(mc.method_1548(), name));
         bypassResourcePacksDuringBotConnect = true;
         mc.execute(
            () -> {
               try {
                  class_412.method_36877(
                     new class_500(new class_442()), mc, class_639.method_2950(address), new class_642(address, address, class_8678.field_45611), false, null
                  );
               } catch (Exception var6) {
                  bypassResourcePacksDuringBotConnect = false;
                  restoreAfterConnectFailure(mc, previous, originalSession, originalServerInfo);
               }
            }
         );
      }
   }

   public static void pulseBots(boolean rightClick) {
      for (BotSessionManager.BotConnection bot : connections) {
         if (isConnectionUsable(bot)) {
            if (rightClick) {
               bot.handler().method_52787(new class_2886(class_1268.field_5808, 0, bot.player().method_36454(), bot.player().method_36455()));
            } else {
               bot.handler().method_52787(new class_2879(class_1268.field_5808));
            }
         }
      }
   }

   public static void sayAll(String message) {
      for (BotSessionManager.BotConnection bot : connections) {
         if (isConnectionUsable(bot)) {
            if (message.startsWith("/")) {
               bot.handler().method_45730(message.substring(1));
            } else {
               bot.handler().method_45729(message);
            }
         }
      }
   }

   public static boolean control(String name) {
      if (name != null && !name.isBlank()) {
         pruneDeadConnections();
         class_310 mc = class_310.method_1551();
         return mc.field_1724 != null && mc.field_1687 != null && name.equalsIgnoreCase(getCurrentSessionName())
            ? true
            : connections.stream().filter(bot -> matchesName(bot.name(), name)).findFirst().map(bot -> {
               if (!isConnectionUsable(bot)) {
                  connections.remove(bot);
                  return false;
               } else {
                  BotSessionManager.BotConnection previous = freezeCurrentSession();
                  if (!activateSession(bot)) {
                     if (previous != null && activateSession(previous)) {
                        connections.remove(previous);
                     }

                     return false;
                  } else {
                     connections.remove(bot);
                     return true;
                  }
               }
            }).orElse(false);
      } else {
         return false;
      }
   }

   public static boolean say(String name, String message) {
      pruneDeadConnections();
      return connections.stream().filter(bot -> matchesName(bot.name(), name)).findFirst().map(bot -> {
         if (!isConnectionUsable(bot)) {
            connections.remove(bot);
            return false;
         } else {
            if (message.startsWith("/")) {
               bot.handler().method_45730(message.substring(1));
            } else {
               bot.handler().method_45729(message);
            }

            return true;
         }
      }).orElse(false);
   }

   public static boolean remove(String name) {
      return name != null && !name.isBlank() ? disconnectSessionsByName(name, class_2561.method_43470("Removed")) > 0 : false;
   }

   public static boolean restore() {
      return restore(null);
   }

   public static boolean restore(String name) {
      pruneDeadConnections();
      String targetName = name != null && !name.isBlank() ? name : (connections.isEmpty() ? "" : connections.get(connections.size() - 1).name());
      return !targetName.isBlank() && control(targetName);
   }

   private static BotSessionManager.BotConnection freezeCurrentSession() {
      class_310 mc = class_310.method_1551();
      if (mc.method_1562() != null && mc.field_1687 != null && mc.field_1724 != null) {
         class_634 handler = mc.method_1562();
         makeNettyBot(handler, mc.method_1548().method_1676(), mc.field_1724);
         BotSessionManager.BotConnection connection = new BotSessionManager.BotConnection(
            mc.method_1548().method_1676(),
            mc.method_1558() != null ? mc.method_1558().field_3761 : "",
            handler.method_48296(),
            handler,
            mc.field_1687,
            mc.field_1724,
            mc.field_1761,
            mc.method_1548(),
            mc.method_1558(),
            ModuleClass.autoForest.captureState()
         );
         replaceConnection(connection);
         clearActiveSession(mc);
         return connection;
      } else {
         return null;
      }
   }

   private static boolean activateSession(BotSessionManager.BotConnection bot) {
      if (!isConnectionUsable(bot)) {
         return false;
      } else {
         class_310 mc = class_310.method_1551();
         IMinecraftClientAccessor accessor = (IMinecraftClientAccessor)mc;
         Channel channel = getChannel(bot.connection());
         if (channel != null && channel.pipeline().get("bot_filter") != null) {
            channel.pipeline().remove("bot_filter");
         }

         try {
            setMinecraftClientField(mc, class_634.class, bot.handler());
            accessor.setSession(bot.session() != null ? bot.session() : createSessionWithName(mc.method_1548(), bot.name()));
            setMinecraftClientField(mc, class_642.class, bot.serverInfo() != null ? bot.serverInfo() : createServerInfo(bot.name(), bot.address()));
            accessor.setItemUseCooldown(0);
            mc.field_1687 = bot.world();
            mc.field_1724 = bot.player();
            mc.field_1719 = bot.player();
            mc.field_1761 = bot.interactionManager();
            if (mc.field_1769 != null) {
               mc.field_1769.method_3244(bot.world());
            }

            ModuleClass.autoForest.applyState(bot.autoForestState());
            bot.handler()
               .method_52787(
                  new class_2830(
                     bot.player().method_23317(),
                     bot.player().method_23318(),
                     bot.player().method_23321(),
                     bot.player().method_36454(),
                     bot.player().method_36455(),
                     bot.player().method_24828(),
                     bot.player().field_5976
                  )
               );
            mc.method_1507(null);
            return true;
         } catch (Exception var5) {
            return false;
         }
      }
   }

   private static void clearActiveSession(class_310 mc) {
      IMinecraftClientAccessor accessor = (IMinecraftClientAccessor)mc;
      setMinecraftClientField(mc, class_634.class, null);
      accessor.setItemUseCooldown(0);
      mc.field_1687 = null;
      mc.field_1724 = null;
      mc.field_1719 = null;
      mc.field_1761 = null;
      if (mc.field_1769 != null) {
         mc.field_1769.method_3244(null);
      }
   }

   private static void replaceConnection(BotSessionManager.BotConnection connection) {
      disconnectSessionsByName(connection.name(), class_2561.method_43470("Replaced"));
      connections.add(connection);
   }

   private static void makeNettyBot(class_634 handler, String name, class_746 botPlayer) {
      Channel channel = getChannel(handler.method_48296());
      if (channel != null) {
         if (channel.pipeline().get("bot_filter") != null) {
            channel.pipeline().remove("bot_filter");
         }

         if (channel.pipeline().get("packet_handler") != null) {
            channel.pipeline()
               .addBefore(
                  "packet_handler",
                  "bot_filter",
                  new ChannelDuplexHandler() {
                     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof class_2670 packet) {
                           handler.method_48296().method_10743(new class_2827(packet.method_11517()));
                           if (botPlayer != null) {
                              handler.method_52787(new class_5911(botPlayer.method_24828(), botPlayer.field_5976));
                           }

                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof class_6373 packet) {
                           handler.method_48296().method_10743(new class_6374(packet.method_36950()));
                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof class_2720 packet) {
                           handler.method_52787(new class_2856(packet.comp_2158(), class_2857.field_13016));
                           handler.method_52787(new class_2856(packet.comp_2158(), class_2857.field_13017));
                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof class_2708 packet) {
                           BotSessionManager.applyFrozenPositionLook(botPlayer, packet);
                           handler.method_52787(new class_2793(packet.comp_3133()));
                           if (botPlayer != null) {
                              handler.method_52787(
                                 new class_2830(
                                    botPlayer.method_23317(),
                                    botPlayer.method_23318(),
                                    botPlayer.method_23321(),
                                    botPlayer.method_36454(),
                                    botPlayer.method_36455(),
                                    botPlayer.method_24828(),
                                    botPlayer.field_5976
                                 )
                              );
                           }

                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof class_10264 packet) {
                           BotSessionManager.applyFrozenEntityPositionSync(botPlayer, packet);
                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof class_2749 packet) {
                           if (botPlayer != null) {
                              botPlayer.method_6033(packet.method_11833());
                           }

                           ReferenceCountUtil.release(msg);
                        } else if (msg instanceof class_2661) {
                           BotSessionManager.connections.removeIf(bot -> BotSessionManager.matchesName(bot.name(), name));
                           ctx.close();
                           ReferenceCountUtil.release(msg);
                        } else {
                           String packetName = msg.getClass().getSimpleName();
                           if (!packetName.contains("Sound")
                              && !packetName.contains("Particle")
                              && !packetName.contains("Screen")
                              && (!BotSessionManager.ignoreBotMessages || !BotSessionManager.isBotMessagePacket(packetName))
                              && !packetName.contains("Explosion")
                              && !packetName.contains("BossBar")
                              && !packetName.contains("Scoreboard")
                              && !packetName.contains("OverlayMessage")) {
                              super.channelRead(ctx, msg);
                           } else {
                              ReferenceCountUtil.release(msg);
                           }
                        }
                     }

                     public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        BotSessionManager.connections.removeIf(bot -> BotSessionManager.matchesName(bot.name(), name));
                        super.channelInactive(ctx);
                     }

                     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        BotSessionManager.connections.removeIf(bot -> BotSessionManager.matchesName(bot.name(), name));
                        ctx.close();
                     }
                  }
               );
         }
      }
   }

   private static boolean isBotMessagePacket(String packetName) {
      return packetName.contains("Chat") || packetName.contains("Message") || packetName.contains("Title") || packetName.contains("Overlay");
   }

   private static Channel getChannel(class_2535 connection) {
      try {
         for (Field field : class_2535.class.getDeclaredFields()) {
            if (Channel.class.isAssignableFrom(field.getType())) {
               field.setAccessible(true);
               return (Channel)field.get(connection);
            }
         }
      } catch (Exception var5) {
      }

      return null;
   }

   private static void applyFrozenPositionLook(class_746 botPlayer, class_2708 packet) {
      if (botPlayer != null && packet != null) {
         double x = readPacketDouble(packet, "x", botPlayer.method_23317());
         double y = readPacketDouble(packet, "y", botPlayer.method_23318());
         double z = readPacketDouble(packet, "z", botPlayer.method_23321());
         float yaw = (float)readPacketDouble(packet, "yaw", botPlayer.method_36454());
         float pitch = (float)readPacketDouble(packet, "pitch", botPlayer.method_36455());
         Object change = readPacketComponent(packet, "change");
         if (change == null) {
            change = readPacketComponent(packet, "flags");
         }

         if (hasRelativeFlag(change, "X")) {
            x += botPlayer.method_23317();
         }

         if (hasRelativeFlag(change, "Y")) {
            y += botPlayer.method_23318();
         }

         if (hasRelativeFlag(change, "Z")) {
            z += botPlayer.method_23321();
         }

         if (hasRelativeFlag(change, "Y_ROT")) {
            yaw += botPlayer.method_36454();
         }

         if (hasRelativeFlag(change, "X_ROT")) {
            pitch += botPlayer.method_36455();
         }

         pitch = class_3532.method_15363(pitch, -90.0F, 90.0F);
         botPlayer.method_5808(x, y, z, yaw, pitch);
         botPlayer.method_36456(yaw);
         botPlayer.method_36457(pitch);
      }
   }

   private static void applyFrozenEntityPositionSync(class_746 botPlayer, class_10264 packet) {
      if (botPlayer != null && packet != null && packet.comp_3223() == botPlayer.method_5628() && packet.comp_3224() != null) {
         class_243 position = packet.comp_3224().comp_3148();
         if (position != null) {
            float yaw = packet.comp_3224().comp_3150();
            float pitch = class_3532.method_15363(packet.comp_3224().comp_3151(), -90.0F, 90.0F);
            botPlayer.method_5808(position.field_1352, position.field_1351, position.field_1350, yaw, pitch);
            botPlayer.method_36456(yaw);
            botPlayer.method_36457(pitch);
            if (packet.comp_3224().comp_3149() != null) {
               botPlayer.method_18799(packet.comp_3224().comp_3149());
            }

            botPlayer.method_24830(packet.comp_3225());
         }
      }
   }

   private static double readPacketDouble(Object packet, String name, double fallback) {
      return readPacketComponent(packet, name) instanceof Number number ? number.doubleValue() : fallback;
   }

   private static Object readPacketComponent(Object packet, String name) {
      if (packet != null && name != null && !name.isBlank()) {
         try {
            Method method = packet.getClass().getMethod(name);
            method.setAccessible(true);
            return method.invoke(packet);
         } catch (Exception var10) {
            try {
               Method methodx = packet.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
               methodx.setAccessible(true);
               return methodx.invoke(packet);
            } catch (Exception var9) {
               try {
                  RecordComponent[] components = packet.getClass().getRecordComponents();
                  if (components != null) {
                     for (RecordComponent component : components) {
                        if (name.equals(component.getName())) {
                           return component.getAccessor().invoke(packet);
                        }
                     }
                  }
               } catch (Exception var8) {
               }

               try {
                  for (Field field : packet.getClass().getDeclaredFields()) {
                     if (name.equalsIgnoreCase(field.getName())) {
                        field.setAccessible(true);
                        return field.get(packet);
                     }
                  }
               } catch (Exception var7) {
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static boolean hasRelativeFlag(Object flags, String flagName) {
      if (flags instanceof Iterable<?> iterable && flagName != null) {
         for (Object flag : iterable) {
            if (flag instanceof Enum<?> enumFlag && flagName.equals(enumFlag.name())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static class_320 createSessionWithName(class_320 current, String name) {
      try {
         Constructor<class_320> constructor = class_320.class
            .getDeclaredConstructor(String.class, UUID.class, String.class, Optional.class, Optional.class, class_321.class);
         constructor.setAccessible(true);
         return constructor.newInstance(
            name, UUID.randomUUID(), current == null ? "" : current.method_1674(), Optional.empty(), Optional.empty(), class_321.field_1988
         );
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   private static void setMinecraftClientField(class_310 mc, Class<?> fieldType, Object value) {
      try {
         for (Field field : class_310.class.getDeclaredFields()) {
            if (field.getType() == fieldType) {
               field.setAccessible(true);
               field.set(mc, value);
               return;
            }
         }
      } catch (Exception var7) {
      }
   }

   private static class_642 createServerInfo(String name, String address) {
      String safeAddress = address == null ? "" : address;
      String safeName = name != null && !name.isBlank() ? name : safeAddress;
      return new class_642(safeName, safeAddress, class_8678.field_45611);
   }

   private static void restoreAfterConnectFailure(
      class_310 mc, BotSessionManager.BotConnection previous, class_320 originalSession, class_642 originalServerInfo
   ) {
      try {
         bypassResourcePacksDuringBotConnect = false;
         if (previous != null && activateSession(previous)) {
            connections.remove(previous);
            return;
         }

         IMinecraftClientAccessor accessor = (IMinecraftClientAccessor)mc;
         accessor.setSession(originalSession);
         setMinecraftClientField(mc, class_642.class, originalServerInfo);
      } catch (Exception var5) {
      }
   }

   private static int disconnectSessionsByName(String name, class_2561 reason) {
      if (name != null && !name.isBlank()) {
         int removed = 0;

         for (BotSessionManager.BotConnection bot : new ArrayList<>(connections)) {
            if (matchesName(bot.name(), name)) {
               connections.remove(bot);
               removed++;

               try {
                  if (bot.connection() != null) {
                     bot.connection().method_10747(reason);
                  }
               } catch (Exception var6) {
               }
            }
         }

         return removed;
      } else {
         return 0;
      }
   }

   private static void pruneDeadConnections() {
      connections.removeIf(bot -> !isConnectionUsable(bot));
   }

   private static boolean isConnectionUsable(BotSessionManager.BotConnection bot) {
      if (bot == null || bot.name() == null || bot.name().isBlank()) {
         return false;
      } else if (bot.connection() != null && bot.handler() != null && bot.world() != null && bot.player() != null) {
         if (bot.player().field_3944 != bot.handler()) {
            return false;
         } else {
            Channel channel = getChannel(bot.connection());
            return channel == null || channel.isOpen();
         }
      } else {
         return false;
      }
   }

   private static boolean matchesName(String left, String right) {
      return left != null && right != null && left.equalsIgnoreCase(right);
   }

   public static final class BotConnection {
      private final String name;
      private final String address;
      private final class_2535 connection;
      private final class_634 handler;
      private final class_638 world;
      private final class_746 player;
      private final class_636 interactionManager;
      private final class_320 session;
      private final class_642 serverInfo;
      private final AutoForest.SessionState autoForestState;

      public BotConnection(
         String name,
         String address,
         class_2535 connection,
         class_634 handler,
         class_638 world,
         class_746 player,
         class_636 interactionManager,
         class_320 session,
         class_642 serverInfo,
         AutoForest.SessionState autoForestState
      ) {
         this.name = name;
         this.address = address;
         this.connection = connection;
         this.handler = handler;
         this.world = world;
         this.player = player;
         this.interactionManager = interactionManager;
         this.session = session;
         this.serverInfo = serverInfo;
         this.autoForestState = autoForestState;
      }

      public String name() {
         return this.name;
      }

      public String address() {
         return this.address;
      }

      public class_2535 connection() {
         return this.connection;
      }

      public class_634 handler() {
         return this.handler;
      }

      public class_638 world() {
         return this.world;
      }

      public class_746 player() {
         return this.player;
      }

      public class_636 interactionManager() {
         return this.interactionManager;
      }

      public class_320 session() {
         return this.session;
      }

      public class_642 serverInfo() {
         return this.serverInfo;
      }

      public AutoForest.SessionState autoForestState() {
         return this.autoForestState;
      }
   }
}
