package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.ShaderUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.mixin.WorldRendererAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
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

public class ShaderEsp extends Module {
   public static ShaderEsp INSTANCE = new ShaderEsp();
   private static final float EPSILON = 0.001F;
   private static final long OUTLINE_RETRY_DELAY_MS = 3000L;
   private static final double MAX_RANGE = 256.0;
   private static final float FILL_ALPHA = 0.7F;
   private static final int FILL_MIN_ITERATIONS = 2;
   private static final float GLOW_VALUE = 0.55F;
   private static final float WIDTH_VALUE = 0.9F;
   private final ListSetting targets = new ListSetting(
      "Цели",
      new BooleanSetting("Игроки", true),
      new BooleanSetting("Кристаллы", true),
      new BooleanSetting("Предметы", false),
      new BooleanSetting("Себя", false)
   );
   private final BooleanSetting fill = new BooleanSetting("Заливка", false);
   private final List<class_276> bloomBuffers = new ArrayList<>();
   private class_276 depthCopyBuffer;
   private int bloomWidth = -1;
   private int bloomHeight = -1;
   private boolean outlineReady;
   private boolean hasOutlineTargetsCached;
   private long nextOutlineRetryAt;

   public ShaderEsp() {
      super("ShaderESP", "Красивая обводка энтити", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.targets, this.fill});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.outlineReady = false;
      this.nextOutlineRetryAt = 0L;
      this.tryEnsureOutlineProcessor();
   }

   @Override
   public void onDisable() {
      for (class_276 fb : this.bloomBuffers) {
         fb.method_1238();
      }

      this.bloomBuffers.clear();
      if (this.depthCopyBuffer != null) {
         this.depthCopyBuffer.method_1238();
         this.depthCopyBuffer = null;
      }

      this.bloomWidth = -1;
      this.bloomHeight = -1;
      this.outlineReady = false;
      this.hasOutlineTargetsCached = false;
      this.nextOutlineRetryAt = 0L;
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (this.isEnable()) {
         if (mc.field_1687 != null && mc.field_1769 != null) {
            this.hasOutlineTargetsCached = this.hasOutlineTargets();
            if (!this.hasOutlineTargetsCached) {
               this.outlineReady = false;
            } else {
               if (!this.outlineReady && System.currentTimeMillis() >= this.nextOutlineRetryAt) {
                  this.tryEnsureOutlineProcessor();
               }
            }
         } else {
            this.outlineReady = false;
            this.hasOutlineTargetsCached = false;
         }
      }
   }

   @EventLink(
      priority = 200
   )
   public void onRender2D(EventRender.Default event) {
      if (this.isEnable() && mc.field_1687 != null && mc.field_1724 != null && mc.field_1769 != null) {
         boolean hasGlow = true;
         boolean hasFill = this.fill.isState();
         if (hasGlow || hasFill) {
            if (this.hasOutlineTargetsCached) {
               if (this.tryEnsureOutlineProcessor()) {
                  class_276 outlineBuffer = this.getOutlineSourceFramebuffer();
                  if (outlineBuffer != null && outlineBuffer.method_30277() != 0) {
                     class_276 mainBuffer = mc.method_1522();
                     this.ensureDepthCopyBuffer(mainBuffer.field_1482, mainBuffer.field_1481);
                     int iterations = Math.max(1, Math.min(8, (int)Math.ceil(1.125)));
                     int fillTexture = 0;
                     if (hasFill) {
                        int fillIterations = Math.max(2, Math.min(6, iterations + 1));
                        fillTexture = this.runKawaseBloom(outlineBuffer.method_30277(), fillIterations);
                     }

                     int blurredTexture = hasGlow ? this.runKawaseBloom(outlineBuffer.method_30277(), iterations) : fillTexture;
                     int color = this.getOutlineColor();
                     mainBuffer.method_1235(false);
                     RenderSystem.enableBlend();
                     RenderSystem.disableDepthTest();
                     RenderSystem.colorMask(true, true, true, false);
                     if (hasFill) {
                        class_5944 fillShader = mc.method_62887().method_62947(ShaderUtils.shaderEspFill);
                        if (fillShader != null) {
                           RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA, class_4535.ZERO, class_4534.ONE);
                           RenderSystem.setShader(ShaderUtils.shaderEspFill);
                           RenderSystem.setShaderTexture(0, outlineBuffer.method_30277());
                           RenderSystem.setShaderTexture(1, fillTexture == 0 ? blurredTexture : fillTexture);
                           this.setUniform(fillShader, "color", ColorUtils.redf(color), ColorUtils.greenf(color), ColorUtils.bluef(color));
                           this.setUniform(fillShader, "alpha", 0.7F);
                           this.setUniform(fillShader, "time", (float)(System.currentTimeMillis() % 100000L) / 1000.0F);
                           this.drawFullscreenQuad();
                        }
                     }

                     if (hasGlow) {
                        class_5944 glowShader = mc.method_62887().method_62947(ShaderUtils.shaderEspGlow);
                        if (glowShader != null) {
                           RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA, class_4535.ZERO, class_4534.ONE);
                           RenderSystem.setShader(ShaderUtils.shaderEspGlow);
                           RenderSystem.setShaderTexture(0, blurredTexture);
                           RenderSystem.setShaderTexture(1, outlineBuffer.method_30277());
                           this.setUniform(glowShader, "color", ColorUtils.redf(color), ColorUtils.greenf(color), ColorUtils.bluef(color));
                           this.setUniform(glowShader, "color2", ColorUtils.redf(color), ColorUtils.greenf(color), ColorUtils.bluef(color));
                           this.setUniform(glowShader, "exposure", 0.05075F);
                           this.setUniform(glowShader, "time", (float)(System.currentTimeMillis() % 100000L) / 1000.0F);
                           this.setUniform(glowShader, "animate", 1.0F);
                           this.drawFullscreenQuadWithDepthTest(mainBuffer, outlineBuffer);
                        }
                     }

                     RenderSystem.colorMask(true, true, true, true);
                     RenderSystem.disableDepthTest();
                     RenderSystem.disableBlend();
                     RenderSystem.defaultBlendFunc();
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                     RenderSystem.setShaderTexture(0, 0);
                     RenderSystem.setShaderTexture(1, 0);
                     mainBuffer.method_1235(true);
                  }
               }
            }
         }
      }
   }

   private void drawFullscreenQuadWithDepthTest(class_276 mainBuffer, class_276 outlineBuffer) {
      if (this.depthCopyBuffer == null) {
         this.drawFullscreenQuad();
      } else {
         GL30.glBindFramebuffer(36008, mainBuffer.field_1476);
         GL30.glBindFramebuffer(36009, this.depthCopyBuffer.field_1476);
         GL30.glBlitFramebuffer(
            0, 0, mainBuffer.field_1482, mainBuffer.field_1481, 0, 0, this.depthCopyBuffer.field_1482, this.depthCopyBuffer.field_1481, 256, 9728
         );
         GL30.glBindFramebuffer(36008, outlineBuffer.field_1476);
         GL30.glBindFramebuffer(36009, mainBuffer.field_1476);
         GL30.glBlitFramebuffer(0, 0, outlineBuffer.field_1482, outlineBuffer.field_1481, 0, 0, mainBuffer.field_1482, mainBuffer.field_1481, 256, 9728);
         mainBuffer.method_1235(false);
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(515);
         RenderSystem.depthMask(false);
         this.drawFullscreenQuad();
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
         GL30.glBindFramebuffer(36008, this.depthCopyBuffer.field_1476);
         GL30.glBindFramebuffer(36009, mainBuffer.field_1476);
         GL30.glBlitFramebuffer(
            0, 0, this.depthCopyBuffer.field_1482, this.depthCopyBuffer.field_1481, 0, 0, mainBuffer.field_1482, mainBuffer.field_1481, 256, 9728
         );
         mainBuffer.method_1235(false);
      }
   }

   private void ensureDepthCopyBuffer(int width, int height) {
      if (this.depthCopyBuffer != null && (this.depthCopyBuffer.field_1482 != width || this.depthCopyBuffer.field_1481 != height)) {
         this.depthCopyBuffer.method_1238();
         this.depthCopyBuffer = null;
      }

      if (this.depthCopyBuffer == null) {
         this.depthCopyBuffer = new class_6367(width, height, true);
      }
   }

   private boolean tryEnsureOutlineProcessor() {
      if (mc.field_1687 != null && mc.field_1769 != null) {
         class_276 outlines = this.getOutlineSourceFramebuffer();
         if (outlines != null && outlines.method_30277() != 0) {
            this.outlineReady = true;
            return true;
         } else {
            if (this.outlineReady) {
               this.outlineReady = false;
            }

            if (System.currentTimeMillis() < this.nextOutlineRetryAt) {
               return false;
            } else {
               try {
                  mc.field_1769.method_3296();
                  outlines = this.getOutlineSourceFramebuffer();
                  this.outlineReady = outlines != null && outlines.method_30277() != 0;
                  if (!this.outlineReady) {
                     this.nextOutlineRetryAt = System.currentTimeMillis() + 3000L;
                  }

                  return this.outlineReady;
               } catch (Throwable var3) {
                  this.outlineReady = false;
                  this.nextOutlineRetryAt = System.currentTimeMillis() + 3000L;
                  return false;
               }
            }
         }
      } else {
         this.outlineReady = false;
         return false;
      }
   }

   private class_276 getOutlineSourceFramebuffer() {
      if (mc.field_1769 instanceof WorldRendererAccessor accessor) {
         class_276 raw = accessor.slikdlc$getEntityOutlineFramebufferRaw();
         if (raw != null && raw.method_30277() != 0) {
            return raw;
         }
      }

      return mc.field_1769.method_22990();
   }

   public boolean shouldOutline(class_1297 entity) {
      if (!this.isEnable() || entity == null || mc.field_1724 == null || mc.field_1687 == null) {
         return false;
      } else if (!entity.method_5805()) {
         return false;
      } else if (entity.method_31481()) {
         return false;
      } else if (entity == mc.field_1724 && !this.targets.is("Себя")) {
         return false;
      } else if (entity.method_5858(mc.field_1724) > 65536.0) {
         return false;
      } else if (entity instanceof class_1657) {
         return this.targets.is("Игроки");
      } else if (entity instanceof class_1511) {
         return this.targets.is("Кристаллы");
      } else {
         return entity instanceof class_1542 ? this.targets.is("Предметы") : false;
      }
   }

   private boolean hasOutlineTargets() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (this.shouldOutline(entity)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public int getOutlineColor() {
      return ColorUtils.setAlphaColor(ColorUtils.getThemeColor(), 255) & 16777215;
   }

   private int runKawaseBloom(int sourceTexture, int iterations) {
      this.ensureBloomBuffers(iterations);
      if (this.bloomBuffers.isEmpty()) {
         return sourceTexture;
      } else {
         int currentTexture = sourceTexture;
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
            return sourceTexture;
         }
      }
   }

   private void ensureBloomBuffers(int iterations) {
      int w = mc.method_22683().method_4489();
      int h = mc.method_22683().method_4506();
      if (this.bloomWidth != w || this.bloomHeight != h) {
         for (class_276 fb : this.bloomBuffers) {
            fb.method_1238();
         }

         this.bloomBuffers.clear();
         this.bloomWidth = w;
         this.bloomHeight = h;
      }

      while (this.bloomBuffers.size() > iterations) {
         int last = this.bloomBuffers.size() - 1;
         this.bloomBuffers.get(last).method_1238();
         this.bloomBuffers.remove(last);
      }

      for (int i = 0; i < iterations; i++) {
         int tw = Math.max(2, w >> i + 1);
         int th = Math.max(2, h >> i + 1);
         if (i >= this.bloomBuffers.size()) {
            class_276 fb = new class_6367(tw, th, false);
            this.setLinearFiltering(fb);
            this.bloomBuffers.add(fb);
         } else {
            class_276 fb = this.bloomBuffers.get(i);
            if (fb.field_1482 != tw || fb.field_1481 != th) {
               fb.method_1238();
               class_276 var12 = new class_6367(tw, th, false);
               this.setLinearFiltering(var12);
               this.bloomBuffers.set(i, var12);
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

   private void setUniform(class_5944 shader, String name, float value) {
      class_284 uniform = shader.method_34582(name);
      if (uniform != null) {
         uniform.method_1251(value);
      }
   }

   private void setUniform(class_5944 shader, String name, float x, float y) {
      class_284 uniform = shader.method_34582(name);
      if (uniform != null) {
         uniform.method_1255(x, y);
      }
   }

   private void setUniform(class_5944 shader, String name, float x, float y, float z) {
      class_284 uniform = shader.method_34582(name);
      if (uniform != null) {
         uniform.method_1249(x, y, z);
      }
   }

   private void setHandsKawaseUniforms(class_5944 shader, int texWidth, int texHeight, float offset) {
      this.setUniform(shader, "uSize", Math.max(1, texWidth), Math.max(1, texHeight));
      this.setUniform(shader, "uOffset", offset, offset);
      this.setUniform(shader, "uHalfPixel", 0.5F / Math.max(1, texWidth), 0.5F / Math.max(1, texHeight));
   }

   private void drawFullscreenQuad() {
      float width = Math.max(mc.method_22683().method_4486(), 1);
      float height = Math.max(mc.method_22683().method_4502(), 1);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      buffer.method_22912(0.0F, 0.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.method_22912(0.0F, height, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.method_22912(width, height, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.method_22912(width, 0.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      class_286.method_43433(buffer.method_60800());
   }
}
