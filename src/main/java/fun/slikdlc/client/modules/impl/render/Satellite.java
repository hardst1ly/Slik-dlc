package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventAttackEntity;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_10055;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_5602;
import net.minecraft.class_7308;
import net.minecraft.class_7833;
import net.minecraft.class_9996;
import net.minecraft.class_4597.class_4598;

public class Satellite extends Module {
   private static final class_2960 ALLAY_TEXTURE = class_2960.method_60656("textures/entity/allay/allay.png");
   private static final long ATTACK_FOLLOW_TIMEOUT_MS = 3600L;
   private static final long ATTACK_LAUNCH_DURATION_MS = 560L;
   private static final long ATTACK_RETURN_DURATION_MS = 920L;
   public static Satellite INSTANCE = new Satellite();
   public final ModeSetting shoulder = new ModeSetting("Плечо", "Правое", "Правое", "Левое");
   public final FloatSetting scale = new FloatSetting("Размер", 0.38F, 0.15F, 1.25F, 0.01F);
   public final FloatSetting offsetX = new FloatSetting("Смещение X", 0.0F, -1.0F, 1.0F, 0.01F);
   public final FloatSetting offsetY = new FloatSetting("Смещение Y", 0.18F, -1.0F, 1.0F, 0.01F);
   public final FloatSetting offsetZ = new FloatSetting("Смещение Z", 0.0F, -1.0F, 1.0F, 0.01F);
   public final FloatSetting rotateX = new FloatSetting("Поворот X", 0.0F, -180.0F, 180.0F, 1.0F);
   public final FloatSetting rotateY = new FloatSetting("Поворот Y", 0.0F, -180.0F, 180.0F, 1.0F);
   public final FloatSetting rotateZ = new FloatSetting("Поворот Z", 0.0F, -180.0F, 180.0F, 1.0F);
   public final BooleanSetting showSelf = new BooleanSetting("Показывать на себе", true);
   public final BooleanSetting showOthers = new BooleanSetting("Показывать на других", true);
   public final BooleanSetting showFriends = new BooleanSetting("Показывать на друзьях", true);
   public final BooleanSetting attackEnemies = new BooleanSetting("Атаковать врагов", true);
   public final BooleanSetting idleAnimation = new BooleanSetting("Idle-анимация", true);
   public final FloatSetting idleSpeed = new FloatSetting("Скорость idle", 1.0F, 0.1F, 3.0F, 0.05F).visible(() -> this.idleAnimation.isState());
   public final FloatSetting idleStrength = new FloatSetting("Сила idle", 0.35F, 0.0F, 1.5F, 0.05F).visible(() -> this.idleAnimation.isState());
   private final class_9996 attackState = new class_9996();
   private class_7308 attackModel;
   private int attackTargetId = Integer.MIN_VALUE;
   private long attackStartedAt;
   private long lastAttackAt;
   private long attackReturnStartedAt;
   private class_243 attackReturnStartPos = new class_243(0.0, 0.0, 0.0);
   private float attackOrbitSeed;
   private float attackCurveSide;
   private float attackCurveLift;
   private float attackCurveDepth;
   private float attackRadiusJitter;
   private float attackHeightJitter;
   private float attackBobSeed;
   private float attackOrbitSpeed;
   private float attackOrbitDirection;
   private float attackLookYaw;
   private float attackLookPitch;
   private boolean attackLookInitialized;

   public Satellite() {
      super("Satellite", "Питомец-аллей на плече", Module.ModuleCategory.RENDER);
      this.addSettings(
         new Setting[]{
            this.shoulder,
            this.scale,
            this.offsetX,
            this.offsetY,
            this.offsetZ,
            this.rotateX,
            this.rotateY,
            this.rotateZ,
            this.showSelf,
            this.showOthers,
            this.showFriends,
            this.attackEnemies,
            this.idleAnimation,
            this.idleSpeed,
            this.idleStrength
         }
      );
   }

   @Override
   public void onDisable() {
      this.clearAttackTarget();
      super.onDisable();
   }

