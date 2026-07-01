package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventChunkReload;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.ShaderUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_284;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_5944;
import net.minecraft.class_6367;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Sonar extends Module {
   public static Sonar INSTANCE = new Sonar();
   private final FloatSetting duration = new FloatSetting("Длительность", 5.6F, 0.8F, 10.0F, 0.1F);
   private final FloatSetting alpha = new FloatSetting("Яркость", 1.0F, 0.1F, 1.0F, 0.01F);
   private final FloatSetting widthMul = new FloatSetting("Ширина", 1.0F, 0.35F, 2.2F, 0.05F);
   private final FloatSetting sharpness = new FloatSetting("Резкость", 24.0F, 4.0F, 80.0F, 1.0F);
   private class_276 depthCopyBuffer;
   private int lastFbWidth = -1;
   private int lastFbHeight = -1;
   private long currentStart;
   private class_243 center = class_243.field_1353;

   public Sonar() {
      super("Sonar", "Сканирует новые чанки", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.duration, this.alpha, this.widthMul, this.sharpness});
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 != null) {
         this.ping(mc.field_1724.method_19538());
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.currentStart = 0L;
      this.deleteDepthCopyFramebuffer();
      super.onDisable();
   }

   @EventLink
   public void onChunkReload(EventChunkReload event) {
      if (mc.field_1724 != null) {
         this.ping(mc.field_1724.method_19538());
      }
   }

   public void renderFromMixin(Matrix4f positionMatrix, Matrix4f projectionMatrix, class_243 camPos) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.currentStart > 0L) {
         float durationMs = this.duration.get() * 1000.0F;
         float elapsed = (float)(System.currentTimeMillis() - this.currentStart);
         if (elapsed >= durationMs) {
            this.currentStart = 0L;
         } else {
            class_276 framebuffer = mc.method_1522();
            this.ensureDepthCopyFramebuffer(framebuffer.field_1482, framebuffer.field_1481);
            if (this.depthCopyBuffer != null) {
               this.depthCopyBuffer.method_29329(framebuffer);
               Matrix4f invView = new Matrix4f(positionMatrix).invert();
               Matrix4f invProj = new Matrix4f(projectionMatrix).invert();
               float far = mc.field_1773.method_32796();
               float t = class_3532.method_15363(elapsed / durationMs, 0.0F, 1.0F);
               float r1 = this.lerp(1.0F, far, (float)Easings.QUINT_OUT.ease(t));
               float r2 = this.lerp(1.0F, far, (float)Easings.QUART_IN_OUT.ease(t));
               float baseRadius = class_3532.method_16439(0.85F, r1, r2);
               float alphaPc = 1.0F - t;
               float alphaWave = (alphaPc > 0.5F ? 1.0F - alphaPc : alphaPc) * 2.0F;
               alphaWave = Math.min(alphaWave * 1.75F, 1.0F);
               float baseAlpha = class_3532.method_15363(this.alpha.get() * alphaWave, 0.0F, 1.0F);
               int c1 = ColorUtils.getThemeColor(0);
               int c2 = ColorUtils.getThemeColor(90);
               int c3 = ColorUtils.getThemeColor(180);
               int c4 = ColorUtils.getThemeColor(270);
               float baseWidth = class_3532.method_15363(6.0F + baseRadius * (0.18F * this.widthMul.get()), 4.0F, Math.max(10.0F, far * 0.42F));
               float baseSharp = this.sharpness.get();
               this.renderPass(
                  invView,
                  invProj,
                  camPos,
                  framebuffer,
                  baseRadius,
                  baseWidth,
                  baseSharp,
                  this.applyAlpha(c1, baseAlpha),
                  this.applyAlpha(c2, baseAlpha),
                  this.applyAlpha(c3, baseAlpha),
                  this.applyAlpha(c4, baseAlpha)
               );
               RenderSystem.defaultBlendFunc();
            }
         }
      }
   }

   private void renderPass(
      Matrix4f invView,
      Matrix4f invProj,
      class_243 camPos,
      class_276 framebuffer,
      float radius,
      float width,
      float sharp,
      int outerColor,
      int midColor,
      int innerColor,
      int scanlineColor
   ) {
      if (!(radius <= 0.001F) && !(width <= 0.001F)) {
         class_5944 shader = mc.method_62887().method_62947(ShaderUtils.scanEffect);
         class_284 invViewUniform = shader.method_34582("invViewMat");
         class_284 invProjUniform = shader.method_34582("invProjMat");
         class_284 posUniform = shader.method_34582("pos");
         class_284 centerUniform = shader.method_34582("center");
         class_284 radiusUniform = shader.method_34582("radius");
         class_284 widthUniform = shader.method_34582("width");
         class_284 sharpnessUniform = shader.method_34582("sharpness");
         class_284 outerColorUniform = shader.method_34582("outerColor");
         class_284 midColorUniform = shader.method_34582("midColor");
         class_284 innerColorUniform = shader.method_34582("innerColor");
         class_284 scanlineColorUniform = shader.method_34582("scanlineColor");
         class_284 debugModeUniform = shader.method_34582("DebugMode");
         if (invViewUniform != null) {
            invViewUniform.method_1250(invView);
         }

         if (invProjUniform != null) {
            invProjUniform.method_1250(invProj);
         }

         if (posUniform != null) {
            posUniform.method_1249((float)camPos.field_1352, (float)camPos.field_1351, (float)camPos.field_1350);
         }

         if (centerUniform != null) {
            centerUniform.method_1249((float)this.center.field_1352, (float)this.center.field_1351, (float)this.center.field_1350);
         }

         if (radiusUniform != null) {
            radiusUniform.method_1251(radius);
         }

         if (widthUniform != null) {
            widthUniform.method_1251(width);
         }

         if (sharpnessUniform != null) {
            sharpnessUniform.method_1251(sharp);
         }

         if (outerColorUniform != null) {
            this.setColor(outerColorUniform, outerColor);
         }

         if (midColorUniform != null) {
            this.setColor(midColorUniform, midColor);
         }

         if (innerColorUniform != null) {
            this.setColor(innerColorUniform, innerColor);
         }

         if (scanlineColorUniform != null) {
            this.setColor(scanlineColorUniform, scanlineColor);
         }

         if (debugModeUniform != null) {
            debugModeUniform.method_35649(0);
         }

         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         int depthTex = this.depthCopyBuffer.method_30278();
         if (depthTex == 0) {
            depthTex = mc.method_1522().method_30278();
         }

         RenderSystem.bindTexture(depthTex);
         GL11.glTexParameteri(3553, 34892, 0);
         GL11.glTexParameteri(3553, 10241, 9728);
         GL11.glTexParameteri(3553, 10240, 9728);
         framebuffer.method_1235(false);
         RenderSystem.setShaderTexture(0, depthTex);
         RenderSystem.setShader(ShaderUtils.scanEffect);
         this.drawFullscreenQuad();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   private void drawFullscreenQuad() {
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1585);
      buffer.method_22912(-1.0F, -1.0F, 0.0F).method_22913(0.0F, 0.0F);
      buffer.method_22912(-1.0F, 1.0F, 0.0F).method_22913(0.0F, 1.0F);
      buffer.method_22912(1.0F, 1.0F, 0.0F).method_22913(1.0F, 1.0F);
      buffer.method_22912(1.0F, -1.0F, 0.0F).method_22913(1.0F, 0.0F);
      class_286.method_43433(buffer.method_60800());
   }

   private void ensureDepthCopyFramebuffer(int width, int height) {
      if (this.depthCopyBuffer == null || this.lastFbWidth != width || this.lastFbHeight != height) {
         this.deleteDepthCopyFramebuffer();
         this.depthCopyBuffer = new class_6367(width, height, true);
         this.lastFbWidth = width;
         this.lastFbHeight = height;
      }
   }

   private void deleteDepthCopyFramebuffer() {
      if (this.depthCopyBuffer != null) {
         this.depthCopyBuffer.method_1238();
         this.depthCopyBuffer = null;
      }

      this.lastFbWidth = -1;
      this.lastFbHeight = -1;
   }

   private void ping(class_243 pos) {
      this.currentStart = System.currentTimeMillis();
      this.center = pos;
   }

   private void setColor(class_284 uniform, int color) {
      int a = color >> 24 & 0xFF;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      if (a == 0) {
         a = 255;
      }

      uniform.method_35657(r / 255.0F, g / 255.0F, b / 255.0F, a / 255.0F);
   }

   private int applyAlpha(int color, float alphaMul) {
      int a = color >> 24 & 0xFF;
      if (a == 0) {
         a = 255;
      }

      a = (int)(a * class_3532.method_15363(alphaMul, 0.0F, 1.0F));
      return color & 16777215 | a << 24;
   }

   private float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }
}
