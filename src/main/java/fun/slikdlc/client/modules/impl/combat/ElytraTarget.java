package fun.slikdlc.client.modules.impl.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventMove;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.combat.PredictUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1671;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class ElytraTarget extends Module {
   private static final String LABEL_ELEMENTS = "Elements";
   private static final String LABEL_RESOLVER = "Resolver";
   private static final String LABEL_RENDER_BOX = "Render Box";
   private static final String LABEL_MACE_TARGET = "Mace Target";
   private static final String LABEL_RESOLVER_TYPE = "Resolver Type";
   private static final String MODE_PREDICTIVE = "Predictive";
   private static final String MODE_UP_POSITION = "Up Position";
   private static final String LABEL_ROTATION_TYPE = "Rotation Type";
   private static final String MODE_MAIN = "Main";
   private static final String MODE_ALTERNATIVE = "Alternative";
   private static final String LABEL_AIM = "Aim";
   private static final String AIM_HEAD = "Head";
   private static final String AIM_BODY = "Body";
   private static final String AIM_FEET = "Feets";
   private static final String LABEL_DISTANCE = "Distance";
   private static final String LABEL_PRE_DISTANCE = "Pre Distance";
   private static final String LABEL_PREDICT_DISTANCE = "Predict Distance";
   private static final String LABEL_MIN_SPEED = "Min Target Speed";
   private static final String LABEL_STOP_BOOSTER_DISTANCE = "Stop Booster Distance";
   private static final String LABEL_UP_DISTANCE = "Up Position Distance";
   private static final String LABEL_DIVIDE_SPEED = "Divide Speed";
   private static final String MODULE_DESCRIPTION = "Поворачивает и предсказывает игроков на элитре";
   private static final double SPEED_SCALE = 20.0D;
   private static final double DIVIDE_FACTOR = 0.5D;

   public static ElytraTarget INSTANCE = new ElytraTarget();

   private static final class_2960 GLOW_TEXTURE = class_2960.method_60655("slikdlc", "textures/trajectories/glow.png");
   private static final int[][] BOX_EDGES = new int[][]{{0, 2}, {2, 6}, {6, 4}, {4, 0}, {1, 3}, {3, 7}, {7, 5}, {5, 1}, {0, 1}, {2, 3}, {6, 7}, {4, 5}};

   private final ListSetting elementsSetting = new ListSetting(
      LABEL_ELEMENTS,
      new BooleanSetting(LABEL_RESOLVER, true),
      new BooleanSetting(LABEL_RENDER_BOX, true),
      new BooleanSetting(LABEL_MACE_TARGET, true)
   );

   private final ListSetting resolverTypeSetting = new ListSetting(
      LABEL_RESOLVER_TYPE,
      new BooleanSetting(MODE_PREDICTIVE, true),
      new BooleanSetting(MODE_UP_POSITION, false)
   ).visible(() -> this.elementsSetting.is(LABEL_RESOLVER));

   private final ModeSetting rotationTypeSetting = new ModeSetting(LABEL_ROTATION_TYPE, MODE_MAIN, MODE_MAIN, MODE_ALTERNATIVE);

   private final ModeSetting aimSetting = new ModeSetting(LABEL_AIM, AIM_HEAD, AIM_HEAD, AIM_BODY, AIM_FEET)
      .visible(() -> this.rotationTypeSetting.is(MODE_ALTERNATIVE));

   private final FloatSetting distanceSetting = new FloatSetting(LABEL_DISTANCE, 3.0F, 1.0F, 5.0F, 1.0F);

   private final FloatSetting preDistanceSetting = new FloatSetting(LABEL_PRE_DISTANCE, 30.0F, 1.0F, 30.0F, 1.0F);

   private final FloatSetting predictDistanceSetting = new FloatSetting(LABEL_PREDICT_DISTANCE, 1.7F, 0.1F, 5.0F, 0.1F)
      .visible(() -> this.elementsSetting.is(LABEL_RESOLVER) && this.resolverTypeSetting.is(MODE_PREDICTIVE));

   public final FloatSetting forward = this.predictDistanceSetting;

   private final FloatSetting minTargetSpeedSetting = new FloatSetting(LABEL_MIN_SPEED, 20.0F, 1.0F, 100.0F, 0.1F)
      .visible(() -> this.elementsSetting.is(LABEL_RESOLVER) && this.resolverTypeSetting.is(MODE_PREDICTIVE));

   private final FloatSetting stopBoosterDistanceSetting = new FloatSetting(LABEL_STOP_BOOSTER_DISTANCE, 2.0F, 0.1F, 10.0F, 0.1F)
      .visible(() -> this.elementsSetting.is(LABEL_RESOLVER) && this.resolverTypeSetting.is(MODE_PREDICTIVE));

   private final FloatSetting upPositionDistanceSetting = new FloatSetting(LABEL_UP_DISTANCE, 20.0F, 0.0F, 30.0F, 1.0F);

   private final BooleanSetting divideSpeedSetting = new BooleanSetting(LABEL_DIVIDE_SPEED, false)
      .visible(() -> this.elementsSetting.is(LABEL_RESOLVER) && this.resolverTypeSetting.is(MODE_PREDICTIVE));

   private double previousTargetSpeed;
   private double currentTargetSpeed;

   private class_238 smoothedPredictionBox;
   private class_1309 smoothedTarget;

   public ElytraTarget() {
      super("ElytraTarget", MODULE_DESCRIPTION, Module.ModuleCategory.COMBAT);
      this.addSettings(
         new Setting[]{
            this.elementsSetting,
            this.resolverTypeSetting,
            this.rotationTypeSetting,
            this.aimSetting,
            this.distanceSetting,
            this.preDistanceSetting,
            this.predictDistanceSetting,
            this.minTargetSpeedSetting,
            this.stopBoosterDistanceSetting,
            this.upPositionDistanceSetting,
            this.divideSpeedSetting
         }
      );
      INSTANCE = this;
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (!this.isEnable() || !this.elementsSetting.is(LABEL_RENDER_BOX)) {
         return;
      }

      Aura aura = ModuleClass.aura;
      if (aura == null || aura.getTarget() == null || this.getPredictDistance() <= 0.0F) {
         this.resetPredictionSmoothing();
         return;
      }

      class_1309 target = aura.getTarget();
      if (!target.method_5805() || !target.method_6128()) {
         this.resetPredictionSmoothing();
         return;
      }

      class_238 predictedBox = this.buildPredictedBox(target);
      this.renderPredictionBox(event, this.smoothPredictionBox(target, predictedBox));
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (!this.isEnable() || mc.field_1724 == null || mc.field_1724.field_6012 % 3 != 0) {
         return;
      }

      Aura aura = ModuleClass.aura;
      if (aura == null || aura.getTarget() == null) {
         return;
      }

      class_1309 target = aura.getTarget();
      double deltaX = target.method_23317() - target.field_6014;
      double deltaY = target.method_23318() - target.field_6036;
      double deltaZ = target.method_23321() - target.field_5969;
      double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * SPEED_SCALE;

      this.previousTargetSpeed = this.currentTargetSpeed;
      this.currentTargetSpeed = speed;
   }

   @EventLink
   public void onMove(EventMove event) {
      if (!this.isEnable() || !this.divideSpeedSetting.isState()) {
         return;
      }

      if (this.isTargetWithinRange(this.stopBoosterDistanceSetting.get() / 2.0F)) {
         class_243 motion = event.getMovePos();
         if (motion != null) {
            event.setMovePos(motion.method_18805(DIVIDE_FACTOR, DIVIDE_FACTOR, DIVIDE_FACTOR));
         }
      }
   }

   @Override
   public void onDisable() {
      this.resetPredictionSmoothing();
      super.onDisable();
   }

   public boolean isUsingResolver() {
      return this.elementsSetting.is(LABEL_RESOLVER);
   }

   public boolean canResolveElytraTarget() {
      Aura aura = ModuleClass.aura;
      return aura != null
         && aura.getTarget() != null
         && mc.field_1724 != null
         && mc.field_1724.method_6128()
         && mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
   }

   public boolean isTargetWithinRange(float distance) {
      Aura aura = ModuleClass.aura;
      if (aura == null || aura.getTarget() == null || mc.field_1724 == null) {
         return false;
      }

      class_1309 target = aura.getTarget();
      class_243 targetPos = target.method_19538();
      float predictDistance = this.getPredictDistance();
      if (predictDistance > 0.0F) {
         class_243 delta = new class_243(
            target.method_23317() - target.field_6014,
            target.method_23318() - target.field_6036,
            target.method_23321() - target.field_5969
         );
         if (delta.method_1027() > 1.0E-6) {
            targetPos = targetPos.method_1019(delta.method_1029().method_1021(predictDistance));
         }
      }

      return mc.field_1724.method_19538().method_1022(targetPos) < distance;
   }

   public float getPredictDistance() {
      Aura aura = ModuleClass.aura;
      boolean canPredict = aura != null
         && aura.getTarget() != null
         && this.elementsSetting.is(LABEL_RESOLVER)
         && this.resolverTypeSetting.is(MODE_PREDICTIVE)
         && aura.getTarget().method_6128()
         && this.isPredictionSpeedValid();
      return canPredict ? this.predictDistanceSetting.get() : 0.0F;
   }

   public float getUpPositionDistance() {
      return this.resolverTypeSetting.is(MODE_UP_POSITION) && this.isFireworkBoost()
         ? this.upPositionDistanceSetting.get()
         : 0.0F;
   }

   public double getPreviousTargetSpeed() {
      return this.previousTargetSpeed;
   }

   public double getCurrentTargetSpeed() {
      return this.currentTargetSpeed;
   }

   public ListSetting getElementsSetting() {
      return this.elementsSetting;
   }

   public ListSetting getResolverTypeSetting() {
      return this.resolverTypeSetting;
   }

   public ModeSetting getRotationTypeSetting() {
      return this.rotationTypeSetting;
   }

   public ModeSetting getAimSetting() {
      return this.aimSetting;
   }

   public FloatSetting getDistanceSetting() {
      return this.distanceSetting;
   }

   public FloatSetting getPreDistanceSetting() {
      return this.preDistanceSetting;
   }

   public boolean isMaceTargetEnabled() {
      return this.elementsSetting.is(LABEL_MACE_TARGET);
   }

   private boolean isPredictionSpeedValid() {
      boolean speedReached = this.currentTargetSpeed >= this.minTargetSpeedSetting.get()
         || this.currentTargetSpeed != this.previousTargetSpeed && this.currentTargetSpeed == 0.0D;
      return this.canResolveElytraTarget() && speedReached;
   }

   private boolean isFireworkBoost() {
      if (mc.field_1724 == null || mc.field_1687 == null || !mc.field_1724.method_6128()) {
         return false;
      }

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1671 firework && firework.method_5805() && firework.method_5739(mc.field_1724) <= 1.5F) {
            return true;
         }
      }

      return mc.field_1724.method_18798().method_1033() > 1.2D;
   }

   private class_238 buildPredictedBox(class_1309 target) {
      class_238 currentBox = target.method_5829();
      int predictTicks = Math.max(0, Math.round(this.getPredictDistance()));
      class_243 predictedCenter = PredictUtils.predict(target, currentBox.method_1005(), predictTicks);
      class_243 offset = predictedCenter.method_1020(currentBox.method_1005());
      return currentBox.method_997(offset);
   }

   private class_238 smoothPredictionBox(class_1309 target, class_238 predictedBox) {
      if (this.smoothedPredictionBox != null
         && this.smoothedTarget == target
         && !(this.smoothedPredictionBox.method_1005().method_1025(predictedBox.method_1005()) > 144.0)) {
         double distance = Math.sqrt(this.smoothedPredictionBox.method_1005().method_1025(predictedBox.method_1005()));
         double smoothFactor = class_3532.method_15350(0.08 + distance * 0.035, 0.08, 0.18);
         this.smoothedPredictionBox = this.lerpBox(this.smoothedPredictionBox, predictedBox, smoothFactor);
         return this.smoothedPredictionBox;
      } else {
         this.smoothedPredictionBox = predictedBox;
         this.smoothedTarget = target;
         return predictedBox;
      }
   }

   private void resetPredictionSmoothing() {
      this.smoothedPredictionBox = null;
      this.smoothedTarget = null;
   }

   private class_238 lerpBox(class_238 from, class_238 to, double factor) {
      return new class_238(
         class_3532.method_16436(factor, from.field_1323, to.field_1323),
         class_3532.method_16436(factor, from.field_1322, to.field_1322),
         class_3532.method_16436(factor, from.field_1321, to.field_1321),
         class_3532.method_16436(factor, from.field_1320, to.field_1320),
         class_3532.method_16436(factor, from.field_1325, to.field_1325),
         class_3532.method_16436(factor, from.field_1324, to.field_1324)
      );
   }

   private void renderPredictionBox(Event3DRender event, class_238 box) {
      class_4587 matrices = event.getMatrices();
      class_4184 camera = event.getCamera();
      class_243 cameraPos = camera.method_19326();
      int themeColor = ColorUtils.getThemeColor();
      int outerColor = ColorUtils.setAlphaColor(themeColor, 118);
      int midColor = ColorUtils.setAlphaColor(ColorUtils.interpolateColor(themeColor, -1, 0.24F), 210);
      int coreColor = ColorUtils.setAlphaColor(ColorUtils.interpolateColor(themeColor, -1, 0.6F), 255);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShaderTexture(0, GLOW_TEXTURE);
      RenderSystem.setShader(class_10142.field_53880);
      Matrix4f matrix = matrices.method_23760().method_23761();
      class_287 quads = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      this.addGlowBox(quads, matrix, cameraPos, box, outerColor, 0.17F);
      this.addGlowBox(quads, matrix, cameraPos, box, midColor, 0.13F);
      this.addGlowBox(quads, matrix, cameraPos, box, coreColor, 0.11F);
      class_286.method_43433(quads.method_60800());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.defaultBlendFunc();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   private void addGlowBox(class_287 buffer, Matrix4f matrix, class_243 camera, class_238 box, int color, float thickness) {
      class_243[] corners = this.getBoxVectors(box);

      for (int[] edge : BOX_EDGES) {
         this.addGlowEdge(buffer, matrix, camera, corners[edge[0]], corners[edge[1]], color, thickness);
      }
   }

   private void addGlowEdge(class_287 buffer, Matrix4f matrix, class_243 camera, class_243 start, class_243 end, int color, float thickness) {
      class_243 edge = end.method_1020(start);
      if (!(edge.method_1027() <= 1.0E-6)) {
         class_243 direction = edge.method_1029();
         double overlap = thickness * 0.22F;
         start = start.method_1020(direction.method_1021(overlap));
         end = end.method_1019(direction.method_1021(overlap));
         edge = end.method_1020(start);
         class_243 center = start.method_1019(end).method_1021(0.5);
         class_243 toCamera = camera.method_1020(center);
         if (toCamera.method_1027() <= 1.0E-6) {
            toCamera = new class_243(0.0, 1.0, 0.0);
         }

         class_243 side = edge.method_1036(toCamera);
         if (side.method_1027() <= 1.0E-6) {
            side = edge.method_1036(new class_243(0.0, 1.0, 0.0));
            if (side.method_1027() <= 1.0E-6) {
               side = edge.method_1036(new class_243(1.0, 0.0, 0.0));
            }
         }

         side = side.method_1029().method_1021(thickness * 0.48F);
         class_243 p1 = start.method_1019(side).method_1020(camera);
         class_243 p2 = start.method_1020(side).method_1020(camera);
         class_243 p3 = end.method_1020(side).method_1020(camera);
         class_243 p4 = end.method_1019(side).method_1020(camera);
         float[] rgba = ColorUtils.rgba(color);
         buffer.method_22918(matrix, (float)p1.field_1352, (float)p1.field_1351, (float)p1.field_1350)
            .method_22913(0.4F, 0.0F)
            .method_22915(rgba[0], rgba[1], rgba[2], rgba[3]);
         buffer.method_22918(matrix, (float)p2.field_1352, (float)p2.field_1351, (float)p2.field_1350)
            .method_22913(0.4F, 1.0F)
            .method_22915(rgba[0], rgba[1], rgba[2], rgba[3]);
         buffer.method_22918(matrix, (float)p3.field_1352, (float)p3.field_1351, (float)p3.field_1350)
            .method_22913(0.4F, 1.0F)
            .method_22915(rgba[0], rgba[1], rgba[2], rgba[3]);
         buffer.method_22918(matrix, (float)p4.field_1352, (float)p4.field_1351, (float)p4.field_1350)
            .method_22913(0.4F, 0.0F)
            .method_22915(rgba[0], rgba[1], rgba[2], rgba[3]);
      }
   }

   private class_243[] getBoxVectors(class_238 box) {
      return new class_243[]{
         new class_243(box.field_1323, box.field_1322, box.field_1321),
         new class_243(box.field_1323, box.field_1325, box.field_1321),
         new class_243(box.field_1320, box.field_1322, box.field_1321),
         new class_243(box.field_1320, box.field_1325, box.field_1321),
         new class_243(box.field_1323, box.field_1322, box.field_1324),
         new class_243(box.field_1323, box.field_1325, box.field_1324),
         new class_243(box.field_1320, box.field_1322, box.field_1324),
         new class_243(box.field_1320, box.field_1325, box.field_1324)
      };
   }
}
