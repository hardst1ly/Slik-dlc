package fun.slikdlc.client.modules.impl.combat.components.rotations;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.api.utils.rotate.RotationUtils;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import java.security.SecureRandom;
import java.util.LinkedList;
import net.minecraft.class_1309;
import net.minecraft.class_241;
import net.minecraft.class_3532;

public class SpookyDuelRotation extends RotationsSystem implements QClient {
   private static final SecureRandom R = new SecureRandom();
   private final float baseSpeed = 25.5F;
   private final float fastSpeed = 44.5F;
   private final LinkedList<Float> yawHist = new LinkedList<>();
   private final LinkedList<Float> pitchHist = new LinkedList<>();
   private final int histSize = 3;
   private long lastHit;
   private long lastPause;
   private long lastBlink;
   private long lastTremor;
   private long lastBreath;
   private long lastMicro;
   private long lastPrediction;
   private int combo;
   private int miss;
   private int focus = 85;
   private float fatigue;
   private float wanderYaw;
   private float wanderPitch;
   private float errorYaw;
   private float errorPitch;
   private float aimDrift;
   private float headNoise;
   private float tremorYaw;
   private float tremorPitch;
   private float breath;
   private float microYaw;
   private float microPitch;
   private float predictedYaw;
   private float predictedPitch;
   private float lastTargetYaw;
   private float lastTargetPitch;
   private final float[] yawVel = new float[3];
   private int velIdx;
   private final float[] bezierBuf = new float[8];
   private int bezierIdx;
   private float avgReaction = 50.0F;
   private final float[] reactionTimes = new float[15];
   private int reactIdx;
   private boolean blinking;
   private long blinkStart;
   private final float[] randCache = new float[64];
   private int randIdx;

   public SpookyDuelRotation() {
      for (int i = 0; i < this.randCache.length; i++) {
         this.randCache[i] = R.nextFloat();
      }

      for (int i = 0; i < this.reactionTimes.length; i++) {
         this.reactionTimes[i] = 40.0F + R.nextFloat() * 30.0F;
      }
   }

   public void reset() {
      this.lastHit = 0L;
      this.lastPause = 0L;
      this.lastBlink = 0L;
      this.lastTremor = 0L;
      this.lastBreath = 0L;
      this.lastMicro = 0L;
      this.lastPrediction = 0L;
      this.combo = 0;
      this.miss = 0;
      this.focus = 85;
      this.fatigue = 0.0F;
      this.wanderYaw = 0.0F;
      this.wanderPitch = 0.0F;
      this.errorYaw = 0.0F;
      this.errorPitch = 0.0F;
      this.aimDrift = 0.0F;
      this.headNoise = 0.0F;
      this.tremorYaw = 0.0F;
      this.tremorPitch = 0.0F;
      this.breath = 0.0F;
      this.microYaw = 0.0F;
      this.microPitch = 0.0F;
      this.predictedYaw = 0.0F;
      this.predictedPitch = 0.0F;
      this.lastTargetYaw = 0.0F;
      this.lastTargetPitch = 0.0F;
      this.velIdx = 0;
      this.bezierIdx = 0;
      this.avgReaction = 50.0F;
      this.reactIdx = 0;
      this.blinking = false;
      this.blinkStart = 0L;
      this.randIdx = 0;
      this.yawHist.clear();
      this.pitchHist.clear();
   }

   private float rnd(float min, float max) {
      this.randIdx = this.randIdx + 1 & 63;
      return min + this.randCache[this.randIdx] * (max - min);
   }

   private float rnd() {
      this.randIdx = this.randIdx + 1 & 63;
      return this.randCache[this.randIdx];
   }

   private float lerp(float t, float a, float b) {
      return a + t * (b - a);
   }

   private float clamp(float v, float min, float max) {
      return Math.max(min, Math.min(max, v));
   }

   private void predict(class_241 target, long time) {
      float dt = (float)Math.min(50L, time - this.lastPrediction) / 1000.0F;
      if (this.lastPrediction > 0L && dt > 0.0F && dt < 0.1F) {
         float yawV = (target.field_1343 - this.lastTargetYaw) / dt;
         float pitchV = (target.field_1342 - this.lastTargetPitch) / dt;
         this.yawVel[this.velIdx] = yawV;
         this.velIdx = (this.velIdx + 1) % 3;
         float avgYawV = (this.yawVel[0] + this.yawVel[1] + this.yawVel[2]) / 3.0F;
         float predTime = 0.04F + this.rnd() * 0.03F;
         this.predictedYaw = target.field_1343 + avgYawV * predTime;
         this.predictedPitch = target.field_1342 + pitchV * predTime;
      } else {
         this.predictedYaw = target.field_1343;
         this.predictedPitch = target.field_1342;
      }

      this.lastTargetYaw = target.field_1343;
      this.lastTargetPitch = target.field_1342;
      this.lastPrediction = time;
   }

