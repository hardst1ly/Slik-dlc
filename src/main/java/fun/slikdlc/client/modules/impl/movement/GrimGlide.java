package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_243;

public class GrimGlide extends Module {
   public static GrimGlide INSTANCE = new GrimGlide();
   private long lastTickTime = 0L;
   private int ticksTwo = 0;

   public GrimGlide() {
      super("GrimGlide", "Ускорение на элитре без фееров", Module.ModuleCategory.MOVEMENT);
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1724.method_6128()) {
         this.ticksTwo++;
         class_243 pos = mc.field_1724.method_19538();
         float yaw = mc.field_1724.method_36454();
         double forward = mc.field_1724.field_6012 % 2 == 0 ? 0.087 : 0.09;
         double dx = -Math.sin(Math.toRadians(yaw)) * forward;
         double dz = Math.cos(Math.toRadians(yaw)) * forward;
         if (System.currentTimeMillis() - this.lastTickTime >= 40L) {
            mc.field_1724.method_5814(pos.method_10216() + dx, pos.method_10214(), pos.method_10215() + dz);
            this.lastTickTime = System.currentTimeMillis();
         }

         if (this.ticksTwo % 40 == 0) {
            mc.field_1724
               .method_18800(
                  dx * ThreadLocalRandom.current().nextFloat(1.001F, 1.0021F),
                  mc.field_1724.method_18798().field_1351 + 0.00600000075995922,
                  dz * ThreadLocalRandom.current().nextFloat(1.001F, 1.0021F)
               );
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.ticksTwo = 0;
      this.lastTickTime = System.currentTimeMillis();
   }

   @Override
   public void onDisable() {
      this.ticksTwo = 0;
      super.onDisable();
   }
}
