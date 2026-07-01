package fun.slikdlc.api.utils.render.fonts.ttf;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import lombok.Generated;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_287;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;

public class CFont {
   protected static final int IMG_SIZE = 512;
   protected CFont.CharData[] charData = new CFont.CharData[1104];
   protected Font font;
   protected boolean antiAlias;
   protected boolean fractionalMetrics;
   protected int fontHeight = -1;
   protected int charOffset = 0;
   protected class_2960 textureId;
   protected int glTextureId;
   private static int textureCounter = 0;

   public CFont(Font font, boolean antiAlias, boolean fractionalMetrics) {
      this.font = font;
      this.antiAlias = antiAlias;
      this.fractionalMetrics = fractionalMetrics;
      this.setupTexture(font, antiAlias, fractionalMetrics, this.charData);
   }

   protected void setupTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CFont.CharData[] chars) {
      BufferedImage img = this.generateFontImage(font, antiAlias, fractionalMetrics, chars);

      try {
         class_1011 nativeImage = new class_1011(img.getWidth(), img.getHeight(), false);

         for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
               int argb = img.getRGB(x, y);
               int a = argb >> 24 & 0xFF;
               int r = argb >> 16 & 0xFF;
               int g = argb >> 8 & 0xFF;
               int b = argb & 0xFF;
               nativeImage.method_61941(x, y, a << 24 | r << 16 | g << 8 | b);
            }
         }

         class_1043 texture = new class_1043(nativeImage);
         this.glTextureId = texture.method_4624();
         String name = "cfont_" + textureCounter++;
         this.textureId = class_2960.method_60655("customfont", name);
         class_310.method_1551().method_1531().method_4616(this.textureId, texture);
      } catch (Exception var14) {
         var14.printStackTrace();
      }
   }

   protected BufferedImage generateFontImage(Font font, boolean antiAlias, boolean fractionalMetrics, CFont.CharData[] chars) {
      BufferedImage bufferedImage = new BufferedImage(512, 512, 2);
      Graphics2D g = (Graphics2D)bufferedImage.getGraphics();
      g.setFont(font);
      g.setColor(new Color(255, 255, 255, 0));
      g.fillRect(0, 0, 512, 512);
      g.setColor(Color.WHITE);
      g.setRenderingHint(
         RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF
      );
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
      FontMetrics fontMetrics = g.getFontMetrics();
      int charHeight = 0;
      int positionX = 0;
      int positionY = 1;

      for (int i = 0; i < chars.length; i++) {
         char ch = (char)i;
         if (ch > 1039 && ch < 1104 || ch < 256) {
            CFont.CharData charData = new CFont.CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), g);
            charData.width = dimensions.getBounds().width + 8;
            charData.height = dimensions.getBounds().height;
            if (positionX + charData.width >= 512) {
               positionX = 0;
               positionY += charHeight;
               charHeight = 0;
            }

            if (charData.height > charHeight) {
               charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;
            if (charData.height > this.fontHeight) {
               this.fontHeight = charData.height;
            }

            chars[i] = charData;
            g.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width;
         }
      }

      return bufferedImage;
   }

   public void drawChar(CFont.CharData[] chars, char c, float x, float y, Matrix4f matrix, class_287 buffer) {
      try {
         if (chars[c] == null) {
            return;
         }

         this.drawQuad(x, y, chars[c].width, chars[c].height, chars[c].storedX, chars[c].storedY, chars[c].width, chars[c].height, matrix, buffer);
      } catch (Exception var8) {
      }
   }

   protected void drawQuad(
      float x, float y, float width, float height, float srcX, float srcY, float srcWidth, float srcHeight, Matrix4f matrix, class_287 buffer
   ) {
      float renderSRCX = srcX / 512.0F;
      float renderSRCY = srcY / 512.0F;
      float renderSRCWidth = srcWidth / 512.0F;
      float renderSRCHeight = srcHeight / 512.0F;
      buffer.method_22918(matrix, x + width, y, 0.0F).method_22913(renderSRCX + renderSRCWidth, renderSRCY);
      buffer.method_22918(matrix, x, y, 0.0F).method_22913(renderSRCX, renderSRCY);
      buffer.method_22918(matrix, x, y + height, 0.0F).method_22913(renderSRCX, renderSRCY + renderSRCHeight);
      buffer.method_22918(matrix, x, y + height, 0.0F).method_22913(renderSRCX, renderSRCY + renderSRCHeight);
      buffer.method_22918(matrix, x + width, y + height, 0.0F).method_22913(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
      buffer.method_22918(matrix, x + width, y, 0.0F).method_22913(renderSRCX + renderSRCWidth, renderSRCY);
   }

   public int getStringHeight(String text) {
      return this.getFontHeight();
   }

   public int getFontHeight() {
      return (this.fontHeight - 8) / 2;
   }

   public int getStringWidth(String text) {
      int width = 0;

      for (char c : text.toCharArray()) {
         if (c < this.charData.length && this.charData[c] != null) {
            width += this.charData[c].width - 8 + this.charOffset;
         }
      }

      return width / 2;
   }

   public void setAntiAlias(boolean antiAlias) {
      if (this.antiAlias != antiAlias) {
         this.antiAlias = antiAlias;
         this.setupTexture(this.font, antiAlias, this.fractionalMetrics, this.charData);
      }
   }

   public void setFractionalMetrics(boolean fractionalMetrics) {
      if (this.fractionalMetrics != fractionalMetrics) {
         this.fractionalMetrics = fractionalMetrics;
         this.setupTexture(this.font, this.antiAlias, fractionalMetrics, this.charData);
      }
   }

   public void setFont(Font font) {
      this.font = font;
      this.setupTexture(font, this.antiAlias, this.fractionalMetrics, this.charData);
   }

   @Generated
   public Font getFont() {
      return this.font;
   }

   @Generated
   public boolean isAntiAlias() {
      return this.antiAlias;
   }

   @Generated
   public boolean isFractionalMetrics() {
      return this.fractionalMetrics;
   }

   @Generated
   public class_2960 getTextureId() {
      return this.textureId;
   }

   @Generated
   public int getGlTextureId() {
      return this.glTextureId;
   }

   protected static class CharData {
      public int width;
      public int height;
      public int storedX;
      public int storedY;

      protected CharData() {
      }
   }
}
