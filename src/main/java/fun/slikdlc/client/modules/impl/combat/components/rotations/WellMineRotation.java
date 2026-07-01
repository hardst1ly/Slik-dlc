package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class WellMineRotation extends RotationsSystem implements QClient {
   private class_1309 currentTarget;
   private float lastYaw = 0.0F;
   private float lastPitch = 0.0F;
   private float acceleration = 0.0F;
   private boolean isBack = false;
   private double randomOffsetX = 0.0;
   private double randomOffsetY = 0.0;
   private double randomOffsetZ = 0.0;

   public WellMineRotation() {
   }

   public void reset() {
      this.currentTarget = null;
      this.acceleration = 0.0F;
      this.isBack = false;
      this.randomOffsetX = 0.0;
      this.randomOffsetY = 0.0;
      this.randomOffsetZ = 0.0;
      if (mc.field_1724 != null) {
         this.lastYaw = mc.field_1724.method_36454();
         this.lastPitch = mc.field_1724.method_36455();
      } else {
         this.lastYaw = 0.0F;
         this.lastPitch = 0.0F;
      }
   }

   private float getGCDValue() {
      float sensitivity = (float)((Double)mc.field_1690.method_42495().method_41753() * 0.6F + 0.2F);
      return sensitivity * sensitivity * sensitivity * 1.2F;
   }

   private void updateRandomOffset(class_1309 target) {
      class_238 box = target.method_5829();
      double boxWidth = box.field_1320 - box.field_1323;
      double boxHeight = box.field_1325 - box.field_1322;
      double boxDepth = box.field_1324 - box.field_1321;
      this.randomOffsetX = (Math.random() - 0.5) * boxWidth * 0.15;
      this.randomOffsetY = (Math.random() - 0.5) * boxHeight * 0.15;
      this.randomOffsetZ = (Math.random() - 0.5) * boxDepth * 0.15;
   }

   @Override
   public void updateRotations(class_1309 target) {
      if (mc.field_1724 != null && target != null) {
         if (this.currentTarget != target) {
            this.currentTarget = target;
            this.acceleration = 0.0F;
            this.isBack = false;
            this.lastYaw = mc.field_1724.method_36454();
            this.lastPitch = mc.field_1724.method_36455();
            this.updateRandomOffset(target);
         }

         class_238 box = this.getPredictedBox(target);
         class_243 eyePos = mc.field_1724.method_33571();
         class_243 centerPoint = box.method_1005().method_1031(this.randomOffsetX, this.randomOffsetY, this.randomOffsetZ);
         class_243 toTarget = centerPoint.method_1020(eyePos);
         float centerYaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(toTarget.field_1350, toTarget.field_1352)) - 90.0);
         float centerPitch = (float)(-Math.toDegrees(Math.atan2(toTarget.field_1351, Math.hypot(toTarget.field_1352, toTarget.field_1350))));
         boolean bothGliding = mc.field_1724.method_6128() && target.method_6128();
         class_243 lookVec = mc.field_1724.method_5828(1.0F);
         class_243 endVec = eyePos.method_1019(lookVec.method_1021(bothGliding ? 1488.0 : 999.0));
         class_238 shrunkBox = box.method_1014(bothGliding ? 0.0 : -0.5);
         boolean inBox = shrunkBox.method_992(eyePos, endVec).isPresent();
         if (bothGliding) {
            if (this.isBack) {
               if (this.acceleration >= -0.02F) {
                  this.acceleration = this.acceleration - (Math.abs(class_3532.method_15393(centerYaw - this.lastYaw)) > 80.0F ? 0.15F : 0.02F);
               }

               if (this.acceleration <= -0.02F) {
                  this.isBack = false;
                  this.updateRandomOffset(target);
               }
            } else {
               this.acceleration += 0.0105F;
               if (this.acceleration >= 0.305F || inBox) {
                  this.isBack = true;
               }
            }
         } else if (this.isBack) {
            if (this.acceleration >= -0.15F) {
               float slowdownSpeed = Math.abs(class_3532.method_15393(centerYaw - this.lastYaw)) > 80.0F ? 0.1F : 0.01F;
               float var21;
               this.acceleration = this.acceleration - (var21 = slowdownSpeed * (0.9F + (float)Math.random() * 0.2F));
            }

            if (this.acceleration <= -0.15F) {
               this.isBack = false;
               this.updateRandomOffset(target);
            }
         } else {
            float accelSpeed = 0.0082F + ((float)Math.random() * 0.002F - 0.001F);
            this.acceleration += accelSpeed;
            float threshold = 0.184F + ((float)Math.random() * 0.03F - 0.015F);
            if (this.acceleration >= threshold || inBox) {
               this.isBack = true;
            }
         }

         float deltaYaw = class_3532.method_15393(centerYaw - this.lastYaw);
         float deltaPitch = centerPitch - this.lastPitch;
         float smooth = Math.max(this.acceleration, 0.0F);
         float humanYawOffset = (float)(Math.sin(System.currentTimeMillis() * 0.001) * 0.04);
         float humanPitchOffset = (float)(Math.cos(System.currentTimeMillis() * 0.0015) * 0.025);
         if (Math.abs(deltaYaw) > 1.0F || Math.abs(deltaPitch) > 1.0F) {
            humanYawOffset += ((float)Math.random() - 0.5F) * 0.035F;
            humanPitchOffset += ((float)Math.random() - 0.5F) * 0.02F;
         }

         float newYaw = this.lastYaw + deltaYaw * class_3532.method_15363(smooth * 1.12F, 0.0F, 1.0F) + humanYawOffset;
         float newPitch = this.lastPitch + deltaPitch * class_3532.method_15363(smooth / 1.88F, 0.0F, 1.0F) + humanPitchOffset;
         float gcd = this.getGCDValue();
         newYaw -= (newYaw - this.lastYaw) % gcd;
         newPitch -= (newPitch - this.lastPitch) % gcd;
         if (newPitch > 89.0F) {
            newPitch = 89.0F;
         }

         if (newPitch < -89.0F) {
            newPitch = -89.0F;
         }

         this.lastYaw = newYaw;
         this.lastPitch = newPitch;
         RotationStorage.update(new Rotation(newYaw, newPitch), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
      }
   }
}
