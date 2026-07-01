package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.api.utils.rotate.RotationUtils;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import fun.slikdlc.client.modules.impl.combat.components.interpolation.BestPoint;
import net.minecraft.class_1309;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class WhiteRiseRotation extends RotationsSystem implements QClient {
   private final Aura aura;
   private class_1309 trackedTarget;
   private float lastYaw;
   private float lastPitch;
   private float speedAcceleration;
   private boolean back;
   private boolean initialized;
   private float jitterOffset;
   private int tickCounter;

   public WhiteRiseRotation(Aura aura) {
      this.aura = aura;
   }

   public void reset() {
      this.trackedTarget = null;
      this.speedAcceleration = 0.0F;
      this.back = false;
      this.jitterOffset = 0.0F;
      this.tickCounter = 0;
      this.initialized = mc.field_1724 != null;
      if (mc.field_1724 != null) {
         this.lastYaw = mc.field_1724.method_36454();
         this.lastPitch = mc.field_1724.method_36455();
      } else {
         this.lastYaw = 0.0F;
         this.lastPitch = 0.0F;
      }
   }

   public void onAttack() {
   }

   @Override
   public void updateRotations(class_1309 target) {
      if (mc.field_1724 != null && target != null) {
         if (mc.field_1724.method_6039()) {
            this.rotate = new class_241(mc.field_1724.method_36454(), mc.field_1724.method_36455());
            this.lastYaw = this.rotate.field_1343;
            this.lastPitch = this.rotate.field_1342;
         } else {
            if (!this.initialized) {
               this.lastYaw = mc.field_1724.method_36454();
               this.lastPitch = mc.field_1724.method_36455();
               this.initialized = true;
            }

            if (this.trackedTarget != target) {
               this.trackedTarget = target;
               this.speedAcceleration = 0.0F;
               this.back = false;
               this.tickCounter = 0;
            }

            this.tickCounter++;
            this.jitterOffset = (float)((Math.sin(this.tickCounter * 0.17) * 0.12 + (Math.random() * 0.08 - 0.04)) * 0.7F);
            class_243 point = BestPoint.getMultipoint(target, 128.0);
            class_241 angle = RotationUtils.getRotations(point);
            float targetYaw = angle.field_1343;
            float targetPitch = angle.field_1342;
            float yawDiff = Math.abs(class_3532.method_15393(targetYaw - this.lastYaw));
            boolean readyToAttack = mc.field_1724.method_7261(1.0F) > 0.9F && this.aura.getWhiteRiseTicksToAttack() <= 1;
            if (!this.back) {
               float gain = 0.0055F;
               if (yawDiff > 60.0F) {
                  gain += 0.028800001F;
               } else if (yawDiff > 30.0F) {
                  gain += 0.014400001F;
               } else {
                  gain += 0.0072000003F;
               }

               if (readyToAttack) {
                  gain += 0.012857143F;
               }

               this.speedAcceleration = this.speedAcceleration + gain * (1.6F + this.jitterOffset);
               if (this.speedAcceleration >= 0.22F) {
                  this.back = true;
               }
            } else {
               float loss = readyToAttack ? 0.045F : 0.008F;
               this.speedAcceleration = this.speedAcceleration - loss * (2.1F + this.jitterOffset);
               if (this.speedAcceleration <= -0.04F) {
                  this.back = false;
               }
            }

            float smooth = class_3532.method_15363(this.speedAcceleration, 0.0F, mc.field_1724.method_6128() ? 0.38F : 0.26F);
            if (readyToAttack) {
               smooth = Math.min(smooth + 0.1F, mc.field_1724.method_6128() ? 0.46F : 0.34F);
            }

            smooth += this.jitterOffset * 0.5F;
            if (this.tickCounter % 7 == 0) {
               smooth += 0.03F;
            }

            float deltaYaw = class_3532.method_15393(targetYaw - this.lastYaw);
            float deltaPitch = targetPitch - this.lastPitch;
            float yawLimit = mc.field_1724.method_6128() ? 42.0F : (readyToAttack ? 28.0F : 20.0F);
            float pitchLimit = mc.field_1724.method_6128() ? 12.0F : (readyToAttack ? 4.5F : 2.8F);
            deltaYaw = class_3532.method_15363(deltaYaw, -yawLimit, yawLimit);
            deltaPitch = class_3532.method_15363(deltaPitch, -pitchLimit, pitchLimit);
            float pitchSpeed = smooth * 0.28F;
            float yawSpeed = smooth * (0.85F + this.jitterOffset * 0.4F);
            float newYaw = this.lastYaw + deltaYaw * yawSpeed;
            float newPitch = this.lastPitch + deltaPitch * pitchSpeed;
            float gcd = GCDUtil.getGCDValue();
            if (gcd > 0.0F) {
               newYaw = this.lastYaw + Math.round((newYaw - this.lastYaw) / gcd) * gcd;
               newPitch = this.lastPitch + Math.round((newPitch - this.lastPitch) / gcd) * gcd;
            }

            newPitch = class_3532.method_15363(newPitch, -89.0F, 89.0F);
            Rotation finalRot = new Rotation(newYaw, newPitch);
            float rotSpeed = mc.field_1724.method_6128() && target.method_6128() ? 360.0F : 45.0F;
            RotationStorage.update(finalRot, rotSpeed, rotSpeed, rotSpeed, rotSpeed, 0, 1, Aura.clientLook.isState());
            this.rotate = new class_241(finalRot.getYaw(), finalRot.getPitch());
            this.lastYaw = finalRot.getYaw();
            this.lastPitch = finalRot.getPitch();
         }
      }
   }

   private class_243 getAimPoint(class_1309 target) {
      class_243 point = BestPoint.getPoint(target);
      if (point == null) {
         point = target.method_5829().method_1005();
      }

      return this.shouldUseElytraPredict(target) ? this.getPredictedPoint(target, point) : point;
   }
}
