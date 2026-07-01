package fun.slikdlc.api.utils.player;

import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;

public final class BoostUtils {
   private static final class_310 mc = class_310.method_1551();
   private static final float BASE_HORIZONTAL = 1.61F;
   private static final float BASE_VERTICAL = 1.5F;
   private static final float[] YAW_TABLE = new float[]{
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.62F,
      1.62F,
      1.62F,
      1.63F,
      1.63F,
      1.64F,
      1.65F,
      1.65F,
      1.66F,
      1.67F,
      1.68F,
      1.69F,
      1.7F,
      1.71F,
      1.72F,
      1.73F,
      1.73F,
      1.75F,
      1.76F,
      1.78F,
      1.79F,
      1.81F,
      1.83F,
      1.85F,
      1.87F,
      1.89F,
      1.91F,
      1.93F,
      1.95F,
      1.98F,
      2.01F,
      2.03F,
      2.06F,
      2.09F,
      2.12F,
      2.16F,
      2.19F,
      2.23F,
      2.27F,
      2.31F,
      2.35F,
      2.31F,
      2.27F,
      2.23F,
      2.19F,
      2.16F,
      2.12F,
      2.09F,
      2.06F,
      2.03F,
      2.01F,
      1.98F,
      1.95F,
      1.93F,
      1.89F,
      1.87F,
      1.85F,
      1.83F,
      1.81F,
      1.79F,
      1.78F,
      1.76F,
      1.75F,
      1.73F,
      1.72F,
      1.71F,
      1.7F,
      1.69F,
      1.68F,
      1.67F,
      1.66F,
      1.65F,
      1.64F,
      1.63F,
      1.63F,
      1.63F,
      1.62F,
      1.62F,
      1.62F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F
   };
   private static final float[] PITCH_TABLE = new float[]{
      1.61F,
      1.61F,
      1.61F,
      1.62F,
      1.62F,
      1.62F,
      1.63F,
      1.63F,
      1.64F,
      1.65F,
      1.65F,
      1.66F,
      1.67F,
      1.68F,
      1.69F,
      1.7F,
      1.71F,
      1.72F,
      1.73F,
      1.73F,
      1.75F,
      1.76F,
      1.78F,
      1.79F,
      1.81F,
      1.83F,
      1.85F,
      1.87F,
      1.89F,
      1.91F,
      1.93F,
      1.95F,
      1.98F,
      2.01F,
      2.03F,
      2.06F,
      2.09F,
      2.12F,
      2.16F,
      2.19F,
      2.23F,
      2.24F,
      2.21F,
      2.21F,
      2.21F,
      2.23F,
      2.23F,
      2.19F,
      2.16F,
      2.12F,
      2.09F,
      2.06F,
      2.03F,
      2.01F,
      1.98F,
      1.95F,
      1.93F,
      1.89F,
      1.87F,
      1.85F,
      1.83F,
      1.81F,
      1.79F,
      1.78F,
      1.76F,
      1.75F,
      1.73F,
      1.72F,
      1.71F,
      1.7F,
      1.69F,
      1.68F,
      1.67F,
      1.66F,
      1.65F,
      1.64F,
      1.63F,
      1.63F,
      1.63F,
      1.62F,
      1.62F,
      1.62F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F,
      1.61F
   };

   public static class_243 getBoost(class_1309 entity) {
      float speed = getRageSpeed(entity);
      class_243 vec3d = entity.method_5720();
      class_243 oldVelocity = class_243.method_1030(entity.method_36455(), entity.method_36454()).method_1021(speed);
      float f = entity.method_36455() * (float) (Math.PI / 180.0);
      double d = Math.sqrt(vec3d.field_1352 * vec3d.field_1352 + vec3d.field_1350 * vec3d.field_1350);
      double e = oldVelocity.method_37267();
      boolean bl = entity.method_18798().field_1351 <= 0.0;
      double g = bl && entity.method_6059(class_1294.field_5906) ? Math.min(entity.method_56989(), 0.01) : entity.method_56989();
      double h = class_3532.method_33723(Math.cos(f));
      oldVelocity = oldVelocity.method_1031(0.0, g * (-1.0 + h * 0.75), 0.0);
      if (oldVelocity.field_1351 < 0.0 && d > 0.0) {
         double i = oldVelocity.field_1351 * -0.1 * h;
         oldVelocity = oldVelocity.method_1031(vec3d.field_1352 * i / d, i, vec3d.field_1350 * i / d);
      }

      if (f < 0.0F && d > 0.0) {
         double i = e * -class_3532.method_15374(f) * 0.04;
         oldVelocity = oldVelocity.method_1031(-vec3d.field_1352 * i / d, i * 3.2, -vec3d.field_1350 * i / d);
      }

      if (d > 0.0) {
         oldVelocity = oldVelocity.method_1031(
            (vec3d.field_1352 / d * e - oldVelocity.field_1352) * 0.1, 0.0, (vec3d.field_1350 / d * e - oldVelocity.field_1350) * 0.1
         );
      }

      double length = oldVelocity.method_1033();
      return new class_243(length, length, length).method_18805(0.99, 0.98, 0.99);
   }

