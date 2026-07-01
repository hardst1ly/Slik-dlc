package fun.slikdlc.client.modules.impl.combat.components.interpolation;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.combat.RayTraceUtil;
import fun.slikdlc.api.utils.math.MathUtils;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.api.utils.rotate.RotationUtils;
import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_3960;

public final class BestPoint implements QClient {
   private static class_243 rotationPoint = class_243.field_1353;
   private static class_243 rotationMotion = class_243.field_1353;

   public static class_243 getRotationPoint() {
      return rotationPoint;
   }

   public static class_243 getNearestPoint(class_1297 entity) {
      class_238 box = entity.method_5829();
      double step = 0.1;
      class_243 bestVec = null;
      double closestDistance = Double.MAX_VALUE;

      for (double x = box.field_1323; x <= box.field_1320; x += step) {
         for (double y = box.field_1322; y <= box.field_1325; y += step) {
            for (double z = box.field_1321; z <= box.field_1324; z += step) {
               class_243 sample = new class_243(x, y, z);
               double dist = mc.field_1724.method_33571().method_1022(sample);
               if (dist < closestDistance) {
                  closestDistance = dist;
                  bestVec = sample;
               }
            }
         }
      }

      return bestVec;
   }

   public static class_243 getPoint(class_1297 target) {
      class_238 box = target.method_5829();
      double width = box.field_1320 - box.field_1323;
      double height = box.field_1325 - box.field_1322;
      double depth = box.field_1324 - box.field_1321;
      double baseX = box.field_1323 + width / 2.0;
      double baseY = box.field_1322 + height * 0.7;
      double baseZ = box.field_1321 + depth / 2.0;
      double time = System.currentTimeMillis() / 50.0;
      int id = target.method_5628();
      double offsetX = Math.sin(time + id) * (width * 0.45);
      double offsetY = Math.cos(time * 0.8 + id) * (height * 0.1);
      double offsetZ = Math.cos(time * 1.2 + id) * (depth * 0.45);
      return new class_243(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static class_243 getPoint2(class_1297 target) {
      class_238 box = target.method_5829();
      double width = box.field_1320 - box.field_1323;
      double height = box.field_1325 - box.field_1322;
      double depth = box.field_1324 - box.field_1321;
      double baseX = box.field_1323 + width / 2.0;
      double baseY = box.field_1322 + height * 0.65;
      double baseZ = box.field_1321 + depth / 2.0;
      double time = System.currentTimeMillis() / 65.0;
      int id = target.method_5628();
      double offsetX = Math.sin(time + id) * (width * 0.7);
      double offsetY = Math.cos(time * 0.8 + id) * (height * 0.4);
      double offsetZ = Math.cos(time * 1.2 + id) * (depth * 0.7);
      return new class_243(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static class_243 getNearestVisiblePoint(class_1297 target, class_243 preferredPoint, double range) {
      if (preferredPoint != null && mc.field_1724 != null && mc.field_1687 != null) {
         if (isPointVisible(target, preferredPoint, range)) {
            return preferredPoint;
         } else {
            class_238 box = target.method_5829();
            double step = 0.12;
            class_243 bestPoint = null;
            double bestDistance = Double.MAX_VALUE;

            for (double x = box.field_1323; x <= box.field_1320; x += step) {
               for (double y = box.field_1322; y <= box.field_1325; y += step) {
                  for (double z = box.field_1321; z <= box.field_1324; z += step) {
                     class_243 sample = new class_243(x, y, z);
                     if (isPointVisible(target, sample, range)) {
                        double distanceToCurrent = sample.method_1025(preferredPoint);
                        if (distanceToCurrent < bestDistance) {
                           bestDistance = distanceToCurrent;
                           bestPoint = sample;
                        }
                     }
                  }
               }
            }

            return bestPoint != null ? bestPoint : preferredPoint;
         }
      } else {
         return preferredPoint;
      }
   }

   private static boolean isPointVisible(class_1297 target, class_243 point, double range) {
      class_243 eyePos = mc.field_1724.method_33571();
      double distance = eyePos.method_1022(point);
      if (distance > range) {
         return false;
      } else {
         class_243 direction = point.method_1020(eyePos).method_1029();
         if (!RayTraceUtil.rayTrace(direction, distance + 0.2, target.method_5829())) {
            return false;
         } else {
            class_3965 blockHit = RayTraceUtil.raycast(eyePos, point, class_3960.field_17558, mc.field_1724);
            return blockHit.method_17783() == class_240.field_1333 || eyePos.method_1025(blockHit.method_17784()) >= eyePos.method_1025(point) - 1.0E-4;
         }
      }
   }

   public static class_243 getMultipoint(class_1297 target, double distance) {
      float minMotionXZ = 0.005F;
      float maxMotionXZ = 0.015F;
      float minMotionY = 0.0015F;
      float maxMotionY = 0.015F;
      double lenghtX = target.method_5829().method_17939();
      double lenghtY = target.method_5829().method_17940();
      double lenghtZ = target.method_5829().method_17941();
      if (rotationMotion.equals(class_243.field_1353)) {
         rotationMotion = new class_243(MathUtils.randomBest(-0.02F, 0.02F), MathUtils.randomBest(-0.02F, 0.02F), MathUtils.randomBest(-0.02F, 0.02F));
      }

      if (rotationPoint.equals(class_243.field_1353)) {
         rotationPoint = new class_243(0.0, lenghtY * 0.5, 0.0);
      }

      rotationPoint = rotationPoint.method_1019(rotationMotion);
      double safeX = (lenghtX - 0.1) / 2.0;
      double safeZ = (lenghtZ - 0.1) / 2.0;
      if (rotationPoint.field_1352 >= safeX) {
         rotationMotion = new class_243(-MathUtils.randomBest(minMotionXZ, maxMotionXZ), rotationMotion.method_10214(), rotationMotion.method_10215());
      } else if (rotationPoint.field_1352 <= -safeX) {
         rotationMotion = new class_243(MathUtils.randomBest(minMotionXZ, maxMotionXZ), rotationMotion.method_10214(), rotationMotion.method_10215());
      }

      if (rotationPoint.field_1351 >= lenghtY * 0.75) {
         rotationMotion = new class_243(rotationMotion.method_10216(), -MathUtils.randomBest(minMotionY, maxMotionY), rotationMotion.method_10215());
      } else if (rotationPoint.field_1351 <= lenghtY * 0.3) {
         rotationMotion = new class_243(rotationMotion.method_10216(), MathUtils.randomBest(minMotionY, maxMotionY), rotationMotion.method_10215());
      }

      if (rotationPoint.field_1350 >= safeZ) {
         rotationMotion = new class_243(rotationMotion.method_10216(), rotationMotion.method_10214(), -MathUtils.randomBest(minMotionXZ, maxMotionXZ));
      } else if (rotationPoint.field_1350 <= -safeZ) {
         rotationMotion = new class_243(rotationMotion.method_10216(), rotationMotion.method_10214(), MathUtils.randomBest(minMotionXZ, maxMotionXZ));
      }

      rotationPoint.method_1031(MathUtils.randomBest(-0.05F, 0.05F), 0.0, MathUtils.randomBest(-0.05F, 0.05F));
      if (!RayTraceUtil.rayTrace(mc.field_1724.method_5720(), distance, target.method_5829())) {
         float halfBox = (float)(lenghtX / 2.0) * 0.8F;

         for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.1F) {
            for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.1F) {
               for (float y1 = (float)(lenghtY * 0.9); y1 >= lenghtY * 0.3; y1 -= 0.1F) {
                  class_243 v1 = new class_243(target.method_23317() + x1, target.method_23318() + y1, target.method_23321() + z1);
                  Rotation rotation = RotationUtils.fromVec3d(v1);
                  if (RayTraceUtil.rayTrace(rotation.toVector(), distance, target.method_5829())) {
                     rotationPoint = new class_243(x1, y1, z1);
                     return target.method_19538().method_1019(rotationPoint);
                  }
               }
            }
         }
      }

      return target.method_19538().method_1019(rotationPoint);
   }

   @Generated
   private BestPoint() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
