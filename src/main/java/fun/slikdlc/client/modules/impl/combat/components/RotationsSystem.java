package fun.slikdlc.client.modules.impl.combat.components;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.combat.PredictUtils;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;

public abstract class RotationsSystem implements QClient {
   public class_241 rotate = class_241.field_1340;

   public RotationsSystem() {
   }

   public abstract void updateRotations(class_1309 var1);

   public static class_241 correctRotation(float yaw, float pitch) {
      if ((yaw != -90.0F || pitch != 90.0F) && yaw != -180.0F) {
         float gcd = GCDUtil.getGCD();
         yaw -= yaw % gcd;
         pitch -= pitch % gcd;
         return new class_241(yaw, pitch);
      } else {
         return new class_241(mc.field_1724.method_36454(), mc.field_1724.method_36455());
      }
   }

   protected boolean shouldUseElytraPredict(class_1309 target) {
      return mc.field_1724 != null
         && target != null
         && mc.field_1724.method_6128()
         && target.method_6128()
         && ModuleClass.elytraTarget != null
         && ModuleClass.elytraTarget.isEnable();
   }

   protected int getElytraPredictTicks() {
      return ModuleClass.elytraTarget == null ? 0 : Math.max(0, ModuleClass.elytraTarget.forward.getValue().intValue());
   }

   protected class_243 getPredictedPoint(class_1309 target, class_243 point) {
      return !this.shouldUseElytraPredict(target) ? point : PredictUtils.bypasselytrahacking(target);
   }

   protected class_238 getPredictedBox(class_1309 target) {
      class_238 box = target.method_5829();
      if (!this.shouldUseElytraPredict(target)) {
         return box;
      } else {
         class_243 currentCenter = box.method_1005();
         class_243 predictedCenter = this.getPredictedPoint(target, currentCenter);
         return box.method_997(predictedCenter.method_1020(currentCenter));
      }
   }
}
