package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.player.MoveUtils;
import fun.slikdlc.api.utils.player.ViaProtocolUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import lombok.Generated;
import net.minecraft.class_310;
import net.minecraft.class_746;

public class Sprint extends Module {
   public static Sprint INSTANCE = new Sprint();
   private static final class_310 CLIENT = class_310.method_1551();
   private final BooleanSetting keepInWater = new BooleanSetting("Сохранять в воде", false);
   private static boolean sprinting;
   private static long time = 0L;
   private static int pauseDepth = 0;
   private static boolean restoreAfterPause = false;
   private class_746 lastPlayer;

   public Sprint() {
      super("Sprint", "Автоматический бег", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.keepInWater});
   }

   @Override
   public void onEnable() {
      resetPauseState();
      sprinting = true;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      resetPauseState();
      sprinting = false;
      this.lastPlayer = null;
      if (mc.field_1690 != null) {
         mc.field_1690.field_1867.method_23481(false);
      }

      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);
      }

      super.onDisable();
   }

   @EventLink
   public void onEvent(EventUpdate ignored) {
      if (mc.field_1724 == null) {
         this.lastPlayer = null;
         resetPauseState();
         if (mc.field_1690 != null) {
            mc.field_1690.field_1867.method_23481(false);
         }
      } else {
         if (this.lastPlayer != mc.field_1724) {
            this.lastPlayer = mc.field_1724;
            resetPauseState();
            sprinting = true;
         }

         boolean legacyProtocol = ViaProtocolUtils.isTargetProtocolBelowOneNineteen();
         boolean inWater = mc.field_1724.method_5799() || mc.field_1724.method_5869();
         boolean shouldSprint = pauseDepth == 0
            && System.currentTimeMillis() >= time
            && sprinting
            && MoveUtils.isMoving()
            && mc.field_1724.field_3913.field_3905 > 0.0F
            && (!legacyProtocol || !mc.field_1724.field_5976 && !mc.field_1724.field_34927)
            && !mc.field_1724.method_6128();
         if (this.keepInWater.isState() && inWater && mc.field_1724.method_5624()) {
            shouldSprint = true;
         }

         mc.field_1690.field_1867.method_23481(shouldSprint);
         mc.field_1724.method_5728(shouldSprint);
      }
   }

   public boolean shouldKeepSprintInWater() {
      return this.isEnable() && this.keepInWater.isState();
   }

   public static void pushPause(long delayMs) {
      restoreAfterPause = restoreAfterPause | shouldRestoreAfterPause();
      pauseDepth++;
      time = Math.max(time, System.currentTimeMillis() + Math.max(0L, delayMs));
      sprinting = false;
      if (CLIENT.field_1690 != null) {
         CLIENT.field_1690.field_1867.method_23481(false);
      }

      if (CLIENT.field_1724 != null) {
         CLIENT.field_1724.method_5728(false);
      }
   }

   public static void popPause() {
      if (pauseDepth > 0) {
         pauseDepth--;
      }

      if (pauseDepth <= 0) {
         time = 0L;
         sprinting = restoreAfterPause;
         restoreAfterPause = false;
      }
   }

   private static boolean shouldRestoreAfterPause() {
      return CLIENT.field_1724 != null && CLIENT.field_1724.method_5624() ? true : ModuleClass.sprint != null && ModuleClass.sprint.isEnable() && sprinting;
   }

   private static void resetPauseState() {
      pauseDepth = 0;
      restoreAfterPause = false;
      time = 0L;
   }

   @Generated
   public static boolean isSprinting() {
      return sprinting;
   }

   @Generated
   public static void setSprinting(boolean sprinting) {
      Sprint.sprinting = sprinting;
   }

   @Generated
   public static long getTime() {
      return time;
   }

   @Generated
   public static void setTime(long time) {
      Sprint.time = time;
   }
}