   private float bezier(float val) {
      this.bezierBuf[this.bezierIdx] = val;
      this.bezierIdx = (this.bezierIdx + 1) % 8;
      if (this.bezierIdx < 3) {
         return val;
      } else {
         float p0 = this.bezierBuf[(this.bezierIdx - 3 + 8) % 8];
         float p1 = this.bezierBuf[(this.bezierIdx - 2 + 8) % 8];
         float p2 = this.bezierBuf[(this.bezierIdx - 1 + 8) % 8];
         float t = 0.3F;
         float mt = 1.0F - t;
         return mt * mt * mt * p0 + 3.0F * mt * mt * t * p1 + 3.0F * mt * t * t * p2 + t * t * t * val;
      }
   }

   private void humanFactors(long time, boolean canHit) {
      if (time - this.lastHit < 100L && this.combo > 2) {
         this.reactionTimes[this.reactIdx] = Math.max(35.0F, this.avgReaction - 1.0F);
         this.reactIdx = (this.reactIdx + 1) % this.reactionTimes.length;
         float s = 0.0F;

         for (float r : this.reactionTimes) {
            s += r;
         }

         this.avgReaction = s / this.reactionTimes.length;
      }

      if (time - this.lastTremor > 20L) {
         this.lastTremor = time;
         float ti = 0.06F * (1.0F + this.fatigue * 0.5F);
         this.tremorYaw = (float)(Math.sin(time / 45.0) * 0.05 + Math.sin(time / 23.0) * 0.03) * ti;
         this.tremorPitch = (float)(Math.cos(time / 48.0) * 0.04 + Math.sin(time / 27.0) * 0.02) * ti;
      }

      if (time - this.lastBreath > 100L) {
         this.lastBreath = time;
         this.breath = (float)(Math.sin(time / 2800.0) * 0.04 + Math.sin(time / 1400.0) * 0.02);
      }

      if ((float)(time - this.lastMicro) > 150.0F + this.rnd(0.0F, 200.0F)) {
         this.lastMicro = time;
         this.microYaw = this.rnd(-0.3F, 0.3F);
         this.microPitch = this.rnd(-0.2F, 0.2F);
      }

      this.microYaw *= 0.92F;
      this.microPitch *= 0.92F;
      if (time - this.lastBlink > 200L) {
         this.lastBlink = time;
         this.headNoise = this.rnd(-0.25F, 0.25F);
      }

      this.headNoise *= 0.95F;
      if (this.blinking && time - this.blinkStart > 100L) {
         this.blinking = false;
      } else if (!this.blinking && time - this.lastBlink > 3500L && this.rnd() < 0.005F) {
         this.blinking = true;
         this.blinkStart = time;
      }

      if (time - this.lastPause > 5000L) {
         if (canHit) {
            this.focus = Math.max(30, this.focus - R.nextInt(10));
         } else {
            this.focus = Math.min(100, this.focus + R.nextInt(15));
         }

         this.fatigue = Math.min(1.0F, this.fatigue + (canHit ? 0.02F : -0.01F));
         this.lastPause = time;
      }

      this.aimDrift = this.lerp(0.99F, this.aimDrift, this.rnd(-1.2F, 1.2F));
      float ws = (canHit ? 0.5F : 1.2F) * (1.0F + this.fatigue * 0.7F);
      this.wanderYaw = this.lerp(0.35F, this.wanderYaw, this.rnd(-0.8F, 0.8F) * ws);
      this.wanderPitch = this.lerp(0.35F, this.wanderPitch, this.rnd(-0.5F, 0.5F) * ws);
   }

   private float microJitter(float amp, long time, boolean yaw) {
      double phase = time / (yaw ? 70 : 90);
      return (float)(Math.sin(phase) * amp * 0.55 + Math.sin(phase * 2.3) * amp * 0.25 + Math.sin(phase * 4.8) * amp * 0.1);
   }

   private float ease(float delta, float maxSpeed) {
      if (Math.abs(delta) < 0.02F) {
         return 0.0F;
      } else {
         float sign = Math.signum(delta);
         float abs = Math.abs(delta);
         float clamped = Math.min(abs, maxSpeed);
         float t = clamped / Math.max(abs, 1.0F);
         return sign * clamped * (1.0F - (float)Math.pow(1.0F - t, 1.85F));
      }
   }

   private float smooth(float val, LinkedList<Float> hist) {
      hist.addLast(val);

      while (hist.size() > 3) {
         hist.removeFirst();
      }

      if (hist.isEmpty()) {
         return val;
      } else {
         float s = 0.0F;
         float w = 0.0F;
         int i = 0;

         for (float v : hist) {
            float ww = (float)Math.pow(1.15F, i++);
            s += v * ww;
            w += ww;
         }

         return s / w;
      }
   }

