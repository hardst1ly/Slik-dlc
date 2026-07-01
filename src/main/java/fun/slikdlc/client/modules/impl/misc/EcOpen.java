package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.events.implement.EventGameUpdate;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1268;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3965;

public class EcOpen extends Module {
   public static EcOpen INSTANCE = new EcOpen();
   private final BindSetting openKey = new BindSetting("Открыть", -1);
   private final FloatSetting range = new FloatSetting("Дистанция", 6.0F, 3.0F, 6.0F, 0.1F);
   private class_2338 targetChest = null;
   private boolean shouldRotate = false;
   private int rotationTicks = 0;
   private float currentYaw;
   private float currentPitch;

   public EcOpen() {
      super("EcOpen", "Открывает эндер сундук по бинду", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.openKey, this.range});
   }

   @Override
   public void onEnable() {
      this.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.reset();
      super.onDisable();
   }

   @EventLink
   public void onBinding(EventBinding event) {
      if (mc.field_1755 == null && mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getKey() == this.openKey.getKey()) {
            this.findEnderChest();
         }
      }
   }

   @EventLink
   public void onGameUpdate(EventGameUpdate event) {
      if (this.shouldRotate && this.targetChest != null && mc.field_1724 != null) {
         if (!mc.field_1687.method_8320(this.targetChest).method_27852(class_2246.field_10443)) {
            this.reset();
         } else {
            class_243 target = class_243.method_24953(this.targetChest);
            float[] rotations = this.calculateRotation(target);
            float deltaYaw = class_3532.method_15393(rotations[0] - this.currentYaw);
            float deltaPitch = rotations[1] - this.currentPitch;
            this.currentYaw += deltaYaw * 0.8F;
            this.currentPitch = class_3532.method_15363(this.currentPitch + deltaPitch * 0.8F, -90.0F, 90.0F);
            RotationStorage.update(new Rotation(this.currentYaw, this.currentPitch), 360.0F, 360.0F, 360.0F, 360.0F, 1, 1, false);
            this.rotationTicks++;
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (this.shouldRotate && this.targetChest != null && mc.field_1724 != null) {
         if (this.rotationTicks >= 2) {
            class_243 hitVec = class_243.method_24953(this.targetChest).method_1031(0.0, 0.5, 0.0);
            class_3965 hitResult = new class_3965(hitVec, class_2350.field_11036, this.targetChest, false);
            mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hitResult);
            mc.field_1724.method_6104(class_1268.field_5808);
            this.reset();
         }

         if (this.rotationTicks > 20) {
            this.reset();
         }
      }
   }

   private void findEnderChest() {
      class_2338 playerPos = mc.field_1724.method_24515();
      int r = this.range.getValue().intValue();
      double maxDist = this.range.getValue().floatValue() * this.range.getValue().floatValue();
      double closestDist = Double.MAX_VALUE;
      class_2338 closest = null;

      for (int x = -r; x <= r; x++) {
         for (int y = -r; y <= r; y++) {
            for (int z = -r; z <= r; z++) {
               class_2338 pos = playerPos.method_10069(x, y, z);
               if (mc.field_1687.method_8320(pos).method_27852(class_2246.field_10443)) {
                  double dist = mc.field_1724.method_33571().method_1025(class_243.method_24953(pos));
                  if (dist < closestDist && dist <= maxDist) {
                     closestDist = dist;
                     closest = pos;
                  }
               }
            }
         }
      }

      if (closest != null) {
         this.targetChest = closest;
         this.shouldRotate = true;
         this.rotationTicks = 0;
         this.currentYaw = mc.field_1724.method_36454();
         this.currentPitch = mc.field_1724.method_36455();
      }
   }

   private float[] calculateRotation(class_243 target) {
      class_243 eye = mc.field_1724.method_33571();
      double dx = target.field_1352 - eye.field_1352;
      double dy = target.field_1351 - eye.field_1351;
      double dz = target.field_1350 - eye.field_1350;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new float[]{yaw, class_3532.method_15363(pitch, -90.0F, 90.0F)};
   }

   private void reset() {
      this.targetChest = null;
      this.shouldRotate = false;
      this.rotationTicks = 0;
   }
}