   @EventLink
   public void onAttack(EventAttackEntity event) {
      if (this.attackEnemies.isState() && event != null && event.getPlayer() != null && event.getTarget() != null && mc.field_1724 != null) {
         if (event.getPlayer().method_5628() == mc.field_1724.method_5628() && event.getTarget() != mc.field_1724) {
            long now = System.currentTimeMillis();
            if (this.attackTargetId != event.getTarget().method_5628()) {
               this.attackStartedAt = now;
               this.randomizeAttackPath(now);
            }

            this.attackTargetId = event.getTarget().method_5628();
            this.lastAttackAt = now;
            this.attackReturnStartedAt = 0L;
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (!this.attackEnemies.isState()) {
         this.clearAttackTarget();
      } else {
         this.updateAttackLifecycle();
      }
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (mc.field_1724 != null && mc.field_1687 != null && event != null) {
         float tickDelta = event.getTickDelta();
         long now = System.currentTimeMillis();
         class_1297 target = this.updateAttackLifecycle();
         if (target != null) {
            this.ensureAttackModel();
            if (this.attackModel != null) {
               this.renderAttackSatellite(event, target, this.getAttackRenderPosition(target, tickDelta, now), tickDelta, now);
            }
         }
      }
   }

   private void renderAttackSatellite(Event3DRender event, class_1297 target, class_243 renderPos, float tickDelta, long now) {
      class_243 cameraPos = event.getCamera().method_19326();
      class_243 targetPos = this.getInterpolatedEntityPos(target, tickDelta);
      float elapsed = (float)(now - this.attackStartedAt) / 1000.0F;
      class_243 focusPos = targetPos.method_1031(0.0, target.method_17682() * 0.56, 0.0);
      float desiredYaw = this.getLookYaw(renderPos, focusPos);
      float desiredPitch = this.getLookPitch(renderPos, focusPos);
      if (!this.attackLookInitialized) {
         this.attackLookYaw = desiredYaw;
         this.attackLookPitch = desiredPitch;
         this.attackLookInitialized = true;
      } else {
         this.attackLookYaw = class_3532.method_17821(0.32F, this.attackLookYaw, desiredYaw);
         this.attackLookPitch = class_3532.method_16439(0.24F, this.attackLookPitch, desiredPitch);
      }

      float headYaw = class_3532.method_15363(class_3532.method_15393(desiredYaw - this.attackLookYaw), -85.0F, 85.0F);
      class_4587 matrices = event.getMatrices();
      matrices.method_22903();
      matrices.method_22904(
         renderPos.field_1352 - cameraPos.field_1352, renderPos.field_1351 - cameraPos.field_1351, renderPos.field_1350 - cameraPos.field_1350
      );
      matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F - this.attackLookYaw));
      matrices.method_22905(this.scale.get(), this.scale.get(), this.scale.get());
      matrices.method_22905(-1.0F, -1.0F, 1.0F);
      matrices.method_46416(0.0F, -1.501F, 0.0F);
      this.attackState.field_53328 = mc.field_1724.field_6012 + tickDelta + elapsed * 20.0F;
      this.attackState.field_53450 = elapsed * 6.4F;
      this.attackState.field_53451 = 0.72F + class_3532.method_15374(elapsed * 7.0F + this.attackBobSeed) * 0.12F;
      this.attackState.field_53447 = headYaw;
      this.attackState.field_53448 = this.attackLookPitch;
      this.attackState.field_53333 = false;
      this.attackState.field_53461 = false;
      this.attackState.field_53462 = false;
      this.attackState.field_53456 = false;
      this.attackState.field_53457 = false;
      this.attackState.field_53458 = target.method_5799();
      this.attackState.field_53446 = this.attackLookYaw;
      this.attackState.field_53453 = 1.0F;
      this.attackState.field_53454 = 1.0F;
      this.attackState.field_53465 = target instanceof class_1309 living ? living.method_18376() : class_4050.field_18076;
      this.attackState.field_53449 = 0.0F;
      this.attackState.field_53460 = false;
      this.attackState.field_53237 = false;
      this.attackState.field_53238 = false;
      this.attackState.field_53239 = 0.0F;
      this.attackState.field_53240 = 0.65F;
      this.attackModel.method_42732(this.attackState);
      class_4598 immediate = mc.method_22940().method_23000();
      class_4588 vertexConsumer = immediate.getBuffer(this.attackModel.method_23500(ALLAY_TEXTURE));
      this.attackModel.method_60879(matrices, vertexConsumer, 15728880, class_4608.field_21444);
      immediate.method_22993();
      matrices.method_22909();
   }