   @Override
   public void updateRotations(class_1309 target) {
      if (mc.field_1724 != null && target != null) {
         long time = System.currentTimeMillis();
         boolean canHit = Aura.INSTANCE.getAttackTimer().finished(467L);
         class_241 targetRot = RotationUtils.getRotations(this.getPredictedBox(target).method_1005());
         this.predict(targetRot, time);
         float yawD = class_3532.method_15393(this.predictedYaw - mc.field_1724.method_36454());
         float pitchD = class_3532.method_15393(this.predictedPitch - mc.field_1724.method_36455());
         float total = (float)Math.hypot(yawD, pitchD);
         if (canHit) {
            if (total < 6.0F) {
               this.combo++;
               this.miss = Math.max(0, this.miss - 1);
               this.lastHit = time;
            } else {
               this.miss++;
            }
         }

         if ((this.miss <= 8 || time - this.lastHit <= 800L) && (!(this.rnd() < 0.008F) || canHit)) {
            if (!this.blinking || !(this.rnd() < 0.5F)) {
               this.humanFactors(time, canHit);
               float fatigueM = 1.0F - this.fatigue * 0.35F;
               float focusM = this.focus / 100.0F;
               float missM = this.miss > 15 ? 0.65F : (this.miss > 8 ? 0.8F : (this.miss > 4 ? 0.9F : 1.0F));
               float reactionM = this.avgReaction / 50.0F;
               float yawSpeed = (Math.abs(yawD) > 32.0F ? 44.5F : 25.5F) * fatigueM * missM * focusM * reactionM;
               float pitchSpeed = (Math.abs(pitchD) > 14.0F ? 31.15F : 17.85F) * fatigueM * missM * focusM * reactionM;
               float yawStep = this.ease(yawD, yawSpeed);
               float pitchStep = this.ease(pitchD, pitchSpeed);
               float inacc = (100 - this.focus) / 100.0F * 0.8F + this.fatigue * 0.5F;
               float targetOffY = this.rnd(-1.2F, 1.2F) * 0.97F * (1.0F - this.fatigue * 0.4F);
               float targetOffP = this.rnd(-0.6F, 0.6F) * 0.97F * (1.0F - this.fatigue * 0.4F);
               float jitterY = this.microJitter(1.5F + this.fatigue * 1.1F, time, true)
                  + this.rnd(-inacc, inacc)
                  + targetOffY
                  + this.aimDrift
                  + this.headNoise
                  + this.tremorYaw * 0.3F
                  + this.breath * 0.2F
                  + this.microYaw;
               float jitterP = this.microJitter(0.7F + this.fatigue * 1.1F, time, false)
                  + this.rnd(-inacc * 0.6F, inacc * 0.6F)
                  + targetOffP
                  + this.aimDrift * 0.5F
                  + this.tremorPitch * 0.3F
                  + this.breath * 0.15F
                  + this.microPitch;
               yawStep = this.smooth(yawStep, this.yawHist);
               pitchStep = this.smooth(pitchStep, this.pitchHist);
               if (canHit && total < 10.0F && this.rnd() < 0.25F) {
                  float over = 0.1F * (1.0F - Math.min(total / 10.0F, 1.0F));
                  yawStep *= 1.0F + over;
                  pitchStep *= 1.0F + over;
               }

               float mouseNoise = (float)Math.sin(time / 150.0) * 0.6F * (Math.abs(yawD) + Math.abs(pitchD)) / 25.0F;
               yawStep += this.errorYaw + this.wanderYaw + mouseNoise;
               pitchStep += this.errorPitch + this.wanderPitch + mouseNoise * 0.7F;
               yawStep = this.clamp(yawStep, -50.0F, 50.0F);
               pitchStep = this.clamp(pitchStep, -30.0F, 30.0F);
               float finalYaw = mc.field_1724.method_36454() + yawStep + jitterY;
               float finalPitch = this.clamp(mc.field_1724.method_36455() + pitchStep + jitterP, -90.0F, 90.0F);
               float yawDiff = finalYaw - mc.field_1724.method_36454();
               if (Math.abs(yawDiff) > 30.0F) {
                  finalYaw = mc.field_1724.method_36454() + Math.signum(yawDiff) * 30.0F;
               }

               finalYaw = this.bezier(finalYaw);
               RotationStorage.update(new Rotation(finalYaw, finalPitch), 360.0F, 45.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
            }
         } else {
            if (this.miss > 8) {
               this.miss = 0;
            }
         }
      }
   }
}
