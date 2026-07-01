package fun.slikdlc.api.utils.input;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.implement.EventMoveInput;
import fun.slikdlc.api.storages.implement.FreeLookStorage;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.class_10185;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public final class MovingUtil implements QClient {
   public static boolean hasPlayerMovement() {
      return mc.field_1724.field_3913.field_3905 != 0.0F || mc.field_1724.field_3913.field_3907 != 0.0F;
   }

   public static double[] calculateDirection(double distance) {
      float forward = mc.field_1724.field_3913.field_3905;
      float sideways = mc.field_1724.field_3913.field_3907;
      float yaw = mc.field_1724.method_36454();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sinYaw = Math.sin(Math.toRadians(yaw + 90.0F));
      double cosYaw = Math.cos(Math.toRadians(yaw + 90.0F));
      double xMovement = forward * distance * cosYaw + sideways * distance * sinYaw;
      double zMovement = forward * distance * sinYaw - sideways * distance * cosYaw;
      return new double[]{xMovement, zMovement};
   }

   public static double getSpeedSqrt(class_1297 entity) {
      double dx = entity.method_23317() - entity.field_6014;
      double dy = entity.method_23318() - entity.field_6036;
      double dz = entity.method_23321() - entity.field_5969;
      return Math.sqrt(dx * dx + dz * dz + dy * dy);
   }

   public static void setVelocity(double velocity) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.field_1724).method_18800(direction[0], mc.field_1724.method_18798().method_10214(), direction[1]);
   }

   public static void setVelocity(double velocity, double y) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.field_1724).method_18800(direction[0], y, direction[1]);
   }

   public static double getDegreesRelativeToView(class_243 positionRelativeToPlayer, float yaw) {
      float optimalYaw = (float)Math.atan2(-positionRelativeToPlayer.field_1352, positionRelativeToPlayer.field_1350);
      double currentYaw = Math.toRadians(class_3532.method_15393(yaw));
      return Math.toDegrees(class_3532.method_15338(optimalYaw - currentYaw));
   }

   public static class_10185 getDirectionalInputForDegrees(class_10185 input, double dgs, float deadAngle) {
      boolean forwards = input.comp_3159();
      boolean backwards = input.comp_3160();
      boolean left = input.comp_3161();
      boolean right = input.comp_3162();
      if (dgs >= -90.0F + deadAngle && dgs <= 90.0F - deadAngle) {
         forwards = true;
      } else if (dgs < -90.0F - deadAngle || dgs > 90.0F + deadAngle) {
         backwards = true;
      }

      if (dgs >= 0.0F + deadAngle && dgs <= 180.0F - deadAngle) {
         right = true;
      } else if (dgs >= -180.0F + deadAngle && dgs <= 0.0F - deadAngle) {
         left = true;
      }

      return new class_10185(forwards, backwards, left, right, input.comp_3163(), input.comp_3164(), input.comp_3165());
   }

   public static void fixMovementFocus(EventMoveInput event, float yaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      if (forward != 0.0F || strafe != 0.0F) {
         double targetAngle = class_3532.method_15338(Math.toDegrees(direction(yaw, forward, strafe)));
         float bestForward = 0.0F;
         float bestStrafe = 0.0F;
         float smallestDifference = Float.MAX_VALUE;

         for (float testForward = -1.0F; testForward <= 1.0F; testForward++) {
            for (float testStrafe = -1.0F; testStrafe <= 1.0F; testStrafe++) {
               if (testForward != 0.0F || testStrafe != 0.0F) {
                  double testAngle = class_3532.method_15338(Math.toDegrees(direction(yaw, testForward, testStrafe)));
                  float difference = Math.abs(class_3532.method_15393((float)(targetAngle - testAngle)));
                  if (difference < smallestDifference) {
                     smallestDifference = difference;
                     bestForward = testForward;
                     bestStrafe = testStrafe;
                  }
               }
            }
         }

         event.setForward(bestForward);
         event.setStrafe(bestStrafe);
      }
   }

   public static void fixMovementFree(EventMoveInput event) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      double angle = class_3532.method_15338(
         Math.toDegrees(direction(mc.field_1724.method_6128() ? mc.field_1724.method_36454() : FreeLookStorage.getFreeYaw(), forward, strafe))
      );
      if (forward != 0.0F || strafe != 0.0F) {
         float closestForward = 0.0F;
         float closestStrafe = 0.0F;
         float closestDifference = Float.MAX_VALUE;

         for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward++) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe++) {
               if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                  double predictedAngle = class_3532.method_15338(Math.toDegrees(direction(mc.field_1724.method_36454(), predictedForward, predictedStrafe)));
                  double difference = Math.abs(angle - predictedAngle);
                  if (difference < closestDifference) {
                     closestDifference = (float)difference;
                     closestForward = predictedForward;
                     closestStrafe = predictedStrafe;
                  }
               }
            }
         }

         event.setForward(closestForward);
         event.setStrafe(closestStrafe);
      }
   }

   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0F) {
         forward = -0.5F;
      }

      if (moveForward > 0.0F) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians(rotationYaw);
   }

   public static class_10185 getDirectionalInputForDegrees(class_10185 input, double dgs) {
      return getDirectionalInputForDegrees(input, dgs, 20.0F);
   }

   @Generated
   private MovingUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