   private static float getRageSpeed(class_1309 entity) {
      float yawAbs = Math.abs(class_3532.method_15393(entity.method_36454()));
      float yawFolded = foldYaw(yawAbs);
      float pitchAbs = Math.abs(clampPitch(entity.method_36455()));
      if (pitchAbs >= 70.0F && pitchAbs <= 90.0F) {
         return 1.615F;
      } else {
         float yawSpeed = YAW_TABLE[Math.min((int)Math.ceil(yawFolded), 90)];
         int pitchIndex = Math.min((int)Math.ceil(pitchAbs), PITCH_TABLE.length - 1);
         float pitchSpeed = PITCH_TABLE[pitchIndex];
         float speed = pitchAbs >= 75.0F ? pitchSpeed : Math.max(yawSpeed, pitchSpeed);
         return Math.max(speed, pitchAbs >= 75.0F ? 1.5F : 1.61F);
      }
   }

   private static float foldYaw(float yawAbs) {
      float folded180 = yawAbs > 180.0F ? 360.0F - yawAbs : yawAbs;
      return folded180 > 90.0F ? 180.0F - folded180 : folded180;
   }

   private static float clampPitch(float pitch) {
      return Math.max(-90.0F, Math.min(90.0F, pitch));
   }

   public static class_243 getBoostAntiTarget(class_1309 entity, float speedSetting) {
      float yaw = Math.abs((entity.method_36454() - 360.0F) % 360.0F);
      float pitch = entity.method_36455();
      float absPitch = Math.abs(pitch);
      float pitchBonus = 0.0F;
      if (absPitch >= 30.0F && absPitch <= 50.0F) {
         pitchBonus = 0.15F;
      } else if (absPitch >= 25.0F && absPitch <= 55.0F) {
         pitchBonus = 0.1F;
      } else if (absPitch >= 20.0F && absPitch <= 60.0F) {
         pitchBonus = 0.05F;
      }

      float speed = speedSetting + pitchBonus;
      float[] centers = new float[]{45.0F, 135.0F, 225.0F, 315.0F};
      float minDiff = 9999.0F;

      for (float c : centers) {
         float diff = Math.abs(yaw - c);
         if (diff < minDiff) {
            minDiff = diff;
         }
      }

      if (minDiff < 15.0F) {
         speed += 0.1F;
      } else if (minDiff < 25.0F) {
         speed += 0.05F;
      }

      speed = Math.min(speed, 2.8F);
      return new class_243(speed, speed, speed);
   }

   public static class_243 getBoostAntiTargetFast(class_1309 entity) {
      float yaw = Math.abs((entity.method_36454() - 360.0F) % 360.0F);
      float pitch = entity.method_36455();
      float absPitch = Math.abs(pitch);
      float speedXZ = 2.5F;
      float speedY = 2.3F;
      if (absPitch >= 35.0F && absPitch <= 50.0F) {
         speedXZ = 2.7F;
         speedY = 2.5F;
      } else if (absPitch >= 30.0F && absPitch <= 55.0F) {
         speedXZ = 2.6F;
         speedY = 2.4F;
      }

      float[] centers = new float[]{45.0F, 135.0F, 225.0F, 315.0F};
      float minDiff = 9999.0F;

      for (float c : centers) {
         float diff = Math.abs(yaw - c);
         if (diff < minDiff) {
            minDiff = diff;
         }
      }

      if (minDiff < 20.0F) {
         speedXZ += 0.15F;
      }

      return new class_243(speedXZ, speedY, speedXZ);
   }

