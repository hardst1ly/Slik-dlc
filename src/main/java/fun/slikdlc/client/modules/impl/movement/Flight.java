package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_243;

public class Flight extends Module {
   public static Flight INSTANCE = new Flight();
   private final FloatSetting speed = new FloatSetting("Скорость", 2.0F, 0.1F, 10.0F, 0.1F);

   public Flight() {
      super("Flight", "Полёт", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.speed});
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null) {
         double spd = this.speed.get();
         float yaw = (float)Math.toRadians(mc.field_1724.method_36454());
         double motionX = 0.0;
         double motionY = 0.0;
         double motionZ = 0.0;
         double forward = 0.0;
         double strafe = 0.0;
         if (mc.field_1690.field_1894.method_1434()) {
            forward++;
         }

         if (mc.field_1690.field_1881.method_1434()) {
            forward--;
         }

         if (mc.field_1690.field_1913.method_1434()) {
            strafe++;
         }

         if (mc.field_1690.field_1849.method_1434()) {
            strafe--;
         }

         if (forward != 0.0 || strafe != 0.0) {
            double angle = Math.atan2(forward, strafe) - (Math.PI / 2);
            motionX = -Math.sin(yaw + angle) * spd;
            motionZ = Math.cos(yaw + angle) * spd;
         }

         if (mc.field_1690.field_1903.method_1434()) {
            motionY = spd;
         } else if (mc.field_1690.field_1832.method_1434()) {
            motionY = -spd;
         }

         mc.field_1724.method_18799(new class_243(motionX, motionY, motionZ));
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.field_1724 != null) {
         mc.field_1724.method_18799(class_243.field_1353);
      }
   }
}
