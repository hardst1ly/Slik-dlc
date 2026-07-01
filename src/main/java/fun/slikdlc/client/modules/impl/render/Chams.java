package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.ShaderUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.mixin.LivingEntityRendererAccessor;
import java.awt.Color;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_10142;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_284;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_5944;
import net.minecraft.class_630;
import net.minecraft.class_742;
import net.minecraft.class_897;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Chams extends Module {
   public static final Chams INSTANCE = new Chams();
   public static final String TARGET_PLAYERS = "Игроков";
   public static final String TARGET_FRIENDS = "Друзей";
   public static final String TARGET_SELF = "Себя";
   private static final int DEFAULT_FILL_ALPHA = 130;
   private static final float DEFAULT_LINE_WIDTH = 0.5F;
   private static final float CLIENT_FILL_SATURATION = 1.18F;
   private static final float CLIENT_FILL_BRIGHTNESS = 1.12F;
   private static final float CLIENT_OUTLINE_SATURATION = 1.12F;
   private static final float CLIENT_OUTLINE_BRIGHTNESS = 1.08F;
   private static final float MIN_PULSE_ALPHA = 0.65F;
   private static final float PULSE_SWING = 0.35F;
   private static final int FRIEND_FILL_COLOR = new Color(85, 255, 85, 60).getRGB();
   private static final int FRIEND_OUTLINE_COLOR = new Color(100, 255, 100, 255).getRGB();
   private static final long OUTLINE_RETRY_DELAY_MS = 3000L;
   private final ListSetting rendering = new ListSetting(
      "Отображать", new BooleanSetting("Игроков", true), new BooleanSetting("Друзей", true), new BooleanSetting("Себя", false)
   );
   private final BooleanSetting waves = new BooleanSetting("Волны", true);
   private final FloatSetting waveSpeedX = new FloatSetting("Скорость X", 0.22F, 0.0F, 1.5F, 0.01F).visible(this.waves::isState);
   private final FloatSetting waveSpeedY = new FloatSetting("Скорость Y", 0.15F, 0.0F, 1.5F, 0.01F).visible(this.waves::isState);
   private final FloatSetting waveScale = new FloatSetting("Размер волн", 1.35F, 0.2F, 4.0F, 0.05F).visible(this.waves::isState);
   private final FloatSetting waveDensity = new FloatSetting("Плотность волн", 1.15F, 0.5F, 3.0F, 0.05F).visible(this.waves::isState);
   private final FloatSetting waveGlow = new FloatSetting("Сила волн", 1.0F, 0.2F, 3.0F, 0.05F).visible(this.waves::isState);
   private final BooleanSetting glow = new BooleanSetting("Свечение", true);
   private final FloatSetting glowIntensity = new FloatSetting("Сила свечения", 2.0F, 1.0F, 5.0F, 0.1F).visible(this.glow::isState);
   private final FloatSetting glowLayers = new FloatSetting("Слои свечения", 3.0F, 1.0F, 6.0F, 1.0F).visible(this.glow::isState);
   private final BooleanSetting pulse = new BooleanSetting("Пульсирование", false);
   private final FloatSetting pulseSpeed = new FloatSetting("Скорость пульсации", 2.0F, 0.5F, 5.0F, 0.1F).visible(this.pulse::isState);
   private final BooleanSetting hideOriginal = new BooleanSetting("Скрыть оригинал", false);
   private final BooleanSetting hideItemsAndCape = new BooleanSetting("Скрывать предметы и плащ", false);
   private final long startTime = System.currentTimeMillis();
   private boolean outlineAssistReady;
   private long nextOutlineRetryAt;

   private Chams() {
      super("Chams", "Чамсы по модели игрока", Module.ModuleCategory.RENDER);
      this.addSettings(
         new Setting[]{
            this.rendering,
            this.waves,
            this.waveSpeedX,
            this.waveSpeedY,
            this.waveScale,
            this.waveDensity,
            this.waveGlow,
            this.glow,
            this.glowIntensity,
            this.glowLayers,
            this.pulse,
            this.pulseSpeed,
            this.hideOriginal,
            this.hideItemsAndCape
         }
      );
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.outlineAssistReady = false;
      this.nextOutlineRetryAt = 0L;
      this.tryEnsureOutlineProcessor();
   }

   @Override
   public void onDisable() {
      this.outlineAssistReady = false;
      this.nextOutlineRetryAt = 0L;
      super.onDisable();
   }

   @EventLink(
      priority = 100
   )
   public void onRender3D(Event3DRender event) {
      if (this.isEnable() && mc.field_1687 != null && mc.field_1724 != null) {
         if (this.hasOutlineAssistTargets() && !this.outlineAssistReady && System.currentTimeMillis() >= this.nextOutlineRetryAt) {
            this.tryEnsureOutlineProcessor();
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);

         for (class_1657 player : mc.field_1687.method_18456()) {
            if (this.affects(player) && (player != mc.field_1724 || mc.field_1690.method_31044() != class_5498.field_26664)) {
               this.renderManualPlayer(event, player);
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private void renderManualPlayer(Event3DRender event, class_1657 player) {
      if (player instanceof class_742 clientPlayer) {
         class_897<?, ?> entityRenderer = mc.method_1561().method_3953(player);
         if (entityRenderer instanceof class_1007 renderer) {
            class_10055 state = renderer.method_62608();
            renderer.method_62604(clientPlayer, state, event.getTickDelta());
            class_591 model = (class_591)renderer.method_4038();
            model.method_62110(state);
            class_4587 matrices = event.getMatrices();
            matrices.method_22903();
            this.setupModelMatrix(matrices, state, renderer, event.getCamera().method_19326(), player, event.getTickDelta());
            int fillColor = this.resolveFillColor(player);
            int outlineColor = this.resolveOutlineColor(player);
            this.renderShaderFillModel(matrices, model, 0.0F, fillColor);
            this.renderOutlineModel(matrices, model, 0.0F, outlineColor);
            matrices.method_22909();
         }
      }
   }

   private void setupModelMatrix(class_4587 matrices, class_10055 state, class_1007 renderer, class_243 cameraPos, class_1657 player, float tickDelta) {
      class_243 pos = player.method_30950(tickDelta);
      double x = pos.field_1352 - cameraPos.field_1352;
      double y = pos.field_1351 - cameraPos.field_1351;
      double z = pos.field_1350 - cameraPos.field_1350;
      matrices.method_22904(x, y, z);
      if (state.field_53463 != null) {
         float eyeOffset = state.field_53331 - 0.1F;
         matrices.method_46416(-state.field_53463.method_10148() * eyeOffset, 0.0F, -state.field_53463.method_10165() * eyeOffset);
      }

      float baseScale = state.field_53453;
      matrices.method_22905(baseScale, baseScale, baseScale);
      LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)renderer;
      accessor.slikdlc$setupTransforms(state, matrices, state.field_53446, baseScale);
      matrices.method_22905(-1.0F, -1.0F, 1.0F);
      accessor.slikdlc$scale(state, matrices);
      matrices.method_46416(0.0F, -1.501F, 0.0F);
   }

   private void renderShaderFillModel(class_4587 matrices, class_572<?> model, float expand, int color) {
      if (!this.waves.isState()) {
         this.renderSolidFillModel(matrices, model, expand, color);
      } else {
         class_5944 shader = mc.method_62887().method_62947(ShaderUtils.chamsFill);
         if (shader != null) {
            RenderSystem.setShader(ShaderUtils.chamsFill);
            this.setUniform(shader, "time", this.waves.isState() ? (float)(System.currentTimeMillis() - this.startTime) / 1000.0F : 0.0F);
            this.setUniform(shader, "speedX", this.waveSpeedX.get());
            this.setUniform(shader, "speedY", this.waveSpeedY.get());
            this.setUniform(shader, "scale", this.waveScale.get());
            this.setUniform(shader, "density", this.waveDensity.get());
            this.setUniform(shader, "glowStrength", this.waveGlow.get());
            class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
            class_630 root = model.method_63512();
            this.renderFillPart(matrices, buffer, root, model.field_3398, -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, expand, color);
            this.renderFillPart(matrices, buffer, root, model.field_3391, -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, expand, color);
            this.renderFillPart(matrices, buffer, root, model.field_3401, -3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
            this.renderFillPart(matrices, buffer, root, model.field_27433, -1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
            this.renderFillPart(matrices, buffer, root, model.field_3392, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
            this.renderFillPart(matrices, buffer, root, model.field_3397, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
            class_286.method_43433(buffer.method_60800());
         }
      }
   }

   private void renderSolidFillModel(class_4587 matrices, class_572<?> model, float expand, int color) {
      RenderSystem.setShader(class_10142.field_53876);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      class_630 root = model.method_63512();
      this.renderSolidFillPart(matrices, buffer, root, model.field_3398, -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, expand, color);
      this.renderSolidFillPart(matrices, buffer, root, model.field_3391, -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, expand, color);
      this.renderSolidFillPart(matrices, buffer, root, model.field_3401, -3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      this.renderSolidFillPart(matrices, buffer, root, model.field_27433, -1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      this.renderSolidFillPart(matrices, buffer, root, model.field_3392, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      this.renderSolidFillPart(matrices, buffer, root, model.field_3397, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      class_286.method_43433(buffer.method_60800());
   }

   private void renderSolidFillPart(
      class_4587 baseStack,
      class_287 buffer,
      class_630 root,
      class_630 part,
      float offX,
      float offY,
      float offZ,
      float width,
      float height,
      float depth,
      float expand,
      int color
   ) {
      baseStack.method_22903();
      root.method_22703(baseStack);
      part.method_22703(baseStack);
      Matrix4f matrix = baseStack.method_23760().method_23761();
      float scale = 0.0625F;
      float expandScale = expand * scale;
      float minX = offX * scale - expandScale;
      float minY = offY * scale - expandScale;
      float minZ = offZ * scale - expandScale;
      float maxX = (offX + width) * scale + expandScale;
      float maxY = (offY + height) * scale + expandScale;
      float maxZ = (offZ + depth) * scale + expandScale;
      this.addSolidQuad(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
      this.addSolidQuad(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, color);
      this.addSolidQuad(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, color);
      this.addSolidQuad(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, color);
      this.addSolidQuad(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, color);
      this.addSolidQuad(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
      baseStack.method_22909();
   }

   private void renderFillPart(
      class_4587 baseStack,
      class_287 buffer,
      class_630 root,
      class_630 part,
      float offX,
      float offY,
      float offZ,
      float width,
      float height,
      float depth,
      float expand,
      int color
   ) {
      baseStack.method_22903();
      root.method_22703(baseStack);
      part.method_22703(baseStack);
      Matrix4f matrix = baseStack.method_23760().method_23761();
      float scale = 0.0625F;
      float expandScale = expand * scale;
      float minX = offX * scale - expandScale;
      float minY = offY * scale - expandScale;
      float minZ = offZ * scale - expandScale;
      float maxX = (offX + width) * scale + expandScale;
      float maxY = (offY + height) * scale + expandScale;
      float maxZ = (offZ + depth) * scale + expandScale;
      this.addQuad(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
      this.addQuad(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, color);
      this.addQuad(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, color);
      this.addQuad(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, color);
      this.addQuad(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, color);
      this.addQuad(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
      baseStack.method_22909();
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
      int color
   ) {
      int r = ColorUtils.red(color);
      int g = ColorUtils.green(color);
      int b = ColorUtils.blue(color);
      int a = ColorUtils.alpha(color);
      float u1 = this.waveU(x1, y1, z1);
      float v1 = this.waveV(x1, y1, z1);
      float u2 = this.waveU(x2, y2, z2);
      float v2 = this.waveV(x2, y2, z2);
      float u3 = this.waveU(x3, y3, z3);
      float v3 = this.waveV(x3, y3, z3);
      float u4 = this.waveU(x4, y4, z4);
      float v4 = this.waveV(x4, y4, z4);
      buffer.method_22918(matrix, x1, y1, z1).method_22913(u1, v1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_22913(u2, v2).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x3, y3, z3).method_22913(u3, v3).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x4, y4, z4).method_22913(u4, v4).method_1336(r, g, b, a);
   }

   private void addSolidQuad(
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
      int color
   ) {
      int r = ColorUtils.red(color);
      int g = ColorUtils.green(color);
      int b = ColorUtils.blue(color);
      int a = ColorUtils.alpha(color);
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x4, y4, z4).method_1336(r, g, b, a);
   }

   private float waveU(float x, float y, float z) {
      return x * 1.15F + z * 0.72F;
   }

   private float waveV(float x, float y, float z) {
      return y * 1.05F - z * 0.38F + x * 0.18F;
   }

   private void renderOutlineModel(class_4587 matrices, class_572<?> model, float expand, int color) {
      RenderSystem.setShader(class_10142.field_53876);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      RenderSystem.lineWidth(0.5F);
      if (this.glow.isState()) {
         RenderSystem.blendFuncSeparate(770, 1, 1, 0);
         int layers = Math.max(1, Math.round(this.glowLayers.get()));
         float intensity = Math.max(1.0F, this.glowIntensity.get());

         for (int index = layers; index >= 1; index--) {
            float layerExpand = expand + index * 0.5F * intensity;
            float alphaMul = 1.0F / (index + 1) * 0.7F;
            int alpha = Math.max(1, Math.min(255, Math.round(ColorUtils.alpha(color) * alphaMul)));
            this.drawOutlineParts(matrices, model, layerExpand, this.withAlpha(color, alpha));
         }
      }

      RenderSystem.defaultBlendFunc();
      this.drawOutlineParts(matrices, model, expand, color);
      GL11.glDisable(2848);
   }

   private void drawOutlineParts(class_4587 matrices, class_572<?> model, float expand, int color) {
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);
      class_630 root = model.method_63512();
      this.renderPartOutlineLines(matrices, buffer, root, model.field_3398, -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, expand, color);
      this.renderPartOutlineLines(matrices, buffer, root, model.field_3391, -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, expand, color);
      this.renderPartOutlineLines(matrices, buffer, root, model.field_3401, -3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      this.renderPartOutlineLines(matrices, buffer, root, model.field_27433, -1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      this.renderPartOutlineLines(matrices, buffer, root, model.field_3392, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      this.renderPartOutlineLines(matrices, buffer, root, model.field_3397, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, expand, color);
      class_286.method_43433(buffer.method_60800());
   }

   private void renderPartOutlineLines(
      class_4587 baseStack,
      class_287 buffer,
      class_630 root,
      class_630 part,
      float offX,
      float offY,
      float offZ,
      float width,
      float height,
      float depth,
      float expand,
      int color
   ) {
      baseStack.method_22903();
      root.method_22703(baseStack);
      part.method_22703(baseStack);
      float scale = 0.0625F;
      float expandScale = expand * scale;
      float minX = offX * scale - expandScale;
      float minY = offY * scale - expandScale;
      float minZ = offZ * scale - expandScale;
      float maxX = (offX + width) * scale + expandScale;
      float maxY = (offY + height) * scale + expandScale;
      float maxZ = (offZ + depth) * scale + expandScale;
      Matrix4f matrix = baseStack.method_23760().method_23761();
      this.addLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, color);
      this.addLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, color);
      this.addLine(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, color);
      this.addLine(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, color);
      this.addLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, color);
      this.addLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, color);
      this.addLine(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, color);
      this.addLine(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, color);
      this.addLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, color);
      this.addLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, color);
      this.addLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, color);
      this.addLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, color);
      baseStack.method_22909();
   }

   private void addLine(class_287 buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
      int r = ColorUtils.red(color);
      int g = ColorUtils.green(color);
      int b = ColorUtils.blue(color);
      int a = ColorUtils.alpha(color);
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(r, g, b, a);
   }

   private void setUniform(class_5944 shader, String name, float value) {
      class_284 uniform = shader.method_34582(name);
      if (uniform != null) {
         uniform.method_1251(value);
      }
   }

   public boolean affects(class_1657 player) {
      if (!this.isEnable() || player == null || !player.method_5805()) {
         return false;
      } else if (player != mc.field_1724) {
         return this.isFriend(player) ? this.rendering.is("Друзей") : this.rendering.is("Игроков");
      } else {
         return this.rendering.is("Себя") && mc.field_1690.method_31044() != class_5498.field_26664;
      }
   }

   public boolean shouldHideBaseModel(class_1657 player) {
      return this.hideOriginal.isState() && this.affects(player);
   }

   public boolean shouldHideItemsAndCape(class_1657 player) {
      return this.hideItemsAndCape.isState() && this.affects(player);
   }

   public boolean shouldUseOutlineAssist(class_1657 player) {
      return this.affects(player);
   }

   public boolean shouldHideOutlineFramebuffer() {
      return this.isEnable() && this.hasOutlineAssistTargets();
   }

   public int resolveFillColor(class_1657 player) {
      return this.applyPulse(this.baseFillColor(player));
   }

   public int resolveOutlineColor(class_1657 player) {
      return this.applyPulse(this.baseOutlineColor(player));
   }

   private int baseFillColor(class_1657 player) {
      return this.isFriend(player) ? FRIEND_FILL_COLOR : this.vividWithAlpha(ColorUtils.getThemeColor(), 1.18F, 1.12F, 130);
   }

   private int baseOutlineColor(class_1657 player) {
      return this.isFriend(player) ? FRIEND_OUTLINE_COLOR : this.vividWithAlpha(ColorUtils.getThemeColor(), 1.12F, 1.08F, 255);
   }

   private int applyPulse(int color) {
      if (!this.pulse.isState()) {
         return color;
      } else {
         float elapsedSeconds = (float)(System.currentTimeMillis() - this.startTime) / 1000.0F;
         float pulseValue = (float)((Math.sin(elapsedSeconds * this.pulseSpeed.get() * Math.PI) + 1.0) * 0.5);
         float alphaMul = 0.65F + 0.35F * pulseValue;
         return ColorUtils.multAlpha(color, alphaMul);
      }
   }

   private int vividWithAlpha(int color, float saturationBoost, float brightnessBoost, int alpha) {
      float[] hsb = Color.RGBtoHSB(ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color), null);
      float saturation = class_3532.method_15363(hsb[1] * saturationBoost, 0.0F, 1.0F);
      float brightness = class_3532.method_15363(Math.max(hsb[2], 0.8F) * brightnessBoost, 0.0F, 1.0F);
      int rgb = Color.HSBtoRGB(hsb[0], saturation, brightness);
      return ColorUtils.rgba(ColorUtils.red(rgb), ColorUtils.green(rgb), ColorUtils.blue(rgb), alpha);
   }

   private int withAlpha(int color, int alpha) {
      return color & 16777215 | (alpha & 0xFF) << 24;
   }

   private boolean isFriend(class_1657 player) {
      return SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.friendStorage != null && SlikDlc.INSTANCE.friendStorage.isFriend(player.method_5477().getString());
   }

   private boolean hasOutlineAssistTargets() {
      if (this.isEnable() && mc.field_1687 != null && mc.field_1724 != null) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (this.shouldUseOutlineAssist(player)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean tryEnsureOutlineProcessor() {
      if (mc.field_1769 == null) {
         this.outlineAssistReady = false;
         return false;
      } else if (mc.field_1769.method_22990() != null) {
         this.outlineAssistReady = true;
         return true;
      } else {
         try {
            mc.field_1769.method_3296();
         } catch (Exception var2) {
         }

         this.outlineAssistReady = mc.field_1769.method_22990() != null;
         if (!this.outlineAssistReady) {
            this.nextOutlineRetryAt = System.currentTimeMillis() + 3000L;
         }

         return this.outlineAssistReady;
      }
   }
}
