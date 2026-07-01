package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_1671;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class FireworkESP extends Module {
   public static FireworkESP INSTANCE = new FireworkESP();
   private final FloatSetting interval = new FloatSetting("Интервал (мс)", 100.0F, 10.0F, 1000.0F, 10.0F);
   private final FloatSetting lifetime = new FloatSetting("Время жизни (мс)", 1000.0F, 100.0F, 5000.0F, 100.0F);
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   private class_243 lastCameraPos = class_243.field_1353;
   private float lastTickDelta;
   private final Map<Integer, FireworkESP.FireworkData> fireworks = new HashMap<>();

   public FireworkESP() {
      super("FireworkESP", "Показывает теги и трейлы фейерверков", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.interval, this.lifetime});
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.fireworks.clear();
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      this.lastProjectionMatrix.set(event.getProjectionMatrix());
      this.lastCameraPos = event.getCamera().method_19326();
      this.lastCameraRotation.set(event.getCamera().method_23767());
      this.lastTickDelta = event.getTickDelta();
      if (mc.field_1687 != null) {
         long currentTime = System.currentTimeMillis();
         this.fireworks.entrySet().removeIf(entry -> {
            class_1297 entityx = mc.field_1687.method_8469(entry.getKey());
            boolean isDead = entityx == null || !entityx.method_5805();
            entry.getValue().points.removeIf(p -> (float)(currentTime - p.timestamp) > this.lifetime.get());
            return isDead && entry.getValue().points.isEmpty();
         });

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1671 && entity.method_5805()) {
               FireworkESP.FireworkData data = this.fireworks.computeIfAbsent(entity.method_5628(), k -> new FireworkESP.FireworkData());
               if ((float)(currentTime - data.lastSpawnTime) >= this.interval.get()) {
                  class_243 pos = new class_243(
                     class_3532.method_16436(this.lastTickDelta, entity.field_6038, entity.method_23317()),
                     class_3532.method_16436(this.lastTickDelta, entity.field_5971, entity.method_23318()) + 0.5,
                     class_3532.method_16436(this.lastTickDelta, entity.field_5989, entity.method_23321())
                  );
                  float ageInSeconds = entity.field_6012 / 20.0F;
                  data.points.add(new FireworkESP.TrailPoint(pos, currentTime, ageInSeconds));
                  data.lastSpawnTime = currentTime;
               }
            }
         }
      }
   }

   @EventLink
   public void onRender2D(EventRender.Default event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_4587 matrices = event.getContext().method_51448();
         class_1799 icon = new class_1799(class_1802.field_8639);
         Font font = Fonts.getFont("sf_regular", 14);
         long currentTime = System.currentTimeMillis();

         for (Entry<Integer, FireworkESP.FireworkData> entry : this.fireworks.entrySet()) {
            FireworkESP.FireworkData data = entry.getValue();

            for (FireworkESP.TrailPoint p : data.points) {
               class_243 screen = this.worldToScreen(p.pos);
               if (screen != null) {
                  float progress = 1.0F - (float)(currentTime - p.timestamp) / this.lifetime.get();
                  progress = class_3532.method_15363(progress, 0.0F, 1.0F);
                  String text = String.format("%.1fs", p.ageSec);
                  this.renderIconRect(event, matrices, font, icon, screen, progress, text);
               }
            }

            class_1297 entity = mc.field_1687.method_8469(entry.getKey());
            if (entity instanceof class_1671 && entity.method_5805()) {
               class_243 currentPos = new class_243(
                  class_3532.method_16436(this.lastTickDelta, entity.field_6038, entity.method_23317()),
                  class_3532.method_16436(this.lastTickDelta, entity.field_5971, entity.method_23318()) + 0.5,
                  class_3532.method_16436(this.lastTickDelta, entity.field_5989, entity.method_23321())
               );
               class_243 screen = this.worldToScreen(currentPos);
               if (screen != null) {
                  String text = String.format("%.1fs", entity.field_6012 / 20.0F);
                  this.renderIconRect(event, matrices, font, icon, screen, 1.0F, text);
               }
            }
         }
      }
   }

   private void renderIconRect(EventRender.Default event, class_4587 matrices, Font font, class_1799 icon, class_243 screen, float progress, String text) {
      float iconScale = 0.6F;
      float rectHeight = 12.0F;
      float padding = 2.5F;
      float gap = 2.0F;
      float textYOffset = 3.5F;
      float animScale = 0.35F + 0.65F * progress;
      int alpha = (int)(200.0F * progress);
      if (alpha > 5) {
         int bgColor = alpha << 24 | 657930;
         int textColor = alpha << 24 | 16777215;
         float textWidth = font != null ? font.getStringWidth(text) : 0.0F;
         float iconWidth = 16.0F * iconScale;
         float totalWidth = padding + iconWidth + gap + textWidth + padding;
         matrices.method_22903();
         matrices.method_22904(screen.field_1352, screen.field_1351, 0.0);
         matrices.method_22905(animScale, animScale, 1.0F);
         RenderUtils.drawRoundedRect(matrices, -totalWidth / 2.0F, -rectHeight / 2.0F, totalWidth, rectHeight, 0.0F, bgColor);
         float currentX = -totalWidth / 2.0F + padding;
         matrices.method_22903();
         matrices.method_46416(currentX, -(16.0F * iconScale) / 2.0F, 0.0F);
         matrices.method_22905(iconScale, iconScale, 1.0F);
         event.getContext().method_51427(icon, 0, 0);
         matrices.method_22909();
         currentX += iconWidth + gap;
         if (font != null) {
            font.drawString(matrices, text, currentX, -rectHeight / 2.0F + textYOffset + 0.5F, textColor);
         }

         matrices.method_22909();
      }
   }

   private class_243 worldToScreen(class_243 worldPos) {
      Vector3f relative = new Vector3f(
         (float)(worldPos.field_1352 - this.lastCameraPos.field_1352),
         (float)(worldPos.field_1351 - this.lastCameraPos.field_1351),
         (float)(worldPos.field_1350 - this.lastCameraPos.field_1350)
      );
      relative.rotate(new Quaternionf(this.lastCameraRotation).conjugate());
      Vector4f clip = new Vector4f(relative.x, relative.y, relative.z, 1.0F);
      this.lastProjectionMatrix.transform(clip);
      float w = clip.w;
      if (w <= 1.0E-5F) {
         return null;
      } else {
         float ndcX = clip.x / w;
         float ndcY = clip.y / w;
         float ndcZ = clip.z / w;
         float screenX = (ndcX * 0.5F + 0.5F) * mc.method_22683().method_4486();
         float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * mc.method_22683().method_4502();
         return !Float.isNaN(screenX) && !Float.isNaN(screenY) && !Float.isInfinite(screenX) && !Float.isInfinite(screenY)
            ? new class_243(screenX, screenY, ndcZ)
            : null;
      }
   }

   private static class FireworkData {
      long lastSpawnTime;
      final List<FireworkESP.TrailPoint> points = new ArrayList<>();

      private FireworkData() {
      }
   }

   private static class TrailPoint {
      final class_243 pos;
      final long timestamp;
      final float ageSec;

      TrailPoint(class_243 pos, long timestamp, float ageSec) {
         this.pos = pos;
         this.timestamp = timestamp;
         this.ageSec = ageSec;
      }
   }
}
