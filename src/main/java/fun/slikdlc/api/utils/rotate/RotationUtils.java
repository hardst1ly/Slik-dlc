package fun.slikdlc.api.utils.rotate;

import fun.slikdlc.api.QClient;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.jetbrains.annotations.NotNull;

public final class RotationUtils implements QClient {
   public static class_239 rayTrace(double dst, float yaw, float pitch) {
      class_243 vec3d = mc.field_1724.method_5836(1.0F);
      class_243 vec3d2 = getRotationVector(pitch, yaw);
      class_243 vec3d3 = vec3d.method_1031(vec3d2.field_1352 * dst, vec3d2.field_1351 * dst, vec3d2.field_1350 * dst);
      return mc.field_1687.method_17742(new class_3959(vec3d, vec3d3, class_3960.field_17559, class_242.field_1348, mc.field_1724));
   }

   static class_243 getBestVector(class_1297 entity) {
      class_243 eyePos = mc.field_1724.method_33571();
      class_238 box = entity.method_5829();
      double step = 0.1;
      class_243 bestVec = null;
      double closestDistance = Double.MAX_VALUE;

      for (double x = box.field_1323; x <= box.field_1320; x += step) {
         for (double y = box.field_1322; y <= box.field_1325; y += step) {
            for (double z = box.field_1321; z <= box.field_1324; z += step) {
               class_243 sample = new class_243(x, y, z);
               double dist = eyePos.method_1022(sample);
               if (dist < closestDistance) {
                  closestDistance = dist;
                  bestVec = sample;
               }
            }
         }
      }

      return bestVec;
   }

   public static Rotation fromVec3d(class_243 vector) {
      return new Rotation(
         (float)class_3532.method_15338(Math.toDegrees(Math.atan2(vector.field_1350, vector.field_1352)) - 90.0),
         (float)class_3532.method_15338(Math.toDegrees(-Math.atan2(vector.field_1351, Math.hypot(vector.field_1352, vector.field_1350))))
      );
   }

   @NotNull
   public static class_243 getRotationVector(float yaw, float pitch) {
      return new class_243(
         class_3532.method_15374(-pitch * (float) (Math.PI / 180.0)) * class_3532.method_15362(yaw * (float) (Math.PI / 180.0)),
         -class_3532.method_15374(yaw * (float) (Math.PI / 180.0)),
         class_3532.method_15362(-pitch * (float) (Math.PI / 180.0)) * class_3532.method_15362(yaw * (float) (Math.PI / 180.0))
      );
   }

   public static class_241 getRotations(class_1297 entity) {
      return getRotations(entity.method_23317(), entity.method_23318(), entity.method_23321());
   }

   public static class_241 getRotations(class_243 vec3d) {
      return getRotations(vec3d.field_1352, vec3d.field_1351, vec3d.field_1350);
   }

   public static class_241 getRotations(double x, double y, double z) {
      double deltaX = x - mc.field_1724.method_23317();
      double deltaY = y - mc.field_1724.method_23320();
      double deltaZ = z - mc.field_1724.method_23321();
      double distance = class_3532.method_15355((float)(deltaX * deltaX + deltaZ * deltaZ));
      float yaw = (float)(class_3532.method_15349(deltaZ, deltaX) * (180.0 / Math.PI) - 90.0);
      float pitch = (float)(-class_3532.method_15349(deltaY, distance) * (180.0 / Math.PI));
      return new class_241(yaw, pitch);
   }

   public static float[] getRotations(class_2350 direction) {
      return switch (direction) {
         case field_11033 -> new float[]{mc.field_1724.method_36454(), 90.0F};
         case field_11036 -> new float[]{mc.field_1724.method_36454(), -90.0F};
         case field_11043 -> new float[]{180.0F, mc.field_1724.method_36455()};
         case field_11035 -> new float[]{0.0F, mc.field_1724.method_36455()};
         case field_11039 -> new float[]{90.0F, mc.field_1724.method_36455()};
         case field_11034 -> new float[]{-90.0F, mc.field_1724.method_36455()};
         default -> throw new MatchException(null, null);
      };
   }

   public static float[] correctRotation(float[] rotations) {
      rotations[0] -= rotations[0] % GCDUtil.getGCDValue();
      rotations[1] -= rotations[1] % GCDUtil.getGCDValue();
      return new float[]{rotations[0], rotations[1]};
   }

   public static float getFixRotate(float rot) {
      return getDeltaMouse(rot) * GCDUtil.getGCDValue();
   }

   public static float getDeltaMouse(float delta) {
      return Math.round(delta / GCDUtil.getGCDValue());
   }

   @Generated
   private RotationUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
