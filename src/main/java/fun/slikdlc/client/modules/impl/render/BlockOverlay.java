package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.Theme;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.ShaderUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_10142;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_276;
import net.minecraft.class_284;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3965;
import net.minecraft.class_5944;
import net.minecraft.class_6367;
import net.minecraft.class_239.class_240;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class BlockOverlay extends Module {
   public static BlockOverlay INSTANCE = new BlockOverlay();
   private final ModeSetting mode = new ModeSetting("Режим", "Шейдер", "Шейдер", "Нитки");
   private final FloatSetting waveSpeed = new FloatSetting("Скорость волн", 1.2F, 0.1F, 5.0F, 0.1F).visible(() -> this.mode.is("Шейдер"));
   private final FloatSetting waveScale = new FloatSetting("Частота волн", 1.0F, 1.0F, 3.0F, 0.1F).visible(() -> this.mode.is("Шейдер"));
   private final FloatSetting lineSpeed = new FloatSetting("Скорость нитей", 1.4F, 0.1F, 5.0F, 0.1F).visible(() -> this.mode.is("Нитки"));
   private final FloatSetting lineJitter = new FloatSetting("Изгиб нитей", 0.55F, 0.0F, 1.5F, 0.01F).visible(() -> this.mode.is("Нитки"));
   private final FloatSetting outline = new FloatSetting("Ширина обводки", 1.1F, 0.1F, 5.0F, 0.1F);
   private final FloatSetting glow = new FloatSetting("Сила свечения", 1.0F, 0.0F, 5.0F, 0.1F);
   private final FloatSetting fill = new FloatSetting("Заливка", 0.6F, 0.0F, 1.0F, 0.01F);
   private final FloatSetting alpha = new FloatSetting("Прозрачность", 1.0F, 0.0F, 1.0F, 0.01F);
   private final FloatSetting smooth = new FloatSetting("Плавность", 0.24F, 0.05F, 0.6F, 0.01F);
   private class_276 maskBuffer;
   private int fbWidth = -1;
   private int fbHeight = -1;
   private boolean hasMask;
   private class_2338 lastBlockPos;
   private class_238 displayBox;
   private class_238 targetBox;
   private int cachedThemeColor1 = -1;
   private int cachedThemeColor2 = -1;

   public BlockOverlay() {
      super("BlockOverlay", "Block overlay shader", Module.ModuleCategory.RENDER);
      this.addSettings(
         new Setting[]{this.mode, this.waveSpeed, this.waveScale, this.lineSpeed, this.lineJitter, this.outline, this.glow, this.fill, this.alpha, this.smooth}
      );
   }

   @Override
   public void onDisable() {
      this.hasMask = false;
      this.lastBlockPos = null;
      this.displayBox = null;
      this.targetBox = null;
      super.onDisable();
   }

   @EventLink(
      priority = -100
   )
   public void onRender3D(Event3DRender event) {
      if (mc != null && mc.field_1687 != null && mc.field_1724 != null) {
         class_238 worldBox = this.getTargetedBlockBox();
         if (worldBox == null) {
            this.hasMask = false;
            this.lastBlockPos = null;
            this.displayBox = null;
            this.targetBox = null;
         } else {
            if (this.displayBox != null && this.targetBox != null && this.lastBlockPos != null) {
               this.targetBox = worldBox;
               this.displayBox = this.lerpBox(this.displayBox, this.targetBox, this.smooth.get());
            } else {
               this.displayBox = worldBox;
               this.targetBox = worldBox;
            }

            this.lastBlockPos = class_2338.method_49637(worldBox.field_1323, worldBox.field_1322, worldBox.field_1321);
            this.updateCachedThemeColors();
            class_243 cam = event.getCamera().method_19326();
            class_238 localBox = this.displayBox.method_989(-cam.field_1352, -cam.field_1351, -cam.field_1350);
            Matrix4f matrix = event.getMatrices().method_23760().method_23761();
            this.ensureMaskBuffer();
            if (this.maskBuffer != null) {
               this.hasMask = true;
               this.maskBuffer.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
               this.maskBuffer.method_1230();
               this.copyMainDepthToMask();
               this.maskBuffer.method_1235(false);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.setShader(class_10142.field_53876);
               this.drawMaskBox(matrix, localBox);
               RenderSystem.depthMask(true);
               RenderSystem.disableDepthTest();
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
               mc.method_1522().method_1235(false);
               if (this.mode.is("Нитки")) {
                  this.drawAnimatedWeb(matrix, localBox);
               }
            }
         }
      }
   }

   @EventLink(
      priority = 200
   )
   public void onRender2D(EventRender.Default event) {
      if (this.hasMask && this.maskBuffer != null) {
         if (!this.mode.is("Нитки")) {
            class_5944 shader = mc.method_62887().method_62947(ShaderUtils.blockOverlay);
            if (shader != null) {
               boolean lineMode = this.mode.is("Нитки");
               int color1 = this.cachedThemeColor1;
               int color2 = this.cachedThemeColor2;
               mc.method_1522().method_1235(false);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.enableDepthTest();
               RenderSystem.setShader(ShaderUtils.blockOverlay);
               RenderSystem.setShaderTexture(0, this.maskBuffer.method_30277());
               this.setUniform(shader, "texelSize", 1.0F / Math.max(1, mc.method_22683().method_4489()), 1.0F / Math.max(1, mc.method_22683().method_4506()));
               this.setUniform(shader, "color", ColorUtils.redf(color1), ColorUtils.greenf(color1), ColorUtils.bluef(color1));
               this.setUniform(shader, "color2", ColorUtils.redf(color2), ColorUtils.greenf(color2), ColorUtils.bluef(color2));
               this.setUniform(shader, "time", (float)(System.currentTimeMillis() % 100000L) / 1000.0F);
               this.setUniform(shader, "speed", this.waveSpeed.get());
               this.setUniform(shader, "scale", this.waveScale.get());
               this.setUniform(shader, "outline", this.outline.get());
               this.setUniform(shader, "glow", lineMode ? 0.0F : this.glow.get());
               this.setUniform(shader, "fill", lineMode ? 0.0F : this.fill.get());
               this.setUniform(shader, "alpha", lineMode ? 1.0F : this.alpha.get());
               this.setUniform(shader, "outlineOnly", lineMode ? 1.0F : 0.0F);
               this.drawFullscreenQuad();
               RenderSystem.enableDepthTest();
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.setShaderTexture(0, 0);
            }
         }
      }
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

   private void ensureMaskBuffer() {
      int w = mc.method_22683().method_4489();
      int h = mc.method_22683().method_4506();
      if (this.maskBuffer == null || this.fbWidth != w || this.fbHeight != h) {
         if (this.maskBuffer != null) {
            this.maskBuffer.method_1238();
         }

         this.maskBuffer = new class_6367(w, h, true);
         this.fbWidth = w;
         this.fbHeight = h;
      }
   }

   private class_238 getTargetedBlockBox() {
      class_239 hit = mc.field_1765;
      if (hit instanceof class_3965 blockHit && hit.method_17783() == class_240.field_1332) {
         class_2338 pos = blockHit.method_17777();
         if (pos == null) {
            return null;
         } else if (mc.field_1687.method_8320(pos).method_26215()) {
            return null;
         } else {
            class_265 shape = mc.field_1687.method_8320(pos).method_26218(mc.field_1687, pos);
            class_238 box = shape.method_1110() ? new class_238(pos) : shape.method_1107().method_996(pos);
            return box.method_1014(0.002);
         }
      } else {
         return null;
      }
   }

   private class_238 lerpBox(class_238 a, class_238 b, float t) {
      return new class_238(
         a.field_1323 + (b.field_1323 - a.field_1323) * t,
         a.field_1322 + (b.field_1322 - a.field_1322) * t,
         a.field_1321 + (b.field_1321 - a.field_1321) * t,
         a.field_1320 + (b.field_1320 - a.field_1320) * t,
         a.field_1325 + (b.field_1325 - a.field_1325) * t,
         a.field_1324 + (b.field_1324 - a.field_1324) * t
      );
   }

   private void drawAnimatedWeb(Matrix4f matrix, class_238 box) {
      int strandsPerFace = 5;
      int samples = 18;
      float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0F * this.lineSpeed.get();
      float lineWidth = 0.0025F;
      float bendBase = 0.06F + this.lineJitter.get() * 0.2F;
      int baseAlpha = Math.max(20, Math.min(255, (int)(this.alpha.get() * 210.0F)));
      int themeColor = this.cachedThemeColor1;
      long seed = this.lastBlockPos != null ? this.lastBlockPos.method_10063() : 1L;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(class_10142.field_53876);
      this.drawFilledBox(matrix, box, ColorUtils.setAlphaColor(themeColor, (int)(this.alpha.get() * this.fill.get() * 170.0F)));

      for (int face = 0; face < 6; face++) {
         int[] neighbors = this.faceNeighbors(face);

         for (int strand = 0; strand < strandsPerFace; strand++) {
            int key = face * 1000 + strand * 53;
            int adj = neighbors[strand % neighbors.length];
            double phase = t * (0.95 + this.rand01(seed, key + 1) * 0.55) + strand * 0.83 + face * 1.11;
            double edgeT = this.clamp01(0.5 + Math.sin(phase * 1.37 + this.rand01(seed, key + 2) * 6.2831853) * 0.38);
            class_243 pivot = this.edgePoint(box, face, adj, edgeT, 0.0015);
            class_243 start = this.facePoint(
               box, face, this.clamp01(0.5 + (this.rand01(seed, key + 3) - 0.5) * 0.46), this.clamp01(0.5 + (this.rand01(seed, key + 4) - 0.5) * 0.46), 0.0015
            );
            class_243 end = this.facePoint(
               box, adj, this.clamp01(0.5 + (this.rand01(seed, key + 5) - 0.5) * 0.46), this.clamp01(0.5 + (this.rand01(seed, key + 6) - 0.5) * 0.46), 0.0015
            );
            class_243[] basisA = this.faceBasis(face);
            class_243[] basisB = this.faceBasis(adj);
            class_243 normalA = this.faceNormal(face);
            class_243 normalB = this.faceNormal(adj);
            double bendA = bendBase * (0.7 + this.rand01(seed, key + 7)) * Math.sin(phase * 1.9 + this.rand01(seed, key + 8) * 6.2831853);
            double bendB = bendBase * (0.7 + this.rand01(seed, key + 9)) * Math.cos(phase * 1.7 + this.rand01(seed, key + 10) * 6.2831853);
            class_243 dirA = pivot.method_1020(start);
            class_243 c1a = start.method_1019(dirA.method_1021(0.38))
               .method_1019(basisA[0].method_1021(bendA))
               .method_1019(basisA[1].method_1021(-bendA * 0.55));
            class_243 c2a = start.method_1019(dirA.method_1021(0.76))
               .method_1019(basisA[0].method_1021(-bendA * 0.65))
               .method_1019(basisA[1].method_1021(bendA * 0.4));
            class_243 dirB = end.method_1020(pivot);
            class_243 c1b = pivot.method_1019(dirB.method_1021(0.24))
               .method_1019(basisB[0].method_1021(bendB))
               .method_1019(basisB[1].method_1021(bendB * 0.45));
            class_243 c2b = pivot.method_1019(dirB.method_1021(0.62))
               .method_1019(basisB[0].method_1021(-bendB * 0.7))
               .method_1019(basisB[1].method_1021(-bendB * 0.35));
            int alphaLine = Math.max(18, Math.min(255, (int)(baseAlpha * (0.74 + 0.26 * Math.sin(phase * 2.6)))));
            int color = ColorUtils.setAlphaColor(themeColor, alphaLine);
            this.drawBezierRibbon(matrix, start, c1a, c2a, pivot, normalA, samples, color, lineWidth);
            this.drawBezierRibbon(matrix, pivot, c1b, c2b, end, normalB, samples, color, lineWidth);
         }
      }

      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   private void copyMainDepthToMask() {
      if (this.maskBuffer != null) {
         int readFbo = GL11.glGetInteger(36010);
         int drawFbo = GL11.glGetInteger(36006);
         int w = mc.method_22683().method_4489();
         int h = mc.method_22683().method_4506();
         GL30.glBindFramebuffer(36008, mc.method_1522().field_1476);
         GL30.glBindFramebuffer(36009, this.maskBuffer.field_1476);
         GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, 256, 9728);
         GL30.glBindFramebuffer(36008, readFbo);
         GL30.glBindFramebuffer(36009, drawFbo);
      }
   }

   private class_243 cubicBezier(class_243 p0, class_243 p1, class_243 p2, class_243 p3, float t) {
      double it = 1.0 - t;
      double it2 = it * it;
      double t2 = t * t;
      return p0.method_1021(it2 * it).method_1019(p1.method_1021(3.0 * it2 * t)).method_1019(p2.method_1021(3.0 * it * t2)).method_1019(p3.method_1021(t2 * t));
   }

   private void drawBezierRibbon(
      Matrix4f matrix, class_243 p0, class_243 p1, class_243 p2, class_243 p3, class_243 faceNormal, int samples, int color, float halfWidth
   ) {
      class_243[] points = new class_243[samples + 1];

      for (int s = 0; s <= samples; s++) {
         float u = (float)s / samples;
         points[s] = this.cubicBezier(p0, p1, p2, p3, u);
      }

      class_287 quads = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);

      for (int i = 0; i < samples; i++) {
         class_243 a = points[i];
         class_243 b = points[i + 1];
         class_243 dir = b.method_1020(a);
         if (!(dir.method_1027() < 1.0E-6)) {
            class_243 perp = faceNormal.method_1036(dir).method_1029().method_1021(halfWidth);
            class_243 aL = a.method_1019(perp);
            class_243 aR = a.method_1020(perp);
            class_243 bL = b.method_1019(perp);
            class_243 bR = b.method_1020(perp);
            quads.method_22918(matrix, (float)aL.field_1352, (float)aL.field_1351, (float)aL.field_1350).method_39415(color);
            quads.method_22918(matrix, (float)aR.field_1352, (float)aR.field_1351, (float)aR.field_1350).method_39415(color);
            quads.method_22918(matrix, (float)bR.field_1352, (float)bR.field_1351, (float)bR.field_1350).method_39415(color);
            quads.method_22918(matrix, (float)bL.field_1352, (float)bL.field_1351, (float)bL.field_1350).method_39415(color);
         }
      }

      class_286.method_43433(quads.method_60800());
   }

   private void updateCachedThemeColors() {
      if (SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.themeStorage != null && SlikDlc.INSTANCE.themeStorage.getThemes() != null) {
         Theme theme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme();
         if (theme == null) {
            this.cachedThemeColor1 = ColorUtils.getThemeColor(0);
            this.cachedThemeColor2 = ColorUtils.getThemeColor(180);
         } else {
            if (!"Rainbow".equals(theme.getName())) {
               int base = theme.color != null && theme.color.length > 0 ? theme.color[0] : ColorUtils.getThemeColor(0);
               this.cachedThemeColor1 = base;
               this.cachedThemeColor2 = base;
            } else {
               this.cachedThemeColor1 = ColorUtils.getThemeColor();
               this.cachedThemeColor2 = ColorUtils.getThemeColor(180);
            }
         }
      } else {
         this.cachedThemeColor1 = ColorUtils.getThemeColor(0);
         this.cachedThemeColor2 = ColorUtils.getThemeColor(180);
      }
   }

   private int[] faceNeighbors(int face) {
      return switch (face) {
         case 0, 1 -> new int[]{2, 3, 4, 5};
         case 2, 3 -> new int[]{0, 1, 4, 5};
         default -> new int[]{0, 1, 2, 3};
      };
   }

   private class_243[] faceBasis(int face) {
      return switch (face) {
         case 0, 1 -> new class_243[]{new class_243(1.0, 0.0, 0.0), new class_243(0.0, 0.0, 1.0)};
         case 2, 3 -> new class_243[]{new class_243(1.0, 0.0, 0.0), new class_243(0.0, 1.0, 0.0)};
         default -> new class_243[]{new class_243(0.0, 0.0, 1.0), new class_243(0.0, 1.0, 0.0)};
      };
   }

   private class_243 faceNormal(int face) {
      return switch (face) {
         case 0 -> new class_243(0.0, 1.0, 0.0);
         case 1 -> new class_243(0.0, -1.0, 0.0);
         case 2 -> new class_243(0.0, 0.0, -1.0);
         case 3 -> new class_243(0.0, 0.0, 1.0);
         case 4 -> new class_243(-1.0, 0.0, 0.0);
         default -> new class_243(1.0, 0.0, 0.0);
      };
   }

   private class_243 edgePoint(class_238 box, int faceA, int faceB, double t, double inset) {
      double x = Double.NaN;
      double y = Double.NaN;
      double z = Double.NaN;
      double[] fixedA = this.faceFixedCoords(box, faceA, inset);
      if (!Double.isNaN(fixedA[0])) {
         x = fixedA[0];
      }

      if (!Double.isNaN(fixedA[1])) {
         y = fixedA[1];
      }

      if (!Double.isNaN(fixedA[2])) {
         z = fixedA[2];
      }

      double[] fixedB = this.faceFixedCoords(box, faceB, inset);
      if (!Double.isNaN(fixedB[0])) {
         x = fixedB[0];
      }

      if (!Double.isNaN(fixedB[1])) {
         y = fixedB[1];
      }

      if (!Double.isNaN(fixedB[2])) {
         z = fixedB[2];
      }

      double tt = this.clamp01(t);
      if (Double.isNaN(x)) {
         x = this.lerp(box.field_1323, box.field_1320, tt);
      }

      if (Double.isNaN(y)) {
         y = this.lerp(box.field_1322, box.field_1325, tt);
      }

      if (Double.isNaN(z)) {
         z = this.lerp(box.field_1321, box.field_1324, tt);
      }

      return new class_243(x, y, z);
   }

   private double[] faceFixedCoords(class_238 box, int face, double inset) {
      return switch (face) {
         case 0 -> new double[]{Double.NaN, box.field_1325 - inset, Double.NaN};
         case 1 -> new double[]{Double.NaN, box.field_1322 + inset, Double.NaN};
         case 2 -> new double[]{Double.NaN, Double.NaN, box.field_1321 + inset};
         case 3 -> new double[]{Double.NaN, Double.NaN, box.field_1324 - inset};
         case 4 -> new double[]{box.field_1323 + inset, Double.NaN, Double.NaN};
         default -> new double[]{box.field_1320 - inset, Double.NaN, Double.NaN};
      };
   }

   private class_243 facePoint(class_238 box, int face, double u, double v, double inset) {
      u = this.clamp01(u);
      v = this.clamp01(v);

      return switch (face) {
         case 0 -> new class_243(this.lerp(box.field_1323, box.field_1320, u), box.field_1325 - inset, this.lerp(box.field_1321, box.field_1324, v));
         case 1 -> new class_243(this.lerp(box.field_1323, box.field_1320, u), box.field_1322 + inset, this.lerp(box.field_1321, box.field_1324, v));
         case 2 -> new class_243(this.lerp(box.field_1323, box.field_1320, u), this.lerp(box.field_1322, box.field_1325, v), box.field_1321 + inset);
         case 3 -> new class_243(this.lerp(box.field_1323, box.field_1320, u), this.lerp(box.field_1322, box.field_1325, v), box.field_1324 - inset);
         case 4 -> new class_243(box.field_1323 + inset, this.lerp(box.field_1322, box.field_1325, v), this.lerp(box.field_1321, box.field_1324, u));
         default -> new class_243(box.field_1320 - inset, this.lerp(box.field_1322, box.field_1325, v), this.lerp(box.field_1321, box.field_1324, u));
      };
   }

   private double rand01(long seed, int salt) {
      long x = seed + -7046029254386353131L * (salt + 1L);
      x ^= x >>> 30;
      x *= -4658895280553007687L;
      x ^= x >>> 27;
      x *= -7723592293110705685L;
      x ^= x >>> 31;
      return (x & 16777215L) / 1.6777216E7;
   }

   private double lerp(double a, double b, double t) {
      return a + (b - a) * t;
   }

   private double clamp01(double v) {
      return Math.max(0.0, Math.min(1.0, v));
   }

   private void drawFilledBox(Matrix4f matrix, class_238 box, int color) {
      class_287 b = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1321).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324).method_39415(color);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1321).method_39415(color);
      class_286.method_43433(b.method_60800());
   }

   private void drawMaskBox(Matrix4f matrix, class_238 box) {
      class_287 b = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      int white = -1;
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1325, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1323, (float)box.field_1322, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1321).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1322, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1324).method_39415(white);
      b.method_22918(matrix, (float)box.field_1320, (float)box.field_1325, (float)box.field_1321).method_39415(white);
      class_286.method_43433(b.method_60800());
   }

   private void drawFullscreenQuad() {
      class_287 b = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      float width = Math.max(mc.method_22683().method_4486(), 1);
      float height = Math.max(mc.method_22683().method_4502(), 1);
      b.method_22912(0.0F, 0.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(0.0F, height, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(width, height, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(width, 0.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      class_286.method_43433(b.method_60800());
   }
}
