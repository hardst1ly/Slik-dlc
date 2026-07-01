package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.storages.implement.helpertstorages.Theme;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import net.minecraft.class_10142;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3882;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Cosmetics extends Module {
   public static Cosmetics INSTANCE = new Cosmetics();
   private static final float PI_STEP = 0.06981317F;
   private static final float WING_SCALE = 1.0F;
   private static final float FLAP_SPEED = 1.6F;
   private static final float FLAP_AMPLITUDE = 25.0F;
   private static final int NIMBUS_ARMS = 2;
   private static final int NIMBUS_SEGMENTS = 17;
   private static final float NIMBUS_RADIUS = 0.45F;
   private static final float NIMBUS_BASE_SIZE = 0.23F;
   private static final double NIMBUS_STEP_RADIANS = 0.11;
   private static final int NIMBUS_MAX_ALPHA = 255;
   private static final int NIMBUS_ALPHA_FALLOFF = 9;
   private static final float NIMBUS_SPEED = 170.0F;
   private static final float CLASSIC_WING_DEFAULT_SPREAD = 8.0F;
   private static final int CLASSIC_WING_DEFAULT_ALPHA = 220;
   private static final Cosmetics.ClassicWingPoint[] CLASSIC_WING_SHAPE = new Cosmetics.ClassicWingPoint[]{
      new Cosmetics.ClassicWingPoint(0.08F, 0.1F, 0.88F),
      new Cosmetics.ClassicWingPoint(0.28F, 0.34F, 0.78F),
      new Cosmetics.ClassicWingPoint(0.56F, 0.82F, 0.62F),
      new Cosmetics.ClassicWingPoint(0.86F, 0.3F, 0.52F),
      new Cosmetics.ClassicWingPoint(1.14F, 0.46F, 0.4F),
      new Cosmetics.ClassicWingPoint(1.24F, 0.04F, 0.3F),
      new Cosmetics.ClassicWingPoint(1.02F, -0.18F, 0.28F),
      new Cosmetics.ClassicWingPoint(1.18F, -0.64F, 0.22F),
      new Cosmetics.ClassicWingPoint(0.86F, -0.46F, 0.2F),
      new Cosmetics.ClassicWingPoint(0.8F, -0.98F, 0.14F),
      new Cosmetics.ClassicWingPoint(0.54F, -0.74F, 0.16F),
      new Cosmetics.ClassicWingPoint(0.3F, -1.16F, 0.12F),
      new Cosmetics.ClassicWingPoint(0.1F, -0.54F, 0.18F)
   };
   private final ListSetting cosmetics = new ListSetting(
      "Косметика",
      new BooleanSetting("Нимб", true),
      new BooleanSetting("Крылья", true),
      new BooleanSetting("Крылья 2", false),
      new BooleanSetting("Китайская шляпа", true)
   );
   private final BooleanSetting butterflyWingAnimation = new BooleanSetting("Анимация крыльев", true).visible(() -> this.cosmetics.is("Крылья"));
   private final FloatSetting butterflyWingSize = new FloatSetting("Размер", 1.0F, 0.65F, 1.8F, 0.05F).visible(() -> this.cosmetics.is("Крылья"));
   private final BooleanSetting classicWingAnimation = new BooleanSetting("Анимация крыльев", true).visible(() -> this.cosmetics.is("Крылья 2"));
   private final FloatSetting classicWingSize = new FloatSetting("Размер", 1.0F, 0.65F, 1.8F, 0.05F).visible(() -> this.cosmetics.is("Крылья 2"));
   private float selfClassicBodyYaw;
   private boolean selfClassicBodyYawInitialized;
   private boolean lastButterflySelected;
   private boolean lastClassicSelected;

   public Cosmetics() {
      super("Cosmetics", "Визуальные украшения", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.cosmetics, this.butterflyWingAnimation, this.butterflyWingSize, this.classicWingAnimation, this.classicWingSize});
   }

   @Override
   public void onDisable() {
      this.selfClassicBodyYawInitialized = false;
      this.lastButterflySelected = false;
      this.lastClassicSelected = false;
      super.onDisable();
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.syncWingSelectionState();
         if (this.cosmetics.is("Нимб")) {
            this.renderNimbus(event);
         }

         boolean renderButterfly = this.cosmetics.is("Крылья");
         boolean renderClassic = this.cosmetics.is("Крылья 2");
         if (renderButterfly || renderClassic) {
            float tickDelta = event.getTickDelta();
            class_4587 matrices = event.getMatrices();
            class_243 cameraPos = event.getCamera().method_19326();

            for (class_1657 player : mc.field_1687.method_18456()) {
               if (this.shouldRenderCosmeticForPlayer(player) && (player != mc.field_1724 || !mc.field_1690.method_31044().method_31034())) {
                  if (renderButterfly) {
                     this.renderButterflyWings(player, tickDelta, matrices, cameraPos);
                  }

                  if (renderClassic) {
                     this.renderClassicWings(player, tickDelta, matrices, cameraPos);
                  }
               }
            }
         }
      }
   }

   private void renderButterflyWings(class_1657 player, float tickDelta, class_4587 matrices, class_243 cameraPos) {
      if (!player.method_6128() && player.method_18376() != class_4050.field_18079 && !player.method_20232()) {
         class_243 velocity = player.method_18798();
         float bodyYaw = class_3532.method_16439(tickDelta, player.field_6220, player.field_6283);
         float yawRad = bodyYaw * (float) (Math.PI / 180.0);
         class_243 forward = new class_243(-class_3532.method_15374(yawRad), 0.0, class_3532.method_15362(yawRad));
         class_243 sideways = new class_243(forward.field_1350, 0.0, -forward.field_1352);
         float forwardMove = (float)(velocity.field_1352 * forward.field_1352 + velocity.field_1350 * forward.field_1350);
         float strafeMove = (float)(velocity.field_1352 * sideways.field_1352 + velocity.field_1350 * sideways.field_1350);
         float verticalMove = (float)velocity.field_1351;
         boolean animated = this.butterflyWingAnimation.isState();
         float smoothLean = animated ? class_3532.method_15363(-forwardMove * 140.0F - verticalMove * 48.0F, -24.0F, 26.0F) : 0.0F;
         float smoothStrafe = animated ? class_3532.method_15363(strafeMove * 90.0F, -10.0F, 10.0F) : 0.0F;
         float wingSpring = animated
            ? class_3532.method_15363(Math.abs(forwardMove) * 0.95F + Math.abs(strafeMove) * 0.65F + Math.abs(verticalMove) * 0.75F, 0.0F, 1.7F)
            : 0.0F;
         float anim = (player.field_6012 + tickDelta) * 0.22F * 1.6F + wingSpring * 0.4F;
         float sin = animated ? class_3532.method_15374(anim) : 0.0F;
         float cos = animated ? class_3532.method_15362(anim) : 0.0F;
         float spreadAngle = 18.0F + wingSpring * 5.0F;
         float pitchAngle = 13.0F + smoothLean * 0.3F + cos * 4.0F;
         float rollAngle = sin * 25.0F + smoothStrafe * 0.75F;
         class_4050 pose = player.method_18376();
         boolean fallFlying = player.method_6128();
         boolean horizontalPose = pose == class_4050.field_18079 || fallFlying;
         if (horizontalPose) {
            spreadAngle -= 4.0F;
            pitchAngle -= 6.0F;
            rollAngle *= 0.72F;
         }

         if (player.method_5715()) {
            spreadAngle -= 3.0F;
            pitchAngle += 8.0F;
         }

         double px = class_3532.method_16436(tickDelta, player.field_6014, player.method_23317()) - cameraPos.field_1352;
         double py = class_3532.method_16436(tickDelta, player.field_6036, player.method_23318()) - cameraPos.field_1351;
         double pz = class_3532.method_16436(tickDelta, player.field_5969, player.method_23321()) - cameraPos.field_1350;
         matrices.method_22903();
         matrices.method_22904(px, py, pz);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-bodyYaw));
         this.applyBackPoseTransform(matrices, player, tickDelta, pose, fallFlying);
         int theme = this.resolveCosmeticThemeColor();
         int topColor = ColorUtils.setAlphaColor(theme, 132);
         int bottomColor = ColorUtils.setAlphaColor(ColorUtils.darken(theme, 0.85F), 102);
         int outlineColor = ColorUtils.setAlphaColor(ColorUtils.darken(theme, 0.58F), 214);
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(770, 771);
         RenderSystem.setShader(class_10142.field_53876);
         float butterflyScale = 1.0F * this.butterflyWingSize.get();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
         this.renderButterflyWing(buffer, matrices, 1.0F, spreadAngle, pitchAngle, rollAngle, butterflyScale, topColor, bottomColor);
         this.renderButterflyWing(buffer, matrices, -1.0F, spreadAngle, pitchAngle, rollAngle, butterflyScale, topColor, bottomColor);
         class_286.method_43433(buffer.method_60800());
         RenderSystem.lineWidth(1.9F);
         class_287 outlineBuffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);
         this.renderButterflyWingOutline(outlineBuffer, matrices, 1.0F, spreadAngle, pitchAngle, rollAngle, butterflyScale, outlineColor);
         this.renderButterflyWingOutline(outlineBuffer, matrices, -1.0F, spreadAngle, pitchAngle, rollAngle, butterflyScale, outlineColor);
         class_286.method_43433(outlineBuffer.method_60800());
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         matrices.method_22909();
      }
   }

   private void renderClassicWings(class_1657 player, float tickDelta, class_4587 matrices, class_243 cameraPos) {
      if (player.method_5805() && !player.method_5767()) {
         if (!player.method_6128() && player.method_18376() != class_4050.field_18079 && !player.method_20232()) {
            double px = class_3532.method_16436(tickDelta, player.field_6014, player.method_23317()) - cameraPos.field_1352;
            double py = class_3532.method_16436(tickDelta, player.field_6036, player.method_23318()) - cameraPos.field_1351;
            double pz = class_3532.method_16436(tickDelta, player.field_5969, player.method_23321()) - cameraPos.field_1350;
            float bodyYaw = this.resolveClassicBodyYaw(player, tickDelta);
            class_243 velocity = player.method_18798();
            float yawRad = bodyYaw * (float) (Math.PI / 180.0);
            class_243 forward = new class_243(-class_3532.method_15374(yawRad), 0.0, class_3532.method_15362(yawRad));
            class_243 sideways = new class_243(forward.field_1350, 0.0, -forward.field_1352);
            float forwardMove = (float)(velocity.field_1352 * forward.field_1352 + velocity.field_1350 * forward.field_1350);
            float strafeMove = (float)(velocity.field_1352 * sideways.field_1352 + velocity.field_1350 * sideways.field_1350);
            float verticalMove = (float)velocity.field_1351;
            boolean animated = this.classicWingAnimation.isState();
            float smoothLean = animated ? class_3532.method_15363(-forwardMove * 140.0F - verticalMove * 48.0F, -24.0F, 26.0F) : 0.0F;
            float smoothStrafe = animated ? class_3532.method_15363(strafeMove * 90.0F, -10.0F, 10.0F) : 0.0F;
            float wingSpring = animated
               ? class_3532.method_15363(Math.abs(forwardMove) * 0.95F + Math.abs(strafeMove) * 0.65F + Math.abs(verticalMove) * 0.75F, 0.0F, 1.7F)
               : 0.0F;
            float anim = (player.field_6012 + tickDelta) * 0.22F * 1.6F + wingSpring * 0.4F;
            float sin = animated ? class_3532.method_15374(anim) : 0.0F;
            float cos = animated ? class_3532.method_15362(anim) : 0.0F;
            float spreadAngle = 18.0F + wingSpring * 5.0F;
            float pitchAngle = 13.0F + smoothLean * 0.3F + cos * 4.0F;
            float rollAngle = sin * 25.0F + smoothStrafe * 0.75F;
            class_4050 pose = player.method_18376();
            boolean fallFlying = player.method_6128();
            boolean horizontalPose = pose == class_4050.field_18079 || fallFlying;
            if (horizontalPose) {
               spreadAngle -= 4.0F;
               pitchAngle -= 6.0F;
               rollAngle *= 0.72F;
            }

            if (player.method_5715()) {
               spreadAngle -= 3.0F;
               pitchAngle += 8.0F;
            }

            Cosmetics.ClassicWingPose wingPose = this.resolveClassicWingPose(player, tickDelta, pose);
            float open = spreadAngle * wingPose.openMultiplier;
            float scale = wingPose.scaleMultiplier * this.classicWingSize.get();
            float animatedSidePitch = wingPose.sidePitch + pitchAngle * 0.18F;
            float animatedSideRoll = wingPose.sideRoll + rollAngle * 0.2F;
            int theme = this.resolveCosmeticThemeColor();
            int baseColor = ColorUtils.setAlphaColor(theme, 220);
            int glowColor = ColorUtils.setAlphaColor(ColorUtils.interpolate(theme, -1, 0.28F), Math.round(48.4F));
            int coreColor = ColorUtils.setAlphaColor(ColorUtils.interpolate(theme, -1, 0.55F), Math.round(57.199997F));
            int outlineColor = ColorUtils.setAlphaColor(ColorUtils.darken(theme, 0.62F), Math.round(136.4F));
            int ribsColor = ColorUtils.setAlphaColor(ColorUtils.interpolate(theme, -1, 0.28F), Math.round(44.0F));
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(770, 771);
            RenderSystem.setShader(class_10142.field_53876);
            matrices.method_22903();
            matrices.method_22904(px, py, pz);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F - bodyYaw));
            if (wingPose.preTranslateY != 0.0F || wingPose.preTranslateZ != 0.0F) {
               matrices.method_46416(0.0F, wingPose.preTranslateY, wingPose.preTranslateZ);
            }

            if (wingPose.pitchRotation != 0.0F) {
               matrices.method_22907(class_7833.field_40714.rotationDegrees(wingPose.pitchRotation));
            }

            if (wingPose.rollRotation != 0.0F) {
               matrices.method_22907(class_7833.field_40718.rotationDegrees(wingPose.rollRotation));
            }

            matrices.method_46416(0.0F, wingPose.anchorY, wingPose.anchorZ);
            matrices.method_22905(scale, scale, scale);
            this.renderClassicWingSide(
               matrices, -1.0F, open, animatedSidePitch, animatedSideRoll, baseColor, glowColor, coreColor, outlineColor, ribsColor, wingPose
            );
            this.renderClassicWingSide(
               matrices, 1.0F, open, animatedSidePitch, animatedSideRoll, baseColor, glowColor, coreColor, outlineColor, ribsColor, wingPose
            );
            matrices.method_22909();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
         }
      }
   }

   private void renderNimbus(Event3DRender event) {
      if (mc.field_1724 != null && mc.field_1687 != null && !mc.field_1690.method_31044().method_31034()) {
         float tickDelta = event.getTickDelta();
         class_243 camera = event.getCamera().method_19326();
         double x = class_3532.method_16436(tickDelta, mc.field_1724.field_6014, mc.field_1724.method_23317());
         double y = class_3532.method_16436(tickDelta, mc.field_1724.field_6036, mc.field_1724.method_23318()) + mc.field_1724.method_17682() + 0.1;
         double z = class_3532.method_16436(tickDelta, mc.field_1724.field_5969, mc.field_1724.method_23321());
         int baseColor = this.resolveCosmeticThemeColor();
         long nowMs = System.currentTimeMillis();
         double radiansPerMillisecond = 0.0029670597283903604;
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, this.getNimbusTexture());
         class_4587 matrices = event.getMatrices();

         for (int arm = 0; arm < 2; arm++) {
            double baseAngle = radiansPerMillisecond * nowMs + arm * Math.PI;

            for (int segment = 0; segment < 17; segment++) {
               double segmentAngle = baseAngle - segment * 0.11;
               double offsetX = Math.cos(segmentAngle) * 0.45F;
               double offsetZ = Math.sin(segmentAngle) * 0.45F;
               float progress = (float)segment / Math.max(1, 16);
               float size = 0.23F * (1.0F - progress * 0.7F);
               int alpha = class_3532.method_15340(255 - segment * 9, 0, 255);
               int segmentColor = ColorUtils.setAlphaColor(baseColor, alpha);
               this.renderNimbusBillboard(
                  matrices,
                  event.getCamera().method_19330(),
                  event.getCamera().method_19329(),
                  x - camera.field_1352 + offsetX,
                  y - camera.field_1351,
                  z - camera.field_1350 + offsetZ,
                  size,
                  segmentColor
               );
            }
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   public void renderChinaHat(class_4587 matrixStack, class_4597 vertexConsumerProvider, class_1657 player, class_3882 model) {
      if (this.isEnable() && this.cosmetics.is("Китайская шляпа")) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            if (this.shouldRenderCosmeticForPlayer(player)) {
               if (player != mc.field_1724 || !mc.field_1690.method_31044().method_31034()) {
                  double radius = player.method_5829().field_1320 - player.method_5829().field_1323;
                  float offset = player.method_6118(class_1304.field_6169).method_7960() ? 0.415F : 0.48F;
                  matrixStack.method_22903();
                  model.method_2838().method_22703(matrixStack);
                  RenderSystem.enableBlend();
                  RenderSystem.enableDepthTest();
                  RenderSystem.disableCull();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.setShader(class_10142.field_53876);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  RenderSystem.lineWidth(2.0F);
                  GL11.glEnable(2848);
                  GL11.glHint(3154, 4354);
                  matrixStack.method_46416(0.0F, -offset, 0.0F);
                  matrixStack.method_22907(class_7833.field_40717.rotationDegrees(180.0F));
                  matrixStack.method_22907(class_7833.field_40716.rotationDegrees(90.0F));
                  Matrix4f matrix = matrixStack.method_23760().method_23761();
                  class_289 tessellator = class_289.method_1348();
                  class_287 buffer = tessellator.method_60827(class_5596.field_27380, class_290.field_1576);
                  float y = 0.0F;
                  int colorTheme = this.resolveCosmeticThemeColor();
                  int coneColor = ColorUtils.setAlphaColor(colorTheme, 125);
                  int outlineColor = ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.5F), 180);

                  for (int i = 0; i <= 180; i++) {
                     float iPi = i * 0.06981317F;
                     float x = (float)(class_3532.method_15374(iPi) * radius);
                     float z = (float)(class_3532.method_15362(iPi) * radius);
                     buffer.method_22918(matrix, x, y, z).method_39415(coneColor);
                     buffer.method_22918(matrix, 0.0F, 0.3F, 0.0F).method_39415(colorTheme);
                  }

                  class_286.method_43433(buffer.method_60800());
                  RenderSystem.depthMask(false);
                  buffer = tessellator.method_60827(class_5596.field_27378, class_290.field_1576);
                  float firstX = 0.0F;
                  float firstZ = 0.0F;
                  boolean firstSet = false;

                  for (int i = 0; i <= 180; i++) {
                     float iPi = i * 0.06981317F;
                     float x = (float)(class_3532.method_15374(iPi) * radius);
                     float z = (float)(class_3532.method_15362(iPi) * radius);
                     buffer.method_22918(matrix, x, y, z).method_39415(outlineColor);
                     if (!firstSet) {
                        firstX = x;
                        firstZ = z;
                        firstSet = true;
                     }
                  }

                  if (firstSet) {
                     buffer.method_22918(matrix, firstX, y, firstZ).method_39415(outlineColor);
                  }

                  class_286.method_43433(buffer.method_60800());
                  RenderSystem.depthMask(true);
                  RenderSystem.enableCull();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  GL11.glDisable(2848);
                  matrixStack.method_22909();
               }
            }
         }
      }
   }

   private class_2960 getNimbusTexture() {
      return class_2960.method_60655("slikdlc", "textures/targetesp/bloom.png");
   }

   private void renderNimbusBillboard(class_4587 matrices, float cameraYaw, float cameraPitch, double x, double y, double z, float size, int color) {
      int a = color >> 24 & 0xFF;
      if (a > 0) {
         int r = color >> 16 & 0xFF;
         int g = color >> 8 & 0xFF;
         int b = color & 0xFF;
         float half = size * 0.5F;
         matrices.method_22903();
         matrices.method_22904(x, y, z);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-cameraYaw));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(cameraPitch));
         Matrix4f matrix = matrices.method_23760().method_23761();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
         buffer.method_22918(matrix, -half, -half, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, -half, half, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, half, half, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, half, -half, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
         class_286.method_43433(buffer.method_60800());
         matrices.method_22909();
      }
   }

   private boolean shouldRenderCosmeticForPlayer(class_1657 player) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         return player == mc.field_1724
            ? true
            : SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.friendStorage != null && SlikDlc.INSTANCE.friendStorage.isFriend(player.method_5477().getString());
      }
   }

   private int getStableThemeColor() {
      if (SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.themeStorage != null && SlikDlc.INSTANCE.themeStorage.getThemes() != null) {
         Theme theme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme();
         return theme != null && theme.color != null && theme.color.length != 0 ? theme.color[0] : ColorUtils.getThemeColor(0);
      } else {
         return ColorUtils.getThemeColor(0);
      }
   }

   private int resolveCosmeticThemeColor() {
      if (SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.themeStorage != null && SlikDlc.INSTANCE.themeStorage.getThemes() != null) {
         Theme theme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme();
         if (theme == null) {
            return ColorUtils.getThemeColor();
         } else {
            return "Rainbow".equals(theme.getName()) ? ColorUtils.getThemeColor() : this.getStableThemeColor();
         }
      } else {
         return ColorUtils.getThemeColor();
      }
   }

   private void syncWingSelectionState() {
      boolean butterfly = this.cosmetics.is("Крылья");
      boolean classic = this.cosmetics.is("Крылья 2");
      if (butterfly && classic) {
         if (butterfly != this.lastButterflySelected && classic == this.lastClassicSelected) {
            this.cosmetics.set("Крылья 2", false);
            classic = false;
         } else {
            this.cosmetics.set("Крылья", false);
            butterfly = false;
         }
      }

      this.lastButterflySelected = butterfly;
      this.lastClassicSelected = classic;
   }

   private void applyBackPoseTransform(class_4587 matrices, class_1657 player, float tickDelta, class_4050 pose, boolean fallFlying) {
      if (fallFlying) {
         float pitch = player.method_5695(tickDelta);
         float clampedPitch = class_3532.method_15363(pitch, -65.0F, 65.0F);
         matrices.method_46416(0.0F, 0.3F, 0.0F);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-(90.0F + clampedPitch)));
         matrices.method_46416(0.0F, -0.15F, 0.12F);
      } else if (pose == class_4050.field_18079) {
         float pitch = player.method_5695(tickDelta);
         float clampedPitch = class_3532.method_15363(pitch, -65.0F, 65.0F);
         matrices.method_46416(0.0F, 0.3F, 0.0F);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-(90.0F + clampedPitch)));
         matrices.method_46416(0.0F, -0.15F, 0.12F);
      } else {
         if (player.method_5715()) {
            matrices.method_46416(0.0F, 1.15F, 0.0F);
            matrices.method_22907(class_7833.field_40714.rotationDegrees(24.0F));
            matrices.method_46416(0.0F, 0.0F, 0.08F);
         } else {
            matrices.method_46416(0.0F, 1.3F, 0.08F);
         }
      }
   }

   private float resolveClassicBodyYaw(class_1657 player, float tickDelta) {
      float targetBodyYaw = class_3532.method_17821(tickDelta, player.field_6220, player.field_6283);
      if (player != mc.field_1724) {
         return targetBodyYaw;
      } else if (!this.selfClassicBodyYawInitialized) {
         this.selfClassicBodyYaw = targetBodyYaw;
         this.selfClassicBodyYawInitialized = true;
         return this.selfClassicBodyYaw;
      } else {
         float delta = class_3532.method_15393(targetBodyYaw - this.selfClassicBodyYaw);
         this.selfClassicBodyYaw = this.selfClassicBodyYaw + class_3532.method_15363(delta, -14.0F, 14.0F);
         return this.selfClassicBodyYaw;
      }
   }

   private Cosmetics.ClassicWingPose resolveClassicWingPose(class_1657 player, float tickDelta, class_4050 pose) {
      float pitch = player.method_5695(tickDelta);
      if (player.method_6128()) {
         float clampedPitch = class_3532.method_15363(pitch, -65.0F, 65.0F);
         return new Cosmetics.ClassicWingPose(
            1.18F, 0.1F, 0.0F, 0.0F, -(90.0F + clampedPitch), 0.0F, 0.76F, 0.92F, 0.1F, 0.58F, 0.05F, 0.0F, 0.06F, -5.0F, -2.0F, 0.13F
         );
      } else if (pose == class_4050.field_18079 || player.method_20232()) {
         float clampedPitch = class_3532.method_15363(pitch, -65.0F, 65.0F);
         float bodyShiftY = player.method_20232() ? 1.1F : 1.18F;
         float bodyShiftZ = player.method_20232() ? 0.18F : 0.12F;
         return new Cosmetics.ClassicWingPose(
            bodyShiftY, bodyShiftZ, 0.18F, 0.48F, -(90.0F + clampedPitch), 0.0F, 0.84F, 0.96F, 0.12F, 0.7F, 0.03F, 0.0F, 0.01F, -7.0F, -3.0F, 0.16F
         );
      } else {
         return player.method_5715()
            ? new Cosmetics.ClassicWingPose(0.0F, 0.0F, 0.96F, 0.1F, 18.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.0F, 0.02F, -11.0F, -4.0F, 0.12F)
            : new Cosmetics.ClassicWingPose(0.0F, 0.0F, 1.18F, 0.1F, 0.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.0F, 0.02F, -11.0F, -4.0F, 0.12F);
      }
   }

   private void renderClassicWingSide(
      class_4587 matrices,
      float side,
      float open,
      float sidePitch,
      float sideRoll,
      int baseColor,
      int glowColor,
      int coreColor,
      int outlineColor,
      int ribsColor,
      Cosmetics.ClassicWingPose pose
   ) {
      matrices.method_22903();
      matrices.method_46416(side * pose.sideOffset, pose.sideYOffset, pose.sideZOffset);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * open));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(side * sideRoll));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(sidePitch));
      RenderSystem.blendFunc(770, 1);
      this.drawClassicWingLayer(matrices, side, 1.22F, glowColor, ColorUtils.setAlphaColor(glowColor, 0));
      this.drawClassicWingLayer(matrices, side, 0.84F, coreColor, ColorUtils.setAlphaColor(coreColor, 0));
      RenderSystem.blendFunc(770, 771);
      this.drawClassicWingLayer(matrices, side, 1.0F, baseColor, ColorUtils.setAlphaColor(baseColor, 10));
      RenderSystem.blendFunc(770, 1);
      this.drawClassicWingOutline(matrices, side, 1.0F, outlineColor);
      this.drawClassicWingRibs(matrices, side, 0.96F, ribsColor);
      matrices.method_22909();
   }

   private void drawClassicWingLayer(class_4587 matrices, float side, float scale, int rootColor, int edgeColor) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27379, class_290.field_1576);

      for (int i = 0; i < CLASSIC_WING_SHAPE.length; i++) {
         Cosmetics.ClassicWingPoint current = CLASSIC_WING_SHAPE[i];
         Cosmetics.ClassicWingPoint next = CLASSIC_WING_SHAPE[(i + 1) % CLASSIC_WING_SHAPE.length];
         this.vertex(buffer, matrix, 0.0F, 0.0F, 0.0F, rootColor);
         this.vertex(buffer, matrix, side * current.x * scale, current.y * scale, 0.0F, this.applyClassicWingPointAlpha(edgeColor, current.alphaMultiplier));
         this.vertex(buffer, matrix, side * next.x * scale, next.y * scale, 0.0F, this.applyClassicWingPointAlpha(edgeColor, next.alphaMultiplier));
      }

      class_286.method_43433(buffer.method_60800());
   }

   private void drawClassicWingOutline(class_4587 matrices, float side, float scale, int color) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);
      RenderSystem.lineWidth(1.35F);
      GL11.glEnable(2848);

      for (int i = 0; i < CLASSIC_WING_SHAPE.length; i++) {
         Cosmetics.ClassicWingPoint current = CLASSIC_WING_SHAPE[i];
         Cosmetics.ClassicWingPoint next = CLASSIC_WING_SHAPE[(i + 1) % CLASSIC_WING_SHAPE.length];
         this.addLine(buffer, matrix, side * current.x * scale, current.y * scale, 0.0F, side * next.x * scale, next.y * scale, 0.0F, color);
      }

      class_286.method_43433(buffer.method_60800());
      GL11.glDisable(2848);
   }

   private void drawClassicWingRibs(class_4587 matrices, float side, float scale, int color) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);
      int[] ribIndices = new int[]{2, 4, 7, 9, 11};
      RenderSystem.lineWidth(0.9F);

      for (int ribIndex : ribIndices) {
         Cosmetics.ClassicWingPoint point = CLASSIC_WING_SHAPE[ribIndex];
         this.vertex(buffer, matrix, 0.0F, 0.0F, 0.0F, ColorUtils.setAlphaColor(color, Math.max(8, (int)((color >> 24 & 0xFF) * 0.75F))));
         this.vertex(buffer, matrix, side * point.x * scale, point.y * scale, 0.0F, this.applyClassicWingPointAlpha(color, point.alphaMultiplier));
      }

      class_286.method_43433(buffer.method_60800());
   }

   private int applyClassicWingPointAlpha(int color, float multiplier) {
      int alpha = color >> 24 & 0xFF;
      return ColorUtils.setAlphaColor(color, Math.max(0, Math.min(255, (int)(alpha * multiplier))));
   }

   private void vertex(class_287 buffer, Matrix4f matrix, float x, float y, float z, int color) {
      buffer.method_22918(matrix, x, y, z).method_39415(color);
   }

   private void renderButterflyWing(
      class_287 buffer, class_4587 matrices, float side, float spread, float pitch, float roll, float scale, int topColor, int bottomColor
   ) {
      float root = 0.12F * scale;
      float topW = 1.52F * scale;
      float topH = 0.64F * scale;
      float lowW = 1.14F * scale;
      float lowH = 0.39F * scale;
      matrices.method_22903();
      matrices.method_46416(0.15F * side, 0.0F, -0.17F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * spread));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(side * roll));
      Matrix4f matrix = matrices.method_23760().method_23761();
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         0.02F,
         -0.01F,
         side * (root + topW * 0.22F),
         topH * 0.98F,
         -0.06F,
         side * (root + topW * 0.88F),
         topH * 0.6F,
         -0.13F,
         topColor,
         bottomColor
      );
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         0.02F,
         -0.01F,
         side * (root + topW * 0.88F),
         topH * 0.6F,
         -0.13F,
         side * (root + topW),
         topH * 0.12F,
         -0.17F,
         topColor,
         bottomColor
      );
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         -0.03F,
         -0.03F,
         side * (root + lowW * 0.26F),
         -lowH * 0.96F,
         -0.11F,
         side * (root + lowW * 0.84F),
         -lowH * 0.54F,
         -0.18F,
         bottomColor,
         topColor
      );
      this.addDoubleSidedGradientTriangle(
         buffer,
         matrix,
         side * root,
         -0.03F,
         -0.03F,
         side * (root + lowW * 0.84F),
         -lowH * 0.54F,
         -0.18F,
         side * (root + lowW),
         -lowH * 0.12F,
         -0.21F,
         bottomColor,
         topColor
      );
      matrices.method_22909();
   }

   private void renderButterflyWingOutline(
      class_287 buffer, class_4587 matrices, float side, float spread, float pitch, float roll, float scale, int outlineColor
   ) {
      float root = 0.12F * scale;
      float topW = 1.52F * scale;
      float topH = 0.64F * scale;
      float lowW = 1.14F * scale;
      float lowH = 0.39F * scale;
      matrices.method_22903();
      matrices.method_46416(0.15F * side, 0.0F, -0.17F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * spread));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(side * roll));
      Matrix4f matrix = matrices.method_23760().method_23761();
      this.addLine(buffer, matrix, side * root, 0.02F, -0.01F, side * (root + topW * 0.22F), topH * 0.98F, -0.06F, outlineColor);
      this.addLine(buffer, matrix, side * (root + topW * 0.22F), topH * 0.98F, -0.06F, side * (root + topW * 0.88F), topH * 0.6F, -0.13F, outlineColor);
      this.addLine(buffer, matrix, side * (root + topW * 0.88F), topH * 0.6F, -0.13F, side * (root + topW), topH * 0.12F, -0.17F, outlineColor);
      this.addLine(buffer, matrix, side * root, -0.03F, -0.03F, side * (root + lowW * 0.26F), -lowH * 0.96F, -0.11F, outlineColor);
      this.addLine(buffer, matrix, side * (root + lowW * 0.26F), -lowH * 0.96F, -0.11F, side * (root + lowW * 0.84F), -lowH * 0.54F, -0.18F, outlineColor);
      this.addLine(buffer, matrix, side * (root + lowW * 0.84F), -lowH * 0.54F, -0.18F, side * (root + lowW), -lowH * 0.12F, -0.21F, outlineColor);
      this.addLine(buffer, matrix, side * root, -0.01F, -0.02F, side * (root + topW * 0.6F), 0.08F, -0.08F, outlineColor);
      matrices.method_22909();
   }

   private void addDoubleSidedQuad(
      class_287 buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int r,
      int g,
      int b,
      int a
   ) {
      this.addQuad(buffer, matrix, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, r, g, b, a);
      this.addQuad(buffer, matrix, x4, y4, z4, x3, y3, z3, x2, y2, z2, x1, y1, z1, r, g, b, a);
   }

   private void addDoubleSidedGradientQuad(
      class_287 buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int nearColor,
      int farColor
   ) {
      int nr = nearColor >> 16 & 0xFF;
      int ng = nearColor >> 8 & 0xFF;
      int nb = nearColor & 0xFF;
      int na = nearColor >> 24 & 0xFF;
      int fr = farColor >> 16 & 0xFF;
      int fg = farColor >> 8 & 0xFF;
      int fb = farColor & 0xFF;
      int fa = farColor >> 24 & 0xFF;
      buffer.method_22918(matrix, x1, y1, z1).method_1336(nr, ng, nb, na);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x4, y4, z4).method_1336(nr, ng, nb, na);
      buffer.method_22918(matrix, x4, y4, z4).method_1336(nr, ng, nb, na);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x1, y1, z1).method_1336(nr, ng, nb, na);
   }

   private void addDoubleSidedGradientTriangle(
      class_287 buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int nearColor, int farColor
   ) {
      int nr = nearColor >> 16 & 0xFF;
      int ng = nearColor >> 8 & 0xFF;
      int nb = nearColor & 0xFF;
      int na = nearColor >> 24 & 0xFF;
      int fr = farColor >> 16 & 0xFF;
      int fg = farColor >> 8 & 0xFF;
      int fb = farColor & 0xFF;
      int fa = farColor >> 24 & 0xFF;
      buffer.method_22918(matrix, x1, y1, z1).method_1336(nr, ng, nb, na);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x1, y1, z1).method_1336(nr, ng, nb, na);
   }

   private void renderWingBoneLine(
      class_287 buffer,
      Matrix4f matrix,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float thickness,
      int colorA,
      int colorB
   ) {
      float vx1 = x1 - x0;
      float vy1 = y1 - y0;
      float len1 = Math.max(1.0E-4F, (float)Math.sqrt(vx1 * vx1 + vy1 * vy1));
      float nx1 = -vy1 / len1 * thickness;
      float ny1 = vx1 / len1 * thickness;
      int aR = colorA >> 16 & 0xFF;
      int aG = colorA >> 8 & 0xFF;
      int aB = colorA & 0xFF;
      int aA = colorA >> 24 & 0xFF;
      int bR = colorB >> 16 & 0xFF;
      int bG = colorB >> 8 & 0xFF;
      int bB = colorB & 0xFF;
      int bA = colorB >> 24 & 0xFF;
      this.addDoubleSidedQuad(buffer, matrix, x0 + nx1, y0 + ny1, z0, x0 - nx1, y0 - ny1, z0, x1 - nx1, y1 - ny1, z1, x1 + nx1, y1 + ny1, z1, aR, aG, aB, aA);
      float vx2 = x2 - x1;
      float vy2 = y2 - y1;
      float len2 = Math.max(1.0E-4F, (float)Math.sqrt(vx2 * vx2 + vy2 * vy2));
      float nx2 = -vy2 / len2 * thickness;
      float ny2 = vx2 / len2 * thickness;
      this.addDoubleSidedQuad(buffer, matrix, x1 + nx2, y1 + ny2, z1, x1 - nx2, y1 - ny2, z1, x2 - nx2, y2 - ny2, z2, x2 + nx2, y2 + ny2, z2, bR, bG, bB, bA);
   }

   private void addQuad(
      class_287 buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int r,
      int g,
      int b,
      int a
   ) {
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x4, y4, z4).method_1336(r, g, b, a);
   }

   private void addLine(class_287 buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(r, g, b, a);
   }

   private static final class ClassicWingPoint {
      private final float x;
      private final float y;
      private final float alphaMultiplier;

      private ClassicWingPoint(float x, float y, float alphaMultiplier) {
         this.x = x;
         this.y = y;
         this.alphaMultiplier = alphaMultiplier;
      }
   }

   private static final class ClassicWingPose {
      private final float preTranslateY;
      private final float preTranslateZ;
      private final float anchorY;
      private final float anchorZ;
      private final float pitchRotation;
      private final float rollRotation;
      private final float openMultiplier;
      private final float scaleMultiplier;
      private final float motionSpreadBoost;
      private final float flapAmplitude;
      private final float sideOffset;
      private final float sideYOffset;
      private final float sideZOffset;
      private final float sideRoll;
      private final float sidePitch;
      private final float flapSpeed;

      private ClassicWingPose(
         float preTranslateY,
         float preTranslateZ,
         float anchorY,
         float anchorZ,
         float pitchRotation,
         float rollRotation,
         float openMultiplier,
         float scaleMultiplier,
         float motionSpreadBoost,
         float flapAmplitude,
         float sideOffset,
         float sideYOffset,
         float sideZOffset,
         float sideRoll,
         float sidePitch,
         float flapSpeed
      ) {
         this.preTranslateY = preTranslateY;
         this.preTranslateZ = preTranslateZ;
         this.anchorY = anchorY;
         this.anchorZ = anchorZ;
         this.pitchRotation = pitchRotation;
         this.rollRotation = rollRotation;
         this.openMultiplier = openMultiplier;
         this.scaleMultiplier = scaleMultiplier;
         this.motionSpreadBoost = motionSpreadBoost;
         this.flapAmplitude = flapAmplitude;
         this.sideOffset = sideOffset;
         this.sideYOffset = sideYOffset;
         this.sideZOffset = sideZOffset;
         this.sideRoll = sideRoll;
         this.sidePitch = sidePitch;
         this.flapSpeed = flapSpeed;
      }
   }
}
