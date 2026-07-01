package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.events.implement.EventMoveInput;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.Comparator;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1684;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class TargetPearl extends Module {
   private static final double MAX_TRACK_DISTANCE = 256.0;
   private static final double MIN_LANDING_DISTANCE = 11.0;
   private static final long LOCAL_THROW_COOLDOWN_MS = 2500L;
   private static final float DIRECT_MIN_PITCH = -25.0F;
   private static final float DIRECT_MAX_PITCH = 35.0F;
   private static final float PITCH_STEP = 0.25F;
   public static final TargetPearl INSTANCE = new TargetPearl();
   private final ModeSetting mode = new ModeSetting("Тип", "Автоматический", "По бинду", "Автоматический");
   private final BindSetting bind = new BindSetting("Бинд", -1).visible(() -> this.mode.is("По бинду"));
   private final BooleanSetting onlyTarget = new BooleanSetting("Только за противником", false);
   private final BooleanSetting ignoreFriends = new BooleanSetting("Игнорировать друзей", true);
   private final TimerUtils timer = new TimerUtils();
   private class_1684 targetPearl;
   private int lastHandledPearlId = -1;
   private long nextThrowAt;
   private boolean isThrowing;
   private class_241 serverRotation;

   public TargetPearl() {
      super("TargetPearl", "Автоматически бросает жемчуг в цель", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.mode, this.bind, this.onlyTarget, this.ignoreFriends});
   }

   @EventLink
   public void onBinding(EventBinding event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null) {
         if (this.mode.is("По бинду") && event.getKey() == this.bind.getKey()) {
            if (this.canThrowNow()) {
               this.aimAndThrowPearl();
            }
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.lastHandledPearlId != -1 && !(mc.field_1687.method_8469(this.lastHandledPearlId) instanceof class_1684 pearl && pearl.method_5805())) {
            this.lastHandledPearlId = -1;
         }

         if (this.mode.is("Автоматический") && this.canThrowNow()) {
            this.aimAndThrowPearl();
         }
      } else {
         this.resetThrowState();
      }
   }

   @EventLink
   public void onMoveInput(EventMoveInput event) {
      if (this.isEnable() && this.isThrowing && this.serverRotation != null) {
         float forward = event.getForward();
         float strafe = event.getStrafe();
         if (forward != 0.0F || strafe != 0.0F) {
            double targetAngle = class_3532.method_15338(Math.toDegrees(direction(this.serverRotation.field_1343, forward, strafe)));
            float bestForward = 0.0F;
            float bestStrafe = 0.0F;
            float smallestDifference = Float.MAX_VALUE;

            for (float testForward = -1.0F; testForward <= 1.0F; testForward++) {
               for (float testStrafe = -1.0F; testStrafe <= 1.0F; testStrafe++) {
                  if (testForward != 0.0F || testStrafe != 0.0F) {
                     double testAngle = class_3532.method_15338(Math.toDegrees(direction(this.serverRotation.field_1343, testForward, testStrafe)));
                     float difference = Math.abs(class_3532.method_15393((float)(targetAngle - testAngle)));
                     if (difference < smallestDifference) {
                        smallestDifference = difference;
                        bestForward = testForward;
                        bestStrafe = testStrafe;
                     }
                  }
               }
            }

            event.setForward(bestForward);
            event.setStrafe(bestStrafe);
         }
      }
   }

   @Override
   public void onDisable() {
      this.resetThrowState();
      this.lastHandledPearlId = -1;
      this.nextThrowAt = 0L;
      this.timer.reset();
      super.onDisable();
   }

   private boolean canThrowNow() {
      return System.currentTimeMillis() < this.nextThrowAt
         ? false
         : !mc.field_1724.method_7357().method_7904(new class_1799(class_1802.field_8634)) && this.timer.finished(1000L);
   }

   private void aimAndThrowPearl() {
      class_243 landingPosition = this.getTargetPearlLandingPosition();
      if (landingPosition == null) {
         this.resetThrowState();
      } else {
         float[] rotations = this.calculateYawPitch(landingPosition);
         if (rotations != null && !Float.isNaN(rotations[0]) && !Float.isNaN(rotations[1])) {
            class_243 trajectoryLanding = this.checkTrajectory(rotations[0], rotations[1]);
            double allowedError = Math.max(3.0, mc.field_1724.method_19538().method_1022(landingPosition) * 0.12);
            if (trajectoryLanding == null || landingPosition.method_1022(trajectoryLanding) > allowedError) {
               this.resetThrowState();
            } else if (!this.hasPearl()) {
               this.resetThrowState();
            } else {
               float previousYaw = mc.field_1724.method_36454();
               float previousPitch = mc.field_1724.method_36455();
               this.isThrowing = true;
               this.serverRotation = new class_241(rotations[0], rotations[1]);

               try {
                  mc.field_1724.method_36456(rotations[0]);
                  mc.field_1724.method_36457(rotations[1]);
                  mc.field_1724.field_3944.method_52787(new class_2831(rotations[0], rotations[1], mc.field_1724.method_24828(), mc.field_1724.field_5976));
                  InventoryUtils.swapAndUseHvH(class_1802.field_8634);
                  this.timer.reset();
                  this.nextThrowAt = System.currentTimeMillis() + 2500L;
                  if (this.targetPearl != null) {
                     this.lastHandledPearlId = this.targetPearl.method_5628();
                  }
               } finally {
                  mc.field_1724.method_36456(previousYaw);
                  mc.field_1724.method_36457(previousPitch);
                  this.resetThrowState();
               }
            }
         } else {
            this.resetThrowState();
         }
      }
   }

   private class_243 getTargetPearlLandingPosition() {
      this.targetPearl = this.getTargetPearl();
      if (this.targetPearl != null && this.targetPearl.method_5805()) {
         class_243 landingPos = this.predictPearlLanding(this.targetPearl);
         return landingPos != null && this.isWithinRange(landingPos) ? landingPos : null;
      } else {
         return null;
      }
   }

   private class_1684 getTargetPearl() {
      class_238 searchBox = mc.field_1724.method_5829().method_1014(256.0);
      class_1309 auraTarget = ModuleClass.INSTANCE != null ? ModuleClass.aura.getTarget() : null;
      return mc.field_1687
         .method_8333(
            mc.field_1724,
            searchBox,
            entity -> entity instanceof class_1684 pearl
               && pearl.method_5805()
               && pearl.method_24921() != mc.field_1724
               && pearl.method_5628() != this.lastHandledPearlId
               && !this.isIgnoredFriend(pearl.method_24921())
               && (!this.onlyTarget.isState() || auraTarget != null && pearl.method_24921() == auraTarget)
         )
         .stream()
         .map(entity -> (class_1684)entity)
         .filter(pearl -> this.getHorizontalDistanceTo(pearl) <= 256.0)
         .min(Comparator.comparingDouble(this::getHorizontalDistanceTo))
         .orElse(null);
   }

   private boolean isIgnoredFriend(class_1297 owner) {
      return this.ignoreFriends.isState() && owner instanceof class_1657 player
         ? SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.friendStorage != null && SlikDlc.INSTANCE.friendStorage.isFriend(player.method_5477().getString())
         : false;
   }

   private double getHorizontalDistanceTo(class_1684 pearl) {
      class_243 playerPos = mc.field_1724.method_19538();
      class_243 pearlPos = pearl.method_19538();
      double dx = pearlPos.field_1352 - playerPos.field_1352;
      double dz = pearlPos.field_1350 - playerPos.field_1350;
      return Math.sqrt(dx * dx + dz * dz);
   }

   private class_243 predictPearlLanding(class_1684 pearl) {
      class_243 position = pearl.method_19538();
      class_243 velocity = pearl.method_18798();
      class_243 lastPosition = position;

      for (int i = 0; i < 200; i++) {
         lastPosition = position;
         position = position.method_1019(velocity);
         if (this.hitsBlock(lastPosition, position) || position.field_1351 <= mc.field_1687.method_31607()) {
            return new class_243(
               class_3532.method_15357(lastPosition.field_1352) + 0.5,
               class_3532.method_15357(lastPosition.field_1351),
               class_3532.method_15357(lastPosition.field_1350) + 0.5
            );
         }

         velocity = this.updatePearlMotion(velocity, position);
      }

      return new class_243(
         class_3532.method_15357(lastPosition.field_1352) + 0.5,
         class_3532.method_15357(lastPosition.field_1351),
         class_3532.method_15357(lastPosition.field_1350) + 0.5
      );
   }

   private class_243 updatePearlMotion(class_243 motion, class_243 position) {
      class_2338 blockPos = class_2338.method_49638(position);
      return mc.field_1687.method_8320(blockPos).method_27852(class_2246.field_10382)
         ? motion.method_1021(0.8).method_1031(0.0, -0.03, 0.0)
         : motion.method_1021(0.99).method_1031(0.0, -0.03, 0.0);
   }

   private boolean isWithinRange(class_243 landingPos) {
      double distanceToLanding = mc.field_1724.method_19538().method_1022(landingPos);
      return distanceToLanding >= 11.0 && distanceToLanding <= 256.0;
   }

   private float[] calculateYawPitch(class_243 targetPosition) {
      class_243 playerPosition = mc.field_1724.method_19538();
      double dx = targetPosition.field_1352 - playerPosition.field_1352;
      double dy = targetPosition.field_1351 - mc.field_1724.method_23320();
      double dz = targetPosition.field_1350 - playerPosition.field_1350;
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
      double allowedError = Math.max(1.5, mc.field_1724.method_19538().method_1022(targetPosition) * 0.08);
      TargetPearl.TrajectoryCandidate directCandidate = this.findBestCandidate(targetPosition, yaw, -25.0F, 35.0F, allowedError, true);
      if (directCandidate != null) {
         return new float[]{yaw, class_3532.method_15363(directCandidate.pitch, -90.0F, 90.0F)};
      } else {
         TargetPearl.TrajectoryCandidate fallbackCandidate = this.findBestCandidate(targetPosition, yaw, -85.0F, 85.0F, allowedError, false);
         if (fallbackCandidate == null) {
            double fallbackPitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance)) + 5.0;
            return new float[]{yaw, class_3532.method_15363((float)fallbackPitch, -90.0F, 90.0F)};
         } else {
            return new float[]{yaw, class_3532.method_15363(fallbackCandidate.pitch, -90.0F, 90.0F)};
         }
      }
   }

   private TargetPearl.TrajectoryCandidate findBestCandidate(
      class_243 targetPosition, float yaw, float minPitch, float maxPitch, double allowedError, boolean preferDirect
   ) {
      class_243 playerPosition = mc.field_1724.method_19538();
      double velocity = 1.5;
      TargetPearl.TrajectoryCandidate bestCandidate = null;

      for (float pitch = minPitch; pitch <= maxPitch; pitch += 0.25F) {
         float pitchRad = (float)Math.toRadians(pitch);
         double vx = -class_3532.method_15374((float)Math.toRadians(yaw)) * class_3532.method_15362(pitchRad) * velocity;
         double vy = -class_3532.method_15374(pitchRad) * velocity;
         double vz = class_3532.method_15362((float)Math.toRadians(yaw)) * class_3532.method_15362(pitchRad) * velocity;
         class_243 pos = new class_243(playerPosition.field_1352, mc.field_1724.method_23320(), playerPosition.field_1350);
         class_243 motion = new class_243(vx, vy, vz);
         int ticks = 0;

         for (int i = 0; i < 200; i++) {
            class_243 previous = pos;
            pos = pos.method_1019(motion);
            motion = this.updatePearlMotion(motion, pos);
            ticks++;
            if (this.hitsEntity(previous, pos)) {
               break;
            }

            if (this.hitsBlock(previous, pos) || !(pos.field_1351 > mc.field_1687.method_31607())) {
               double distanceToTarget = pos.method_1022(targetPosition);
               TargetPearl.TrajectoryCandidate candidate = new TargetPearl.TrajectoryCandidate(pitch, distanceToTarget, ticks, pos);
               if (this.isBetterCandidate(candidate, bestCandidate, allowedError, preferDirect)) {
                  bestCandidate = candidate;
               }
               break;
            }
         }
      }

      return bestCandidate != null && !(bestCandidate.distanceToTarget > allowedError) ? bestCandidate : null;
   }

   private boolean isBetterCandidate(
      TargetPearl.TrajectoryCandidate candidate, TargetPearl.TrajectoryCandidate currentBest, double allowedError, boolean preferDirect
   ) {
      if (currentBest == null) {
         return true;
      } else {
         boolean candidateAccurate = candidate.distanceToTarget <= allowedError;
         boolean bestAccurate = currentBest.distanceToTarget <= allowedError;
         if (candidateAccurate != bestAccurate) {
            return candidateAccurate;
         } else {
            if (preferDirect && candidateAccurate && bestAccurate) {
               float candidatePitchAbs = Math.abs(candidate.pitch);
               float bestPitchAbs = Math.abs(currentBest.pitch);
               if (Math.abs(candidatePitchAbs - bestPitchAbs) > 0.01F) {
                  return candidatePitchAbs < bestPitchAbs;
               }

               if (candidate.ticks != currentBest.ticks) {
                  return candidate.ticks < currentBest.ticks;
               }
            }

            if (Math.abs(candidate.distanceToTarget - currentBest.distanceToTarget) > 0.01) {
               return candidate.distanceToTarget < currentBest.distanceToTarget;
            } else if (candidate.ticks != currentBest.ticks) {
               return candidate.ticks < currentBest.ticks;
            } else {
               return !preferDirect ? Math.abs(candidate.pitch) < Math.abs(currentBest.pitch) : false;
            }
         }
      }
   }

   private class_243 checkTrajectory(float yaw, float pitch) {
      float yawRad = (float)Math.toRadians(yaw);
      float pitchRad = (float)Math.toRadians(pitch);
      double velocity = 1.5;
      double x = mc.field_1724.method_23317() - class_3532.method_15362(yawRad) * 0.16F;
      double y = mc.field_1724.method_23318() + mc.field_1724.method_18381(mc.field_1724.method_18376()) - 0.1;
      double z = mc.field_1724.method_23321() - class_3532.method_15374(yawRad) * 0.16F;
      double motionX = -class_3532.method_15374(yawRad) * class_3532.method_15362(pitchRad) * velocity;
      double motionY = -class_3532.method_15374(pitchRad) * velocity;
      double motionZ = class_3532.method_15362(yawRad) * class_3532.method_15362(pitchRad) * velocity;
      class_243 position = new class_243(x, y, z);
      class_243 motion = new class_243(motionX, motionY, motionZ);

      for (int i = 0; i <= 200; i++) {
         class_243 previous = position;
         position = position.method_1019(motion);
         motion = this.updatePearlMotion(motion, position);
         if (this.hitsEntity(previous, position)) {
            return null;
         }

         if (this.hitsBlock(previous, position) || position.field_1351 <= mc.field_1687.method_31607()) {
            return new class_243(
               class_3532.method_15357(position.field_1352) + 0.5,
               class_3532.method_15357(position.field_1351),
               class_3532.method_15357(position.field_1350) + 0.5
            );
         }
      }

      return null;
   }

   private boolean hitsBlock(class_243 from, class_243 to) {
      return mc.field_1687.method_17742(new class_3959(from, to, class_3960.field_17558, class_242.field_1348, mc.field_1724)).method_17783()
         == class_240.field_1332;
   }

   private boolean hitsEntity(class_243 from, class_243 to) {
      class_238 searchBox = new class_238(from, to).method_1014(0.3);

      for (class_1297 entity : mc.field_1687.method_8333(mc.field_1724, searchBox, entityx -> {
         if (!entityx.method_5805() || entityx.method_7325() || entityx.field_5960) {
            return false;
         } else {
            return entityx == this.targetPearl ? false : !(entityx instanceof class_1684);
         }
      })) {
         if (entity.method_5829().method_1014(0.25).method_992(from, to).isPresent()) {
            return true;
         }
      }

      return false;
   }

   private boolean hasPearl() {
      return mc.field_1724.method_6047().method_31574(class_1802.field_8634)
         || mc.field_1724.method_6079().method_31574(class_1802.field_8634)
         || InventoryUtils.find(class_1802.field_8634, 0, 8) != -1
         || InventoryUtils.find(class_1802.field_8634, 9, 45) != -1;
   }

   private void resetThrowState() {
      this.isThrowing = false;
      this.targetPearl = null;
      this.serverRotation = null;
   }

   private static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0F) {
         forward = -0.5F;
      } else if (moveForward > 0.0F) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians(rotationYaw);
   }

   private static final class TrajectoryCandidate {
      private final float pitch;
      private final double distanceToTarget;
      private final int ticks;
      private final class_243 landingPos;

      private TrajectoryCandidate(float pitch, double distanceToTarget, int ticks, class_243 landingPos) {
         this.pitch = pitch;
         this.distanceToTarget = distanceToTarget;
         this.ticks = ticks;
         this.landingPos = landingPos;
      }
   }
}