   public boolean shouldRender(class_10055 playerState) {
      if (this.isEnable() && mc.field_1724 != null && mc.field_1687 != null && playerState != null && !playerState.field_53542) {
         boolean self = playerState.field_53528 == mc.field_1724.method_5628();
         if (!self) {
            return mc.field_1687.method_8469(playerState.field_53528) instanceof class_1657 player
                  && SlikDlc.INSTANCE != null
                  && SlikDlc.INSTANCE.friendStorage != null
                  && SlikDlc.INSTANCE.friendStorage.isFriend(player.method_5477().getString())
               ? this.showFriends.isState()
               : this.showOthers.isState();
         } else {
            return this.hasActiveAttackTarget() ? false : this.shouldRenderOwnShoulderPet();
         }
      } else {
         return false;
      }
   }

   public boolean isLeftShoulder() {
      return this.shoulder.is("Левое");
   }

   public boolean hasActiveAttackTarget() {
      return this.updateAttackLifecycle() != null;
   }

   private boolean shouldRenderOwnShoulderPet() {
      return this.showSelf.isState() && !mc.field_1690.method_31044().method_31034();
   }

   private class_1297 updateAttackLifecycle() {
      if (this.attackEnemies.isState() && mc.field_1687 != null && mc.field_1724 != null && this.attackTargetId != Integer.MIN_VALUE) {
         class_1297 target = mc.field_1687.method_8469(this.attackTargetId);
         if (target != null && !target.method_31481() && target != mc.field_1724) {
            if (target instanceof class_1309 living && !living.method_5805()) {
               this.clearAttackTarget();
               return null;
            } else if (mc.field_1724.method_5858(target) > 4096.0) {
               this.clearAttackTarget();
               return null;
            } else {
               long now = System.currentTimeMillis();
               if (this.attackReturnStartedAt == 0L && now - this.lastAttackAt > 3600L) {
                  float elapsed = (float)(now - this.attackStartedAt) / 1000.0F;
                  this.attackReturnStartPos = this.getOrbitPosition(target, this.getInterpolatedEntityPos(target, 1.0F), elapsed);
                  this.attackReturnStartedAt = now;
               }

               if (this.attackReturnStartedAt != 0L && now - this.attackReturnStartedAt > 920L) {
                  this.clearAttackTarget();
                  return null;
               } else {
                  return target;
               }
            }
         } else {
            this.clearAttackTarget();
            return null;
         }
      } else {
         return null;
      }
   }

   private class_243 getAttackRenderPosition(class_1297 target, float tickDelta, long now) {
      class_243 shoulderPos = this.getShoulderWorldPosition(tickDelta);
      class_243 targetPos = this.getInterpolatedEntityPos(target, tickDelta);
      float elapsed = (float)(now - this.attackStartedAt) / 1000.0F;
      class_243 orbitPos = this.getOrbitPosition(target, targetPos, elapsed);
      if (this.attackReturnStartedAt == 0L) {
         float launchProgress = class_3532.method_15363((float)(now - this.attackStartedAt) / 560.0F, 0.0F, 1.0F);
         return launchProgress < 1.0F ? this.buildLaunchCurve(shoulderPos, orbitPos, launchProgress) : orbitPos;
      } else {
         float returnProgress = class_3532.method_15363((float)(now - this.attackReturnStartedAt) / 920.0F, 0.0F, 1.0F);
         return this.buildReturnCurve(this.attackReturnStartPos, shoulderPos, returnProgress);
      }
   }

