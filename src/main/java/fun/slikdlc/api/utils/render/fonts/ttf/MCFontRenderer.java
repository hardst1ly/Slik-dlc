package fun.slikdlc.api.utils.render.fonts.ttf;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.awt.Font;
import net.minecraft.class_10142;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class MCFontRenderer extends CFont {
   private final int[] colorCode = new int[32];
   protected CFont.CharData[] boldChars = new CFont.CharData[1104];
   protected CFont.CharData[] italicChars = new CFont.CharData[1104];
   protected CFont.CharData[] boldItalicChars = new CFont.CharData[1104];
   protected int texBold;
   protected int texItalic;
   protected int texItalicBold;

   public MCFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
      super(font, antiAlias, fractionalMetrics);
      this.setupBoldItalicIDs();

      for (int index = 0; index < 32; index++) {
         int noClue = (index >> 3 & 1) * 85;
         int red = (index >> 2 & 1) * 170 + noClue;
         int green = (index >> 1 & 1) * 170 + noClue;
         int blue = (index & 1) * 170 + noClue;
         if (index == 6) {
            red += 85;
         }

         if (index >= 16) {
            red /= 4;
            green /= 4;
            blue /= 4;
         }

         this.colorCode[index] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
      }
   }

   public float drawStringWithShadow(String text, double x, double y, int color) {
      float shadowWidth = this.drawString(text, x + 0.5, y + 0.5, color, true);
      return Math.max(shadowWidth, this.drawString(text, x, y, color, false));
   }

   public float drawGradientString(String text, float x, float y, int topColor, int bottomColor) {
      if (text == null) {
         return 0.0F;
      } else {
         x--;
         if ((topColor & -67108864) == 0) {
            topColor |= -16777216;
         }

         if ((bottomColor & -67108864) == 0) {
            bottomColor |= -16777216;
         }

         float topAlpha = (topColor >> 24 & 0xFF) / 255.0F;
         float topRed = (topColor >> 16 & 0xFF) / 255.0F;
         float topGreen = (topColor >> 8 & 0xFF) / 255.0F;
         float topBlue = (topColor & 0xFF) / 255.0F;
         float botAlpha = (bottomColor >> 24 & 0xFF) / 255.0F;
         float botRed = (bottomColor >> 16 & 0xFF) / 255.0F;
         float botGreen = (bottomColor >> 8 & 0xFF) / 255.0F;
         float botBlue = (bottomColor & 0xFF) / 255.0F;
         double posX = x * 2.0;
         double posY = (y - 3.0) * 2.0;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         Matrix4f matrix = new Matrix4f();
         matrix.scale(0.5F, 0.5F, 0.5F);
         CFont.CharData[] currentData = this.charData;
         int size = text.length();

         for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (character < currentData.length && currentData[character] != null) {
               RenderSystem.setShaderTexture(0, this.glTextureId);
               RenderSystem.setShader(class_10142.field_53880);
               class_289 tessellator = class_289.method_1348();
               class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
               CFont.CharData cd = currentData[character];
               float charXPos = cd.storedX;
               float charYPos = cd.storedY;
               float width = cd.width;
               float height = cd.height;
               float u0 = charXPos / 512.0F;
               float v0 = charYPos / 512.0F;
               float u1 = (charXPos + width) / 512.0F;
               float v1 = (charYPos + height) / 512.0F;
               buffer.method_22918(matrix, (float)posX, (float)posY, 0.0F).method_22913(u0, v0).method_22915(topRed, topGreen, topBlue, topAlpha);
               buffer.method_22918(matrix, (float)posX, (float)posY + height, 0.0F).method_22913(u0, v1).method_22915(botRed, botGreen, botBlue, botAlpha);
               buffer.method_22918(matrix, (float)posX + width, (float)posY + height, 0.0F)
                  .method_22913(u1, v1)
                  .method_22915(botRed, botGreen, botBlue, botAlpha);
               buffer.method_22918(matrix, (float)posX + width, (float)posY, 0.0F).method_22913(u1, v0).method_22915(topRed, topGreen, topBlue, topAlpha);
               class_286.method_43433(buffer.method_60800());
               posX += cd.width - 8 + this.charOffset;
            } else if (character == ' ' || character == 160) {
               posX += 8.0;
            }
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         return (float)posX / 2.0F;
      }
   }

   public float drawGradientStringHorizontal(String text, float x, float y, int leftColor, int rightColor) {
      if (text == null) {
         return 0.0F;
      } else {
         x--;
         if ((leftColor & -67108864) == 0) {
            leftColor |= -16777216;
         }

         if ((rightColor & -67108864) == 0) {
            rightColor |= -16777216;
         }

         double posX = x * 2.0;
         double posY = (y - 3.0) * 2.0;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         Matrix4f matrix = new Matrix4f();
         matrix.scale(0.5F, 0.5F, 0.5F);
         CFont.CharData[] currentData = this.charData;
         int size = text.length();
         float totalWidth = this.getStringWidth(text) * 2.0F;
         float currentWidth = 0.0F;

         for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (character < currentData.length && currentData[character] != null) {
               RenderSystem.setShaderTexture(0, this.glTextureId);
               RenderSystem.setShader(class_10142.field_53880);
               class_289 tessellator = class_289.method_1348();
               class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
               CFont.CharData cd = currentData[character];
               float charXPos = cd.storedX;
               float charYPos = cd.storedY;
               float width = cd.width;
               float height = cd.height;
               float charWidth = cd.width - 8 + this.charOffset;
               float u0 = charXPos / 512.0F;
               float v0 = charYPos / 512.0F;
               float u1 = (charXPos + width) / 512.0F;
               float v1 = (charYPos + height) / 512.0F;
               float firstMix = totalWidth <= 0.0F ? 0.0F : currentWidth / totalWidth;
               float lastMix = totalWidth <= 0.0F ? 1.0F : (currentWidth + charWidth) / totalWidth;
               int firstColor = this.colorMix(leftColor, rightColor, firstMix);
               int lastColor = this.colorMix(leftColor, rightColor, lastMix);
               float firstAlpha = (firstColor >> 24 & 0xFF) / 255.0F;
               float firstRed = (firstColor >> 16 & 0xFF) / 255.0F;
               float firstGreen = (firstColor >> 8 & 0xFF) / 255.0F;
               float firstBlue = (firstColor & 0xFF) / 255.0F;
               float lastAlpha = (lastColor >> 24 & 0xFF) / 255.0F;
               float lastRed = (lastColor >> 16 & 0xFF) / 255.0F;
               float lastGreen = (lastColor >> 8 & 0xFF) / 255.0F;
               float lastBlue = (lastColor & 0xFF) / 255.0F;
               buffer.method_22918(matrix, (float)posX, (float)posY, 0.0F).method_22913(u0, v0).method_22915(firstRed, firstGreen, firstBlue, firstAlpha);
               buffer.method_22918(matrix, (float)posX, (float)posY + height, 0.0F)
                  .method_22913(u0, v1)
                  .method_22915(firstRed, firstGreen, firstBlue, firstAlpha);
               buffer.method_22918(matrix, (float)posX + width, (float)posY + height, 0.0F)
                  .method_22913(u1, v1)
                  .method_22915(lastRed, lastGreen, lastBlue, lastAlpha);
               buffer.method_22918(matrix, (float)posX + width, (float)posY, 0.0F).method_22913(u1, v0).method_22915(lastRed, lastGreen, lastBlue, lastAlpha);
               class_286.method_43433(buffer.method_60800());
               posX += charWidth;
               currentWidth += charWidth;
            } else if (character == ' ' || character == 160) {
               posX += 8.0;
               currentWidth += 8.0F;
            }
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         return (float)posX / 2.0F;
      }
   }

   private int colorMix(int startColor, int endColor, float mix) {
      float startAlpha = (startColor >> 24 & 0xFF) / 255.0F;
      float startRed = (startColor >> 16 & 0xFF) / 255.0F;
      float startGreen = (startColor >> 8 & 0xFF) / 255.0F;
      float startBlue = (startColor & 0xFF) / 255.0F;
      float endAlpha = (endColor >> 24 & 0xFF) / 255.0F;
      float endRed = (endColor >> 16 & 0xFF) / 255.0F;
      float endGreen = (endColor >> 8 & 0xFF) / 255.0F;
      float endBlue = (endColor & 0xFF) / 255.0F;
      int mixAlpha = (int)(((1.0F - mix) * startAlpha + mix * endAlpha) * 255.0F);
      int mixRed = (int)(((1.0F - mix) * startRed + mix * endRed) * 255.0F);
      int mixGreen = (int)(((1.0F - mix) * startGreen + mix * endGreen) * 255.0F);
      int mixBlue = (int)(((1.0F - mix) * startBlue + mix * endBlue) * 255.0F);
      return mixAlpha << 24 | mixRed << 16 | mixGreen << 8 | mixBlue;
   }

   public float drawString(String text, float x, float y, int color) {
      return this.drawString(text, x, y, color, false);
   }

   public float drawCenteredString(String text, float x, float y, int color) {
      return this.drawString(text, x - this.getStringWidth(text) / 2.0F, y, color);
   }

   public float drawCenteredStringWithShadow(String text, float x, float y, int color) {
      return this.drawStringWithShadow(text, x - this.getStringWidth(text) / 2.0F, y, color);
   }

   public float drawString(String text, double x, double y, int color, boolean shadow) {
      x--;
      if (text == null) {
         return 0.0F;
      } else {
         if (color == 553648127) {
            color = 16777215;
         }

         if ((color & -67108864) == 0) {
            color |= -16777216;
         }

         if (shadow) {
            color = (color & 16579836) >> 2 | color & new Color(20, 20, 20, 200).getRGB();
         }

         CFont.CharData[] currentData = this.charData;
         float alpha = (color >> 24 & 0xFF) / 255.0F;
         boolean bold = false;
         boolean italic = false;
         boolean strikethrough = false;
         boolean underline = false;
         x *= 2.0;
         y = (y - 3.0) * 2.0;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
         Matrix4f matrix = new Matrix4f();
         matrix.scale(0.5F, 0.5F, 0.5F);
         int size = text.length();
         int currentTexture = this.glTextureId;
         RenderSystem.setShaderTexture(0, currentTexture);
         RenderSystem.setShader(class_10142.field_53879);

         for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (String.valueOf(character).equals("§") && i < size - 1) {
               int colorIndex = 21;

               try {
                  colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
               } catch (Exception var21) {
                  var21.printStackTrace();
               }

               if (colorIndex >= 16) {
                  if (colorIndex == 17) {
                     bold = true;
                     if (italic) {
                        currentTexture = this.texItalicBold;
                        currentData = this.boldItalicChars;
                     } else {
                        currentTexture = this.texBold;
                        currentData = this.boldChars;
                     }
                  } else if (colorIndex == 18) {
                     strikethrough = true;
                  } else if (colorIndex == 19) {
                     underline = true;
                  } else if (colorIndex == 20) {
                     italic = true;
                     if (bold) {
                        currentTexture = this.texItalicBold;
                        currentData = this.boldItalicChars;
                     } else {
                        currentTexture = this.texItalic;
                        currentData = this.italicChars;
                     }
                  } else if (colorIndex == 21) {
                     bold = false;
                     italic = false;
                     underline = false;
                     strikethrough = false;
                     RenderSystem.setShaderColor((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
                     currentTexture = this.glTextureId;
                     currentData = this.charData;
                  }
               } else {
                  bold = false;
                  italic = false;
                  underline = false;
                  strikethrough = false;
                  currentTexture = this.glTextureId;
                  currentData = this.charData;
                  if (colorIndex < 0 || colorIndex > 15) {
                     colorIndex = 15;
                  }

                  if (shadow) {
                     colorIndex += 16;
                  }

                  int colorcode = this.colorCode[colorIndex];
                  RenderSystem.setShaderColor((colorcode >> 16 & 0xFF) / 255.0F, (colorcode >> 8 & 0xFF) / 255.0F, (colorcode & 0xFF) / 255.0F, alpha);
               }

               i++;
            } else if (character < currentData.length && currentData[character] != null) {
               RenderSystem.setShaderTexture(0, currentTexture);
               class_289 tessellator = class_289.method_1348();
               class_287 buffer = tessellator.method_60827(class_5596.field_27379, class_290.field_1585);
               this.drawChar(currentData, character, (float)x, (float)y, matrix, buffer);
               class_286.method_43433(buffer.method_60800());
               if (strikethrough) {
                  this.drawLine(
                     x,
                     y + currentData[character].height / 2.0F,
                     x + currentData[character].width - 8.0,
                     y + currentData[character].height / 2.0F,
                     1.0F,
                     matrix
                  );
               }

               if (underline) {
                  this.drawLine(
                     x, y + currentData[character].height - 2.0, x + currentData[character].width - 8.0, y + currentData[character].height - 2.0, 1.0F, matrix
                  );
               }

               x += currentData[character].width - 8 + this.charOffset;
            }
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         return (float)x / 2.0F;
      }
   }

   @Override
   public int getStringWidth(String text) {
      int width = 0;
      CFont.CharData[] currentData = this.charData;
      boolean bold = false;
      boolean italic = false;
      int size = text.length();

      for (int i = 0; i < size; i++) {
         char character = text.charAt(i);
         if (String.valueOf(character).equals("§") && i < size - 1) {
            int colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
            if (colorIndex < 16) {
               bold = false;
               italic = false;
            } else if (colorIndex == 17) {
               bold = true;
               currentData = italic ? this.boldItalicChars : this.boldChars;
            } else if (colorIndex == 20) {
               italic = true;
               currentData = bold ? this.boldItalicChars : this.italicChars;
            } else if (colorIndex == 21) {
               bold = false;
               italic = false;
               currentData = this.charData;
            }

            i++;
         } else if (character < currentData.length && currentData[character] != null) {
            width += currentData[character].width - 8 + this.charOffset;
         }
      }

      return width / 2;
   }

   @Override
   public void setFont(Font font) {
      super.setFont(font);
      this.setupBoldItalicIDs();
   }

   @Override
   public void setAntiAlias(boolean antiAlias) {
      super.setAntiAlias(antiAlias);
      this.setupBoldItalicIDs();
   }

   @Override
   public void setFractionalMetrics(boolean fractionalMetrics) {
      super.setFractionalMetrics(fractionalMetrics);
      this.setupBoldItalicIDs();
   }

   private void setupBoldItalicIDs() {
      CFont boldFont = new CFont(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics);
      this.texBold = boldFont.getGlTextureId();
      this.boldChars = boldFont.charData;
      CFont italicFont = new CFont(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics);
      this.texItalic = italicFont.getGlTextureId();
      this.italicChars = italicFont.charData;
      CFont boldItalicFont = new CFont(this.font.deriveFont(3), this.antiAlias, this.fractionalMetrics);
      this.texItalicBold = boldItalicFont.getGlTextureId();
      this.boldItalicChars = boldItalicFont.charData;
   }

   private void drawLine(double x, double y, double x1, double y1, float width, Matrix4f matrix) {
      RenderSystem.setShader(class_10142.field_53875);
      RenderSystem.lineWidth(width);
      class_289 tessellator = class_289.method_1348();
      class_287 buffer = tessellator.method_60827(class_5596.field_27377, class_290.field_1592);
      buffer.method_22918(matrix, (float)x, (float)y, 0.0F);
      buffer.method_22918(matrix, (float)x1, (float)y1, 0.0F);
      class_286.method_43433(buffer.method_60800());
   }

   public void drawStringWithOutline(String text, double x, double y, int color) {
      this.drawString(text, x - 0.5, y, Color.BLACK.getRGB(), false);
      this.drawString(text, x + 0.5, y, Color.BLACK.getRGB(), false);
      this.drawString(text, x, y - 0.5, Color.BLACK.getRGB(), false);
      this.drawString(text, x, y + 0.5, Color.BLACK.getRGB(), false);
      this.drawString(text, x, y, color, false);
   }

   public void drawCenteredStringWithOutline(String text, float x, float y, int color) {
      this.drawCenteredString(text, x - 0.5F, y, Color.BLACK.getRGB());
      this.drawCenteredString(text, x + 0.5F, y, Color.BLACK.getRGB());
      this.drawCenteredString(text, x, y - 0.5F, Color.BLACK.getRGB());
      this.drawCenteredString(text, x, y + 0.5F, Color.BLACK.getRGB());
      this.drawCenteredString(text, x, y, color);
   }
}
