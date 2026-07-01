package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.class_10142;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class LineGlyphes extends Module {
   public static LineGlyphes INSTANCE = new LineGlyphes();
   private static final class_2382[] AXIS_DIRECTIONS = new class_2382[]{
      new class_2382(1, 0, 0), new class_2382(-1, 0, 0), new class_2382(0, 1, 0), new class_2382(0, -1, 0), new class_2382(0, 0, 1), new class_2382(0, 0, -1)
   };
   private final FloatSetting glyphsCount = new FloatSetting("Количество Линий", 70.0F, 10.0F, 200.0F, 1.0F);
   private final BooleanSetting slowSpeed = new BooleanSetting("Медленная Скорость", false);
   private final BooleanSetting applyStippleLines = new BooleanSetting("Пунктирные Линии", false);
   private final FloatSetting stippleStepPixels = new FloatSetting("Шаг Пунктира", 3.0F, 0.5F, 20.0F, 0.5F).visible(() -> this.applyStippleLines.isState());
   private final BooleanSetting linesGlowing = new BooleanSetting("Свечение", false);
   private final Random rand = new Random(93882L);
   private final List<LineGlyphes.GlyphPath> glyphs = new ArrayList<>();

   public LineGlyphes() {
      super("LineGlyphes", "Анимированные линии по миру", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.glyphsCount, this.slowSpeed, this.applyStippleLines, this.stippleStepPixels, this.linesGlowing});
   }

   @Override
   public void onDisable() {
      this.glyphs.clear();
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate ignored) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.updateGlyphs();
         this.maintainGlyphCount();
      } else {
         this.glyphs.clear();
      }
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (mc.field_1724 != null && mc.field_1687 != null && event.getCamera() != null) {
         this.drawAllGlyphs(event);
      }
   }

   private void drawAllGlyphs(Event3DRender event) {
      if (!this.glyphs.isEmpty()) {
         List<LineGlyphes.GlyphPath> drawable = new ArrayList<>();

         for (LineGlyphes.GlyphPath glyph : this.glyphs) {
            if (glyph.getAlpha() > 0.01F && glyph.getPointCount() >= 2) {
               drawable.add(glyph);
            }
         }

         if (!drawable.isEmpty()) {
            class_4587 matrices = event.getMatrices();
            class_243 cameraPos = event.getCamera().method_19326();
            matrices.method_22903();
            matrices.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);

            try {
               this.setupRenderState();
               if (this.linesGlowing.isState()) {
                  this.setAdditiveBlend();
                  this.drawGlyphPass(matrices, drawable, event.getTickDelta(), 4.0F, 0.08F);
                  this.drawGlyphPass(matrices, drawable, event.getTickDelta(), 2.5F, 0.15F);
                  this.drawGlyphPass(matrices, drawable, event.getTickDelta(), 1.5F, 0.25F);
               }

               this.setNormalBlend();
               this.drawGlyphPass(matrices, drawable, event.getTickDelta(), 1.0F, 1.0F);
            } finally {
               this.restoreRenderState();
               matrices.method_22909();
            }
         }
      }
   }

   private void drawGlyphPass(class_4587 matrices, List<LineGlyphes.GlyphPath> drawable, float partialTicks, float widthMul, float alphaMul) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      boolean dashed = this.applyStippleLines.isState();
      float dashStep = Math.max(0.04F, this.stippleStepPixels.get() * 0.08F);
      float gapStep = dashStep * 0.85F;
      RenderSystem.lineWidth(Math.min(1.15F * widthMul, 12.0F));
      List<LineGlyphes.VertexData> vertices = new ArrayList<>();
      int colorIndex = 0;

      for (LineGlyphes.GlyphPath glyph : drawable) {
         float glyphAlpha = glyph.getAlpha() * alphaMul;
         if (!(glyphAlpha <= 0.01F)) {
            List<class_243> points = glyph.getSmoothedPositions(partialTicks);
            int pointCount = points.size();
            if (pointCount >= 2) {
               for (int i = 0; i < pointCount - 1; i++) {
                  class_243 from = points.get(i);
                  class_243 to = points.get(i + 1);
                  float segmentAlpha = glyphAlpha * (0.25F + (float)i / pointCount / 1.75F);
                  int color = this.getThemeColorWithAlpha(segmentAlpha);
                  colorIndex += 180;
                  if (dashed) {
                     this.collectDashedSegment(vertices, from, to, color, dashStep, gapStep);
                  } else {
                     this.collectSegment(vertices, from, to, color);
                  }
               }
            }
         }
      }

      float markerSize = 0.018F * widthMul;
      int pointColorIndex = 0;

      for (LineGlyphes.GlyphPath glyphx : drawable) {
         float glyphAlpha = glyphx.getAlpha() * alphaMul;
         if (!(glyphAlpha <= 0.01F)) {
            List<class_243> points = glyphx.getSmoothedPositions(partialTicks);
            int pointCount = points.size();
            if (pointCount >= 2) {
               for (int ix = 0; ix < pointCount; ix++) {
                  class_243 pos = points.get(ix);
                  float localAlpha = glyphAlpha * (0.25F + (float)ix / pointCount / 1.75F);
                  int color = this.getThemeColorWithAlpha(localAlpha);
                  pointColorIndex += 180;
                  this.collectPointCross(vertices, pos, color, markerSize);
               }
            }
         }
      }

      if (!vertices.isEmpty()) {
         class_289 tessellator = class_289.method_1348();
         class_287 buffer = tessellator.method_60827(class_5596.field_29344, class_290.field_1576);

         for (LineGlyphes.VertexData v : vertices) {
            buffer.method_22918(matrix, v.x, v.y, v.z).method_1336(v.r, v.g, v.b, v.a);
         }

         class_286.method_43433(buffer.method_60800());
      }

      RenderSystem.lineWidth(1.0F);
   }

   private int getThemeColorWithAlpha(float alphaMul) {
      int themed = ColorUtils.getThemeColor();
      float alpha = class_3532.method_15363(alphaMul, 0.0F, 1.0F);
      int r = themed >> 16 & 0xFF;
      int g = themed >> 8 & 0xFF;
      int b = themed & 0xFF;
      int a = (int)(alpha * 255.0F);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private void collectSegment(List<LineGlyphes.VertexData> vertices, class_243 from, class_243 to, int color) {
      vertices.add(new LineGlyphes.VertexData((float)from.field_1352, (float)from.field_1351, (float)from.field_1350, color));
      vertices.add(new LineGlyphes.VertexData((float)to.field_1352, (float)to.field_1351, (float)to.field_1350, color));
   }

   private void collectDashedSegment(List<LineGlyphes.VertexData> vertices, class_243 from, class_243 to, int color, float dashLen, float gapLen) {
      double dx = to.field_1352 - from.field_1352;
      double dy = to.field_1351 - from.field_1351;
      double dz = to.field_1350 - from.field_1350;
      double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(length < 1.0E-4)) {
         double nx = dx / length;
         double ny = dy / length;
         double nz = dz / length;
         double cursor = 0.0;

         for (boolean draw = true; cursor < length; draw = !draw) {
            double step = draw ? dashLen : gapLen;
            double next = Math.min(length, cursor + step);
            if (draw) {
               float sx = (float)(from.field_1352 + nx * cursor);
               float sy = (float)(from.field_1351 + ny * cursor);
               float sz = (float)(from.field_1350 + nz * cursor);
               float ex = (float)(from.field_1352 + nx * next);
               float ey = (float)(from.field_1351 + ny * next);
               float ez = (float)(from.field_1350 + nz * next);
               vertices.add(new LineGlyphes.VertexData(sx, sy, sz, color));
               vertices.add(new LineGlyphes.VertexData(ex, ey, ez, color));
            }

            cursor = next;
         }
      }
   }

   private void collectPointCross(List<LineGlyphes.VertexData> vertices, class_243 pos, int color, float size) {
      float x = (float)pos.field_1352;
      float y = (float)pos.field_1351;
      float z = (float)pos.field_1350;
      vertices.add(new LineGlyphes.VertexData(x - size, y, z, color));
      vertices.add(new LineGlyphes.VertexData(x + size, y, z, color));
      vertices.add(new LineGlyphes.VertexData(x, y - size, z, color));
      vertices.add(new LineGlyphes.VertexData(x, y + size, z, color));
      vertices.add(new LineGlyphes.VertexData(x, y, z - size, color));
      vertices.add(new LineGlyphes.VertexData(x, y, z + size, color));
   }

   private void setupRenderState() {
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(class_10142.field_53876);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
   }

   private void setAdditiveBlend() {
      RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE, class_4535.ONE, class_4534.ZERO);
   }

   private void setNormalBlend() {
      RenderSystem.defaultBlendFunc();
   }

   private void restoreRenderState() {
      RenderSystem.lineWidth(1.0F);
      GL11.glDisable(2848);
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void updateGlyphs() {
      Iterator<LineGlyphes.GlyphPath> iterator = this.glyphs.iterator();

      while (iterator.hasNext()) {
         LineGlyphes.GlyphPath glyph = iterator.next();
         glyph.update();
         if (glyph.isDead()) {
            iterator.remove();
         }
      }
   }

   private void maintainGlyphCount() {
      int targetCount = this.maxGlyphCount();

      int activeCount;
      for (activeCount = this.countActiveGlyphs(); activeCount < targetCount; activeCount++) {
         this.glyphs.add(new LineGlyphes.GlyphPath(this.randomGlyphSpawnPos(), this.randInt(7, 12)));
      }

      if (activeCount > targetCount) {
         for (LineGlyphes.GlyphPath glyph : this.glyphs) {
            if (activeCount <= targetCount) {
               break;
            }

            if (!glyph.isRemoving()) {
               glyph.setWantToRemove();
               activeCount--;
            }
         }
      }
   }

   private int countActiveGlyphs() {
      int count = 0;

      for (LineGlyphes.GlyphPath glyph : this.glyphs) {
         if (!glyph.isRemoving()) {
            count++;
         }
      }

      return count;
   }

   private int maxGlyphCount() {
      return Math.max(1, Math.round(this.glyphsCount.get()));
   }

   private class_2382 randomGlyphSpawnPos() {
      int minDistance = 6;
      int maxDistance = 24;
      int minY = 0;
      int maxY = 12;
      class_243 cameraPos = this.getCameraPos();

      for (int attempt = 0; attempt < 16; attempt++) {
         int distance = this.randInt(6, 25);
         float yawBase = mc.field_1724 != null ? mc.field_1724.method_36454() : 0.0F;
         float randomYaw = yawBase + this.randFloat(-135.0F, 135.0F);
         float yawRad = (float)Math.toRadians(randomYaw);
         int offsetX = (int)(-(class_3532.method_15374(yawRad) * distance));
         int offsetY = this.randInt(0, 13);
         int offsetZ = (int)(class_3532.method_15362(yawRad) * distance);
         class_2382 spawn = new class_2382(
            (int)Math.floor(cameraPos.field_1352) + offsetX, (int)Math.floor(cameraPos.field_1351) + offsetY, (int)Math.floor(cameraPos.field_1350) + offsetZ
         );
         if (this.isSpawnPosFree(spawn)) {
            return spawn;
         }
      }

      return new class_2382((int)Math.floor(cameraPos.field_1352), (int)Math.floor(cameraPos.field_1351), (int)Math.floor(cameraPos.field_1350));
   }

   private boolean isSpawnPosFree(class_2382 pos) {
      if (mc.field_1687 == null) {
         return true;
      } else {
         class_2338 bp = new class_2338(pos.method_10263(), pos.method_10264(), pos.method_10260());
         class_2680 state = mc.field_1687.method_8320(bp);
         if (state.method_26215()) {
            return true;
         } else {
            return !state.method_26227().method_15769() ? false : state.method_26220(mc.field_1687, bp).method_1110();
         }
      }
   }

   private class_243 getCameraPos() {
      class_4184 camera = mc.field_1773 != null ? mc.field_1773.method_19418() : null;
      if (camera != null) {
         return camera.method_19326();
      } else {
         return mc.field_1724 != null ? mc.field_1724.method_19538() : class_243.field_1353;
      }
   }

   private class_2382 randomAxisDirection() {
      return AXIS_DIRECTIONS[this.rand.nextInt(AXIS_DIRECTIONS.length)];
   }

   private class_2382 nextOrthogonalDirection(class_2382 previousDirection) {
      for (int i = 0; i < 12; i++) {
         class_2382 candidate = this.randomAxisDirection();
         int dot = candidate.method_10263() * previousDirection.method_10263()
            + candidate.method_10264() * previousDirection.method_10264()
            + candidate.method_10260() * previousDirection.method_10260();
         if (dot == 0) {
            return candidate;
         }
      }

      return this.randomAxisDirection();
   }

   private int randInt(int minInclusive, int maxExclusive) {
      return maxExclusive <= minInclusive ? minInclusive : this.rand.nextInt(maxExclusive - minInclusive) + minInclusive;
   }

   private float randFloat(float minInclusive, float maxInclusive) {
      return maxInclusive <= minInclusive ? minInclusive : minInclusive + this.rand.nextFloat() * (maxInclusive - minInclusive);
   }

   private float moveAdvanceFromTicks(int ticksSet, int ticksLeft, float partialTicks) {
      if (ticksSet <= 0) {
         return 1.0F;
      } else {
         float progress = 1.0F - (ticksLeft - partialTicks) / ticksSet;
         return class_3532.method_15363(progress, 0.0F, 1.0F);
      }
   }

   private static double lerp(double start, double end, double delta) {
      return start + (end - start) * delta;
   }

   private class GlyphPath {
      private final List<class_2382> points = new ArrayList<>();
      private final AnimationUtils alpha = new AnimationUtils(0.0F, 8.0F, Easings.QUAD_OUT);
      private class_2382 lastDirection;
      private int currentStepTicks;
      private int lastStepSet;
      private int stepsLeft;
      private boolean removing;

      GlyphPath(class_2382 spawnPos, int maxSteps) {
         this.points.add(spawnPos);
         this.lastDirection = LineGlyphes.this.randomAxisDirection();
         this.stepsLeft = maxSteps;
         this.alpha.setValue(0.0F);
      }

      void update() {
         this.alpha.update(this.removing ? 0.0F : 1.0F);
         if (!this.removing) {
            if (this.stepsLeft <= 0) {
               this.setWantToRemove();
            } else if (this.currentStepTicks > 0) {
               this.currentStepTicks = this.currentStepTicks - (LineGlyphes.this.slowSpeed.isState() ? 1 : 2);
               if (this.currentStepTicks < 0) {
                  this.currentStepTicks = 0;
               }
            } else {
               class_2382 last = this.points.get(this.points.size() - 1);
               boolean added = false;

               for (int attempt = 0; attempt < 8; attempt++) {
                  class_2382 nextDirection = LineGlyphes.this.nextOrthogonalDirection(this.lastDirection);
                  int step = LineGlyphes.this.randInt(1, 4);
                  class_2382 next = new class_2382(
                     last.method_10263() + nextDirection.method_10263() * step,
                     last.method_10264() + nextDirection.method_10264() * step,
                     last.method_10260() + nextDirection.method_10260() * step
                  );
                  if (LineGlyphes.this.isSpawnPosFree(next)) {
                     this.lastDirection = nextDirection;
                     this.lastStepSet = step;
                     this.currentStepTicks = step;
                     this.points.add(next);
                     this.stepsLeft--;
                     added = true;
                     break;
                  }
               }

               if (!added) {
                  this.setWantToRemove();
               }
            }
         }
      }

      List<class_243> getSmoothedPositions(float partialTicks) {
         List<class_243> smoothed = new ArrayList<>(this.points.size());
         float moveAdvance = LineGlyphes.this.moveAdvanceFromTicks(this.lastStepSet, this.currentStepTicks, partialTicks);

         for (int i = 0; i < this.points.size(); i++) {
            class_2382 point = this.points.get(i);
            double x = point.method_10263();
            double y = point.method_10264();
            double z = point.method_10260();
            if (this.points.size() >= 2 && i == this.points.size() - 1) {
               class_2382 previous = this.points.get(this.points.size() - 2);
               x = LineGlyphes.lerp((double)previous.method_10263(), x, (double)moveAdvance);
               y = LineGlyphes.lerp((double)previous.method_10264(), y, (double)moveAdvance);
               z = LineGlyphes.lerp((double)previous.method_10260(), z, (double)moveAdvance);
            }

            smoothed.add(new class_243(x, y, z));
         }

         return smoothed;
      }

      int getPointCount() {
         return this.points.size();
      }

      float getAlpha() {
         return class_3532.method_15363(this.alpha.getValue(), 0.0F, 1.0F);
      }

      boolean isRemoving() {
         return this.removing;
      }

      void setWantToRemove() {
         this.removing = true;
      }

      boolean isDead() {
         return this.removing && this.getAlpha() <= 0.03F;
      }
   }

   private static class VertexData {
      final float x;
      final float y;
      final float z;
      final int r;
      final int g;
      final int b;
      final int a;

      VertexData(float x, float y, float z, int color) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.a = color >> 24 & 0xFF;
         this.r = color >> 16 & 0xFF;
         this.g = color >> 8 & 0xFF;
         this.b = color & 0xFF;
      }
   }
}
