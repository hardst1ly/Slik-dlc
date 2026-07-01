package fun.slikdlc.api.storages.implement;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventKeyboardInput;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import lombok.Generated;
import net.minecraft.class_3532;

public class RotationStorage implements QClient {
   public static RotationStorage instance;
   private RotationStorage.RotationTask currentTask = RotationStorage.RotationTask.IDLE;
   private float currentYawSpeed;
   private float currentPitchSpeed;
   private float currentYawReturnSpeed;
   private float currentPitchReturnSpeed;
   private int currentPriority;
   private int currentTimeout;
   private int idleTicks;
   private Rotation targetRotation;

   public RotationStorage() {
      instance = this;
      EventInvoker.register(this);
   }

   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0F) {
         forward = -0.5F;
      }

      if (moveForward > 0.0F) {
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

   public static void fixMovement(EventKeyboardInput event, float yaw) {
      float forward = event.getMovementForward();
      float strafe = event.getMovementSideways();
      if (forward != 0.0F || strafe != 0.0F) {
         double targetAngle = class_3532.method_15338(Math.toDegrees(direction(yaw, forward, strafe)));
         float bestForward = 0.0F;
         float bestStrafe = 0.0F;
         float smallestDifference = Float.MAX_VALUE;

         for (float testForward = -1.0F; testForward <= 1.0F; testForward++) {
            for (float testStrafe = -1.0F; testStrafe <= 1.0F; testStrafe++) {
               if (testForward != 0.0F || testStrafe != 0.0F) {
                  double testAngle = class_3532.method_15338(Math.toDegrees(direction(yaw, testForward, testStrafe)));
                  float difference = Math.abs(class_3532.method_15393((float)(targetAngle - testAngle)));
                  if (difference < smallestDifference) {
                     smallestDifference = difference;
                     bestForward = testForward;
                     bestStrafe = testStrafe;
                  }
               }
            }
         }

         event.setMovementForward(bestForward);
         event.setMovementSideways(bestStrafe);
      }
   }

   @EventLink
   public void onInput(EventKeyboardInput event) {
      if (this.isRotating()) {
         fixMovement(event, class_3532.method_15393(mc.field_1773.method_19418().method_19330()));
      }
   }

   private void resetRotation() {
      Rotation targetRotation = new Rotation(FreeLookStorage.getFreeYaw(), FreeLookStorage.getFreePitch());
      if (this.updateRotation(targetRotation, this.currentYawReturnSpeed(), this.currentPitchReturnSpeed())) {
         this.stopRotation();
      }
   }

   @EventLink
   public void onEventTick(EventUpdate event) {
      if (this.currentTask().equals(RotationStorage.RotationTask.AIM) && this.idleTicks() > this.currentTimeout()) {
         this.currentTask(RotationStorage.RotationTask.RESET);
      }

      if (this.currentTask().equals(RotationStorage.RotationTask.RESET)) {
         this.resetRotation();
      }

      this.idleTicks++;
   }

   public static void update(
      Rotation target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation
   ) {
      RotationStorage instance = RotationStorage.instance;
      if (mc.field_1724 != null) {
         if (instance.currentPriority() <= priority) {
            if (instance.currentTask().equals(RotationStorage.RotationTask.IDLE) && !clientRotation) {
               FreeLookStorage.setActive(true);
            }

            instance.currentYawSpeed(yawSpeed);
            instance.currentPitchSpeed(pitchSpeed);
            instance.currentYawReturnSpeed(yawReturnSpeed);
            instance.currentPitchReturnSpeed(pitchReturnSpeed);
            instance.currentTimeout(timeout);
            instance.currentPriority(priority);
            instance.currentTask(RotationStorage.RotationTask.AIM);
            instance.targetRotation(target);
            instance.updateRotation(target, yawSpeed, pitchSpeed);
         }
      }
   }

   public static void update(Rotation targetRotation, float turnSpeed, float returnSpeed, int timeout, int priority) {
      update(targetRotation, turnSpeed, turnSpeed, returnSpeed, returnSpeed, timeout, priority, false);
   }

   public static void update(Rotation targetRotation, float yawSpeed, float pitchSpeed, float returnSpeed, int timeout, int priority) {
      update(targetRotation, yawSpeed, pitchSpeed, returnSpeed, returnSpeed, timeout, priority, false);
   }

   private boolean updateRotation(Rotation targetRotation, float yawSpeed, float pitchSpeed) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         Rotation currentRotation = new Rotation(mc.field_1724);
         float yawDelta = class_3532.method_15393(targetRotation.getYaw() - currentRotation.getYaw());
         float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();
         float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
         float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);
         float yaw = mc.field_1724.method_36454();
         yaw += GCDUtil.getFixedRotation(class_3532.method_15363(yawDelta, -clampedYaw, clampedYaw));
         mc.field_1724.method_36456(yaw);
         mc.field_1724
            .method_36457(
               class_3532.method_15363(
                  mc.field_1724.method_36455() + GCDUtil.getFixedRotation(class_3532.method_15363(pitchDelta, -clampedPitch, clampedPitch)), -90.0F, 90.0F
               )
            );
         this.idleTicks(0);
         return new Rotation(mc.field_1724).getDelta(targetRotation) < 1.0F;
      }
   }

   public void stopRotation() {
      this.currentTask(RotationStorage.RotationTask.IDLE);
      this.currentPriority(0);
      FreeLookStorage.setActive(false);
   }

   public boolean isRotating() {
      return !this.currentTask.equals(RotationStorage.RotationTask.IDLE);
   }

   @Generated
   public RotationStorage.RotationTask currentTask() {
      return this.currentTask;
   }

   @Generated
   public float currentYawSpeed() {
      return this.currentYawSpeed;
   }

   @Generated
   public float currentPitchSpeed() {
      return this.currentPitchSpeed;
   }

   @Generated
   public float currentYawReturnSpeed() {
      return this.currentYawReturnSpeed;
   }

   @Generated
   public float currentPitchReturnSpeed() {
      return this.currentPitchReturnSpeed;
   }

   @Generated
   public int currentPriority() {
      return this.currentPriority;
   }

   @Generated
   public int currentTimeout() {
      return this.currentTimeout;
   }

   @Generated
   public int idleTicks() {
      return this.idleTicks;
   }

   @Generated
   public Rotation targetRotation() {
      return this.targetRotation;
   }

   @Generated
   public RotationStorage currentTask(RotationStorage.RotationTask currentTask) {
      this.currentTask = currentTask;
      return this;
   }

   @Generated
   public RotationStorage currentYawSpeed(float currentYawSpeed) {
      this.currentYawSpeed = currentYawSpeed;
      return this;
   }

   @Generated
   public RotationStorage currentPitchSpeed(float currentPitchSpeed) {
      this.currentPitchSpeed = currentPitchSpeed;
      return this;
   }

   @Generated
   public RotationStorage currentYawReturnSpeed(float currentYawReturnSpeed) {
      this.currentYawReturnSpeed = currentYawReturnSpeed;
      return this;
   }

   @Generated
   public RotationStorage currentPitchReturnSpeed(float currentPitchReturnSpeed) {
      this.currentPitchReturnSpeed = currentPitchReturnSpeed;
      return this;
   }

   @Generated
   public RotationStorage currentPriority(int currentPriority) {
      this.currentPriority = currentPriority;
      return this;
   }

   @Generated
   public RotationStorage currentTimeout(int currentTimeout) {
      this.currentTimeout = currentTimeout;
      return this;
   }

   @Generated
   public RotationStorage idleTicks(int idleTicks) {
      this.idleTicks = idleTicks;
      return this;
   }

   @Generated
   public RotationStorage targetRotation(Rotation targetRotation) {
      this.targetRotation = targetRotation;
      return this;
   }

   public static enum RotationTask {
      AIM,
      RESET,
      IDLE;

      private RotationTask() {
      }
   }
}
