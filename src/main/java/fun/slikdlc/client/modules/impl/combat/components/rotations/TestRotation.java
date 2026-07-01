package fun.slikdlc.client.modules.impl.combat.components.rotations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.api.utils.rotate.RotationUtils;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.combat.components.RotationsSystem;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class TestRotation extends RotationsSystem implements QClient {
   private static final Path DATASET_PATH = Path.of(System.getProperty("user.home"), "Desktop", "data.json");
   private final List<TestRotation.DatasetFrame> frames = new ArrayList<>();
   private class_1309 trackedTarget;
   private class_1309 trackedRotationTarget;
   private class_243 currentAimPoint;
   private class_243 targetAimPoint;
   private long lastModified = Long.MIN_VALUE;
   private long lastLoadAttempt;
   private boolean datasetReady;
   private int playbackIndex;
   private int aimPointTicks;
   private int aimPointRefreshTicks;
   private int smoothProfileTicks;
   private float smoothYawStep;
   private float smoothPitchStep;
   private float smoothYaw;
   private float smoothPitch;
   private float yawSmoothFactor = 1.0F;
   private float pitchSmoothFactor = 1.0F;
   private boolean hasRotationState;

   public TestRotation() {
   }

   public void reset() {
      this.trackedTarget = null;
      this.trackedRotationTarget = null;
      this.currentAimPoint = null;
      this.targetAimPoint = null;
      this.playbackIndex = 0;
      this.aimPointTicks = 0;
      this.aimPointRefreshTicks = 0;
      this.smoothProfileTicks = 0;
      this.smoothYawStep = 0.0F;
      this.smoothPitchStep = 0.0F;
      this.smoothYaw = 0.0F;
      this.smoothPitch = 0.0F;
      this.yawSmoothFactor = 1.0F;
      this.pitchSmoothFactor = 1.0F;
      this.hasRotationState = false;
   }

   @Override
   public void updateRotations(class_1309 target) {
      if (mc.field_1724 != null && target != null) {
         boolean focus = this.shouldFocus();
         this.ensureDatasetLoaded();
         class_243 aimPoint = this.selectAimPoint(target, focus);
         class_241 rot = RotationUtils.getRotations(aimPoint);
         if (this.datasetReady && !this.frames.isEmpty()) {
            float currentYaw = mc.field_1724.method_36454();
            float currentPitch = mc.field_1724.method_36455();
            this.syncRotationState(target, currentYaw, currentPitch);
            float remainingYaw = class_3532.method_15393(rot.field_1343 - this.smoothYaw);
            float remainingPitch = rot.field_1342 - this.smoothPitch;
            TestRotation.DatasetFrame frame = this.pickFrame(remainingYaw, remainingPitch, focus);
            this.updateSmoothProfile(frame, remainingYaw, remainingPitch, focus);
            float gcd = Math.max(GCDUtil.getGCDValue(), 1.0E-4F);
            float yawStep = this.buildAxisStep(remainingYaw, frame, true, focus);
            float pitchStep = this.buildAxisStep(remainingPitch, frame, false, focus);
            yawStep += this.buildJitter(frame, remainingYaw, true, gcd);
            pitchStep += this.buildJitter(frame, remainingPitch, false, gcd);
            this.smoothYawStep = this.smoothAxisStep(this.smoothYawStep, yawStep, remainingYaw, true, focus);
            this.smoothPitchStep = this.smoothAxisStep(this.smoothPitchStep, pitchStep, remainingPitch, false, focus);
            float quantizedYawStep = this.quantizeDelta(this.smoothYawStep, remainingYaw, gcd, true);
            float quantizedPitchStep = this.quantizeDelta(this.smoothPitchStep, remainingPitch, gcd, false);
            this.smoothYaw = class_3532.method_15393(this.smoothYaw + quantizedYawStep);
            this.smoothPitch = class_3532.method_15363(this.smoothPitch + quantizedPitchStep, -89.0F, 89.0F);
            RotationStorage.update(new Rotation(this.smoothYaw, this.smoothPitch), 360.0F, 360.0F, 45.0F, 45.0F, 0, 1, Aura.clientLook.isState());
         } else {
            RotationStorage.update(
               new Rotation(rot.field_1343, class_3532.method_15363(rot.field_1342, -89.0F, 89.0F)),
               360.0F,
               360.0F,
               45.0F,
               45.0F,
               0,
               1,
               Aura.clientLook.isState()
            );
         }
      }
   }

   private boolean shouldFocus() {
      float cooldown = mc.field_1724.method_7261(1.5F);
      boolean fallingForCrit = !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 < 0.0 && mc.field_1724.field_6017 > 0.0F;
      return cooldown >= 0.88F || fallingForCrit;
   }

   private void ensureDatasetLoaded() {
      long now = System.currentTimeMillis();
      if (this.shouldReload(now)) {
         this.lastLoadAttempt = now;
         long modified = this.readLastModified();
         if (!this.datasetReady || modified != this.lastModified) {
            this.frames.clear();
            this.datasetReady = false;
            if (!Files.exists(DATASET_PATH)) {
               this.lastModified = Long.MIN_VALUE;
            } else {
               try (Reader reader = Files.newBufferedReader(DATASET_PATH)) {
                  for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
                     if (element.isJsonObject()) {
                        TestRotation.DatasetFrame frame = this.parseFrame(element.getAsJsonObject());
                        if (frame != null) {
                           this.frames.add(frame);
                        }
                     }
                  }

                  this.datasetReady = !this.frames.isEmpty();
                  this.lastModified = modified;
                  this.reset();
               } catch (IllegalStateException | IOException var12) {
                  this.datasetReady = false;
                  this.lastModified = Long.MIN_VALUE;
                  this.reset();
               }
            }
         }
      }
   }

   private boolean shouldReload(long now) {
      return this.datasetReady && !this.frames.isEmpty() ? now - this.lastLoadAttempt >= 3000L : now - this.lastLoadAttempt >= 1500L;
   }

   private long readLastModified() {
      try {
         return Files.exists(DATASET_PATH) ? Files.getLastModifiedTime(DATASET_PATH).toMillis() : Long.MIN_VALUE;
      } catch (IOException var2) {
         return Long.MIN_VALUE;
      }
   }

   private TestRotation.DatasetFrame parseFrame(JsonObject object) {
      float fromYaw = this.getFloat(object, "fromYaw");
      float toYaw = this.getFloat(object, "toYaw");
      float fromPitch = this.getFloat(object, "fromPitch");
      float toPitch = this.getFloat(object, "toPitch");
      float signedYaw = class_3532.method_15393(toYaw - fromYaw);
      float signedPitch = toPitch - fromPitch;
      float absYaw = Math.abs(signedYaw);
      float absPitch = Math.abs(signedPitch);
      float deltaYaw = Math.max(this.getFloat(object, "deltaYaw"), absYaw);
      float deltaPitch = Math.max(this.getFloat(object, "deltaPitch"), absPitch);
      if (deltaYaw <= 0.0F && deltaPitch <= 0.0F) {
         return null;
      } else {
         TestRotation.DatasetFrame frame = new TestRotation.DatasetFrame();
         frame.deltaYaw = deltaYaw;
         frame.deltaPitch = deltaPitch;
         frame.signedYaw = signedYaw != 0.0F ? signedYaw : Math.signum(this.getFloat(object, "jitterYawDir")) * deltaYaw;
         frame.signedPitch = signedPitch != 0.0F ? signedPitch : Math.signum(this.getFloat(object, "jitterPitchDir")) * deltaPitch;
         frame.rotationSpeed = Math.max(this.getFloat(object, "rotationSpeed"), 0.0F);
         frame.jitterScore = Math.max(this.getFloat(object, "jitterScore"), 0.0F);
         frame.jitterYawSpeed = Math.max(this.getFloat(object, "jitterYawSpeed"), 0.0F);
         frame.jitterPitchSpeed = Math.max(this.getFloat(object, "jitterPitchSpeed"), 0.0F);
         frame.isJittering = this.getBoolean(object, "isJittering");
         frame.attacking = this.getBoolean(object, "attacking");
         frame.combatFrame = this.getBoolean(object, "isCombatFrame");
         frame.instantSnap = this.getBoolean(object, "isInstantSnap");
         frame.timeDeltaMs = Math.max(1L, object.has("timeDeltaMs") ? object.get("timeDeltaMs").getAsLong() : 50L);
         return frame;
      }
   }

   private TestRotation.DatasetFrame pickFrame(float remainingYaw, float remainingPitch, boolean focus) {
      float pressure = Math.abs(remainingYaw) + Math.abs(remainingPitch) * 0.82F;
      int size = this.frames.size();
      int window = Math.min(size, focus ? 78 : 56);
      int bestIndex = this.playbackIndex % size;
      float bestScore = Float.MAX_VALUE;

      for (int i = 0; i < window; i++) {
         int index = (this.playbackIndex + i) % size;
         TestRotation.DatasetFrame frame = this.frames.get(index);
         float framePressure = frame.deltaYaw + frame.deltaPitch * 0.82F;
         float score = Math.abs(framePressure - pressure);
         if (focus) {
            if (!frame.isCombatLike()) {
               score += 3.0F;
            }

            if (frame.instantSnap) {
               score -= 0.5F;
            }
         } else if (frame.isCombatLike()) {
            score++;
         }

         if (pressure < 10.0F && frame.isJittering) {
            score -= Math.min(frame.jitterScore, 2.6F) * 0.2F;
         }

         score += i * 0.032F;
         if (score < bestScore) {
            bestScore = score;
            bestIndex = index;
         }
      }

      this.playbackIndex = (bestIndex + 1) % size;
      return this.frames.get(bestIndex);
   }

   private void updateSmoothProfile(TestRotation.DatasetFrame frame, float remainingYaw, float remainingPitch, boolean focus) {
      if (this.smoothProfileTicks > 0) {
         this.smoothProfileTicks--;
      } else {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         float pressure = class_3532.method_15363((Math.abs(remainingYaw) + Math.abs(remainingPitch)) / 32.0F, 0.0F, 1.0F);
         float timePressure = class_3532.method_15363((float)frame.timeDeltaMs / 120.0F, 0.0F, 1.0F);
         float yawMin = focus ? 0.94F : 0.86F;
         float yawMax = focus ? 1.12F : 1.04F;
         float pitchMin = focus ? 0.92F : 0.84F;
         float pitchMax = focus ? 1.08F : 1.0F;
         this.yawSmoothFactor = random.nextFloat(yawMin, yawMax + pressure * 0.08F + timePressure * 0.04F);
         this.pitchSmoothFactor = random.nextFloat(pitchMin, pitchMax + pressure * 0.06F + timePressure * 0.03F);
         if (frame.isCombatLike()) {
            this.yawSmoothFactor *= 1.02F;
            this.pitchSmoothFactor *= 1.015F;
         }

         this.smoothProfileTicks = random.nextInt(focus ? 2 : 3, focus ? 6 : 8);
      }
   }

   private float buildAxisStep(float remaining, TestRotation.DatasetFrame frame, boolean yawAxis, boolean focus) {
      float desiredAbs = Math.abs(remaining);
      if (desiredAbs <= 1.0E-4F) {
         return 0.0F;
      } else {
         float template = yawAxis ? frame.deltaYaw : frame.deltaPitch;
         float speedBoost = 0.3F + class_3532.method_15363(frame.rotationSpeed * (yawAxis ? 3.4F : 2.8F), 0.0F, yawAxis ? 0.2F : 0.16F);
         float pressureBoost = class_3532.method_15363(desiredAbs / (yawAxis ? 105.0F : 82.0F), 0.09F, yawAxis ? 0.52F : 0.46F);
         float step = Math.max(template * Math.max(speedBoost, pressureBoost), yawAxis ? 0.03F : 0.024F);
         if (frame.instantSnap) {
            step = Math.max(step, desiredAbs * (yawAxis ? 0.085F : 0.065F));
         }

         if (frame.attacking || frame.combatFrame) {
            step *= yawAxis ? 1.02F : 1.015F;
         }

         step *= yawAxis ? 0.5F : 0.46F;
         float finishThreshold = yawAxis ? 6.0F : 4.0F;
         if (desiredAbs < finishThreshold) {
            float finishBoost = 1.0F + (finishThreshold - desiredAbs) / finishThreshold * 0.18F;
            step *= finishBoost;
         }

         float maxStep = yawAxis
            ? Math.max(0.48F, desiredAbs * (frame.instantSnap ? 0.11F : 0.065F))
            : Math.max(0.34F, desiredAbs * (frame.instantSnap ? 0.09F : 0.058F));
         step = Math.min(step, maxStep);
         step = Math.min(step, desiredAbs);
         return Math.signum(remaining) * step;
      }
   }

   private float buildJitter(TestRotation.DatasetFrame frame, float remaining, boolean yawAxis, float gcd) {
      float desiredAbs = Math.abs(remaining);
      if (desiredAbs > (yawAxis ? 6.5F : 4.8F)) {
         return 0.0F;
      } else {
         float speed = yawAxis ? frame.jitterYawSpeed : frame.jitterPitchSpeed;
         float base = gcd * class_3532.method_15363(frame.jitterScore * 0.01F, 0.0F, yawAxis ? 0.15F : 0.11F);
         base += gcd * class_3532.method_15363(speed * (yawAxis ? 1.3F : 1.0F), 0.0F, yawAxis ? 0.1F : 0.07F);
         if (frame.isJittering) {
            base *= 1.05F;
         }

         if (base <= 0.0F) {
            return 0.0F;
         } else {
            float direction = ThreadLocalRandom.current().nextBoolean() ? 1.0F : -1.0F;
            float jitter = base * ThreadLocalRandom.current().nextFloat(0.3F, 0.95F) * direction;
            if (Math.abs(jitter) > desiredAbs && Math.signum(jitter) == Math.signum(remaining)) {
               jitter = remaining;
            }

            return jitter;
         }
      }
   }

   private void syncRotationState(class_1309 target, float currentYaw, float currentPitch) {
      if (!this.hasRotationState || this.trackedRotationTarget != target) {
         this.trackedRotationTarget = target;
         this.smoothYaw = currentYaw;
         this.smoothPitch = currentPitch;
         this.smoothYawStep = 0.0F;
         this.smoothPitchStep = 0.0F;
         this.yawSmoothFactor = 1.0F;
         this.pitchSmoothFactor = 1.0F;
         this.smoothProfileTicks = 0;
         this.hasRotationState = true;
      }
   }

   private float smoothAxisStep(float currentStep, float desiredStep, float remaining, boolean yawAxis, boolean focus) {
      float desiredAbs = Math.abs(remaining);
      if (desiredAbs <= 1.0E-4F) {
         return 0.0F;
      } else {
         float baseAlpha = yawAxis ? (focus ? 0.092F : 0.06F) : (focus ? 0.082F : 0.055F);
         float alpha = baseAlpha * (yawAxis ? this.yawSmoothFactor : this.pitchSmoothFactor);
         float smoothed = currentStep + (desiredStep - currentStep) * class_3532.method_15363(alpha, 0.025F, 0.16F);
         float minCap = yawAxis ? 0.13F : 0.1F;
         float capScale = yawAxis ? (focus ? 0.056F : 0.036F) : (focus ? 0.046F : 0.032F);
         float randomFactor = yawAxis ? this.yawSmoothFactor : this.pitchSmoothFactor;
         float maxCap = minCap + desiredAbs * capScale * class_3532.method_15363(randomFactor, 0.88F, 1.18F);
         float finishThreshold = yawAxis ? 5.5F : 3.8F;
         if (desiredAbs < finishThreshold) {
            maxCap *= 1.12F;
         }

         smoothed = class_3532.method_15363(smoothed, -maxCap, maxCap);
         if (Math.abs(remaining) < Math.abs(smoothed) && Math.signum(remaining) == Math.signum(smoothed)) {
            smoothed = remaining;
         }

         return smoothed;
      }
   }

   private float quantizeDelta(float wantedDelta, float remaining, float gcd, boolean yawAxis) {
      float limited = wantedDelta;
      if (Math.abs(remaining) < Math.abs(wantedDelta) && Math.signum(remaining) == Math.signum(wantedDelta)) {
         limited = remaining;
      }

      float quantized = Math.round(limited / gcd) * gcd;
      if (quantized == 0.0F && Math.abs(limited) >= gcd * 0.2F) {
         quantized = Math.signum(limited) * gcd;
      }

      if (Math.abs(remaining) < Math.abs(quantized) && Math.signum(remaining) == Math.signum(quantized)) {
         quantized = remaining;
      }

      if (!yawAxis) {
         quantized = class_3532.method_15363(quantized, -89.0F, 89.0F);
      }

      return quantized;
   }

   private class_243 selectAimPoint(class_1309 target, boolean focus) {
      if (this.trackedTarget == target && this.currentAimPoint != null && this.targetAimPoint != null) {
         if (this.aimPointTicks++ >= this.aimPointRefreshTicks) {
            this.targetAimPoint = this.createAimPoint(target, focus);
            this.aimPointTicks = 0;
            this.aimPointRefreshTicks = this.randomRefreshTicks(focus);
         }

         float lerp = focus ? 0.06F : 0.04F;
         this.currentAimPoint = new class_243(
            class_3532.method_16436(lerp, this.currentAimPoint.field_1352, this.targetAimPoint.field_1352),
            class_3532.method_16436(lerp, this.currentAimPoint.field_1351, this.targetAimPoint.field_1351),
            class_3532.method_16436(lerp, this.currentAimPoint.field_1350, this.targetAimPoint.field_1350)
         );
         return this.currentAimPoint;
      } else {
         this.trackedTarget = target;
         this.targetAimPoint = this.createAimPoint(target, focus);
         this.currentAimPoint = this.targetAimPoint;
         this.aimPointTicks = 0;
         this.aimPointRefreshTicks = this.randomRefreshTicks(focus);
         return this.currentAimPoint;
      }
   }

   private int randomRefreshTicks(boolean focus) {
      return ThreadLocalRandom.current().nextInt(focus ? 7 : 10, focus ? 13 : 18);
   }

   private class_243 createAimPoint(class_1309 target, boolean focus) {
      class_238 box = this.getPredictedBox(target);
      ThreadLocalRandom random = ThreadLocalRandom.current();
      double x = class_3532.method_16436(random.nextDouble(0.45, 0.55), box.field_1323, box.field_1320);
      double y = class_3532.method_16436(random.nextDouble(focus ? 0.53 : 0.49, focus ? 0.7 : 0.76), box.field_1322, box.field_1325);
      double z = class_3532.method_16436(random.nextDouble(0.45, 0.55), box.field_1321, box.field_1324);
      return new class_243(x, y, z);
   }

   private float getFloat(JsonObject object, String key) {
      return object.has(key) ? object.get(key).getAsFloat() : 0.0F;
   }

   private boolean getBoolean(JsonObject object, String key) {
      return object.has(key) && object.get(key).getAsBoolean();
   }

   private static class DatasetFrame {
      float deltaYaw;
      float deltaPitch;
      float signedYaw;
      float signedPitch;
      float rotationSpeed;
      float jitterScore;
      float jitterYawSpeed;
      float jitterPitchSpeed;
      long timeDeltaMs;
      boolean isJittering;
      boolean attacking;
      boolean combatFrame;
      boolean instantSnap;

      private DatasetFrame() {
      }

      boolean isCombatLike() {
         return this.attacking || this.combatFrame || this.instantSnap;
      }
   }
}