   private class_243 getOrbitPosition(class_1297 target, class_243 targetPos, float elapsed) {
      double baseRadius = Math.max(0.86, target.method_17681() * 1.05 + 0.46) * this.attackRadiusJitter;
      double angle = this.attackOrbitSeed * (float) (Math.PI / 180.0) + elapsed * this.attackOrbitSpeed * this.attackOrbitDirection;
      double radiusPulse = Math.sin(elapsed * 1.25F + this.attackBobSeed * 0.45F) * 0.07;
      double orbitRadius = baseRadius + radiusPulse;
      double orbitX = Math.cos(angle) * orbitRadius;
      double orbitZ = Math.sin(angle) * orbitRadius;
      double orbitY = targetPos.field_1351
         + target.method_17682() * (0.78 + this.attackHeightJitter)
         + Math.sin(elapsed * 2.9F + this.attackBobSeed) * 0.2
         + Math.cos(elapsed * 1.8F + this.attackBobSeed * 0.8F) * 0.08;
      return new class_243(targetPos.field_1352 + orbitX, orbitY, targetPos.field_1350 + orbitZ);
   }

   private class_243 buildLaunchCurve(class_243 start, class_243 end, float progress) {
      float eased = this.easeInOut(progress);
      class_243 direction = end.method_1020(start);
      class_243 horizontal = new class_243(direction.field_1352, 0.0, direction.field_1350);
      if (horizontal.method_1027() < 1.0E-4) {
         horizontal = new class_243(0.0, 0.0, 1.0);
      } else {
         horizontal = horizontal.method_1029();
      }

      class_243 sideways = new class_243(horizontal.field_1350, 0.0, -horizontal.field_1352).method_1029();
      class_243 lift = new class_243(0.0, this.attackCurveLift, 0.0);
      class_243 control1 = start.method_1019(sideways.method_1021(this.attackCurveSide * 0.52)).method_1019(lift.method_1021(0.82));
      class_243 control2 = end.method_1019(sideways.method_1021(-this.attackCurveSide * 0.28))
         .method_1019(horizontal.method_1021(this.attackCurveDepth * 0.18))
         .method_1019(lift.method_1021(0.58));
      return this.cubicBezier(start, control1, control2, end, eased);
   }

   private class_243 buildReturnCurve(class_243 start, class_243 end, float progress) {
      float eased = this.easeInOut(progress);
      class_243 direction = end.method_1020(start);
      class_243 horizontal = new class_243(direction.field_1352, 0.0, direction.field_1350);
      if (horizontal.method_1027() < 1.0E-4) {
         horizontal = new class_243(0.0, 0.0, 1.0);
      } else {
         horizontal = horizontal.method_1029();
      }

      class_243 sideways = new class_243(horizontal.field_1350, 0.0, -horizontal.field_1352).method_1029();
      class_243 lift = new class_243(0.0, this.attackCurveLift * 0.72, 0.0);
      class_243 control1 = start.method_1019(sideways.method_1021(-this.attackCurveSide * 0.24)).method_1019(lift.method_1021(0.62));
      class_243 control2 = end.method_1019(sideways.method_1021(this.attackCurveSide * 0.3))
         .method_1019(horizontal.method_1021(-this.attackCurveDepth * 0.1))
         .method_1019(lift.method_1021(0.22));
      class_243 bezier = this.cubicBezier(start, control1, control2, end, eased);
      return eased > 0.985F ? end : bezier;
   }

   private class_243 getShoulderWorldPosition(float tickDelta) {
      class_243 playerPos = this.getInterpolatedEntityPos(mc.field_1724, tickDelta);
      float bodyYaw = class_3532.method_17821(tickDelta, mc.field_1724.field_6220, mc.field_1724.field_6283);
      float yawRad = bodyYaw * (float) (Math.PI / 180.0);
      class_243 forward = new class_243(-class_3532.method_15374(yawRad), 0.0, class_3532.method_15362(yawRad));
      class_243 right = new class_243(forward.field_1350, 0.0, -forward.field_1352);
      double side = (this.isLeftShoulder() ? 1.0 : -1.0) * mc.field_1724.method_17681() * 0.42;
      double height = mc.field_1724.method_17682() - (mc.field_1724.method_5715() ? 0.38 : 0.24);
      double back = 0.0;
      class_243 shoulderPos = playerPos.method_1031(0.0, height, 0.0)
         .method_1019(right.method_1021(side))
         .method_1019(forward.method_1021(back))
         .method_1019(right.method_1021(this.offsetX.get() * 0.65))
         .method_1031(0.0, this.offsetY.get() * 0.45, 0.0)
         .method_1019(forward.method_1021(this.offsetZ.get() * 0.35));
      if (this.idleAnimation.isState()) {
         float time = (mc.field_1724.field_6012 + tickDelta) * (0.7F + this.idleSpeed.get() * 0.65F);
         float bob = class_3532.method_15374(time * 0.42F) * 0.03F * this.idleStrength.get();
         shoulderPos = shoulderPos.method_1031(0.0, bob, 0.0);
      }

      return shoulderPos;
   }

