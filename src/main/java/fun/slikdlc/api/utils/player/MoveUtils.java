package fun.slikdlc.api.utils.player;

import fun.slikdlc.api.utils.input.MovingUtil;
import java.util.Objects;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;

public class MoveUtils {
   private static final class_310 mc = class_310.method_1551();

   public MoveUtils() {
   }

   public static void setMotion(double motion) {
      if (mc.field_1724 != null) {
         double forward = mc.field_1724.field_3913.field_3905;
         double strafe = mc.field_1724.field_3913.field_3907;
         float yaw = mc.field_1724.method_36454();
         if (forward == 0.0 && strafe == 0.0) {
            mc.field_1724.method_18800(0.0, mc.field_1724.method_18798().field_1351, 0.0);
         } else {
            if (forward != 0.0) {
               if (strafe > 0.0) {
                  yaw += forward > 0.0 ? -45 : 45;
               } else if (strafe < 0.0) {
                  yaw += forward > 0.0 ? 45 : -45;
               }

               strafe = 0.0;
               if (forward > 0.0) {
                  forward = 1.0;
               } else if (forward < 0.0) {
                  forward = -1.0;
               }
            }

            double motionX = forward * motion * class_3532.method_15362((float)Math.toRadians(yaw + 90.0F))
               + strafe * motion * class_3532.method_15374((float)Math.toRadians(yaw + 90.0F));
            double motionZ = forward * motion * class_3532.method_15374((float)Math.toRadians(yaw + 90.0F))
               - strafe * motion * class_3532.method_15362((float)Math.toRadians(yaw + 90.0F));
            mc.field_1724.method_18800(motionX, mc.field_1724.method_18798().field_1351, motionZ);
         }
      }
   }

   public static double getSpeed() {
      if (mc.field_1724 == null) {
         return 0.0;
      } else {
         class_243 velocity = mc.field_1724.method_18798();
         return Math.sqrt(velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350);
      }
   }

   public static void setVelocity(double velocity) {
      double[] direction = MovingUtil.calculateDirection(velocity);
      Objects.requireNonNull(mc.field_1724).method_18800(direction[0], mc.field_1724.method_18798().method_10214(), direction[1]);
   }

   public static void setVelocity(double velocity, double y) {
      double[] direction = MovingUtil.calculateDirection(velocity);
      Objects.requireNonNull(mc.field_1724).method_18800(direction[0], y, direction[1]);
   }

   public static void strafe() {
      strafe(getSpeed());
   }

   public static void strafe(double speed) {
      if (mc.field_1724 != null) {
         float yaw = mc.field_1724.method_36454();
         double forward = mc.field_1724.field_3913.field_3905;
         double strafe = mc.field_1724.field_3913.field_3907;
         if (forward == 0.0 && strafe == 0.0) {
            mc.field_1724.method_18800(0.0, mc.field_1724.method_18798().field_1351, 0.0);
         } else {
            if (forward != 0.0) {
               if (strafe > 0.0) {
                  yaw += forward > 0.0 ? -45.0F : 45.0F;
               } else if (strafe < 0.0) {
                  yaw += forward > 0.0 ? 45.0F : -45.0F;
               }

               strafe = 0.0;
               forward = forward > 0.0 ? 1.0 : -1.0;
            }

            double rad = Math.toRadians(yaw + 90.0F);
            double motionX = forward * speed * Math.cos(rad) + strafe * speed * Math.sin(rad);
            double motionZ = forward * speed * Math.sin(rad) - strafe * speed * Math.cos(rad);
            mc.field_1724.method_18800(motionX, mc.field_1724.method_18798().field_1351, motionZ);
         }
      }
   }

   public static boolean isMoving() {
      return mc.field_1724 == null ? false : mc.field_1724.field_3913.field_3905 != 0.0F || mc.field_1724.field_3913.field_3907 != 0.0F;
   }
}
