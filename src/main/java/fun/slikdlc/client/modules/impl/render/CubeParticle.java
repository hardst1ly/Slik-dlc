package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.color.ColorUtils;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Matrix4f;

class CubeParticle implements QClient {
   double x;
   double y;
   double z;
   double worldX;
   double worldY;
   double worldZ;
   long time;
   class_1309 entity;
   boolean fading;
   long fadeStartTime;
   float vx;
   float vy;
   float vz;
   float rotX;
   float rotY;
   float rotZ;
   float rotSpeedX;
   float rotSpeedY;
   float rotSpeedZ;

   public CubeParticle(class_1309 entity, double x, double y, double z) {
      this.entity = entity;
      this.x = x;
      this.y = y;
      this.z = z;
      this.time = System.currentTimeMillis();
      this.rotX = (float)(Math.random() * 360.0);
      this.rotY = (float)(Math.random() * 360.0);
      this.rotZ = (float)(Math.random() * 360.0);
      this.rotSpeedX = 1.4F + (float)Math.random() * 3.4F;
      this.rotSpeedY = 1.4F + (float)Math.random() * 3.4F;
      this.rotSpeedZ = 1.4F + (float)Math.random() * 3.4F;
      this.vx = (float)((Math.random() - 0.5) * 0.0022);
      this.vy = 0.031F + (float)Math.random() * 0.02F;
      this.vz = (float)((Math.random() - 0.5) * 0.0022);
   }

   public void update(float dt, long now, class_1309 currentTarget) {
      float step = dt * 60.0F;
      this.rotX = this.rotX + this.rotSpeedX * step;
      this.rotY = this.rotY + this.rotSpeedY * step;
      this.rotZ = this.rotZ + this.rotSpeedZ * step;
      if (!this.fading) {
         this.x = this.x + this.vx * step;
         this.y = this.y + this.vy * step;
         this.z = this.z + this.vz * step;
         this.vx *= 0.992F;
         this.vz *= 0.992F;
         this.vy *= 0.989F;
         if (this.entity != null) {
            double shoulderHeight = Math.max(2.2, this.entity.method_17682() * 1.85);
            if (this.y >= shoulderHeight) {
               this.y = shoulderHeight;
               this.beginFade(now);
               return;
            }
         }

         boolean targetLost = currentTarget == null || this.entity == null || !this.entity.method_5805() || this.entity != currentTarget;
         if (targetLost || now - this.time >= 560L) {
            this.beginFade(now);
         }
      }
   }

   public boolean shouldRemove(long now) {
      return this.fading && now - this.fadeStartTime >= 320L;
   }

   public int getRenderColor(int baseColor, int redColor, float hurtPC, long now) {
      float alpha = this.getAlpha(now);
      if (alpha <= 0.001F) {
         return 0;
      } else {
         int color = ColorUtils.replAlpha(baseColor, (int)(alpha * 255.0F));
         int hurt = ColorUtils.replAlpha(redColor, (int)(alpha * 255.0F));
         return TargetESP.INSTANCE.overCol(color, hurt, hurtPC);
      }
   }

   public boolean appendCubeFaces(class_287 faceBuilder, class_4587 ms, class_243 cam, float partialTicks, int color) {
      float alpha = (color >> 24 & 0xFF) / 255.0F;
      if (alpha <= 0.001F) {
         return false;
      } else {
         class_243 renderPos = this.getRenderPos(partialTicks);
         if (renderPos == null) {
            return false;
         } else {
            float fadeScale = this.fading
               ? class_3532.method_16439(class_3532.method_15363((float)(System.currentTimeMillis() - this.fadeStartTime) / 320.0F, 0.0F, 1.0F), 1.0F, 0.45F)
               : 1.0F;
            float scale = 0.12F * fadeScale;
            ms.method_22903();
            ms.method_22904(renderPos.field_1352 - cam.field_1352, renderPos.field_1351 - cam.field_1351, renderPos.field_1350 - cam.field_1350);
            ms.method_22907(class_7833.field_40714.rotationDegrees(this.rotX));
            ms.method_22907(class_7833.field_40716.rotationDegrees(this.rotY));
            ms.method_22907(class_7833.field_40718.rotationDegrees(this.rotZ));
            ms.method_22905(scale, scale, scale);
            Matrix4f m = ms.method_23760().method_23761();
            this.appendFaces(faceBuilder, m, color);
            ms.method_22909();
            return true;
         }
      }
   }

   public boolean appendCubeLines(class_287 lineBuilder, class_4587 ms, class_243 cam, float partialTicks, int color) {
      float alpha = (color >> 24 & 0xFF) / 255.0F;
      if (alpha <= 0.001F) {
         return false;
      } else {
         class_243 renderPos = this.getRenderPos(partialTicks);
         if (renderPos == null) {
            return false;
         } else {
            float fadeScale = this.fading
               ? class_3532.method_16439(class_3532.method_15363((float)(System.currentTimeMillis() - this.fadeStartTime) / 320.0F, 0.0F, 1.0F), 1.0F, 0.45F)
               : 1.0F;
            float scale = 0.12F * fadeScale;
            ms.method_22903();
            ms.method_22904(renderPos.field_1352 - cam.field_1352, renderPos.field_1351 - cam.field_1351, renderPos.field_1350 - cam.field_1350);
            ms.method_22907(class_7833.field_40714.rotationDegrees(this.rotX));
            ms.method_22907(class_7833.field_40716.rotationDegrees(this.rotY));
            ms.method_22907(class_7833.field_40718.rotationDegrees(this.rotZ));
            ms.method_22905(scale, scale, scale);
            Matrix4f m = ms.method_23760().method_23761();
            this.appendEdges(lineBuilder, m, ColorUtils.replAlpha(color, Math.max(1, (int)((color >> 24 & 0xFF) * 0.7F))));
            ms.method_22909();
            return true;
         }
      }
   }

