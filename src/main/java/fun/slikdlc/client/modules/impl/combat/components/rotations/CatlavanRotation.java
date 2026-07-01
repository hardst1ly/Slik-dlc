package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.combat.RayTraceUtil;
import fun.slikdlc.api.utils.input.MovingUtil;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import fun.slikdlc.api.events.implement.EventMoveInput;
import net.minecraft.class_1309;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class CatlavanRotation extends RotationsSystem {
   private static final long ATTACK_TIMER_PERIOD = 1200L;
   private static final long JITTER_PERIOD = 300L;
   private static final long JITTER_HALF_PERIOD = 150L;
   private static final float COOLDOWN_PARTIAL_TICKS = 1.5F;
   private static final float MIN_COOLDOWN = 0.95F;
   private static final double YAW_OFFSET = 90.0D;

   private class_241 aimRotation = class_241.field_1340;
   private float baseYaw;
   private final TimerUtils attackTimer = new TimerUtils();
   private boolean shouldRotate = false;

   public CatlavanRotation() {
   }

   public void onEnable() {
      if (mc.field_1724 != null) {
         this.aimRotation = new class_241(mc.field_1724.method_36454(), mc.field_1724.method_36455());
         this.baseYaw = mc.field_1724.method_36454();
      }
   }

   public void reset() {
      this.aimRotation = class_241.field_1340;
      this.shouldRotate = false;
      this.attackTimer.reset();
   }

   @Override
   public void updateRotations(class_1309 target) {
      this.updateAimRotation(target);
      RotationStorage.update(
         new Rotation(this.aimRotation.field_1343, this.aimRotation.field_1342),
         360.0F,
         360.0F,
         40.0F,
         35.0F,
         1,
         1,
         Aura.clientLook.isState()
      );
   }

   public boolean isAttackReady() {
      return mc.field_1724 != null && mc.field_1724.method_7261(COOLDOWN_PARTIAL_TICKS) >= MIN_COOLDOWN;
   }

   public boolean tryAttack(class_1309 target, float range) {
      if (target == null || mc.field_1724 == null || mc.field_1761 == null) {
         return false;
      }

      if (!this.isAttackReady()) {
         this.shouldRotate = true;
         return false;
      }

      if (!this.attackTimer.finished(ATTACK_TIMER_PERIOD)) {
         this.shouldRotate = true;
         return false;
      }

      if (RayTraceUtil.isViewEntity(target, this.aimRotation.field_1343, this.aimRotation.field_1342, range, true)) {
         mc.field_1761.method_2918(mc.field_1724, target);
         mc.field_1724.method_6104(net.minecraft.class_1268.field_5808);
         this.attackTimer.reset();
         this.shouldRotate = false;
         return true;
      }

      this.shouldRotate = true;
      return false;
   }

   public void applyMoveCorrection(EventMoveInput event) {
      if (!this.shouldRotate || mc.field_1724 == null) {
         return;
      }

      float yaw = this.baseYaw;
      if (mc.field_1724.field_5976) {
         yaw += MathUtilsRandomRange(-5.0F, 5.0F);
      }

      MovingUtil.fixMovementFocus(event, this.aimRotation.field_1343 - yaw);
   }

   public class_241 getAimRotation() {
      return this.aimRotation;
   }

   public boolean shouldRotate() {
      return this.shouldRotate;
   }

   private void updateAimRotation(class_1309 target) {
      float targetYaw;
      float targetPitch;
      if (target != null) {
         class_243 relative = target.method_19538()
            .method_1031(
               0.0,
               class_3532.method_15363(
                  (float)(mc.field_1724.method_33571().field_1351 - target.method_23318()),
                  0.0F,
                  (float)(target.method_17682() * (mc.field_1724.method_5739(target) / Math.max(0.01F, target.method_5829().method_17940())))
               ),
               0.0
            )
            .method_1020(mc.field_1724.method_33571());
         targetYaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(relative.field_1350, relative.field_1352)) - YAW_OFFSET);
         targetPitch = (float)(-Math.toDegrees(Math.atan2(relative.field_1351, Math.hypot(relative.field_1352, relative.field_1350))));
         float yawStep = class_3532.method_15393(targetYaw - this.aimRotation.field_1343);
         float pitchStep = class_3532.method_15393(targetPitch - this.aimRotation.field_1342);
         targetYaw = this.aimRotation.field_1343 + yawStep;
         targetPitch = class_3532.method_15363(this.aimRotation.field_1342 + pitchStep, -90.0F, 90.0F);
         targetYaw = this.applyGcd(targetYaw, this.aimRotation.field_1343);
         targetPitch = this.applyGcd(targetPitch, this.aimRotation.field_1342);
      } else {
         targetYaw = mc.field_1724.method_36454();
         targetPitch = mc.field_1724.method_36455();
      }

      float jitterYaw;
      float jitterPitch;
      if (System.currentTimeMillis() % JITTER_PERIOD > JITTER_HALF_PERIOD) {
         jitterYaw = targetYaw - 15.0F;
         jitterPitch = targetPitch - 5.0F;
      } else {
         jitterYaw = targetYaw + 15.0F;
         jitterPitch = targetPitch + 1.5F;
      }

      float smoothYaw = this.aimRotation.field_1343 + (jitterYaw - this.aimRotation.field_1343) / 1.8F;
      float smoothPitch = class_3532.method_15363(this.aimRotation.field_1342 + (jitterPitch - this.aimRotation.field_1342) / 1.8F, -90.0F, 90.0F);
      smoothYaw = this.applyGcd(smoothYaw, this.aimRotation.field_1343);
      smoothPitch = this.applyGcd(smoothPitch, this.aimRotation.field_1342);
      this.aimRotation = new class_241(smoothYaw, smoothPitch);
   }

   private float applyGcd(float value, float previous) {
      float gcd = GCDUtil.getGCDValue();
      if (gcd <= 0.0F) {
         return value;
      }

      return value - (value - previous) % gcd;
   }

   private static float MathUtilsRandomRange(float min, float max) {
      return min + (float)Math.random() * (max - min);
   }
}
