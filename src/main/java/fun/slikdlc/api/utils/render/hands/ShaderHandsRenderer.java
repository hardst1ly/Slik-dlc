package fun.slikdlc.api.utils.render.hands;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.ShaderUtils;
import fun.slikdlc.client.modules.impl.render.ShaderHands;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_276;
import net.minecraft.class_284;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_5944;
import net.minecraft.class_6367;
import net.minecraft.class_293.class_5596;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ShaderHandsRenderer implements QClient {
   private static final float EPSILON = 0.001F;
   private static ShaderHandsRenderer instance;
   private class_276 beforeBuffer;
   private class_276 afterBuffer;
   private class_276 maskBuffer;
   private final List<class_276> bloomBuffers = new ArrayList<>();
   private int width = -1;
   private int height = -1;
   private boolean hasBeforeCapture;
   private boolean pendingComposite;
   private int configuredBeforeDepthTex = -1;
   private int configuredAfterDepthTex = -1;

   public ShaderHandsRenderer() {
   }

   public static ShaderHandsRenderer getInstance() {
      if (instance == null) {
         instance = new ShaderHandsRenderer();
      }

      return instance;
   }

   public void captureBeforeHands() {
      ShaderHands module = this.getModule();
      if (!this.isEffectEnabled(module)) {
         this.invalidateState();
      } else {
         this.ensureBuffers();
         if (this.beforeBuffer != null) {
            this.copyMainFramebuffer(this.beforeBuffer);
            this.hasBeforeCapture = true;
         }
      }
   }

   public void captureAfterHands() {
      ShaderHands module = this.getModule();
      if (!this.isEffectEnabled(module)) {
         this.invalidateState();
      } else {
         this.ensureBuffers();
         if (this.beforeBuffer != null && this.afterBuffer != null && this.maskBuffer != null) {
            if (this.hasBeforeCapture) {
               this.copyMainFramebuffer(this.afterBuffer);
               this.pendingComposite = true;
            }
         }
      }
   }

   public void renderOverlayIfPending() {
      if (this.pendingComposite) {
         this.ensureBuffers();
         if (this.beforeBuffer != null && this.afterBuffer != null && this.maskBuffer != null) {
            ShaderHands module = this.getModule();
            if (!this.isEffectEnabled(module)) {
               this.invalidateState();
            } else {
               class_5944 maskShader = mc.method_62887().method_62947(ShaderUtils.shaderHandsMaskDiff);
               if (maskShader == null) {
                  this.invalidateState();
               } else {
                  this.maskBuffer.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
                  this.maskBuffer.method_1230();
                  this.maskBuffer.method_1235(false);
                  RenderSystem.disableDepthTest();
                  RenderSystem.disableBlend();
                  RenderSystem.setShader(ShaderUtils.shaderHandsMaskDiff);
                  RenderSystem.setShaderTexture(0, this.beforeBuffer.method_30277());
                  RenderSystem.setShaderTexture(1, this.afterBuffer.method_30277());
                  int beforeDepth = this.beforeBuffer.method_30278();
                  int afterDepth = this.afterBuffer.method_30278();
                  if (beforeDepth != 0 && beforeDepth != this.configuredBeforeDepthTex) {
                     this.configureDepthTexture(beforeDepth);
                     this.configuredBeforeDepthTex = beforeDepth;
                  }

                  if (afterDepth != 0 && afterDepth != this.configuredAfterDepthTex) {
                     this.configureDepthTexture(afterDepth);
                     this.configuredAfterDepthTex = afterDepth;
                  }

                  RenderSystem.setShaderTexture(2, beforeDepth);
                  RenderSystem.setShaderTexture(3, afterDepth);
                  this.drawFullscreenQuad();
                  RenderSystem.enableDepthTest();
                  float glowValue = module.glow.get();
                  float fillValue = module.fill.get();
                  float alphaValue = module.alpha.get();
                  float outlineValue = module.outline.get();
                  boolean hasGlow = glowValue > 0.001F;
                  boolean hasFill = fillValue > 0.001F && alphaValue > 0.001F;
                  int color1 = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
                     ? ColorUtils.getThemeColor(0)
                     : SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0];
                  if (module.mode.is("Красивый")) {
                     this.renderPrettyMode(module, color1, color1, glowValue, fillValue, alphaValue, outlineValue);
                     this.invalidateState();
                  } else {
                     int blurredMaskTexture = 0;
                     if (hasGlow) {
                        int iterations = Math.max(3, Math.min(8, 4 + Math.round(outlineValue * 0.7F)));
                        blurredMaskTexture = this.runKawaseBloom(iterations);
                     }

                     mc.method_1522().method_1235(true);
                     RenderSystem.enableBlend();
                     RenderSystem.colorMask(true, true, true, false);
                     RenderSystem.disableDepthTest();
                     class_5944 glowShader = hasGlow ? mc.method_62887().method_62947(ShaderUtils.shaderHandsGlow) : null;
                     if (glowShader != null) {
                        RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE, class_4535.ZERO, class_4534.ONE);
                        RenderSystem.setShader(ShaderUtils.shaderHandsGlow);
                        RenderSystem.setShaderTexture(0, blurredMaskTexture);
                        RenderSystem.setShaderTexture(1, this.maskBuffer.method_30277());
                        this.setUniform(glowShader, "color", ColorUtils.redf(color1), ColorUtils.greenf(color1), ColorUtils.bluef(color1));
                        this.setUniform(glowShader, "color2", ColorUtils.redf(color1), ColorUtils.greenf(color1), ColorUtils.bluef(color1));
                        this.setUniform(glowShader, "exposure", 1.0F + glowValue * 1.8F);
                        this.drawFullscreenQuad();
                     }

                     if (hasFill) {
                        class_5944 overlayShader = mc.method_62887().method_62947(ShaderUtils.shaderHandsOverlay);
                        if (overlayShader == null) {
                           this.restoreCompositeState();
                           this.invalidateState();
                           return;
                        }

                        RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA, class_4535.ZERO, class_4534.ONE);
                        RenderSystem.setShader(ShaderUtils.shaderHandsOverlay);
                        RenderSystem.setShaderTexture(0, this.maskBuffer.method_30277());
                        this.setUniform(overlayShader, "color", ColorUtils.redf(color1), ColorUtils.greenf(color1), ColorUtils.bluef(color1));
                        this.setUniform(overlayShader, "fill", fillValue);
                        this.setUniform(overlayShader, "alpha", alphaValue);
                        this.drawFullscreenQuad();
                     }

                     this.restoreCompositeState();
                     this.invalidateState();
                  }
               }
            }
         }
      }
   }

   public void invalidateState() {
      this.hasBeforeCapture = false;
      this.pendingComposite = false;
      this.configuredBeforeDepthTex = -1;
      this.configuredAfterDepthTex = -1;
   }

   private int runKawaseBloom(int iterations) {
      this.ensureBloomBuffers(iterations);
      if (this.bloomBuffers.isEmpty()) {
         return this.maskBuffer.method_30277();
      } else {
         int currentTexture = this.maskBuffer.method_30277();
         class_5944 downShader = mc.method_62887().method_62947(ShaderUtils.shaderHandsKawaseDown);
         class_5944 upShader = mc.method_62887().method_62947(ShaderUtils.shaderHandsKawaseUp);
         if (downShader != null && upShader != null) {
            for (int i = 0; i < iterations; i++) {
               class_276 dst = this.bloomBuffers.get(i);
               dst.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
               dst.method_1230();
               dst.method_1235(true);
               RenderSystem.setShader(ShaderUtils.shaderHandsKawaseDown);
               RenderSystem.setShaderTexture(0, currentTexture);
               this.setHandsKawaseUniforms(downShader, dst.field_1482, dst.field_1481, 1.0F + i);
               this.drawFullscreenQuad();
               currentTexture = dst.method_30277();
            }

            for (int i = iterations - 1; i >= 1; i--) {
               class_276 dst = this.bloomBuffers.get(i - 1);
               dst.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
               dst.method_1230();
               dst.method_1235(true);
               RenderSystem.setShader(ShaderUtils.shaderHandsKawaseUp);
               RenderSystem.setShaderTexture(0, currentTexture);
               this.setHandsKawaseUniforms(upShader, dst.field_1482, dst.field_1481, 1.0F + i);
               this.setUniform(upShader, "color", 1.0F, 1.0F, 1.0F);
               this.drawFullscreenQuad();
               currentTexture = dst.method_30277();
            }

            mc.method_1522().method_1235(true);
            return currentTexture;
         } else {
            return currentTexture;
         }
      }
   }

   private void copyMainFramebuffer(class_276 target) {
      int readFbo = GL11.glGetInteger(36010);
      int drawFbo = GL11.glGetInteger(36006);
      GL30.glBindFramebuffer(36008, mc.method_1522().field_1476);
      GL30.glBindFramebuffer(36009, target.field_1476);
      GL30.glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, this.width, this.height, 16640, 9728);
      GL30.glBindFramebuffer(36008, readFbo);
      GL30.glBindFramebuffer(36009, drawFbo);
      mc.method_1522().method_1235(true);
   }

   private void configureDepthTexture(int depthTex) {
      RenderSystem.bindTexture(depthTex);
      GL11.glTexParameteri(3553, 34892, 0);
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      RenderSystem.bindTexture(0);
   }

   private void ensureBuffers() {
      int w = mc.method_22683().method_4489();
      int h = mc.method_22683().method_4506();
      if (w != this.width || h != this.height || this.beforeBuffer == null || this.afterBuffer == null || this.maskBuffer == null) {
         if (this.beforeBuffer != null) {
            this.beforeBuffer.method_1238();
         }

         if (this.afterBuffer != null) {
            this.afterBuffer.method_1238();
         }

         if (this.maskBuffer != null) {
            this.maskBuffer.method_1238();
         }

         for (class_276 fb : this.bloomBuffers) {
            fb.method_1238();
         }

         this.bloomBuffers.clear();
         this.beforeBuffer = new class_6367(w, h, true);
         this.afterBuffer = new class_6367(w, h, true);
         this.maskBuffer = new class_6367(w, h, true);
         this.width = w;
         this.height = h;
         this.configuredBeforeDepthTex = -1;
         this.configuredAfterDepthTex = -1;
      }
   }

   private void ensureBloomBuffers(int iterations) {
      while (this.bloomBuffers.size() > iterations) {
         int last = this.bloomBuffers.size() - 1;
         this.bloomBuffers.get(last).method_1238();
         this.bloomBuffers.remove(last);
      }

      for (int i = 0; i < iterations; i++) {
         int w = Math.max(2, this.width >> i + 1);
         int h = Math.max(2, this.height >> i + 1);
         if (i >= this.bloomBuffers.size()) {
            class_276 fb = new class_6367(w, h, false);
            this.setLinearFiltering(fb);
            this.bloomBuffers.add(fb);
         } else {
            class_276 fb = this.bloomBuffers.get(i);
            if (fb.field_1482 != w || fb.field_1481 != h) {
               fb.method_1238();
               class_276 var8 = new class_6367(w, h, false);
               this.setLinearFiltering(var8);
               this.bloomBuffers.set(i, var8);
            }
         }
      }
   }

   private void setLinearFiltering(class_276 fb) {
      RenderSystem.bindTexture(fb.method_30277());
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.bindTexture(0);
   }

   private ShaderHands getModule() {
      return SlikDlc.INSTANCE != null && ModuleClass.INSTANCE != null ? ModuleClass.shaderHands : null;
   }

   private void renderPrettyMode(ShaderHands module, int color1, int color2, float glowValue, float fillValue, float alphaValue, float outlineValue) {
      class_5944 shader = mc.method_62887().method_62947(ShaderUtils.blockOverlay);
      if (shader != null) {
         mc.method_1522().method_1235(false);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(ShaderUtils.blockOverlay);
         RenderSystem.setShaderTexture(0, this.maskBuffer.method_30277());
         this.setUniform(shader, "texelSize", 1.0F / Math.max(1, mc.method_22683().method_4489()), 1.0F / Math.max(1, mc.method_22683().method_4506()));
         this.setUniform(shader, "color", ColorUtils.redf(color1), ColorUtils.greenf(color1), ColorUtils.bluef(color1));
         this.setUniform(shader, "color2", ColorUtils.redf(color2), ColorUtils.greenf(color2), ColorUtils.bluef(color2));
         this.setUniform(shader, "time", (float)(System.currentTimeMillis() % 100000L) / 1000.0F);
         this.setUniform(shader, "speed", module.waveSpeed.get());
         this.setUniform(shader, "scale", module.waveScale.get());
         this.setUniform(shader, "outline", outlineValue);
         this.setUniform(shader, "glow", glowValue);
         this.setUniform(shader, "fill", fillValue);
         this.setUniform(shader, "alpha", alphaValue);
         this.setUniform(shader, "outlineOnly", 0.0F);
         this.drawFullscreenQuad();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         this.restoreCompositeState();
      }
   }

   private void restoreCompositeState() {
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.setShaderTexture(1, 0);
      RenderSystem.setShaderTexture(2, 0);
      RenderSystem.setShaderTexture(3, 0);
      mc.method_1522().method_1235(true);
   }

   private boolean isEffectEnabled(ShaderHands module) {
      if (module != null && module.isEnable()) {
         boolean hasGlow = module.glow.get() > 0.001F;
         boolean hasFill = module.fill.get() > 0.001F && module.alpha.get() > 0.001F;
         return hasGlow || hasFill;
      } else {
         return false;
      }
   }

   private void setUniform(class_5944 shader, String name, float v) {
      class_284 u = shader.method_34582(name);
      if (u != null) {
         u.method_1251(v);
      }
   }

   private void setUniform(class_5944 shader, String name, float x, float y) {
      class_284 u = shader.method_34582(name);
      if (u != null) {
         u.method_1255(x, y);
      }
   }

   private void setUniform(class_5944 shader, String name, float x, float y, float z) {
      class_284 u = shader.method_34582(name);
      if (u != null) {
         u.method_1249(x, y, z);
      }
   }

   private void setHandsKawaseUniforms(class_5944 shader, int texWidth, int texHeight, float offset) {
      this.setUniform(shader, "uSize", Math.max(1, texWidth), Math.max(1, texHeight));
      this.setUniform(shader, "uOffset", offset, offset);
      this.setUniform(shader, "uHalfPixel", 0.5F / Math.max(1, texWidth), 0.5F / Math.max(1, texHeight));
   }

   private void drawFullscreenQuad() {
      float sw = Math.max(mc.method_22683().method_4486(), 1);
      float sh = Math.max(mc.method_22683().method_4502(), 1);
      class_287 b = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      b.method_22912(0.0F, 0.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(0.0F, sh, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(sw, sh, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(sw, 0.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      class_286.method_43433(b.method_60800());
   }
}
