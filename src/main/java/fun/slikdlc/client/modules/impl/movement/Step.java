package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;

public class Step extends Module {
   public static Step INSTANCE = new Step();
   public ModeSetting mode = new ModeSetting("Режим", "Vanilla", "Vanilla", "NCP", "Motion");
   public FloatSetting height = new FloatSetting("Высота", 1.0F, 1.0F, 10.0F, 0.5F);
   public BooleanSetting reverse = new BooleanSetting("Reverse", false);
   public FloatSetting reverseHeight = new FloatSetting("Высота Reverse", 1.0F, 1.0F, 10.0F, 0.5F);
   private int timer = 0;

   public Step() {
      super("Step", "Моментально взбирается на блок", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.height, this.reverse, this.reverseHeight});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.timer = 0;
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.reverse.isState()
            && mc.field_1724.method_24828()
            && !mc.field_1690.field_1903.method_1434()
            && !mc.field_1724.method_5715()
            && !this.isBlockAbove()) {
            float fallDistance = this.reverseHeight.get();
            if (this.canFall(fallDistance)) {
               class_243 vel = mc.field_1724.method_18798();
               mc.field_1724.method_18800(vel.field_1352, -fallDistance, vel.field_1350);
            }
         }

         if (mc.field_1724.field_5976 && mc.field_1724.method_24828() && !mc.field_1690.field_1903.method_1434()) {
            float stepHeight = this.getStepHeight();
            if (stepHeight > 0.6F && stepHeight <= this.height.get()) {
               if (this.mode.is("Vanilla")) {
                  this.handleVanillaStep(stepHeight);
               }

               if (this.mode.is("NCP")) {
                  this.handleNCPStep(stepHeight);
               }

               if (this.mode.is("Motion")) {
                  this.handleMotionStep(stepHeight);
               }
            }
         } else {
            this.timer = 0;
         }
      }
   }

   private void handleVanillaStep(float stepHeight) {
      mc.field_1724.method_5814(mc.field_1724.method_23317(), mc.field_1724.method_23318() + stepHeight, mc.field_1724.method_23321());
   }

   private void handleNCPStep(float stepHeight) {
      double[] offsets = null;
      double baseY = mc.field_1724.method_23318();
      if (stepHeight <= 1.0F) {
         offsets = new double[]{0.42, 0.753};
      } else if (stepHeight <= 1.5F) {
         offsets = new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
      } else if (stepHeight <= 2.0F) {
         offsets = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
      } else if (stepHeight <= 2.5F) {
         offsets = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
      } else if (stepHeight <= 3.0F) {
         offsets = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43, 1.78, 2.1, 2.4, 2.7};
      }

      if (offsets != null) {
         for (double offset : offsets) {
            mc.field_1724.method_5814(mc.field_1724.method_23317(), baseY + offset, mc.field_1724.method_23321());
         }
      }
   }

   private void handleMotionStep(float stepHeight) {
      class_243 velocity = mc.field_1724.method_18798();
      double motionY = 0.42;
      if (stepHeight <= 1.0F) {
         motionY = 0.42;
      } else if (stepHeight <= 1.5F) {
         motionY = 0.52;
      } else if (stepHeight <= 2.0F) {
         motionY = 0.62;
      } else if (stepHeight <= 2.5F) {
         motionY = 0.72;
      } else if (stepHeight <= 3.0F) {
         motionY = 0.82;
      }

      mc.field_1724.method_18800(velocity.field_1352, motionY, velocity.field_1350);
   }

   private float getStepHeight() {
      class_238 box = mc.field_1724.method_5829();
      float maxY = 0.0F;
      double checkDistance = 0.3;
      double playerYaw = Math.toRadians(mc.field_1724.method_36454());
      double offsetX = -Math.sin(playerYaw) * checkDistance;
      double offsetZ = Math.cos(playerYaw) * checkDistance;

      for (double y = 0.6; y <= this.height.get() + 0.6; y += 0.1) {
         class_238 testBox = box.method_989(offsetX, y, offsetZ);

         for (class_2338 pos : class_2338.method_10094(
            (int)Math.floor(testBox.field_1323),
            (int)Math.floor(testBox.field_1322),
            (int)Math.floor(testBox.field_1321),
            (int)Math.floor(testBox.field_1320),
            (int)Math.floor(testBox.field_1325),
            (int)Math.floor(testBox.field_1324)
         )) {
            class_2680 state = mc.field_1687.method_8320(pos);
            if (!state.method_26215()) {
               class_265 shape = state.method_26220(mc.field_1687, pos);
               if (!shape.method_1110()) {
                  for (class_238 collisionBox : shape.method_1090()) {
                     class_238 offsetBox = collisionBox.method_996(pos);
                     float blockHeight = (float)(offsetBox.field_1325 - mc.field_1724.method_23318());
                     if (blockHeight > 0.6F && blockHeight <= this.height.get()) {
                        maxY = Math.max(maxY, blockHeight);
                     }
                  }
               }
            }
         }
      }

      return maxY;
   }

   private boolean isBlockAbove() {
      class_238 box = mc.field_1724.method_5829().method_989(0.0, 1.0, 0.0);

      for (class_2338 pos : class_2338.method_10094(
         (int)Math.floor(box.field_1323),
         (int)Math.floor(box.field_1322),
         (int)Math.floor(box.field_1321),
         (int)Math.floor(box.field_1320),
         (int)Math.floor(box.field_1325),
         (int)Math.floor(box.field_1324)
      )) {
         if (!mc.field_1687.method_8320(pos).method_26215()) {
            return true;
         }
      }

      return false;
   }

   private boolean canFall(float distance) {
      class_238 box = mc.field_1724.method_5829();

      for (double y = 0.1; y <= distance; y += 0.1) {
         class_238 testBox = box.method_989(0.0, -y, 0.0);

         for (class_2338 pos : class_2338.method_10094(
            (int)Math.floor(testBox.field_1323),
            (int)Math.floor(testBox.field_1322),
            (int)Math.floor(testBox.field_1321),
            (int)Math.floor(testBox.field_1320),
            (int)Math.floor(testBox.field_1325),
            (int)Math.floor(testBox.field_1324)
         )) {
            if (!mc.field_1687.method_8320(pos).method_26215()) {
               return false;
            }
         }
      }

      return true;
   }
}
