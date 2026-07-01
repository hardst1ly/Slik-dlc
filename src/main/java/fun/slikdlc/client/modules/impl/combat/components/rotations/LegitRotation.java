package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.api.utils.rotate.RotationUtils;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class LegitRotation extends RotationsSystem implements QClient {
   public LegitRotation() {
   }

   @Override
   public void updateRotations(class_1309 target) {
      class_243 eyePos = mc.field_1724.method_5836(1.0F);
      class_243 lookVec = mc.field_1724.method_5828(1.0F);
      class_243 reachVec = eyePos.method_1019(lookVec.method_1021(999.0));
      class_238 box = this.getPredictedBox(target);
      double shrinkXZ = target.method_6128() ? -0.5 : 0.1F;
      double shrinkY = target.method_6128() ? -0.5 : 0.1F;
      box = new class_238(
         box.field_1323 + box.method_17939() * shrinkXZ / 2.0,
         box.field_1322,
         box.field_1321 + box.method_17941() * shrinkXZ / 2.0,
         box.field_1320 - box.method_17939() * shrinkXZ / 2.0,
         box.field_1325 - box.method_17940() * shrinkY,
         box.field_1324 - box.method_17941() * shrinkXZ / 2.0
      );
      Optional<class_243> hit = box.method_992(eyePos, reachVec);
      boolean inside = box.method_1006(eyePos);
      if (hit.isPresent() || inside) {
         Aura.adjYaw = class_3532.method_15363(Aura.adjYaw - ThreadLocalRandom.current().nextFloat(0.005F, 0.02F), 0.0F, 1.0F);
         Aura.adjPitch = class_3532.method_15363(Aura.adjPitch - ThreadLocalRandom.current().nextFloat(0.005F, 0.02F), 0.0F, 1.0F);
      } else if (mc.field_1724.method_6128()) {
         Aura.adjYaw = class_3532.method_15363(Aura.adjYaw + ThreadLocalRandom.current().nextFloat(5.0E-4F, 0.005F), 0.0F, 1.0F);
         Aura.adjPitch = class_3532.method_15363(Aura.adjPitch + ThreadLocalRandom.current().nextFloat(9.0E-4F, 0.009F), 0.0F, 1.0F);
      } else if (target.method_20232()) {
         Aura.adjYaw = class_3532.method_15363(Aura.adjYaw + ThreadLocalRandom.current().nextFloat(9.0E-5F, 0.009F), 0.0F, 1.0F);
         Aura.adjPitch = class_3532.method_15363(Aura.adjPitch + ThreadLocalRandom.current().nextFloat(9.0E-5F, 9.0E-4F), 0.0F, 1.0F);
      } else {
         Aura.adjYaw = class_3532.method_15363(Aura.adjYaw + ThreadLocalRandom.current().nextFloat(9.0E-5F, 0.009F), 0.0F, 1.0F);
         Aura.adjPitch = class_3532.method_15363(Aura.adjPitch + ThreadLocalRandom.current().nextFloat(9.0E-4F, 0.009F), 0.0F, 1.0F);
      }

      class_241 targetRot = RotationUtils.getRotations(this.getPredictedPoint(target, target.method_30951(1.0F)));
      float currentYaw = mc.field_1724.method_36454();
      float currentPitch = mc.field_1724.method_36455();
      float diffYaw = class_3532.method_15393(targetRot.field_1343 - currentYaw);
      float diffPitch = class_3532.method_15393(targetRot.field_1342 - currentPitch);
      float newYaw = currentYaw + diffYaw * Aura.adjYaw;
      float newPitch = currentPitch + diffPitch * Aura.adjPitch;
      Aura.otvodkaYaw = 0.0F;
      Aura.otvodkaPitch = 0.0F;
      RotationStorage.update(new Rotation(newYaw, newPitch), 360.0F, 360.0F, 40.0F, 35.0F, 1, 1, Aura.clientLook.isState());
   }
}