   public static class_243 getBoostAntiTargetWithAura(class_1309 entity, float auraRotatePitch, float auraRotateYaw, float speedSetting) {
      float absPitch = Math.abs(auraRotatePitch);
      float speedXZ = speedSetting;
      float speedY;
      if (absPitch >= 38.0F && absPitch <= 52.0F) {
         speedXZ = Math.min(speedSetting + 0.2F, 2.7F);
         speedY = Math.min(speedSetting + 0.15F, 2.5F);
      } else if (absPitch >= 30.0F && absPitch <= 60.0F) {
         speedXZ = Math.min(speedSetting + 0.1F, 2.6F);
         speedY = Math.min(speedSetting + 0.1F, 2.4F);
      } else if (absPitch >= 25.0F && absPitch <= 65.0F) {
         speedY = speedSetting - 0.05F;
      } else {
         speedXZ = speedSetting - 0.1F;
         speedY = speedSetting - 0.15F;
      }

      return new class_243(speedXZ, speedY, speedXZ);
   }

   public static class_243 getBoostslime(class_1309 entity) {
      return getBoostCustom(entity, 42.0F);
   }

   public static class_243 getBoostbravo(class_1309 entity) {
      return getBoostCustom(entity, 39.0F);
   }

   public static class_243 getBoostrw(class_1309 entity) {
      return getBoostCustom(entity, 33.2F);
   }

   public static class_243 getBoostCustom(class_1309 entity, float targetBps) {
      float maxSpeed = targetBps / 20.0F;
      float yaw = Math.abs((entity.method_36454() - 360.0F) % 360.0F);
      float pitch = entity.method_36455();
      float minSpeed = Math.min(maxSpeed * 0.7F, 1.67F);
      float[] centers = new float[]{45.0F, 135.0F, 225.0F, 315.0F};
      float minDiff = 9999.0F;

      for (float c : centers) {
         float diff = Math.abs(yaw - c);
         if (diff < minDiff) {
            minDiff = diff;
         }
      }

      float yawFactor = 1.0F - minDiff / 45.0F;
      yawFactor = Math.max(0.0F, Math.min(1.0F, yawFactor));
      float pitchFactor = getPitchFactor(pitch);
      float combinedFactor = yawFactor * pitchFactor;
      float speed = minSpeed + (maxSpeed - minSpeed) * combinedFactor;
      class_243 vec3d = entity.method_5720();
      class_243 oldVelocity = class_243.method_1030(pitch, entity.method_36454()).method_1021(speed);
      float f = pitch * (float) (Math.PI / 180.0);
      double d = Math.sqrt(vec3d.field_1352 * vec3d.field_1352 + vec3d.field_1350 * vec3d.field_1350);
      double e = oldVelocity.method_37267();
      boolean bl = entity.method_18798().field_1351 <= 0.0;
      double g = bl && entity.method_6059(class_1294.field_5906) ? Math.min(entity.method_56989(), 0.01) : entity.method_56989();
      double h = class_3532.method_33723(Math.cos(f));
      oldVelocity = oldVelocity.method_1031(0.0, g * (-1.0 + h * 0.75), 0.0);
      if (oldVelocity.field_1351 < 0.0 && d > 0.0) {
         double i = oldVelocity.field_1351 * -0.1 * h;
         oldVelocity = oldVelocity.method_1031(vec3d.field_1352 * i / d, i, vec3d.field_1350 * i / d);
      }

      if (f < 0.0F && d > 0.0) {
         double i = e * -class_3532.method_15374(f) * 0.04;
         oldVelocity = oldVelocity.method_1031(-vec3d.field_1352 * i / d, i * 3.2, -vec3d.field_1350 * i / d);
      }

      if (d > 0.0) {
         oldVelocity = oldVelocity.method_1031(
            (vec3d.field_1352 / d * e - oldVelocity.field_1352) * 0.1, 0.0, (vec3d.field_1350 / d * e - oldVelocity.field_1350) * 0.1
         );
      }

      double length = oldVelocity.method_1033();
      return new class_243(length, length, length).method_18805(0.99, 0.98, 0.99);
   }

   public static class_243 getBoostFixedBps(class_1309 entity, float targetBps) {
      float speed = targetBps / 20.0F;
      return new class_243(speed, speed, speed).method_18805(0.99, 0.98, 0.99);
   }

   private static float getPitchFactor(float pitch) {
      float absPitch = Math.abs(pitch);
      if (absPitch <= 5.0F) {
         return 1.0F;
      } else if (absPitch <= 15.0F) {
         return 0.95F;
      } else if (absPitch <= 25.0F) {
         return 0.85F;
      } else if (absPitch <= 35.0F) {
         return 0.75F;
      } else if (absPitch <= 45.0F) {
         return 0.65F;
      } else if (absPitch <= 55.0F) {
         return 0.55F;
      } else if (absPitch <= 65.0F) {
         return 0.45F;
      } else {
         return absPitch <= 75.0F ? 0.35F : 0.25F;
      }
   }

   private BoostUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
