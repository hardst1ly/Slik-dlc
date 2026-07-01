package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1684;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_239.class_240;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Projectile extends Module {
   private final Font impactFont = Fonts.getFont("sf_regular", 14);
   public static Projectile INSTANCE = new Projectile();
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("slikdlc", "textures/particle/bloom.png");
   private final FloatSetting size = new FloatSetting("Размер", 1.2F, 0.6F, 2.4F, 0.1F);
   private final List<Projectile.ImpactPoint> impactPoints = new ArrayList<>();
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   private class_243 lastCameraPos = class_243.field_1353;
   private boolean hasMatrices;

   public Projectile() {
      super("Projectile", "Траектория жемчуга эндера", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.size});
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.impactPoints.clear();
         this.hasMatrices = true;
         this.lastProjectionMatrix.set(event.getProjectionMatrix());
         this.lastCameraPos = event.getCamera().method_19326();
         this.lastCameraRotation.set(event.getCamera().method_23767());
         class_4587 matrices = event.getMatrices();
         class_4184 camera = event.getCamera();
         class_243 cameraPos = camera.method_19326();
         Quaternionf cameraRotation = camera.method_23767();
         float tickDelta = event.getTickDelta();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
         class_238 searchBox = mc.field_1724.method_5829().method_1014(128.0);

         for (class_1684 pearl : mc.field_1687.method_8390(class_1684.class, searchBox, class_1297::method_5805)) {
            List<class_243> points = this.simulate(pearl, tickDelta);
            if (points.size() >= 2) {
               float seconds = (points.size() - 1) / 20.0F;
               class_243 impactPos = points.get(points.size() - 1);
               this.impactPoints.add(new Projectile.ImpactPoint(impactPos, seconds));
               float quadSize = this.size.get() * 0.2F;
               int color = ColorUtils.setAlphaColor(ColorUtils.getThemeColor(), 40);
               int r = color >> 16 & 0xFF;
               int g = color >> 8 & 0xFF;
               int b = color & 0xFF;
               int a = color >> 24 & 0xFF;
               matrices.method_22903();
               matrices.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);
               class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);

               for (int i = 0; i < points.size() - 1; i++) {
                  class_243 start = points.get(i);
                  class_243 end = points.get(i + 1);
                  int samples = Math.max(2, Math.min(12, (int)Math.ceil(start.method_1022(end) / Math.max(quadSize * 1.75F, 0.08F))));

                  for (int j = 0; j < samples; j++) {
                     class_243 interp = start.method_35590(end, (double)j / samples);
                     matrices.method_22903();
                     matrices.method_22904(interp.field_1352, interp.field_1351, interp.field_1350);
                     matrices.method_22907(cameraRotation);
                     Matrix4f matrix = matrices.method_23760().method_23761();
                     buffer.method_22918(matrix, -quadSize, -quadSize, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
                     buffer.method_22918(matrix, -quadSize, quadSize, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
                     buffer.method_22918(matrix, quadSize, quadSize, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
                     buffer.method_22918(matrix, quadSize, -quadSize, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
                     matrices.method_22909();
                  }
               }

               class_286.method_43433(buffer.method_60800());
               matrices.method_22909();
               float markerSize = quadSize * 1.6F;
               int markerColor = ColorUtils.setAlphaColor(ColorUtils.getThemeColor(), 170);
               int mr = markerColor >> 16 & 0xFF;
               int mg = markerColor >> 8 & 0xFF;
               int mb = markerColor & 0xFF;
               int ma = markerColor >> 24 & 0xFF;
               float mx = (float)(impactPos.field_1352 - cameraPos.field_1352);
               float my = (float)(impactPos.field_1351 - cameraPos.field_1351 + 0.03F);
               float mz = (float)(impactPos.field_1350 - cameraPos.field_1350);
               matrices.method_22903();
               matrices.method_46416(mx, my, mz);
               matrices.method_22907(cameraRotation);
               Matrix4f markerMatrix = matrices.method_23760().method_23761();
               class_287 marker = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
               marker.method_22918(markerMatrix, -markerSize, -markerSize, 0.0F).method_22913(0.0F, 0.0F).method_1336(mr, mg, mb, ma);
               marker.method_22918(markerMatrix, -markerSize, markerSize, 0.0F).method_22913(0.0F, 1.0F).method_1336(mr, mg, mb, ma);
               marker.method_22918(markerMatrix, markerSize, markerSize, 0.0F).method_22913(1.0F, 1.0F).method_1336(mr, mg, mb, ma);
               marker.method_22918(markerMatrix, markerSize, -markerSize, 0.0F).method_22913(1.0F, 0.0F).method_1336(mr, mg, mb, ma);
               class_286.method_43433(marker.method_60800());
               matrices.method_22909();
            }
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   @EventLink
   public void onRender2D(EventRender.Default event) {
      if (this.hasMatrices && !this.impactPoints.isEmpty() && mc.field_1724 != null) {
         class_4587 matrices = event.getContext().method_51448();
         Font font = this.impactFont;
         if (font != null) {
            int themeColor = ColorUtils.getThemeColor();

            for (Projectile.ImpactPoint impact : this.impactPoints) {
               class_243 screen = this.worldToScreen(impact.pos());
               if (screen != null) {
                  String text = this.formatOneDecimal(impact.seconds()) + " сек";
                  float textWidth = font.getStringWidth(text);
                  float boxWidth = textWidth + 10.0F;
                  float boxHeight = 12.5F;
                  float x = (float)screen.field_1352 - boxWidth / 2.0F;
                  float y = (float)screen.field_1351 - 6.0F;
                  RenderUtils.drawDefaultHudThemedPanelWithStroke(
                     matrices, x, y, boxWidth, boxHeight, 3.0F, 3.5F, themeColor, ModuleClass.interfaceModule.strokeStyle.getCurrent()
                  );
                  font.drawString(matrices, text, x + 5.5F, y + 4.55F, -1);
               }
            }
         }
      }
   }

   private class_243 worldToScreen(class_243 worldPos) {
      if (mc != null && mc.method_22683() != null) {
         Vector3f relative = new Vector3f(
            (float)(worldPos.field_1352 - this.lastCameraPos.field_1352),
            (float)(worldPos.field_1351 - this.lastCameraPos.field_1351),
            (float)(worldPos.field_1350 - this.lastCameraPos.field_1350)
         );
         Quaternionf invCameraRot = new Quaternionf(this.lastCameraRotation).conjugate();
         relative.rotate(invCameraRot);
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
            if (!Float.isNaN(screenX) && !Float.isNaN(screenY) && !Float.isInfinite(screenX) && !Float.isInfinite(screenY)) {
               return !(screenX < -400.0F)
                     && !(screenY < -400.0F)
                     && !(screenX > mc.method_22683().method_4486() + 400)
                     && !(screenY > mc.method_22683().method_4502() + 400)
                  ? new class_243(screenX, screenY, ndcZ)
                  : null;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private String formatOneDecimal(float value) {
      int scaled = Math.round(value * 10.0F);
      return scaled / 10 + "." + Math.abs(scaled % 10);
   }

   private List<class_243> simulate(class_1684 pearl, float tickDelta) {
      List<class_243> points = new ArrayList<>();
      class_243 pos = new class_243(
         class_3532.method_16436(tickDelta, pearl.field_6014, pearl.method_23317()),
         class_3532.method_16436(tickDelta, pearl.field_6036, pearl.method_23318()),
         class_3532.method_16436(tickDelta, pearl.field_5969, pearl.method_23321())
      );
      class_243 motion = pearl.method_18798();
      points.add(pos);

      for (int i = 0; i < 300; i++) {
         class_243 nextPos = pos.method_1019(motion);
         class_3965 hit = mc.field_1687.method_17742(new class_3959(pos, nextPos, class_3960.field_17558, class_242.field_1348, mc.field_1724));
         if (hit.method_17783() == class_240.field_1332) {
            points.add(hit.method_17784());
            break;
         }

         points.add(nextPos);
         pos = nextPos;
         boolean inWater = mc.field_1687.method_8320(class_2338.method_49638(nextPos)).method_27852(class_2246.field_10382);
         double drag = inWater ? 0.8 : 0.99;
         motion = motion.method_1021(drag).method_1023(0.0, 0.03, 0.0);
         if (nextPos.field_1351 <= mc.field_1687.method_31607()) {
            break;
         }
      }

      return points;
   }

   private record ImpactPoint(class_243 pos, float seconds) {
   }
}
