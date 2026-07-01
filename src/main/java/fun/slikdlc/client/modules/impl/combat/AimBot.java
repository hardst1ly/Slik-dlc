package fun.slikdlc.client.modules.impl.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventGameUpdate;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.combat.components.gcd.GCDUtil;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_10142;
import net.minecraft.class_1309;
import net.minecraft.class_1588;
import net.minecraft.class_1642;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class AimBot extends Module {
   public static AimBot INSTANCE = new AimBot();
   private final ListSetting targetTypes = new ListSetting(
      "Типы целей",
      new BooleanSetting("Игроки", true),
      new BooleanSetting("В броне", true),
      new BooleanSetting("Без брони", false),
      new BooleanSetting("Мобы", false),
      new BooleanSetting("Зомби", false)
   );
   private final FloatSetting range = new FloatSetting("Дистанция", 40.0F, 10.0F, 100.0F, 1.0F);
   private final FloatSetting aimTime = new FloatSetting("Время наводки (тики)", 10.0F, 0.0F, 40.0F, 1.0F);
   private final BooleanSetting silentRotations = new BooleanSetting("Тихие повороты", true);
   private final BooleanSetting showCrosshair = new BooleanSetting("Показать прицел", true);
   private final FloatSetting crosshairSize = new FloatSetting("Размер прицела", 1.0F, 0.3F, 3.0F, 0.1F);
   private class_1309 target = null;
   private boolean isAiming = false;
   private float aimProgress = 0.0F;
   private Rotation targetRotation = null;

   public AimBot() {
      super("AimBot", "Авто-наведение для лука и арбалета", Module.ModuleCategory.COMBAT);
      this.addSettings(new Setting[]{this.targetTypes, this.range, this.aimTime, this.silentRotations, this.showCrosshair, this.crosshairSize});
   }

   private class_2960 getCrosshairTexture() {
      return class_2960.method_60655("slikdlc", "textures/cross/hit.png");
   }

   private boolean isHoldingBowOrCrossbow() {
      class_1799 mainHand = mc.field_1724.method_6047();
      class_1799 offHand = mc.field_1724.method_6079();
      return mainHand.method_7909() instanceof class_1753
         || mainHand.method_7909() instanceof class_1764
         || offHand.method_7909() instanceof class_1753
         || offHand.method_7909() instanceof class_1764;
   }

   private boolean isUsingBowOrCrossbow() {
      return mc.field_1724.method_6115() && this.isHoldingBowOrCrossbow();
   }

   private boolean isValidTarget(class_1309 entity) {
      if (entity == mc.field_1724) {
         return false;
      } else if (!entity.method_5805() || entity.method_6032() <= 0.0F) {
         return false;
      } else if (!(entity instanceof class_1657)) {
         if (entity instanceof class_1642) {
            return this.targetTypes.is("Зомби");
         } else {
            return entity instanceof class_1588 ? this.targetTypes.is("Мобы") : false;
         }
      } else if (!this.targetTypes.is("Игроки")) {
         return false;
      } else if (SlikDlc.INSTANCE.friendStorage.isFriend(entity.method_5477().getString())) {
         return false;
      } else {
         boolean hasArmor = false;
         class_1657 player = (class_1657)entity;

         for (class_1799 armor : player.method_5661()) {
            if (!armor.method_7960()) {
               hasArmor = true;
               break;
            }
         }

         if (this.targetTypes.is("В броне") && hasArmor) {
            return true;
         } else {
            return this.targetTypes.is("Без брони") && !hasArmor ? true : !this.targetTypes.is("В броне") && !this.targetTypes.is("Без брони");
         }
      }
   }

   private class_1309 findBestTarget() {
      List<class_1309> targets = new ArrayList<>();
      class_238 searchBox = mc.field_1724.method_5829().method_1014(this.range.getValue().floatValue());

      for (class_1309 entity : mc.field_1687.method_8390(class_1309.class, searchBox, e -> true)) {
         if (this.isValidTarget(entity)) {
            double dist = mc.field_1724.method_5739(entity);
            if (!(dist > this.range.getValue().floatValue())) {
               targets.add(entity);
            }
         }
      }

      if (targets.isEmpty()) {
         return null;
      } else {
         targets.sort(Comparator.comparingDouble(entityx -> mc.field_1724.method_5739(entityx)));
         return targets.get(0);
      }
   }

   private Rotation calculateBowRotation(class_1309 target) {
      class_243 eyes = mc.field_1724.method_33571();
      class_243 targetPos = target.method_5829().method_1005();
      double dx = targetPos.field_1352 - eyes.field_1352;
      double dy = targetPos.field_1351 - eyes.field_1351;
      double dz = targetPos.field_1350 - eyes.field_1350;
      double distance = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distance)));
      return new Rotation(yaw, pitch);
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (this.showCrosshair.isState() && this.target != null && this.isAiming) {
         float partialTicks = event.getTickDelta();
         class_243 targetPos = new class_243(
            class_3532.method_16436(partialTicks, this.target.field_6038, this.target.method_23317()),
            class_3532.method_16436(partialTicks, this.target.field_5971, this.target.method_23318()) + this.target.method_17682() / 2.0,
            class_3532.method_16436(partialTicks, this.target.field_5989, this.target.method_23321())
         );
         class_243 cameraPos = mc.field_1773.method_19418().method_19326();
         class_4587 matrices = event.getMatrices();
         double renderX = targetPos.field_1352 - cameraPos.field_1352;
         double renderY = targetPos.field_1351 - cameraPos.field_1351;
         double renderZ = targetPos.field_1350 - cameraPos.field_1350;
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.setShaderTexture(0, this.getCrosshairTexture());
         RenderSystem.setShader(class_10142.field_53880);
         matrices.method_22903();
         matrices.method_22904(renderX, renderY, renderZ);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
         float size = this.crosshairSize.get() * 0.5F;
         int alpha = (int)(255.0F * this.aimProgress);
         int color = ColorUtils.getThemeColor();
         int r = color >> 16 & 0xFF;
         int g = color >> 8 & 0xFF;
         int b = color & 0xFF;
         Matrix4f matrix = matrices.method_23760().method_23761();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
         buffer.method_22918(matrix, -size, -size, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, alpha);
         buffer.method_22918(matrix, -size, size, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, alpha);
         buffer.method_22918(matrix, size, size, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, alpha);
         buffer.method_22918(matrix, size, -size, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, alpha);
         class_286.method_43433(buffer.method_60800());
         matrices.method_22909();
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   @EventLink
   public void onGameUpdate(EventGameUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.isAiming = this.isUsingBowOrCrossbow();
         if (this.isAiming) {
            class_1309 newTarget = this.findBestTarget();
            if (newTarget != null) {
               if (this.target != newTarget) {
                  this.target = newTarget;
                  this.aimProgress = 0.0F;
               }

               Rotation newRotation = this.calculateBowRotation(this.target);
               float maxStep = 1.0F / Math.max(1.0F, this.aimTime.getValue().floatValue());
               this.aimProgress = Math.min(this.aimProgress + maxStep, 1.0F);
               float currentYaw = mc.field_1724.method_36454();
               float currentPitch = mc.field_1724.method_36455();
               float targetYaw = newRotation.getYaw();
               float targetPitch = newRotation.getPitch();
               float yawDiff = class_3532.method_15393(targetYaw - currentYaw);
               float pitchDiff = targetPitch - currentPitch;
               float stepYaw = yawDiff * this.aimProgress;
               float stepPitch = pitchDiff * this.aimProgress;
               this.targetRotation = new Rotation(currentYaw + stepYaw, currentPitch + stepPitch);
            }
         } else {
            this.target = null;
            this.targetRotation = null;
            this.aimProgress = 0.0F;
         }
      }
   }

   @EventLink
   public void onUpdate(EventGameUpdate ignoredghj) {
      if (this.target != null && this.isAiming && this.targetRotation != null) {
         if (this.silentRotations.isState()) {
            float gcd = GCDUtil.getGCD();
            float yaw = this.targetRotation.getYaw();
            float pitch = this.targetRotation.getPitch();
            yaw -= (yaw - mc.field_1724.method_36454()) % gcd;
            pitch -= (pitch - mc.field_1724.method_36455()) % gcd;
            RotationStorage.update(new Rotation(yaw, pitch), 180.0F, 180.0F, 45.0F, 45.0F, 0, 2, false);
         } else {
            mc.field_1724.method_36456(this.targetRotation.getYaw());
            mc.field_1724.method_36457(this.targetRotation.getPitch());
         }
      }
   }

   public class_1309 getTarget() {
      return this.target;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.target = null;
      this.isAiming = false;
      this.aimProgress = 0.0F;
      this.targetRotation = null;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.target = null;
      this.isAiming = false;
      this.aimProgress = 0.0F;
      this.targetRotation = null;
   }
}
