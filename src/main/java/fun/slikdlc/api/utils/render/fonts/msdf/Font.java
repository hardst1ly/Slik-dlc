package fun.slikdlc.api.utils.render.fonts.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.render.ShaderUtils;
import net.minecraft.class_284;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import net.minecraft.class_5944;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class Font implements QClient {
   private static final char FORMATTING_CODE_PREFIX = '§';
   private final MsdfFont font;
   private final float size;

   public Font(MsdfFont font, float size) {
      this.font = font;
      this.size = size;
   }

   public Font(String name, float size) {
      this.font = MsdfFont.builder().atlas(name).data(name).build();
      this.size = size;
   }

   public void drawString(class_4587 matrixStack, String text, double x, double y, int color) {
      this.draw(matrixStack, text, (float)x, (float)y, color);
   }

   public void drawString(class_4587 matrixStack, String text, float x, float y, int color) {
      this.draw(matrixStack, text, x, y, color);
   }

   public void drawString(String text, float x, float y, int color) {
      class_4587 stack = new class_4587();
      this.draw(stack, text, x, y, color);
   }

   public void drawCenteredString(class_4587 matrixStack, String text, double x, double y, int color) {
      this.draw(matrixStack, text, (float)(x - this.getStringWidth(text) / 2.0), (float)y, color);
   }

   public void drawCenteredString(class_4587 matrixStack, String text, float x, float y, int color) {
      this.draw(matrixStack, text, x - this.getStringWidth(text) / 2.0F, y, color);
   }

   public void drawRight(class_4587 matrixStack, String text, double x, double y, int color) {
      this.draw(matrixStack, text, (float)(x - this.getStringWidth(text)), (float)y, color);
   }

   public void drawRight(class_4587 matrixStack, String text, float x, float y, int color) {
      this.draw(matrixStack, text, x - this.getStringWidth(text), y, color);
   }

   public void draw(class_4587 stack, String text, double x, double y, int color) {
      this.draw(stack, text, (float)x, (float)y, color);
   }

   public void draw(class_4587 stack, String text, float x, float y, int color) {
      if (text != null && !text.isEmpty()) {
         float localSize = this.size * 0.5F;
         if (this.hasDrawableGlyphs(text, localSize)) {
            y--;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            class_5944 shader = mc.method_62887().method_62947(ShaderUtils.fontsMsdf);
            if (shader != null) {
               this.setupShaderUniforms(shader, color);
               RenderSystem.setShaderTexture(0, this.font.getTextureId());
               this.font.setFiltered();
               Matrix4f matrix = stack.method_23760().method_23761();
               class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
               this.font.applyGlyphs(matrix, buffer, localSize, text, 0.0F, x, y + this.font.getBaselineHeight() * localSize, 0.0F, 255, 255, 255, 255);
               RenderSystem.setShader(ShaderUtils.fontsMsdf);
               class_286.method_43433(buffer.method_60800());
               RenderSystem.setShaderTexture(0, 0);
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
            }
         }
      }
   }

   public void drawGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor) {
      class_4587 stack = new class_4587();
      this.drawGradientStringHorizontal(stack, text, x, y, leftColor, rightColor);
   }

   public void drawGradientStringHorizontal(class_4587 stack, String text, float x, float y, int leftColor, int rightColor) {
      if (text != null && !text.isEmpty()) {
         float totalWidth = this.getStringWidth(text);
         float currentX = x;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            float charWidth = this.getStringWidth(charStr);
            float progress = totalWidth > 0.0F ? (currentX - x) / totalWidth : 0.0F;
            int color = interpolateColor(leftColor, rightColor, progress);
            this.draw(stack, charStr, currentX, y, color);
            currentX += charWidth;
         }
      }
   }

   public void drawGradientStringHorizontal(
      class_4587 stack, String text, float x, float y, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor
   ) {
      if (text != null && !text.isEmpty()) {
         float totalWidth = this.getStringWidth(text);
         float currentX = x;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            float charWidth = this.getStringWidth(charStr);
            float progress = totalWidth > 0.0F ? (currentX - x) / totalWidth : 0.0F;
            int topColor = interpolateColor(topLeftColor, topRightColor, progress);
            int bottomColor = interpolateColor(bottomLeftColor, bottomRightColor, progress);
            int color = interpolateColor(topColor, bottomColor, 0.5F);
            this.draw(stack, charStr, currentX, y, color);
            currentX += charWidth;
         }
      }
   }

   public void drawGradientStringVertical(class_4587 stack, String text, float x, float y, int topColor, int bottomColor) {
      if (text != null && !text.isEmpty()) {
         int color = interpolateColor(topColor, bottomColor, 0.5F);
         this.draw(stack, text, x, y, color);
      }
   }

   public void drawStringWithFade(class_4587 stack, String text, float x, float y, float maxWidth, int color) {
      if (text != null && !text.isEmpty()) {
         if (!(maxWidth <= 1.0F)) {
            int originalAlpha = color >>> 24 & 0xFF;
            if (originalAlpha == 0) {
               originalAlpha = 255;
            }

            if (originalAlpha > 4) {
               float localSize = this.size * 0.5F;
               y--;
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableCull();
               class_5944 shader = mc.method_62887().method_62947(ShaderUtils.fontsMsdf);
               if (shader != null) {
                  class_284 textureSizeUniform = shader.method_34582("TextureSize");
                  class_284 rangeUniform = shader.method_34582("Range");
                  class_284 thicknessUniform = shader.method_34582("Thickness");
                  class_284 edgeStrengthUniform = shader.method_34582("EdgeStrength");
                  class_284 colorUniform = shader.method_34582("Color");
                  class_284 outlineUniform = shader.method_34582("Outline");
                  class_284 outlineThicknessUniform = shader.method_34582("OutlineThickness");
                  class_284 outlineColorUniform = shader.method_34582("OutlineColor");
                  if (textureSizeUniform != null) {
                     textureSizeUniform.method_1255(this.font.getAtlasWidth(), this.font.getAtlasHeight());
                  }

                  if (rangeUniform != null) {
                     rangeUniform.method_1251(this.font.getRange());
                  }

                  if (thicknessUniform != null) {
                     thicknessUniform.method_1251(0.0F);
                  }

                  if (edgeStrengthUniform != null) {
                     edgeStrengthUniform.method_1251(0.5F);
                  }

                  if (outlineUniform != null) {
                     outlineUniform.method_35649(0);
                  }

                  if (outlineThicknessUniform != null) {
                     outlineThicknessUniform.method_1251(0.0F);
                  }

                  if (outlineColorUniform != null) {
                     outlineColorUniform.method_35657(1.0F, 1.0F, 1.0F, 1.0F);
                  }

                  RenderSystem.setShaderTexture(0, this.font.getTextureId());
                  this.font.setFiltered();
                  float currentX = x;
                  float fadeZoneWidth = 25.0F;
                  float fadeStartX = x + maxWidth - fadeZoneWidth;

                  for (int i = 0; i < text.length(); i++) {
                     String charStr = String.valueOf(text.charAt(i));
                     float charWidth = this.getStringWidth(charStr);
                     if (currentX > x + maxWidth && i > 0) {
                        break;
                     }

                     int finalColor = color;
                     if (currentX > fadeStartX) {
                        float progressIntoFade = (currentX - fadeStartX) / fadeZoneWidth;
                        progressIntoFade = Math.max(0.0F, Math.min(1.0F, progressIntoFade));
                        float fadeFactor = (float)Math.cos(progressIntoFade * Math.PI / 2.0);
                        int newAlpha = (int)(originalAlpha * fadeFactor);
                        finalColor = color & 16777215 | newAlpha << 24;
                     }

                     if ((finalColor >>> 24 & 0xFF) > 4) {
                        float[] rgba = this.extractRgba(finalColor);
                        if (colorUniform != null) {
                           colorUniform.method_35657(rgba[0], rgba[1], rgba[2], rgba[3]);
                        }

                        Matrix4f matrix = stack.method_23760().method_23761();
                        class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
                        this.font
                           .applyGlyphs(
                              matrix, buffer, localSize, charStr, 0.0F, currentX, y + this.font.getBaselineHeight() * localSize, 0.0F, 255, 255, 255, 255
                           );
                        RenderSystem.setShader(ShaderUtils.fontsMsdf);
                        class_286.method_43433(buffer.method_60800());
                     }

                     currentX += charWidth;
                  }

                  RenderSystem.setShaderTexture(0, 0);
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
               }
            }
         }
      }
   }

   public void drawAnimatedGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor, float speed) {
      class_4587 stack = new class_4587();
      this.drawAnimatedGradientStringHorizontal(stack, text, x, y, leftColor, rightColor, speed, 1.15F);
   }

   public void drawAnimatedGradientStringHorizontal(class_4587 stack, String text, float x, float y, int leftColor, int rightColor, float speed) {
      this.drawAnimatedGradientStringHorizontal(stack, text, x, y, leftColor, rightColor, speed, 1.15F);
   }

   public void drawAnimatedGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor, float speed, float waveScale) {
      class_4587 stack = new class_4587();
      this.drawAnimatedGradientStringHorizontal(stack, text, x, y, leftColor, rightColor, speed, waveScale);
   }

   public void drawAnimatedGradientStringHorizontal(
      class_4587 stack, String text, float x, float y, int leftColor, int rightColor, float speed, float waveScale
   ) {
      if (text != null && !text.isEmpty()) {
         float totalWidth = this.getStringWidth(text);
         float currentX = x;
         double timeOffset = System.currentTimeMillis() * 0.001 * Math.max(0.01F, speed) % 2.0;
         float safeWaveScale = Math.max(0.01F, waveScale);

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String charStr = String.valueOf(c);
            float charWidth = this.getStringWidth(charStr);
            float baseProgress = totalWidth > 0.0F ? (currentX - x) / totalWidth : 0.0F;
            float animatedProgress = pingPong01(baseProgress * safeWaveScale + (float)timeOffset);
            int color = interpolateColor(leftColor, rightColor, animatedProgress);
            this.draw(stack, charStr, currentX, y, color);
            currentX += charWidth;
         }
      }
   }

   public void drawStringWithOutline(class_4587 stack, String text, float x, float y, int color, int outlineColor) {
      if (text != null && !text.isEmpty()) {
         this.draw(stack, text, x - 1.0F, y, outlineColor);
         this.draw(stack, text, x + 1.0F, y, outlineColor);
         this.draw(stack, text, x, y - 1.0F, outlineColor);
         this.draw(stack, text, x, y + 1.0F, outlineColor);
         this.draw(stack, text, x, y, color);
      }
   }

   public void drawStringWithShadow(class_4587 stack, String text, float x, float y, int color) {
      if (text != null && !text.isEmpty()) {
         int shadowColor = 1426063360;
         this.draw(stack, text, x + 1.0F, y + 1.0F, shadowColor);
         this.draw(stack, text, x, y, color);
      }
   }

   public void drawParagraph(class_4587 stack, String text, double x, double y, int defaultColor) {
      this.drawParagraph(stack, text, (float)x, (float)y, defaultColor);
   }

   public void drawParagraph(class_4587 stack, String text, float x, float y, int defaultColor) {
      if (text != null && !text.isEmpty()) {
         float localSize = this.size * 0.5F;
         y--;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         class_5944 shader = mc.method_62887().method_62947(ShaderUtils.fontsMsdf);
         if (shader != null) {
            class_284 textureSizeUniform = shader.method_34582("TextureSize");
            class_284 rangeUniform = shader.method_34582("Range");
            class_284 thicknessUniform = shader.method_34582("Thickness");
            class_284 edgeStrengthUniform = shader.method_34582("EdgeStrength");
            class_284 colorUniform = shader.method_34582("Color");
            if (textureSizeUniform != null) {
               textureSizeUniform.method_1255(this.font.getAtlasWidth(), this.font.getAtlasHeight());
            }

            if (rangeUniform != null) {
               rangeUniform.method_1251(this.font.getRange());
            }

            if (thicknessUniform != null) {
               thicknessUniform.method_1251(0.0F);
            }

            if (edgeStrengthUniform != null) {
               edgeStrengthUniform.method_1251(0.5F);
            }

            RenderSystem.setShaderTexture(0, this.font.getTextureId());
            this.font.setFiltered();
            float currentX = x;
            int currentColor = defaultColor;
            StringBuilder segment = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
               char c = text.charAt(i);
               if (c == 167 && i + 1 < text.length()) {
                  if (!segment.isEmpty()) {
                     this.drawSegment(stack, colorUniform, segment.toString(), currentX, y + this.font.getBaselineHeight() * localSize, localSize, currentColor);
                     currentX += this.getStringWidth(segment.toString());
                     segment.setLength(0);
                  }

                  char code = text.charAt(i + 1);
                  int newColor = this.getColorFromCode(code, defaultColor);
                  if (newColor != -1) {
                     currentColor = newColor;
                  }

                  i++;
               } else {
                  segment.append(c);
               }
            }

            if (!segment.isEmpty()) {
               this.drawSegment(stack, colorUniform, segment.toString(), currentX, y + this.font.getBaselineHeight() * localSize, localSize, currentColor);
            }

            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
         }
      }
   }

   private void drawSegment(class_4587 stack, class_284 colorUniform, String text, float x, float y, float size, int color) {
      if (this.hasDrawableGlyphs(text, size)) {
         float[] rgba = this.extractRgba(color);
         if (colorUniform != null) {
            colorUniform.method_35657(rgba[0], rgba[1], rgba[2], rgba[3]);
         }

         Matrix4f matrix = stack.method_23760().method_23761();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
         this.font.applyGlyphs(matrix, buffer, size, text, 0.0F, x, y, 0.0F, 255, 255, 255, 255);
         RenderSystem.setShader(ShaderUtils.fontsMsdf);
         class_286.method_43433(buffer.method_60800());
      }
   }

   private boolean hasDrawableGlyphs(String text, float renderSize) {
      return text != null && !text.isEmpty() && this.font.getWidth(text, renderSize) > 0.0F;
   }

   private void setupShaderUniforms(class_5944 shader, int color) {
      class_284 textureSizeUniform = shader.method_34582("TextureSize");
      class_284 rangeUniform = shader.method_34582("Range");
      class_284 thicknessUniform = shader.method_34582("Thickness");
      class_284 edgeStrengthUniform = shader.method_34582("EdgeStrength");
      class_284 colorUniform = shader.method_34582("Color");
      class_284 outlineUniform = shader.method_34582("Outline");
      class_284 outlineThicknessUniform = shader.method_34582("OutlineThickness");
      class_284 outlineColorUniform = shader.method_34582("OutlineColor");
      if (textureSizeUniform != null) {
         textureSizeUniform.method_1255(this.font.getAtlasWidth(), this.font.getAtlasHeight());
      }

      if (rangeUniform != null) {
         rangeUniform.method_1251(this.font.getRange());
      }

      if (thicknessUniform != null) {
         thicknessUniform.method_1251(0.0F);
      }

      if (edgeStrengthUniform != null) {
         edgeStrengthUniform.method_1251(0.5F);
      }

      if (outlineUniform != null) {
         outlineUniform.method_35649(0);
      }

      if (outlineThicknessUniform != null) {
         outlineThicknessUniform.method_1251(0.0F);
      }

      if (outlineColorUniform != null) {
         outlineColorUniform.method_35657(0.0F, 0.0F, 0.0F, 1.0F);
      }

      float[] rgba = this.extractRgba(color);
      if (colorUniform != null) {
         colorUniform.method_35657(rgba[0], rgba[1], rgba[2], rgba[3]);
      }
   }

   private int getColorFromCode(char code, int defaultColor) {
      int alpha = defaultColor >> 24 & 0xFF;
      if (alpha == 0) {
         alpha = 255;
      }
      return switch (code) {
         case '0' -> alpha << 24 | 0;
         case '1' -> alpha << 24 | 170;
         case '2' -> alpha << 24 | 43520;
         case '3' -> alpha << 24 | 43690;
         case '4' -> alpha << 24 | 11141120;
         case '5' -> alpha << 24 | 11141290;
         case '6' -> alpha << 24 | 16755200;
         case '7' -> alpha << 24 | 11184810;
         case '8' -> alpha << 24 | 5592405;
         case '9' -> alpha << 24 | 5592575;
         default -> -1;
         case 'A', 'a' -> alpha << 24 | 5635925;
         case 'B', 'b' -> alpha << 24 | 5636095;
         case 'C', 'c' -> alpha << 24 | 16733525;
         case 'D', 'd' -> alpha << 24 | 16733695;
         case 'E', 'e' -> alpha << 24 | 16777045;
         case 'F', 'f' -> alpha << 24 | 16777215;
         case 'R', 'r' -> defaultColor;
      };
   }

   private float[] extractRgba(int color) {
      int a = color >> 24 & 0xFF;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      if (a == 0) {
         a = 255;
      }

      return new float[]{r / 255.0F, g / 255.0F, b / 255.0F, a / 255.0F};
   }

   public static int interpolateColor(int color1, int color2, float progress) {
      progress = Math.max(0.0F, Math.min(1.0F, progress));
      int a1 = color1 >> 24 & 0xFF;
      int r1 = color1 >> 16 & 0xFF;
      int g1 = color1 >> 8 & 0xFF;
      int b1 = color1 & 0xFF;
      int a2 = color2 >> 24 & 0xFF;
      int r2 = color2 >> 16 & 0xFF;
      int g2 = color2 >> 8 & 0xFF;
      int b2 = color2 & 0xFF;
      if (a1 == 0) {
         a1 = 255;
      }

      if (a2 == 0) {
         a2 = 255;
      }

      int a = (int)(a1 + (a2 - a1) * progress);
      int r = (int)(r1 + (r2 - r1) * progress);
      int g = (int)(g1 + (g2 - g1) * progress);
      int b = (int)(b1 + (b2 - b1) * progress);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static float pingPong01(float value) {
      float wrapped = value % 2.0F;
      if (wrapped < 0.0F) {
         wrapped += 2.0F;
      }

      return wrapped > 1.0F ? 2.0F - wrapped : wrapped;
   }

   public float getStringWidth(String text) {
      return text == null ? 0.0F : this.font.getWidth(this.stripFormattingCodes(text), this.size) / 2.0F;
   }

   public float getWidth(String text) {
      return this.getStringWidth(text);
   }

   public float getHeight() {
      return this.size;
   }

   public float getFontHeight() {
      return this.size;
   }

   public MsdfFont getFont() {
      return this.font;
   }

   public float getSize() {
      return this.size;
   }

   private String stripFormattingCodes(String text) {
      if (text != null && text.indexOf(167) >= 0) {
         StringBuilder clean = new StringBuilder(text.length());

         for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == 167 && i + 1 < text.length()) {
               i++;
            } else {
               clean.append(current);
            }
         }

         return clean.toString();
      } else {
         return text;
      }
   }
}
