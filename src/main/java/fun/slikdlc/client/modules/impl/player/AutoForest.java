package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.bot.BotSessionManager;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2626;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_2879;
import net.minecraft.class_3481;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2846.class_2847;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class AutoForest extends Module {
   public static AutoForest INSTANCE = new AutoForest();
   private static final double MAX_RANGE = 4.0;
   private static final double MAX_RANGE_SQ = 16.0;
   private static final long DEFAULT_BREAK_DELAY_MS = 3L;
   private static final float AUTO_FAST_BREAK_SPEED = 1.0F;
   private static final float DEFAULT_PACKETS_PER_SECOND = 100.0F;
   private static final long VISUAL_TTL_MS = 300000L;
   private static final long NICK_REMINDER_DELAY_MS = 5000L;
   private static final String MODE_NORMAL_ALIAS = "normal";
   private static final String MODE_FAST_ALIAS = "fast";
   private final ModeSetting breakMode = new ModeSetting("Режим ломания", "Обычный", "Обычный", "Быстрый");
   private final FloatSetting packetsPerSecond = new FloatSetting("Пакетов в секунду", 100.0F, 1.0F, 100.0F, 1.0F).visible(() -> this.breakMode.is("Быстрый"));
   private final FloatSetting breakRadius = new FloatSetting("Радиус", 4.0F, 1.0F, 6.0F, 0.5F);
   private final BooleanSetting swing = new BooleanSetting("Махать рукой", true);
   private final BooleanSetting autoSell = new BooleanSetting("Авто продажа дерева", true);
   private final BooleanSetting autoPay = new BooleanSetting("AutoPay", false);
   private final BooleanSetting preserveVisuals = new BooleanSetting("Сохранять визуализацию", true);
   private final FloatSetting payAmount = new FloatSetting("Сумма перевода", 1000.0F, 500.0F, 25000.0F, 500.0F).visible(this.autoPay::isState);
   private final FloatSetting intervalSeconds = new FloatSetting("Задержка", 20.0F, 1.0F, 60.0F, 1.0F);
   private final Map<class_2338, class_2680> preservedBlocks = new HashMap<>();
   private final Map<class_2338, Long> lastUpdateTime = new HashMap<>();
   private final Set<class_2338> managedBlocks = new HashSet<>();
   private boolean currentSessionEnabled;
   private class_2338 targetPos;
   private String payTarget = "";
   private long lastBreakTime;
   private long lastPacketTime;
   private long lastSellTime;
   private long lastPayTime;
   private long lastNickReminderTime;

   public AutoForest() {
      super("AutoForest", "Автоматически ломает бревна и переводит деньги", Module.ModuleCategory.PLAYER);
      this.addSettings(
         new Setting[]{
            this.breakMode,
            this.packetsPerSecond,
            this.breakRadius,
            this.swing,
            this.autoSell,
            this.autoPay,
            this.preserveVisuals,
            this.payAmount,
            this.intervalSeconds
         }
      );
      EventInvoker.register(this);
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (this.currentSessionEnabled && mc.field_1724 != null && mc.field_1687 != null) {
         this.tickCurrentSession();
      }

      this.tickFrozenBots();
   }

   private void tickCurrentSession() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.method_1562() != null) {
         long now = System.currentTimeMillis();
         long scheduleDelay = Math.max(1000L, (long)(this.intervalSeconds.get() * 500.0F));
         if (this.autoSell.isState() && now - this.lastSellTime >= scheduleDelay) {
            mc.method_1562().method_45730("sellwood");
            this.lastSellTime = now;
         }

         if (this.autoPay.isState()) {
            if (this.payTarget.isBlank()) {
               if (now - this.lastNickReminderTime >= 5000L) {
                  this.lastNickReminderTime = now;
                  ChatUtils.sendMessage("Укажите ник для перевода через .autoles pay <nick>");
               }
            } else if (now - this.lastPayTime >= scheduleDelay + 200L) {
               mc.method_1562().method_45730("pay " + this.payTarget + " " + (int)this.payAmount.get());
               this.lastPayTime = now;
            }
         }

         if (this.targetPos != null && (!this.isLog(this.targetPos) || !this.isInRange(this.targetPos) || !this.isVisible(this.targetPos))) {
            this.targetPos = null;
         }

         if (this.targetPos == null) {
            this.targetPos = this.findNearestLog();
         }

         if (this.targetPos != null) {
            this.breakTarget(now);
         }

         if (this.preserveVisuals.isState()) {
            this.updateVisualization(now);
         }
      } else {
         this.targetPos = null;
      }
   }

   private void tickFrozenBots() {
      for (BotSessionManager.BotConnection bot : BotSessionManager.getConnections()) {
         AutoForest.SessionState state = bot.autoForestState();
         if (state != null && state.enabled() && bot.player() != null && bot.world() != null && bot.handler() != null) {
            try {
               this.tickBotSession(bot, state);
            } catch (Exception var5) {
               state.enabled(false);
               state.targetPos(null);
            }
         }
      }
   }

   private void tickBotSession(BotSessionManager.BotConnection bot, AutoForest.SessionState state) {
      if (!bot.player().method_31481() && bot.player().method_5805()) {
         long now = System.currentTimeMillis();
         long scheduleDelay = Math.max(1000L, (long)(Math.max(1.0F, state.intervalSeconds()) * 500.0F));
         if (state.autoSell() && now - state.lastSellTime() >= scheduleDelay) {
            bot.handler().method_45730("sellwood");
            state.lastSellTime(now);
         }

         if (state.autoPay()) {
            if (state.payTarget().isBlank()) {
               if (now - state.lastNickReminderTime() >= 5000L) {
                  state.lastNickReminderTime(now);
               }
            } else if (now - state.lastPayTime() >= scheduleDelay + 200L) {
               bot.handler().method_45730("pay " + state.payTarget() + " " + (int)state.payAmount());
               state.lastPayTime(now);
            }
         }

         if (state.targetPos() != null
            && (
               !this.isLog(bot.world(), state.targetPos())
                  || !this.isInRange(bot.player(), state.targetPos())
                  || !this.isVisible(bot.world(), bot.player(), state.targetPos())
            )) {
            state.targetPos(null);
         }

         if (state.targetPos() == null) {
            state.targetPos(this.findNearestLog(bot.world(), bot.player(), state.breakRadius()));
         }

         if (state.targetPos() != null) {
            if ("fast".equals(state.modeAlias())) {
               long interval = Math.max(1L, (long)(1000.0F / Math.max(1.0F, state.packetsPerSecond())));
               if (now - state.lastPacketTime() >= interval) {
                  this.performFastBreak(bot.handler(), bot.interactionManager(), bot.player(), bot.world(), state.targetPos(), state.swing());
                  state.lastPacketTime(now);
               }
            } else if (now - state.lastBreakTime() >= 3L) {
               if (bot.interactionManager() != null) {
                  bot.interactionManager().method_2910(state.targetPos(), class_2350.field_11036);
                  bot.interactionManager().method_2902(state.targetPos(), class_2350.field_11036);
               } else {
                  this.performFastBreak(bot.handler(), bot.interactionManager(), bot.player(), bot.world(), state.targetPos(), state.swing());
               }

               if (state.swing()) {
                  bot.handler().method_52787(new class_2879(class_1268.field_5808));
               }

               state.lastBreakTime(now);
            }
         }
      } else {
         state.enabled(false);
         state.targetPos(null);
      }
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (this.currentSessionEnabled && this.preserveVisuals.isState() && mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getType() == EventPacket.Type.SEND && event.getPacket() instanceof class_2846 packet) {
            this.handleDigPacket(packet);
         } else {
            if (event.getType() == EventPacket.Type.RECEIVE && event.getPacket() instanceof class_2626 packet) {
               class_2338 pos = packet.method_11309();
               class_2680 savedState = this.preservedBlocks.get(pos);
               if (savedState == null) {
                  return;
               }

               class_2680 serverState = packet.method_11308();
               if (serverState.method_26215() || !serverState.equals(savedState)) {
                  event.cancel();
                  this.setClientBlock(pos, savedState);
                  this.lastUpdateTime.put(pos, System.currentTimeMillis());
               }
            }
         }
      }
   }

   private void breakTarget(long now) {
      if (this.targetPos != null && mc.field_1724 != null && mc.field_1724.field_3944 != null && mc.field_1761 != null) {
         if (this.breakMode.is("Быстрый")) {
            long interval = Math.max(1L, (long)(1000.0F / Math.max(1.0F, this.packetsPerSecond.get())));
            if (now - this.lastPacketTime >= interval) {
               this.performFastBreak(this.targetPos);
               this.lastPacketTime = now;
            }
         } else if (now - this.lastBreakTime >= 3L) {
            mc.field_1761.method_2910(this.targetPos, class_2350.field_11036);
            mc.field_1761.method_2902(this.targetPos, class_2350.field_11036);
            if (this.swing.isState()) {
               mc.field_1724.method_6104(class_1268.field_5808);
            }

            this.lastBreakTime = now;
         }
      }
   }

   private void performFastBreak(class_2338 pos) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1724.field_3944 != null) {
         this.performFastBreak(mc.field_1724.field_3944, mc.field_1761, mc.field_1724, mc.field_1687, pos, this.swing.isState());
      }
   }

   private void performFastBreak(class_634 handler, class_636 interactionManager, class_746 player, class_638 world, class_2338 pos, boolean shouldSwing) {
      if (handler != null && player != null && pos != null) {
         boolean accelerated = false;
         if (interactionManager != null && world != null) {
            interactionManager.method_2910(pos, class_2350.field_11036);
            accelerated = FastBreak.accelerateClientBreak(interactionManager, player, world, pos, class_2350.field_11036, 1.0F, shouldSwing);
         }

         if (!accelerated) {
            FastBreak.packetBreak(handler, player, pos, class_2350.field_11036, shouldSwing);
         }
      }
   }

   private class_2338 findNearestLog() {
      return this.findNearestLog(mc.field_1687, mc.field_1724, this.breakRadius.get());
   }

   private class_2338 findNearestLog(class_638 world, class_746 player, float radiusValue) {
      if (player != null && world != null) {
         class_2338 playerPos = player.method_24515();
         int radius = Math.round(radiusValue);
         return class_2338.method_20437(playerPos.method_10069(-radius, -radius, -radius), playerPos.method_10069(radius, radius, radius))
            .<class_2338>map(class_2338::method_10062)
            .filter(pos -> this.isLog(world, pos))
            .filter(pos -> this.isInRange(player, pos))
            .filter(pos -> this.isVisible(world, player, pos))
            .min(Comparator.comparingDouble(pos -> player.method_5707(class_243.method_24953(pos))))
            .orElse(null);
      } else {
         return null;
      }
   }

   private boolean isInRange(class_2338 pos) {
      return this.isInRange(mc.field_1724, pos);
   }

   private boolean isInRange(class_746 player, class_2338 pos) {
      return player != null && player.method_5707(class_243.method_24953(pos)) <= 16.0;
   }

   private boolean isVisible(class_2338 pos) {
      return this.isVisible(mc.field_1687, mc.field_1724, pos);
   }

   private boolean isVisible(class_638 world, class_746 player, class_2338 pos) {
      if (player != null && world != null) {
         class_243 eyePos = player.method_33571();
         class_243 targetCenter = class_243.method_24953(pos);
         class_3965 hit = world.method_17742(new class_3959(eyePos, targetCenter, class_3960.field_17558, class_242.field_1348, player));
         return hit == null || hit.method_17783() == class_240.field_1333 || pos.equals(hit.method_17777());
      } else {
         return false;
      }
   }

   private boolean isLog(class_2338 pos) {
      return this.isLog(mc.field_1687, pos);
   }

   private boolean isLog(class_638 world, class_2338 pos) {
      return world != null && world.method_8320(pos).method_26164(class_3481.field_15475);
   }

   private void handleDigPacket(class_2846 packet) {
      class_2847 action = packet.method_12363();
      if (action == class_2847.field_12968 || action == class_2847.field_12973) {
         class_2338 pos = packet.method_12362();
         if (this.isLog(pos)) {
            class_2680 state = mc.field_1687.method_8320(pos);
            if (!state.method_26215()) {
               this.preservedBlocks.put(pos, state);
               this.managedBlocks.add(pos);
               this.lastUpdateTime.put(pos, System.currentTimeMillis());
               this.setClientBlock(pos, state);
            }
         }
      }
   }

   private void updateVisualization(long now) {
      class_638 toRemove = mc.field_1687;
      if (toRemove instanceof class_638) {
         class_638 clientWorld = toRemove;
         HashSet<class_2338> var11 = new HashSet<>();

         for (Entry<class_2338, class_2680> entry : this.preservedBlocks.entrySet()) {
            class_2338 pos = entry.getKey();
            class_2680 savedState = entry.getValue();
            class_2680 currentState = clientWorld.method_8320(pos);
            if (currentState == null || !currentState.equals(savedState)) {
               clientWorld.method_8652(pos, savedState, 0);
               this.lastUpdateTime.put(pos, now);
            }

            Long lastSeen = this.lastUpdateTime.get(pos);
            if (lastSeen != null && now - lastSeen > 300000L) {
               var11.add(pos);
            }
         }

         for (class_2338 posx : var11) {
            this.preservedBlocks.remove(posx);
            this.lastUpdateTime.remove(posx);
            this.managedBlocks.remove(posx);
         }
      }
   }

   private void restoreVisualState() {
      class_638 var2 = mc.field_1687;
      if (!(var2 instanceof class_638)) {
         this.preservedBlocks.clear();
         this.lastUpdateTime.clear();
         this.managedBlocks.clear();
      } else {
         class_638 clientWorld = var2;

         for (class_2338 pos : this.managedBlocks) {
            clientWorld.method_8652(pos, mc.field_1687.method_8320(pos), 0);
         }

         this.preservedBlocks.clear();
         this.lastUpdateTime.clear();
         this.managedBlocks.clear();
      }
   }

   private void setClientBlock(class_2338 pos, class_2680 state) {
      class_638 var4 = mc.field_1687;
      if (var4 instanceof class_638) {
         var4.method_8652(pos, state, 0);
      }
   }

   public List<String> getModeSuggestions() {
      return List.of("normal", "fast");
   }

   public boolean setModeAlias(String alias) {
      if (alias != null && !alias.isBlank()) {
         String var2 = alias.trim().toLowerCase(Locale.ROOT);

         return switch (var2) {
            case "normal", "default", "обычный" -> {
               this.breakMode.set(this.breakMode.getMods().get(0));
               yield true;
            }
            case "fast", "quick", "быстрый" -> {
               if (this.breakMode.getMods().size() < 2) {
                  yield false;
               } else {
                  this.breakMode.set(this.breakMode.getMods().get(1));
                  yield true;
               }
            }
            default -> false;
         };
      } else {
         return false;
      }
   }

   public String getModeAlias() {
      return this.breakMode.getMods().size() > 1 && this.breakMode.is(this.breakMode.getMods().get(1)) ? "fast" : "normal";
   }

   public void enableForCurrentSession() {
      this.currentSessionEnabled = true;
      this.resetRuntimeState();
      this.restoreVisualState();
   }

   public void disableForCurrentSession() {
      this.currentSessionEnabled = false;
      this.restoreVisualState();
      this.resetRuntimeState();
   }

   public boolean isCurrentSessionEnabled() {
      return this.currentSessionEnabled;
   }

   public void setSwingEnabled(boolean value) {
      this.swing.setState(value);
   }

   public boolean isSwingEnabled() {
      return this.swing.isState();
   }

   public void setAutoSellEnabled(boolean value) {
      this.autoSell.setState(value);
   }

   public boolean isAutoSellEnabled() {
      return this.autoSell.isState();
   }

   public void setAutoPayEnabled(boolean value) {
      this.autoPay.setState(value);
      if (!value) {
         this.lastNickReminderTime = 0L;
      }
   }

   public boolean isAutoPayEnabled() {
      return this.autoPay.isState();
   }

   public void setPreserveVisualsEnabled(boolean value) {
      this.preserveVisuals.setState(value);
   }

   public boolean isPreserveVisualsEnabled() {
      return this.preserveVisuals.isState();
   }

   public void setPacketsPerSecond(float value) {
      this.packetsPerSecond.setValue(value);
   }

   public float getPacketsPerSecond() {
      return this.packetsPerSecond.get();
   }

   public void setBreakRadius(float value) {
      this.breakRadius.setValue(value);
   }

   public float getBreakRadius() {
      return this.breakRadius.get();
   }

   public void setPayAmount(float value) {
      this.payAmount.setValue(value);
   }

   public float getPayAmount() {
      return this.payAmount.get();
   }

   public void setIntervalSeconds(float value) {
      this.intervalSeconds.setValue(value);
   }

   public float getIntervalSeconds() {
      return this.intervalSeconds.get();
   }

   public boolean setPayTarget(String target) {
      String trimmed = target == null ? "" : target.trim();
      if (trimmed.isEmpty()) {
         return false;
      } else {
         this.payTarget = trimmed;
         this.lastNickReminderTime = 0L;
         return true;
      }
   }

   public String getPayTarget() {
      return this.payTarget;
   }

   public boolean capturePayTargetFromChat(String message) {
      return false;
   }

   public void clearPayTarget() {
      this.payTarget = "";
      this.lastNickReminderTime = 0L;
   }

   public AutoForest.SessionState captureState() {
      AutoForest.SessionState state = new AutoForest.SessionState();
      state.enabled(this.currentSessionEnabled);
      state.modeAlias(this.getModeAlias());
      state.packetsPerSecond(this.packetsPerSecond.get());
      state.breakRadius(this.breakRadius.get());
      state.swing(this.swing.isState());
      state.autoSell(this.autoSell.isState());
      state.autoPay(this.autoPay.isState());
      state.preserveVisuals(this.preserveVisuals.isState());
      state.payAmount(this.payAmount.get());
      state.intervalSeconds(this.intervalSeconds.get());
      state.payTarget(this.payTarget);
      state.targetPos(this.targetPos);
      state.lastBreakTime(this.lastBreakTime);
      state.lastPacketTime(this.lastPacketTime);
      state.lastSellTime(this.lastSellTime);
      state.lastPayTime(this.lastPayTime);
      state.lastNickReminderTime(this.lastNickReminderTime);
      state.preservedBlocks(new HashMap<>(this.preservedBlocks));
      state.lastUpdateTime(new HashMap<>(this.lastUpdateTime));
      state.managedBlocks(new HashSet<>(this.managedBlocks));
      return state;
   }

   public void applyState(AutoForest.SessionState state) {
      if (state == null) {
         this.resetToDefaults();
      } else {
         this.currentSessionEnabled = state.enabled();
         this.setModeAlias(state.modeAlias());
         this.packetsPerSecond.setValue(state.packetsPerSecond());
         this.breakRadius.setValue(state.breakRadius());
         this.swing.setState(state.swing());
         this.autoSell.setState(state.autoSell());
         this.autoPay.setState(state.autoPay());
         this.preserveVisuals.setState(state.preserveVisuals());
         this.payAmount.setValue(state.payAmount());
         this.intervalSeconds.setValue(state.intervalSeconds());
         this.payTarget = state.payTarget();
         this.targetPos = state.targetPos();
         this.lastBreakTime = state.lastBreakTime();
         this.lastPacketTime = state.lastPacketTime();
         this.lastSellTime = state.lastSellTime();
         this.lastPayTime = state.lastPayTime();
         this.lastNickReminderTime = state.lastNickReminderTime();
         this.preservedBlocks.clear();
         this.preservedBlocks.putAll(state.preservedBlocks());
         this.lastUpdateTime.clear();
         this.lastUpdateTime.putAll(state.lastUpdateTime());
         this.managedBlocks.clear();
         this.managedBlocks.addAll(state.managedBlocks());
      }
   }

   public void resetToDefaults() {
      this.currentSessionEnabled = false;
      this.setModeAlias("normal");
      this.packetsPerSecond.setValue(100.0F);
      this.breakRadius.setValue(4.0F);
      this.swing.setState(true);
      this.autoSell.setState(true);
      this.autoPay.setState(false);
      this.preserveVisuals.setState(true);
      this.payAmount.setValue(1000.0F);
      this.intervalSeconds.setValue(20.0F);
      this.payTarget = "";
      this.restoreVisualState();
      this.resetRuntimeState();
   }

   private void resetRuntimeState() {
      this.targetPos = null;
      this.lastBreakTime = 0L;
      this.lastPacketTime = 0L;
      this.lastSellTime = 0L;
      this.lastPayTime = 0L;
      this.lastNickReminderTime = 0L;
      this.preservedBlocks.clear();
      this.lastUpdateTime.clear();
      this.managedBlocks.clear();
   }

   public static final class SessionState {
      private boolean enabled;
      private String modeAlias = "normal";
      private float packetsPerSecond = 100.0F;
      private float breakRadius = 4.0F;
      private boolean swing = true;
      private boolean autoSell = true;
      private boolean autoPay;
      private boolean preserveVisuals = true;
      private float payAmount = 1000.0F;
      private float intervalSeconds = 20.0F;
      private String payTarget = "";
      private class_2338 targetPos;
      private long lastBreakTime;
      private long lastPacketTime;
      private long lastSellTime;
      private long lastPayTime;
      private long lastNickReminderTime;
      private Map<class_2338, class_2680> preservedBlocks = new HashMap<>();
      private Map<class_2338, Long> lastUpdateTime = new HashMap<>();
      private Set<class_2338> managedBlocks = new HashSet<>();

      public SessionState() {
      }

      public boolean enabled() {
         return this.enabled;
      }

      public void enabled(boolean value) {
         this.enabled = value;
      }

      public String modeAlias() {
         return this.modeAlias;
      }

      public void modeAlias(String value) {
         this.modeAlias = value == null ? "normal" : value;
      }

      public float packetsPerSecond() {
         return this.packetsPerSecond;
      }

      public void packetsPerSecond(float value) {
         this.packetsPerSecond = value;
      }

      public float breakRadius() {
         return this.breakRadius;
      }

      public void breakRadius(float value) {
         this.breakRadius = value;
      }

      public boolean swing() {
         return this.swing;
      }

      public void swing(boolean value) {
         this.swing = value;
      }

      public boolean autoSell() {
         return this.autoSell;
      }

      public void autoSell(boolean value) {
         this.autoSell = value;
      }

      public boolean autoPay() {
         return this.autoPay;
      }

      public void autoPay(boolean value) {
         this.autoPay = value;
      }

      public boolean preserveVisuals() {
         return this.preserveVisuals;
      }

      public void preserveVisuals(boolean value) {
         this.preserveVisuals = value;
      }

      public float payAmount() {
         return this.payAmount;
      }

      public void payAmount(float value) {
         this.payAmount = value;
      }

      public float intervalSeconds() {
         return this.intervalSeconds;
      }

      public void intervalSeconds(float value) {
         this.intervalSeconds = value;
      }

      public String payTarget() {
         return this.payTarget == null ? "" : this.payTarget;
      }

      public void payTarget(String value) {
         this.payTarget = value == null ? "" : value;
      }

      public class_2338 targetPos() {
         return this.targetPos;
      }

      public void targetPos(class_2338 value) {
         this.targetPos = value;
      }

      public long lastBreakTime() {
         return this.lastBreakTime;
      }

      public void lastBreakTime(long value) {
         this.lastBreakTime = value;
      }

      public long lastPacketTime() {
         return this.lastPacketTime;
      }

      public void lastPacketTime(long value) {
         this.lastPacketTime = value;
      }

      public long lastSellTime() {
         return this.lastSellTime;
      }

      public void lastSellTime(long value) {
         this.lastSellTime = value;
      }

      public long lastPayTime() {
         return this.lastPayTime;
      }

      public void lastPayTime(long value) {
         this.lastPayTime = value;
      }

      public long lastNickReminderTime() {
         return this.lastNickReminderTime;
      }

      public void lastNickReminderTime(long value) {
         this.lastNickReminderTime = value;
      }

      public Map<class_2338, class_2680> preservedBlocks() {
         return this.preservedBlocks;
      }

      public void preservedBlocks(Map<class_2338, class_2680> value) {
         this.preservedBlocks = (Map<class_2338, class_2680>)(value == null ? new HashMap<>() : value);
      }

      public Map<class_2338, Long> lastUpdateTime() {
         return this.lastUpdateTime;
      }

      public void lastUpdateTime(Map<class_2338, Long> value) {
         this.lastUpdateTime = (Map<class_2338, Long>)(value == null ? new HashMap<>() : value);
      }

      public Set<class_2338> managedBlocks() {
         return this.managedBlocks;
      }

      public void managedBlocks(Set<class_2338> value) {
         this.managedBlocks = (Set<class_2338>)(value == null ? new HashSet<>() : value);
      }
   }
}
