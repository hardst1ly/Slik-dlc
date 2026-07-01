package fun.slikdlc.api.utils.render.blur;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.render.ShaderUtils;
import lombok.Generated;
import net.minecraft.class_276;
import net.minecraft.class_284;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_5944;
import net.minecraft.class_6367;
import net.minecraft.class_293.class_5596;
import org.lwjgl.opengl.GL30;

public class BlurProgram implements QClient {
   private static BlurProgram instance;
   private static class_276 buffer1;
   private static class_276 buffer2;
   private int lastWidth = -1;
   private int lastHeight = -1;
   private long lastUpdateTime = 0L;
   private boolean requestedThisFrame = true;
   private float blurOffset = 1.0F;
   private final int iterations = 4;

   public BlurProgram() {
   }

   public static BlurProgram getInstance() {
      if (instance == null) {
         instance = new BlurProgram();
      }

      return instance;
   }

   public void beginFrame() {
      boolean shouldDraw = this.requestedThisFrame;
      this.requestedThisFrame = false;
      if (shouldDraw) {
         this.draw();
      }
   }

   public void request() {
      this.requestedThisFrame = true;
   }

   private void draw() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastUpdateTime >= 16L) {
         this.lastUpdateTime = currentTime;
         int width = mc.method_22683().method_4489();
         int height = mc.method_22683().method_4506();
         if (buffer1 == null || buffer2 == null || this.lastWidth != width || this.lastHeight != height) {
            if (buffer1 != null) {
               buffer1.method_1238();
            }

            if (buffer2 != null) {
               buffer2.method_1238();
            }

            buffer1 = new class_6367(width, height, false);
            buffer2 = new class_6367(width, height, false);
            this.setLinearFiltering(buffer1);
            this.setLinearFiltering(buffer2);
            this.lastWidth = width;
            this.lastHeight = height;
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         class_5944 kawaseDown = mc.method_62887().method_62947(ShaderUtils.kawaseDown);
         class_5944 kawaseUp = mc.method_62887().method_62947(ShaderUtils.kawaseUp);
         buffer1.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
         buffer1.method_1230();
         buffer1.method_1235(true);
         RenderSystem.setShader(ShaderUtils.kawaseDown);
         mc.method_1522().method_35610();
         RenderSystem.setShaderTexture(0, mc.method_1522().method_30277());
         this.setKawaseUniforms(kawaseDown, width, height);
         this.drawQuad(mc.method_22683().method_4486(), mc.method_22683().method_4502());
         mc.method_1522().method_1242();
         buffer1.method_1240();
         class_276[] buffers = new class_276[]{buffer1, buffer2};

         for (int i = 1; i < 4; i++) {
            int srcIndex = (i + 1) % 2;
            int dstIndex = i % 2;
            class_276 src = buffers[srcIndex];
            class_276 dst = buffers[dstIndex];
            dst.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
            dst.method_1230();
            dst.method_1235(true);
            RenderSystem.setShader(ShaderUtils.kawaseDown);
            src.method_35610();
            RenderSystem.setShaderTexture(0, src.method_30277());
            this.setKawaseUniforms(kawaseDown, src.field_1482, src.field_1481);
            this.drawQuad(mc.method_22683().method_4486(), mc.method_22683().method_4502());
            src.method_1242();
            dst.method_1240();
         }

         for (int i = 0; i < 4; i++) {
            int srcIndex = i % 2;
            int dstIndex = (i + 1) % 2;
            class_276 src = buffers[srcIndex];
            class_276 dst = buffers[dstIndex];
            dst.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
            dst.method_1230();
            dst.method_1235(true);
            RenderSystem.setShader(ShaderUtils.kawaseUp);
            src.method_35610();
            RenderSystem.setShaderTexture(0, src.method_30277());
            this.setKawaseUniforms(kawaseUp, src.field_1482, src.field_1481);
            this.drawQuad(mc.method_22683().method_4486(), mc.method_22683().method_4502());
            src.method_1242();
            dst.method_1240();
         }

         RenderSystem.disableBlend();
         mc.method_1522().method_1235(true);
         RenderSystem.setShaderTexture(0, 0);
      }
   }

   private void setLinearFiltering(class_276 framebuffer) {
      RenderSystem.bindTexture(framebuffer.method_30277());
      GL30.glTexParameteri(3553, 10241, 9729);
      GL30.glTexParameteri(3553, 10240, 9729);
      RenderSystem.bindTexture(0);
   }

   private void setKawaseUniforms(class_5944 shader, int texWidth, int texHeight) {
      class_284 resolutionUniform = shader.method_34582("Resolution");
      class_284 offsetUniform = shader.method_34582("Offset");
      class_284 saturationUniform = shader.method_34582("Saturation");
      class_284 tintIntensityUniform = shader.method_34582("TintIntensity");
      class_284 tintColorUniform = shader.method_34582("TintColor");
      if (resolutionUniform != null) {
         resolutionUniform.method_1255(1.0F / texWidth, 1.0F / texHeight);
      }

      if (offsetUniform != null) {
         offsetUniform.method_1251(this.blurOffset);
      }

      if (saturationUniform != null) {
         saturationUniform.method_1251(1.0F);
      }

      if (tintIntensityUniform != null) {
         tintIntensityUniform.method_1251(0.0F);
      }

      if (tintColorUniform != null) {
         tintColorUniform.method_1249(1.0F, 1.0F, 1.0F);
      }
   }

   private void drawQuad(float width, float height) {
      class_287 builder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      builder.method_22912(0.0F, 0.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      builder.method_22912(0.0F, height, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      builder.method_22912(width, height, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      builder.method_22912(width, 0.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      class_286.method_43433(builder.method_60800());
   }

   public static int getTexture() {
      getInstance().request();
      return buffer1 != null ? buffer1.method_30277() : 0;
   }

   @Generated
   public static class_276 getBuffer1() {
      return buffer1;
   }

   @Generated
   public static class_276 getBuffer2() {
      return buffer2;
   }

   @Generated
   public void setBlurOffset(float blurOffset) {
      this.blurOffset = blurOffset;
   }
}
