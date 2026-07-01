package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventAttackEntity;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class HitMarker extends Module {
   public static HitMarker INSTANCE = new HitMarker();
   private final FloatSetting size = new FloatSetting("Размер", 0.5F, 0.1F, 2.0F, 0.05F);
   private final FloatSetting fadeInTime = new FloatSetting("Время появления", 100.0F, 50.0F, 500.0F, 10.0F);
   private final FloatSetting displayTime = new FloatSetting("Время показа", 300.0F, 100.0F, 1000.0F, 50.0F);
   private final FloatSetting fadeOutTime = new FloatSetting("Время исчезновения", 200.0F, 50.0F, 500.0F, 10.0F);
   private final BooleanSetting glow = new BooleanSetting("Свечение", true);
   private final BooleanSetting scale = new BooleanSetting("Анимация масштаба", true);
   private final ArrayList<HitMarker.HitMarkerData> hitMarkers = new ArrayList<>();

   public HitMarker() {
      super("HitMarker", "Показывает маркер при ударе", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.size, this.fadeInTime, this.displayTime, this.fadeOutTime, this.glow, this.scale});
   }

   @Override
   public void onDisable() {
      this.hitMarkers.clear();
      super.onDisable();
   }

   private class_2960 getTexture() {
      return class_2960.method_60655("slikdlc", "textures/cross/cross.png");
   }

   @EventLink
   public void onAttack(EventAttackEntity event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_1297 target = event.getTarget();
         if (target != null) {
            synchronized (this.hitMarkers) {
               this.hitMarkers
                  .add(
                     new HitMarker.HitMarkerData(
                        this.resolveHitPosition(event.getPlayer(), target),
                        System.currentTimeMillis(),
                        (long)this.fadeInTime.get(),
                        (long)this.displayTime.get(),
                        (long)this.fadeOutTime.get()
                     )
                  );
            }
         }
      }
   }

   private class_243 resolveHitPosition(class_1297 attacker, class_1297 target) {
      class_243 fallback = new class_243(target.method_23317(), target.method_23318() + target.method_17682() / 2.0, target.method_23321());
      if (attacker == null) {
         return fallback;
      } else {
         class_243 eyePos = attacker.method_5836(1.0F);
         class_243 lookVec = attacker.method_5828(1.0F);
         class_243 targetCenter = target.method_5829().method_1005();
         double distance = Math.max(eyePos.method_1022(targetCenter) + 1.0, 6.0);
         class_243 reachPos = eyePos.method_1019(lookVec.method_1021(distance));
         Optional<class_243> hitPos = target.method_5829().method_992(eyePos, reachPos);
         return hitPos.isPresent() ? hitPos.get() : eyePos.method_1019(lookVec.method_1021(eyePos.method_1022(targetCenter)));
      }
   }

   @EventLink
   public void onRender3D(Event3DRender e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         synchronized (this.hitMarkers) {
            this.hitMarkers.removeIf(HitMarker.HitMarkerData::isDead);
         }

         if (!this.hitMarkers.isEmpty()) {
            class_4587 matrices = e.getMatrices();
            class_243 camera = mc.field_1773.method_19418().method_19326();
            class_2960 texture = this.getTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            if (this.glow.isState()) {
               RenderSystem.blendFunc(770, 1);
            } else {
               RenderSystem.defaultBlendFunc();
            }

            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(class_10142.field_53880);
            ArrayList<HitMarker.HitMarkerData> renderList;
            synchronized (this.hitMarkers) {
               renderList = new ArrayList<>(this.hitMarkers);
            }

            int color = ColorUtils.getThemeColor();
            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;

            for (HitMarker.HitMarkerData marker : renderList) {
               float alpha = marker.getAlpha();
               if (!(alpha <= 0.0F)) {
                  double x = marker.position.field_1352 - camera.field_1352;
                  double y = marker.position.field_1351 - camera.field_1351;
                  double z = marker.position.field_1350 - camera.field_1350;
                  matrices.method_22903();
                  matrices.method_46416((float)x, (float)y, (float)z);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
                  float currentSize = this.size.get();
                  if (this.scale.isState()) {
                     float scaleMultiplier = marker.getScaleMultiplier();
                     currentSize *= scaleMultiplier;
                  }

                  Matrix4f matrix = matrices.method_23760().method_23761();
                  float half = currentSize / 2.0F;
                  int alphaInt = (int)(alpha * 255.0F);
                  class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
                  buffer.method_22918(matrix, -half, -half, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, alphaInt);
                  buffer.method_22918(matrix, -half, half, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, alphaInt);
                  buffer.method_22918(matrix, half, half, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, alphaInt);
                  buffer.method_22918(matrix, half, -half, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, alphaInt);
                  class_286.method_43433(buffer.method_60800());
                  matrices.method_22909();
               }
            }

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }

   static class HitMarkerData {
      class_243 position;
      long birthTime;
      long fadeInTime;
      long displayTime;
      long fadeOutTime;

      HitMarkerData(class_243 position, long birthTime, long fadeInTime, long displayTime, long fadeOutTime) {
         this.position = position;
         this.birthTime = birthTime;
         this.fadeInTime = fadeInTime;
         this.displayTime = displayTime;
         this.fadeOutTime = fadeOutTime;
      }

      boolean isDead() {
         return System.currentTimeMillis() - this.birthTime >= this.fadeInTime + this.displayTime + this.fadeOutTime;
      }

      float getAlpha() {
         long elapsed = System.currentTimeMillis() - this.birthTime;
         if (elapsed < this.fadeInTime) {
            float progress = (float)elapsed / (float)this.fadeInTime;
            return this.easeOutCubic(progress);
         } else if (elapsed < this.fadeInTime + this.displayTime) {
            return 1.0F;
         } else {
            long fadeOutElapsed = elapsed - this.fadeInTime - this.displayTime;
            float progress = Math.min(1.0F, (float)fadeOutElapsed / (float)this.fadeOutTime);
            return 1.0F - this.easeInCubic(progress);
         }
      }

      float getScaleMultiplier() {
         long elapsed = System.currentTimeMillis() - this.birthTime;
         if (elapsed < this.fadeInTime) {
            float progress = (float)elapsed / (float)this.fadeInTime;
            return 0.5F + 0.5F * this.easeOutBack(progress);
         } else if (elapsed < this.fadeInTime + this.displayTime) {
            return 1.0F;
         } else {
            long fadeOutElapsed = elapsed - this.fadeInTime - this.displayTime;
            float progress = Math.min(1.0F, (float)fadeOutElapsed / (float)this.fadeOutTime);
            return 1.0F - 0.3F * this.easeInCubic(progress);
         }
      }

      private float easeOutCubic(float x) {
         return 1.0F - (float)Math.pow(1.0 - x, 3.0);
      }

      private float easeInCubic(float x) {
         return x * x * x;
      }

      private float easeOutBack(float x) {
         float c1 = 1.70158F;
         float c3 = c1 + 1.0F;
         return 1.0F + c3 * (float)Math.pow(x - 1.0, 3.0) + c1 * (float)Math.pow(x - 1.0, 2.0);
      }
   }
}
