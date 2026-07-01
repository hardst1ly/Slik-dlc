package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class FunTimeRotation extends RotationsSystem implements QClient {
   private long smoothbackShakeStartMs = -1L;

   public FunTimeRotation() {
   }

   public void reset() {
      this.smoothbackShakeStartMs = -1L;
   }

   private float calculateRotationDistance(float yawDiff, float pitchDiff) {
      return (float)Math.hypot(yawDiff, pitchDiff);
   }

   private int randomInt(int min, int max) {
      return min + (int)(Math.random() * (max - min + 1));
   }

   @Override
   public void updateRotations(class_1309 target) {
      if (mc.field_1724 != null) {
         Aura aura = Aura.INSTANCE;
         boolean canAttack = aura.getAttackTimer().finished(467L);
         if (aura.isEnable() && target != null && canAttack) {
            this.smoothbackShakeStartMs = -1L;
            class_243 targetPos = target.method_5829().method_1005();
            class_243 playerEye = mc.field_1724.method_33571();
            class_243 delta = targetPos.method_1020(playerEye);
            float targetYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
            float targetPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350))));
            float currentYaw = mc.field_1724.method_36454();
            float currentPitch = mc.field_1724.method_36455();
            float yawDiff = class_3532.method_15393(targetYaw - currentYaw);
            float pitchDiff = class_3532.method_15393(targetPitch - currentPitch);
            float rotationDistance = this.calculateRotationDistance(yawDiff, pitchDiff);
            float maxStepYaw = Math.abs(yawDiff / rotationDistance) * 130.0F;
            float maxStepPitch = Math.abs(pitchDiff / rotationDistance) * 130.0F;
            float finalYaw = class_3532.method_16439(0.85F, currentYaw, currentYaw + class_3532.method_15363(yawDiff, -maxStepYaw, maxStepYaw));
            float finalPitch = class_3532.method_16439(0.85F, currentPitch, currentPitch + class_3532.method_15363(pitchDiff, -maxStepPitch, maxStepPitch));
            RotationStorage.update(new Rotation(finalYaw, finalPitch), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
         } else {
            float currentYaw = mc.field_1724.method_36454();
            float currentPitch = mc.field_1724.method_36455();
            float yawDiff = 0.0F;
            float pitchDiff = 0.0F;
            float rotationDistance = this.calculateRotationDistance(yawDiff, pitchDiff);
            float shakeYaw = (float)(this.randomInt(8, 11) * Math.sin(System.currentTimeMillis() / 55.0));
            float shakePitch = (float)(this.randomInt(4, 8) * Math.cos(System.currentTimeMillis() / 55.0));
            if (aura.isEnable() && target != null) {
               this.smoothbackShakeStartMs = -1L;
            } else {
               if (this.smoothbackShakeStartMs < 0L) {
                  this.smoothbackShakeStartMs = System.currentTimeMillis();
               }

               float shakeFactor = 1.0F - class_3532.method_15363((float)(System.currentTimeMillis() - this.smoothbackShakeStartMs) / 1000.0F, 0.0F, 1.0F);
               shakeYaw *= shakeFactor;
               shakePitch *= shakeFactor;
            }

            float maxReturnYaw = 45.0F;
            float maxReturnPitch = 45.0F;
            float finalYaw = class_3532.method_16439(0.85F, currentYaw, currentYaw + class_3532.method_15363(yawDiff, -maxReturnYaw, maxReturnYaw) + shakeYaw);
            float finalPitch = class_3532.method_16439(
               0.85F, currentPitch, currentPitch + class_3532.method_15363(pitchDiff, -maxReturnPitch, maxReturnPitch) + shakePitch
            );
            RotationStorage.update(new Rotation(finalYaw, finalPitch), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
         }
      }
   }
}
