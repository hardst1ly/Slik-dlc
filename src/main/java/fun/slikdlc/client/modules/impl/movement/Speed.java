package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.combat.PredictUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import org.jetbrains.annotations.NotNull;

public class Speed extends Module implements QClient {
   public static Speed INSTANCE = new Speed();
   private final FloatSetting speed = new FloatSetting("Скорость", 1.0F, 0.1F, 2.0F, 0.01F);
   private final FloatSetting radius = new FloatSetting("Радиус", 1.0F, 0.01F, 3.0F, 0.1F);
   private final FloatSetting predict = new FloatSetting("Предикт", 1.0F, 0.0F, 5.0F, 0.1F);
   private final BooleanSetting onlyElytra = new BooleanSetting("Только на элитре", false);

   public Speed() {
      super("Speed", "Дополнительное ускорение", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.speed, this.radius, this.predict, this.onlyElytra});
   }

   @EventLink
   private void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.collisionSpeed();
      }
   }

   private void collisionSpeed() {
      Aura aura = ModuleClass.aura;
      if (aura != null && aura.isEnable()) {
         class_1309 target = aura.getTarget();
         if (target != null && target != mc.field_1724) {
            if (!this.onlyElytra.isState() || mc.field_1724.method_6128()) {
               class_238 expandedBox = mc.field_1724.method_5829().method_1014(this.radius.getValue().doubleValue());
               boolean canSpeed = false;
               if (mc.field_1724.method_6128() || target.method_5829().method_994(expandedBox)) {
                  if (mc.field_1724.method_6128()) {
                     class_243 predictedPos = PredictUtils.predict(target, target.method_19538(), this.predict.getValue().intValue());
                     double distanceToPredict = mc.field_1724.method_33571().method_1022(predictedPos);
                     double distanceToTarget = mc.field_1724.method_33571().method_1022(target.method_5829().method_1005());
                     if (distanceToPredict <= 2.5 || distanceToTarget <= 2.5) {
                        canSpeed = true;
                     }
                  } else {
                     canSpeed = true;
                  }
               }

               if (canSpeed) {
                  class_243 newVelocity = this.calculateVelocity(target);
                  mc.field_1724.method_18799(newVelocity);
               }
            }
         }
      }
   }

   @NotNull
   private class_243 calculateVelocity(class_1309 target) {
      class_243 predictedPos = PredictUtils.predict(target, target.method_19538(), this.predict.getValue().intValue());
      double deltaX = predictedPos.field_1352 - mc.field_1724.method_23317();
      double deltaZ = predictedPos.field_1350 - mc.field_1724.method_23321();
      float targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
      double radYaw = Math.toRadians(targetYaw);
      double force = 0.072 * this.speed.getValue().doubleValue();
      class_243 currentVelocity = mc.field_1724.method_18798();
      return new class_243(
         currentVelocity.field_1352 + -Math.sin(radYaw) * force, currentVelocity.field_1351, currentVelocity.field_1350 + Math.cos(radYaw) * force
      );
   }
}
