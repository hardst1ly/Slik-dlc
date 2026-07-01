package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class SlothRotation extends RotationsSystem implements QClient {
   private class_1309 trackedTarget;
   private float currentYaw;
   private float currentPitch;
   private float velocityYaw;
   private float velocityPitch;
   private double aimPointX;
   private double aimPointY;
   private double aimPointZ;
   private float noiseAngle;
   private final float noiseAmplitude = 1.8F;
   private int hitPhase;
   private int hitTimer;
   private float pitchBeforeHit;
   private long firstSeenTime;
   private int reactionMs;
   private boolean reactionComplete;
   private float lastSentYaw;
   private float lastSentPitch;
   private float smoothYaw;
   private float smoothPitch;

   public SlothRotation() {
   }

   public void reset() {
      this.trackedTarget = null;
      this.velocityYaw = this.velocityPitch = 0.0F;
      this.aimPointX = this.aimPointY = this.aimPointZ = 0.0;
      this.noiseAngle = 0.0F;
      this.hitPhase = this.hitTimer = 0;
      this.firstSeenTime = 0L;
      this.reactionComplete = false;
      this.reactionMs = 0;
      if (mc.field_1724 != null) {
         this.currentYaw = mc.field_1724.method_36454();
         this.currentPitch = mc.field_1724.method_36455();
         this.lastSentYaw = this.currentYaw;
         this.lastSentPitch = this.currentPitch;
         this.smoothYaw = this.currentYaw;
         this.smoothPitch = this.currentPitch;
      } else {
         this.currentYaw = this.currentPitch = 0.0F;
         this.lastSentYaw = this.lastSentPitch = 0.0F;
         this.smoothYaw = this.smoothPitch = 0.0F;
      }
   }

   private float calcGcd() {
      double s = (Double)mc.field_1690.method_42495().method_41753() * 0.6 + 0.2;
      return (float)(s * s * s * 1.2);
   }

   private void pickAimPoint(class_1309 e) {
      class_238 bb = e.method_5829();
      double w = bb.field_1320 - bb.field_1323;
      double h = bb.field_1325 - bb.field_1322;
      double d = bb.field_1324 - bb.field_1321;
      this.aimPointX = (Math.random() - 0.5) * w * 0.12;
      this.aimPointY = (Math.random() - 0.5) * h * 0.11;
      this.aimPointZ = (Math.random() - 0.5) * d * 0.12;
   }

   public void onAttack() {
      this.hitPhase = 1;
      this.hitTimer = 0;
      this.pitchBeforeHit = this.currentPitch;
   }

   private float measureAngle(class_1309 e) {
      if (mc.field_1724 == null) {
         return 0.0F;
      } else {
         class_243 eyes = mc.field_1724.method_33571();
         class_243 mid = e.method_5829().method_1005();
         class_243 delta = mid.method_1020(eyes);
         float needYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
         float needPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, delta.method_37267())));
         float dYaw = Math.abs(class_3532.method_15393(needYaw - mc.field_1724.method_36454()));
         float dPitch = Math.abs(needPitch - mc.field_1724.method_36455());
         return dYaw + dPitch;
      }
   }

   private int computeReaction(float angle) {
      if (angle > 130.0F) {
         return 140 + (int)(Math.random() * 90.0);
      } else if (angle > 70.0F) {
         return 90 + (int)(Math.random() * 60.0);
      } else {
         return angle > 30.0F ? 45 + (int)(Math.random() * 35.0) : 12 + (int)(Math.random() * 20.0);
      }
   }

   private boolean isMovingForward() {
      return mc.field_1724 == null ? false : mc.field_1690.field_1894.method_1434();
   }

   private boolean isOvertakingTarget(class_1309 target) {
      if (mc.field_1724 != null && target != null) {
         class_243 playerPos = mc.field_1724.method_19538();
         class_243 targetPos = target.method_19538();
         class_243 playerVel = new class_243(
            mc.field_1724.method_23317() - mc.field_1724.field_6014,
            mc.field_1724.method_23318() - mc.field_1724.field_6036,
            mc.field_1724.method_23321() - mc.field_1724.field_5969
         );
         class_243 targetVel = new class_243(
            target.method_23317() - target.field_6014, target.method_23318() - target.field_6036, target.method_23321() - target.field_5969
         );
         class_243 toTarget = targetPos.method_1020(playerPos).method_1029();
         double playerSpeedToTarget = playerVel.method_1026(toTarget);
         double targetSpeedToPlayer = targetVel.method_1026(toTarget.method_1021(-1.0));
         double relativeSpeed = playerSpeedToTarget + targetSpeedToPlayer;
         double distance = Math.sqrt(Math.pow(playerPos.field_1352 - targetPos.field_1352, 2.0) + Math.pow(playerPos.field_1350 - targetPos.field_1350, 2.0));
         return relativeSpeed > 0.05 && distance < 4.0;
      } else {
         return false;
      }
   }

   private float[] generateNoise(float dist) {
      this.noiseAngle = this.noiseAngle + (0.042F + (float)(Math.random() * 0.018F));
      float scale = class_3532.method_15363(dist / 4.5F, 0.25F, 1.0F);
      float amp = 1.8F * scale;
      float n1 = (float)Math.sin(this.noiseAngle * 0.87) * 0.38F;
      float n2 = (float)Math.sin(this.noiseAngle * 1.43 + 0.75) * 0.28F;
      float n3 = (float)Math.cos(this.noiseAngle * 1.18 + 0.35) * 0.32F;
      float n4 = (float)Math.cos(this.noiseAngle * 1.76 + 1.42) * 0.23F;
      float yawNoise = (n1 + n2) * amp;
      float pitchNoise = (n3 + n4) * amp * 0.52F;
      yawNoise += ((float)Math.random() - 0.5F) * amp * 0.13F;
      pitchNoise += ((float)Math.random() - 0.5F) * amp * 0.09F;
      return new float[]{yawNoise, pitchNoise};
   }

   private float smoothStep(float x) {
      x = class_3532.method_15363(x, 0.0F, 1.0F);
      return x * x * (3.0F - 2.0F * x);
   }

   private float accelCurve(float x) {
      x = class_3532.method_15363(x, 0.0F, 1.0F);
      return 1.0F - (1.0F - x) * (1.0F - x);
   }

   private float springInterp(float current, float target, float vel, float stiffness, float damping) {
      float diff = target - current;
      float acc = diff * stiffness - vel * damping;
      return vel + acc;
   }

   private float smoothLerp(float from, float to, float alpha) {
      alpha = class_3532.method_15363(alpha, 0.0F, 1.0F);
      float delta = class_3532.method_15393(to - from);
      return from + delta * alpha;
   }

   private float calculateCurrentAngle(float targetYaw, float targetPitch) {
      float dYaw = Math.abs(class_3532.method_15393(targetYaw - this.currentYaw));
      float dPitch = Math.abs(targetPitch - this.currentPitch);
      return dYaw + dPitch;
   }

   @Override
   public void updateRotations(class_1309 target) {
      if (mc.field_1724 != null && target != null) {
         boolean playerFlying = mc.field_1724.method_6128();
         if (this.trackedTarget != target) {
            this.trackedTarget = target;
            this.currentYaw = mc.field_1724.method_36454();
            this.currentPitch = mc.field_1724.method_36455();
            this.lastSentYaw = this.currentYaw;
            this.lastSentPitch = this.currentPitch;
            this.smoothYaw = this.currentYaw;
            this.smoothPitch = this.currentPitch;
            this.velocityYaw = this.velocityPitch = 0.0F;
            this.pickAimPoint(target);
            this.hitPhase = this.hitTimer = 0;
            this.noiseAngle = (float)(Math.random() * Math.PI * 2.0);
            float angleDiff = this.measureAngle(target);
            this.reactionMs = this.computeReaction(angleDiff);
            this.firstSeenTime = System.currentTimeMillis();
            this.reactionComplete = false;
         }

         class_243 eyePos = mc.field_1724.method_33571();
         class_243 targetCenter = this.getPredictedPoint(target, target.method_5829().method_1005());
         float distance = (float)eyePos.method_1022(targetCenter);
         float gcd = this.calcGcd();
         if (!this.reactionComplete) {
            long elapsed = System.currentTimeMillis() - this.firstSeenTime;
            if (elapsed < this.reactionMs) {
               float jitterY = ((float)Math.random() - 0.5F) * 0.22F;
               float jitterP = ((float)Math.random() - 0.5F) * 0.14F;
               float outY = this.lastSentYaw + jitterY;
               float outP = class_3532.method_15363(this.lastSentPitch + jitterP, -89.0F, 89.0F);
               outY -= (outY - this.lastSentYaw) % gcd;
               outP -= (outP - this.lastSentPitch) % gcd;
               this.lastSentYaw = outY;
               this.lastSentPitch = outP;
               RotationStorage.update(new Rotation(outY, outP), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
               return;
            }

            this.reactionComplete = true;
         }

         float[] noise = this.generateNoise(distance);
         if (this.hitPhase > 0) {
            this.hitTimer++;
            int upDuration = 25;
            int downDuration = 20;
            float targetPitchUp = -89.0F;
            if (this.hitPhase == 1) {
               float t = (float)this.hitTimer / upDuration;
               t = class_3532.method_15363(t, 0.0F, 1.0F);
               float curved = this.accelCurve(t);
               this.currentPitch = class_3532.method_16439(curved, this.pitchBeforeHit, targetPitchUp);
               if (this.hitTimer >= upDuration) {
                  this.hitPhase = 2;
                  this.hitTimer = 0;
               }
            } else if (this.hitPhase == 2) {
               float goal = this.pitchBeforeHit;
               float t = (float)this.hitTimer / downDuration;
               t = class_3532.method_15363(t, 0.0F, 1.0F);
               float curved = this.smoothStep(t);
               this.currentPitch = class_3532.method_16439(curved, targetPitchUp, goal);
               if (this.hitTimer >= downDuration) {
                  this.hitPhase = 0;
                  this.hitTimer = 0;
               }
            }

            float outY = this.currentYaw + noise[0];
            float outP = class_3532.method_15363(this.currentPitch + noise[1], -89.0F, 89.0F);
            outY -= (outY - this.lastSentYaw) % gcd;
            outP -= (outP - this.lastSentPitch) % gcd;
            this.lastSentYaw = outY;
            this.lastSentPitch = outP;
            RotationStorage.update(new Rotation(outY, outP), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
         } else {
            if (Math.random() < 0.015) {
               this.pickAimPoint(target);
            }

            class_243 targetVel = new class_243(
               target.method_23317() - target.field_6014, target.method_23318() - target.field_6036, target.method_23321() - target.field_5969
            );
            int predictTicks = this.shouldUseElytraPredict(target) ? 0 : 2;
            class_243 predictedCenter = targetCenter.method_1019(targetVel.method_1021(predictTicks));
            class_243 aimPos = predictedCenter.method_1031(this.aimPointX, this.aimPointY, this.aimPointZ);
            class_243 direction = aimPos.method_1020(eyePos);
            float wantYaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(direction.field_1350, direction.field_1352)) - 90.0);
            float wantPitch = (float)(-Math.toDegrees(Math.atan2(direction.field_1351, direction.method_37267())));
            float diffYaw = class_3532.method_15393(wantYaw - this.currentYaw);
            float diffPitch = wantPitch - this.currentPitch;
            float speedMultiplier = 1.0F;
            if (playerFlying) {
               float currentAngle = this.calculateCurrentAngle(wantYaw, wantPitch);
               if (currentAngle > 120.0F) {
                  speedMultiplier = 0.18F;
               } else if (currentAngle > 80.0F) {
                  float t = (currentAngle - 80.0F) / 40.0F;
                  speedMultiplier = class_3532.method_16439(this.smoothStep(t), 0.35F, 0.18F);
               } else if (currentAngle > 25.0F) {
                  float t = (currentAngle - 25.0F) / 55.0F;
                  speedMultiplier = class_3532.method_16439(this.smoothStep(t), 0.65F, 0.35F);
               } else {
                  speedMultiplier = 0.65F + 0.35F * (1.0F - currentAngle / 25.0F);
               }
            } else {
               boolean movingForward = this.isMovingForward();
               boolean overtaking = this.isOvertakingTarget(target);
               if (movingForward || overtaking) {
                  speedMultiplier = 0.5F;
               }
            }

            float stiffness = (0.038F + (float)Math.random() * 0.009F) * speedMultiplier;
            float damping = 0.68F + 0.12F * (1.0F - speedMultiplier);
            float totalDiff = (float)Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
            if (totalDiff > 32.0F) {
               stiffness += 0.018F * speedMultiplier;
            } else if (totalDiff < 4.2F) {
               stiffness *= 0.48F;
            }

            stiffness += class_3532.method_15363((distance - 1.6F) / 7.5F, 0.0F, 0.045F) * speedMultiplier;
            this.velocityYaw = this.springInterp(this.currentYaw, this.currentYaw + diffYaw, this.velocityYaw, stiffness, damping);
            this.velocityPitch = this.springInterp(this.currentPitch, wantPitch, this.velocityPitch, stiffness * 0.87F, damping);
            float maxVelYaw = 7.5F * speedMultiplier;
            float maxVelPitch = 5.8F * speedMultiplier;
            this.velocityYaw = class_3532.method_15363(this.velocityYaw, -maxVelYaw, maxVelYaw);
            this.velocityPitch = class_3532.method_15363(this.velocityPitch, -maxVelPitch, maxVelPitch);
            this.currentYaw = this.currentYaw + this.velocityYaw;
            this.currentPitch = this.currentPitch + this.velocityPitch;
            this.currentPitch = class_3532.method_15363(this.currentPitch, -89.0F, 89.0F);
            float smoothFactor = playerFlying ? 0.3F + speedMultiplier * 0.4F : 0.85F;
            this.smoothYaw = this.smoothLerp(this.smoothYaw, this.currentYaw, smoothFactor);
            this.smoothPitch = this.smoothLerp(this.smoothPitch, this.currentPitch, smoothFactor * 0.95F);
            float outY = this.smoothYaw + noise[0];
            float outP = this.smoothPitch + noise[1];
            outP = class_3532.method_15363(outP, -89.0F, 89.0F);
            outY -= (outY - this.lastSentYaw) % gcd;
            outP -= (outP - this.lastSentPitch) % gcd;
            this.lastSentYaw = outY;
            this.lastSentPitch = outP;
            RotationStorage.update(new Rotation(outY, outP), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
         }
      }
   }
}
