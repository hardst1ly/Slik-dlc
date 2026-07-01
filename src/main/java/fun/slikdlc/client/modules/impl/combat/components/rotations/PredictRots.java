package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import net.minecraft.class_1309;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class PredictRots extends RotationsSystem implements QClient {
   public PredictRots() {
   }

   public class_241 rotating(class_241 rotation, class_1309 target) {
      class_243 vec = this.calcPointed(target);
      float rawYaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(vec.field_1350, vec.field_1352)) - 90.0);
      float rawPitch = (float)class_3532.method_15338(Math.toDegrees(-Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350))));
      float yawDelta = class_3532.method_15393(rawYaw - rotation.field_1343);
      float pitchDelta = class_3532.method_15393(rawPitch - rotation.field_1342);
      if (Math.abs(yawDelta) > 180.0F) {
         yawDelta -= Math.signum(yawDelta) * 360.0F;
      }

      float additionYaw = class_3532.method_15363(yawDelta, -180.0F, 180.0F);
      float additionPitch = class_3532.method_15363(pitchDelta, -90.0F, 90.0F);
      float yaw = rotation.field_1343 + additionYaw;
      float pitch = rotation.field_1342 + additionPitch;
      float yawFinal = GCDUtil.getFixedRotation(yaw);
      float pitchFinal = GCDUtil.getFixedRotation(pitch);
      return new class_241(yawFinal, pitchFinal);
   }

   private class_243 calcPointed(class_1309 target) {
      if (target != null) {
         class_243 vecPosition = this.getPredictedPoint(target, target.method_5829().method_1005());
         return new class_243(
            vecPosition.method_10216() - mc.field_1724.method_23317(),
            vecPosition.method_10214() - mc.field_1724.method_23318(),
            vecPosition.method_10215() - mc.field_1724.method_23321()
         );
      } else {
         return class_243.field_1353;
      }
   }

   @Override
   public void updateRotations(class_1309 entity) {
   }
}