   private class_243 getInterpolatedEntityPos(class_1297 entity, float tickDelta) {
      return new class_243(
         class_3532.method_16436(tickDelta, entity.field_6014, entity.method_23317()),
         class_3532.method_16436(tickDelta, entity.field_6036, entity.method_23318()),
         class_3532.method_16436(tickDelta, entity.field_5969, entity.method_23321())
      );
   }

   private class_243 cubicBezier(class_243 p0, class_243 p1, class_243 p2, class_243 p3, float t) {
      float inv = 1.0F - t;
      double w0 = inv * inv * inv;
      double w1 = 3.0 * inv * inv * t;
      double w2 = 3.0 * inv * t * t;
      double w3 = t * t * t;
      return new class_243(
         p0.field_1352 * w0 + p1.field_1352 * w1 + p2.field_1352 * w2 + p3.field_1352 * w3,
         p0.field_1351 * w0 + p1.field_1351 * w1 + p2.field_1351 * w2 + p3.field_1351 * w3,
         p0.field_1350 * w0 + p1.field_1350 * w1 + p2.field_1350 * w2 + p3.field_1350 * w3
      );
   }

   private float easeInOut(float value) {
      float clamped = class_3532.method_15363(value, 0.0F, 1.0F);
      return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
   }

   private void ensureAttackModel() {
      if (this.attackModel == null && mc != null) {
         this.attackModel = new class_7308(mc.method_31974().method_32072(class_5602.field_38455));
      }
   }

   private void randomizeAttackPath(long now) {
      this.attackOrbitSeed = this.randomRange(0.0F, 360.0F);
      this.attackCurveSide = this.randomRange(-1.1F, 1.1F);
      this.attackCurveLift = this.randomRange(0.48F, 0.96F);
      this.attackCurveDepth = this.randomRange(-0.42F, 0.42F);
      this.attackRadiusJitter = this.randomRange(0.92F, 1.24F);
      this.attackHeightJitter = this.randomRange(-0.06F, 0.14F);
      this.attackBobSeed = this.randomRange(0.0F, (float) (Math.PI * 2));
      this.attackOrbitSpeed = this.randomRange(1.7F, 2.45F);
      this.attackOrbitDirection = Math.random() > 0.5 ? 1.0F : -1.0F;
   }

   private float randomRange(float min, float max) {
      return min + (float)Math.random() * (max - min);
   }

   private float getLookYaw(class_243 from, class_243 to) {
      double dx = to.field_1352 - from.field_1352;
      double dz = to.field_1350 - from.field_1350;
      return (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
   }

   private float getLookPitch(class_243 from, class_243 to) {
      double dx = to.field_1352 - from.field_1352;
      double dy = to.field_1351 - from.field_1351;
      double dz = to.field_1350 - from.field_1350;
      double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
      return class_3532.method_15363((float)(-Math.toDegrees(Math.atan2(dy, horizontalDistance))), -35.0F, 35.0F);
   }

   private void clearAttackTarget() {
      this.attackTargetId = Integer.MIN_VALUE;
      this.attackStartedAt = 0L;
      this.lastAttackAt = 0L;
      this.attackReturnStartedAt = 0L;
      this.attackReturnStartPos = new class_243(0.0, 0.0, 0.0);
      this.attackLookYaw = 0.0F;
      this.attackLookPitch = 0.0F;
      this.attackLookInitialized = false;
   }
}
