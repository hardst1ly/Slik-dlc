package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_2879;
import net.minecraft.class_3965;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2846.class_2847;

public class FastBreak extends Module {
   public static FastBreak INSTANCE = new FastBreak();
   private final FloatSetting speed = new FloatSetting("Ускорение", 0.5F, 0.3F, 1.0F, 0.1F);

   public FastBreak() {
      super("FastBreak", "Ускоряет ломание блоков", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.speed});
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (mc.field_1765 instanceof class_3965 hit) {
            if (mc.field_1690.field_1886.method_1434()) {
               accelerateClientBreak(mc.field_1761, mc.field_1724, mc.field_1687, hit.method_17777(), hit.method_17780(), this.speed.get(), true);
            }
         }
      }
   }

   public float getSpeed() {
      return this.speed.get();
   }

   public static int getExtraTicks(float speed) {
      return Math.max(1, Math.round(Math.max(0.3F, speed) / 0.35F));
   }

   public static boolean accelerateClientBreak(
      class_636 interactionManager, class_746 player, class_638 world, class_2338 pos, class_2350 side, float speed, boolean swing
   ) {
      if (interactionManager != null && player != null && world != null && pos != null) {
         class_2680 state = world.method_8320(pos);
         if (state != null && !state.method_26215()) {
            class_2350 breakSide = side == null ? class_2350.field_11036 : side;
            int extraTicks = getExtraTicks(speed);

            for (int i = 0; i < extraTicks; i++) {
               interactionManager.method_2902(pos, breakSide);
            }

            if (swing) {
               player.method_6104(class_1268.field_5808);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean packetBreak(class_634 handler, class_746 player, class_2338 pos, class_2350 side, boolean swing) {
      if (handler != null && player != null && pos != null) {
         class_2350 breakSide = side == null ? class_2350.field_11036 : side;
         handler.method_52787(new class_2846(class_2847.field_12968, pos, breakSide));
         handler.method_52787(new class_2846(class_2847.field_12973, pos, breakSide));
         if (swing) {
            handler.method_52787(new class_2879(class_1268.field_5808));
         }

         return true;
      } else {
         return false;
      }
   }
}
