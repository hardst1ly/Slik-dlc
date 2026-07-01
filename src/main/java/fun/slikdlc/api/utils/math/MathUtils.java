package fun.slikdlc.api.utils.math;

import fun.slikdlc.api.QClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class MathUtils implements QClient {
   public static FastRandom fastRandomize = new FastRandom();

   public MathUtils() {
   }

   public static double direction(float rotationYaw, double moveForward, double moveStrafing) {
      if (moveForward < 0.0) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0) {
         forward = -0.5F;
      } else if (moveForward > 0.0) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians(rotationYaw);
   }

   public static float randomNew(double min, double max) {
      return min > max ? (float)(fastRandomize.nextFloat() * (min - max) + max) : (float)(fastRandomize.nextFloat() * (max - min) + min);
   }

   public static double getBps(class_1297 player) {
      double dx = player.method_23317() - player.field_6014;
      double dy = player.method_23318() - player.field_6036;
      double dz = player.method_23321() - player.field_5969;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return distance * 20.0;
   }

   public static float calculateBPS() {
      if (mc.field_1724 == null) {
         return 0.0F;
      } else {
         double dx = mc.field_1724.method_23317() - mc.field_1724.field_6014;
         double dy = mc.field_1724.method_23318() - mc.field_1724.field_6036;
         double dz = mc.field_1724.method_23321() - mc.field_1724.field_5969;
         double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         float timerSpeed = 1.0F;
         float bps = (float)(distance * timerSpeed * 20.0);
         return Math.round(bps * 10.0F) / 10.0F;
      }
   }

   public static double getTargetCompensatedSpeed(class_1297 target) {
      double baseSpeed = 1.5;
      if (target == null) {
         return 1.5;
      } else {
         double targetBps = calculateBPS();
         double speedFactor = 0.00342;
         double bonusSpeed = targetBps * 0.00342;
         return 1.5 + bonusSpeed;
      }
   }

   public static float random(float min, float max) {
      SecureRandom secureRandom = new SecureRandom();
      double randA = secureRandom.nextDouble();
      double randB = secureRandom.nextDouble();
      double randC = secureRandom.nextGaussian() * 0.02F;
      double smoothFactor = Math.pow(randA, 1.0 + secureRandom.nextDouble() * 0.7);
      double mixFactor = (randB * 0.8 + 0.1) * (Math.log1p(randA * 3.0) * 0.5 + 0.5);
      return (float)(min + (max - min) * smoothFactor * mixFactor + randC);
   }

   public static double randomBest(double min, double max) {
      return ThreadLocalRandom.current().nextDouble() * (max - min) + min;
   }

   public static boolean isHovered(double x, double y, double width, double height, double mouseX, double mouseY) {
      return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
   }

   public static float interpolate(float prev, float to, float value) {
      return prev + (to - prev) * value;
   }

   public static class_243 interpolate(class_243 end, class_243 start, float multiple) {
      return new class_243(
         interpolate(end.method_10216(), start.method_10216(), (double)multiple),
         interpolate(end.method_10214(), start.method_10214(), (double)multiple),
         interpolate(end.method_10215(), start.method_10215(), (double)multiple)
      );
   }

   public static class_243 interpolate(class_1297 entity, float partialTicks) {
      double posX = class_3532.method_16436(partialTicks, entity.field_6014, entity.method_23317());
      double posY = class_3532.method_16436(partialTicks, entity.field_6036, entity.method_23318());
      double posZ = class_3532.method_16436(partialTicks, entity.field_5969, entity.method_23321());
      return new class_243(posX, posY, posZ);
   }

   public static double interpolate(double current, double old, double scale) {
      return old + (current - old) * scale;
   }

   public static float round(float number) {
      return Math.round(number * 10.0F) / 10.0F;
   }

   public static double round(double num, double increment) {
      double v = Math.round(num / increment) * increment;
      BigDecimal bd = new BigDecimal(v);
      bd = bd.setScale(2, RoundingMode.HALF_UP);
      return bd.doubleValue();
   }

   public static float lerp(float current, float old, float scale) {
      return current + (old - current) * clamp(scale, 0.0F, 1.0F);
   }

   public static float clamp(float value, float min, float max) {
      return value <= min ? min : Math.min(value, max);
   }

   public static double clamp(double min, double max, double n) {
      return Math.max(min, Math.min(max, n));
   }

   public static <T extends Number> T ler1p(T input, T target, double step) {
      double start = input.doubleValue();
      double end = target.doubleValue();
      double result = start + step * (end - start);
      if (input instanceof Integer) {
         return (T)(Object)(int)Math.round(result);
      } else if (input instanceof Double) {
         return (T)(Object)result;
      } else if (input instanceof Float) {
         return (T)(Object)(float)result;
      } else if (input instanceof Long) {
         return (T)(Object)Math.round(result);
      } else if (input instanceof Short) {
         return (T)(Object)(short)Math.round(result);
      } else if (input instanceof Byte) {
         return (T)(Object)(byte)Math.round(result);
      } else {
         throw new IllegalArgumentException("Unsupported type: " + input.getClass().getSimpleName());
      }
   }
}
