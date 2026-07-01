package fun.slikdlc.api.utils.render.fonts.msdf;

import net.minecraft.class_4588;
import org.joml.Matrix4f;

public final class MsdfGlyph {
   private final int code;
   private final float minU;
   private final float maxU;
   private final float minV;
   private final float maxV;
   private final float advance;
   private final float topPosition;
   private final float width;
   private final float height;

   public MsdfGlyph(
      int unicode,
      float advance,
      float planeLeft,
      float planeTop,
      float planeRight,
      float planeBottom,
      float atlasLeft,
      float atlasTop,
      float atlasRight,
      float atlasBottom,
      float atlasWidth,
      float atlasHeight
   ) {
      this.code = unicode;
      this.advance = advance;
      if (atlasLeft == 0.0F && atlasRight == 0.0F && atlasTop == 0.0F && atlasBottom == 0.0F) {
         this.minU = 0.0F;
         this.maxU = 0.0F;
         this.minV = 0.0F;
         this.maxV = 0.0F;
      } else {
         this.minU = atlasLeft / atlasWidth;
         this.maxU = atlasRight / atlasWidth;
         this.minV = 1.0F - atlasTop / atlasHeight;
         this.maxV = 1.0F - atlasBottom / atlasHeight;
      }

      if (planeLeft == 0.0F && planeRight == 0.0F && planeTop == 0.0F && planeBottom == 0.0F) {
         this.width = 0.0F;
         this.height = 0.0F;
         this.topPosition = 0.0F;
      } else {
         this.width = planeRight - planeLeft;
         this.height = planeTop - planeBottom;
         this.topPosition = planeTop;
      }
   }

   public float apply(Matrix4f matrix, class_4588 consumer, float size, float x, float y, float z, int red, int green, int blue, int alpha) {
      y -= this.topPosition * size;
      y--;
      float w = this.width * size;
      float h = this.height * size;
      consumer.method_22918(matrix, x, y, z).method_1336(red, green, blue, alpha).method_22913(this.minU, this.minV);
      consumer.method_22918(matrix, x, y + h, z).method_1336(red, green, blue, alpha).method_22913(this.minU, this.maxV);
      consumer.method_22918(matrix, x + w, y + h, z).method_1336(red, green, blue, alpha).method_22913(this.maxU, this.maxV);
      consumer.method_22918(matrix, x + w, y, z).method_1336(red, green, blue, alpha).method_22913(this.maxU, this.minV);
      return this.width * (size - 1.0F) + (Character.isSpaceChar(this.code) ? this.advance * size : 0.0F);
   }

   public float getWidth(float size) {
      return this.width * (size - 1.0F) + (Character.isSpaceChar(this.code) ? this.advance * size : 0.0F);
   }

   public int getCharCode() {
      return this.code;
   }
}
