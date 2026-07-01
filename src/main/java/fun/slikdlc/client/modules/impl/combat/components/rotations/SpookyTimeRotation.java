package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import net.minecraft.class_1309;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3966;

public class SpookyTimeRotation extends RotationsSystem implements QClient {
   private float slowPitchTicks = 0.0F;
   private float slowYawTicks = 0.0F;

   public SpookyTimeRotation() {
   }

   public void reset() {
      this.slowPitchTicks = 0.0F;
      this.slowYawTicks = 0.0F;
   }

   private float interpolate(float current, float target, float speed) {
      return current + (target - current) * speed;
   }

   private float randomInRange(float min, float max) {
      return min + (float)Math.random() * (max - min);
   }

   private boolean hasCollisionWith(class_1309 entity) {
      return entity != null && mc.field_1724 != null ? mc.field_1724.method_5829().method_994(entity.method_5829()) : false;
   }

   private boolean isStandingOnBlock(class_1309 entity) {
      class_243 pos = entity.method_19538();
      class_2338 blockPos = class_2338.method_49637(pos.field_1352, pos.field_1351 - 0.1, pos.field_1350);
      return mc.field_1687 != null && !mc.field_1687.method_8320(blockPos).method_26215();
   }

   private boolean isAir(double x, double y, double z) {
      if (mc.field_1687 == null) {
         return true;
      } else {
         class_2338 pos = class_2338.method_49637(x, y, z);
         return mc.field_1687.method_8320(pos).method_26215();
      }
   }

   private boolean stalin(class_1309 target) {
      class_243 pos = target.method_19538();
      class_238 hitbox = target.method_5829();
      float off = 0.05F;
      return !this.isAir(hitbox.field_1323 - off, pos.field_1351, hitbox.field_1321 - off)
         || !this.isAir(hitbox.field_1320 + off, pos.field_1351, hitbox.field_1321 - off)
         || !this.isAir(hitbox.field_1323 - off, pos.field_1351, hitbox.field_1324 + off)
         || !this.isAir(hitbox.field_1320 + off, pos.field_1351, hitbox.field_1324 + off);
   }

   private class_3966 raytraceEntity(float distance, Rotation rotation) {
      if (mc.field_1724 == null) {
         return null;
      } else {
         class_243 start = mc.field_1724.method_33571();
         class_243 direction = rotation.toVector();
         class_243 end = start.method_1019(direction.method_1021(distance));
         class_3966 result = null;
         double closestDistance = distance;
         if (mc.field_1687 != null) {
            for (class_1309 entity : mc.field_1687.method_8390(class_1309.class, mc.field_1724.method_5829().method_1014(distance), e -> e != mc.field_1724)) {
               class_238 box = entity.method_5829();
               if (box.method_992(start, end).isPresent()) {
                  double dist = start.method_1022(box.method_1005());
                  if (dist < closestDistance) {
                     closestDistance = dist;
                     result = new class_3966(entity);
                  }
               }
            }
         }

         return result;
      }
   }

   @Override
   public void updateRotations(class_1309 target) {
      if (mc.field_1724 != null && target != null) {
         class_243 targetPos = target.method_5829().method_1005();
         class_243 playerEye = mc.field_1724.method_33571();
         class_243 delta = targetPos.method_1020(playerEye);
         float targetYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
         float targetPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350))));
         float currentYaw = mc.field_1724.method_36454();
         float currentPitch = mc.field_1724.method_36455();
         float yawDelta = class_3532.method_15393(targetYaw - currentYaw);
         float pitchDelta = class_3532.method_15393(targetPitch - currentPitch);
         float auraDistance = 6.0F;
         float distanceToTarget = (float)mc.field_1724.method_19538().method_1022(target.method_19538());
         float distanceFactor = class_3532.method_15363(0.3F + 0.7F * (distanceToTarget / auraDistance), 0.0F, 1.0F);
         boolean hasTarget = true;
         Rotation currentRotation = new Rotation(currentYaw, currentPitch);
         class_3966 hitResult = this.raytraceEntity(auraDistance, currentRotation);
         boolean hasTrace = hitResult != null && hitResult.method_17782() == target;
         float raytrace = hasTrace ? 0.4F : 1.0F;
         boolean check = hasTarget
            && this.hasCollisionWith(target)
            && (this.stalin(target) || this.isStandingOnBlock(target) && this.isStandingOnBlock(mc.field_1724));
         if (check) {
            yawDelta /= 30.0F;
         }

         float random = this.randomInRange(0.5F, 0.7F);
         float yawSpeed = this.randomInRange(25.0F, 31.0F);
         float pitchSpeed = this.randomInRange(6.0F, 7.5F);
         if (hasTrace) {
            this.slowYawTicks = this.interpolate(this.slowYawTicks, 0.7F, 0.6F);
            this.slowPitchTicks = this.interpolate(this.slowPitchTicks, 0.5F, 0.5F);
         } else {
            this.slowYawTicks = this.interpolate(this.slowYawTicks, 1.0F, 0.9F);
            this.slowPitchTicks = this.interpolate(this.slowPitchTicks, 1.0F, 0.2F);
         }

         yawSpeed *= this.slowYawTicks;
         pitchSpeed *= this.slowPitchTicks;
         yawDelta += (float)(Math.cos(System.currentTimeMillis() / 50.0) * (3.0F * distanceFactor));
         pitchDelta += (float)(Math.sin(System.currentTimeMillis() / 60.0) * (9.0F * distanceFactor));
         float finalYaw = currentYaw + class_3532.method_15363(yawDelta, -yawSpeed, yawSpeed) * random;
         float finalPitch = currentPitch + class_3532.method_15363(pitchDelta, -pitchSpeed, pitchSpeed) * random / yawSpeed * 14.0F * raytrace;
         RotationStorage.update(new Rotation(finalYaw, finalPitch), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
      }
   }
}