   public boolean appendBloom(class_287 builder, class_4587 ms, class_243 camPos, float camYaw, float camPitch, float partialTicks, int colorInt, long now) {
      float alpha = this.getAlpha(now);
      if (alpha <= 0.001F) {
         return false;
      } else {
         class_243 renderPos = this.getRenderPos(partialTicks);
         if (renderPos == null) {
            return false;
         } else {
            float fadeScale = this.fading
               ? class_3532.method_16439(class_3532.method_15363((float)(now - this.fadeStartTime) / 320.0F, 0.0F, 1.0F), 1.0F, 0.55F)
               : 1.0F;
            float glowScale = 0.95F * fadeScale;
            int ai = (int)(alpha * 0.15F * 255.0F);
            if (ai <= 0) {
               return false;
            } else {
               int r = colorInt >> 16 & 0xFF;
               int g = colorInt >> 8 & 0xFF;
               int b = colorInt & 0xFF;
               ms.method_22903();
               ms.method_22904(renderPos.field_1352 - camPos.field_1352, renderPos.field_1351 - camPos.field_1351, renderPos.field_1350 - camPos.field_1350);
               ms.method_22907(class_7833.field_40716.rotationDegrees(-camYaw));
               ms.method_22907(class_7833.field_40714.rotationDegrees(camPitch));
               ms.method_22905(glowScale, glowScale, glowScale);
               Matrix4f m = ms.method_23760().method_23761();
               builder.method_22918(m, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, ai);
               builder.method_22918(m, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, ai);
               builder.method_22918(m, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, ai);
               builder.method_22918(m, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, ai);
               ms.method_22909();
               return true;
            }
         }
      }
   }

   private void beginFade(long now) {
      if (!this.fading) {
         class_243 renderPos = this.getRenderPos(1.0F);
         if (renderPos != null) {
            this.worldX = renderPos.field_1352;
            this.worldY = renderPos.field_1351;
            this.worldZ = renderPos.field_1350;
         }

         this.fadeStartTime = now;
         this.fading = true;
         this.entity = null;
      }
   }

   private float getAlpha(long now) {
      if (!this.fading) {
         float fadeIn = class_3532.method_15363((float)(now - this.time) / 140.0F, 0.0F, 1.0F);
         float preFade = 1.0F - class_3532.method_15363((float)(now - this.time - 440L) / 120.0F, 0.0F, 0.35F);
         return fadeIn * preFade;
      } else {
         return 1.0F - class_3532.method_15363((float)(now - this.fadeStartTime) / 320.0F, 0.0F, 1.0F);
      }
   }

   private class_243 getRenderPos(float partialTicks) {
      return !this.fading && this.entity != null
         ? new class_243(
            class_3532.method_16436(partialTicks, this.entity.field_6038, this.entity.method_23317()) + this.x,
            class_3532.method_16436(partialTicks, this.entity.field_5971, this.entity.method_23318()) + this.y,
            class_3532.method_16436(partialTicks, this.entity.field_5989, this.entity.method_23321()) + this.z
         )
         : new class_243(this.worldX, this.worldY, this.worldZ);
   }

   private void appendFaces(class_287 fb, Matrix4f m, int color) {
      float min = -0.5F;
      float max = 0.5F;
      int fillColor = ColorUtils.replAlpha(color, Math.max(1, (int)((color >> 24 & 0xFF) * 0.16F)));
      this.addFace(fb, m, min, min, min, max, max, max, fillColor);
   }

   private void appendEdges(class_287 buf, Matrix4f m, int color) {
      for (byte[] edge : TargetESP.CUBE_EDGES) {
         buf.method_22918(m, edge[0] * 0.5F, edge[1] * 0.5F, edge[2] * 0.5F).method_39415(color);
         buf.method_22918(m, edge[3] * 0.5F, edge[4] * 0.5F, edge[5] * 0.5F).method_39415(color);
      }
   }

   private void addFace(class_287 buf, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
      buf.method_22918(m, x1, y1, z1).method_39415(color);
      buf.method_22918(m, x2, y1, z1).method_39415(color);
      buf.method_22918(m, x2, y1, z2).method_39415(color);
      buf.method_22918(m, x1, y1, z2).method_39415(color);
      buf.method_22918(m, x1, y2, z1).method_39415(color);
      buf.method_22918(m, x1, y2, z2).method_39415(color);
      buf.method_22918(m, x2, y2, z2).method_39415(color);
      buf.method_22918(m, x2, y2, z1).method_39415(color);
      buf.method_22918(m, x1, y1, z1).method_39415(color);
      buf.method_22918(m, x1, y2, z1).method_39415(color);
      buf.method_22918(m, x2, y2, z1).method_39415(color);
      buf.method_22918(m, x2, y1, z1).method_39415(color);
      buf.method_22918(m, x1, y1, z2).method_39415(color);
      buf.method_22918(m, x2, y1, z2).method_39415(color);
      buf.method_22918(m, x2, y2, z2).method_39415(color);
      buf.method_22918(m, x1, y2, z2).method_39415(color);
      buf.method_22918(m, x1, y1, z1).method_39415(color);
      buf.method_22918(m, x1, y1, z2).method_39415(color);
      buf.method_22918(m, x1, y2, z2).method_39415(color);
      buf.method_22918(m, x1, y2, z1).method_39415(color);
      buf.method_22918(m, x2, y1, z1).method_39415(color);
      buf.method_22918(m, x2, y2, z1).method_39415(color);
      buf.method_22918(m, x2, y2, z2).method_39415(color);
      buf.method_22918(m, x2, y1, z2).method_39415(color);
   }
}
