package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10142;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class Trails extends Module {
   public static Trails INSTANCE = new Trails();
   private final FloatSetting duration = new FloatSetting("Длительность", 300.0F, 100.0F, 1000.0F, 10.0F);
   private final List<Trails.Point> points = new ArrayList<>();

   public Trails() {
      super("Trails", "Красивый след за игроком", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.duration});
   }

   @Override
   public void onDisable() {
      this.points.clear();
      super.onDisable();
   }

   @EventLink
   public void onRender(Event3DRender event) {
      if (mc.field_1690.method_31044() != class_5498.field_26664) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            long currentTime = System.currentTimeMillis();
            this.points.removeIf(p -> (float)(currentTime - p.time) > this.duration.get());
            class_243 playerPos = this.interpolatePlayerPosition(event.getTickDelta());
            this.points.add(new Trails.Point(playerPos));
            this.render3DPoints(event.getMatrices());
         }
      }
   }

   private class_243 interpolatePlayerPosition(float partialTicks) {
      return new class_243(
         class_3532.method_16436(partialTicks, mc.field_1724.field_6014, mc.field_1724.method_23317()),
         class_3532.method_16436(partialTicks, mc.field_1724.field_6036, mc.field_1724.method_23318()),
         class_3532.method_16436(partialTicks, mc.field_1724.field_5969, mc.field_1724.method_23321())
      );
   }

   private class_243 interpolatePlayerPosition(class_1657 playerEntity, float partialTicks) {
      return new class_243(
         class_3532.method_16436(partialTicks, playerEntity.field_6014, playerEntity.method_23317()),
         class_3532.method_16436(partialTicks, playerEntity.field_6036, playerEntity.method_23318()),
         class_3532.method_16436(partialTicks, playerEntity.field_5969, playerEntity.method_23321())
      );
   }

   private void render3DPoints(class_4587 matrixStack) {
      if (this.points.size() >= 2) {
         this.startRendering();
         matrixStack.method_22903();
         class_243 view = mc.field_1773.method_19418().method_19326();
         matrixStack.method_22904(-view.field_1352, -view.field_1351, -view.field_1350);
         Matrix4f matrix = matrixStack.method_23760().method_23761();
         int themeColor = ColorUtils.getThemeColor();
         float red = ColorUtils.redf(themeColor);
         float green = ColorUtils.greenf(themeColor);
         float blue = ColorUtils.bluef(themeColor);
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);
         int index = 0;

         for (Trails.Point p : this.points) {
            float alpha = (float)index / this.points.size() * 0.7F;
            int alphaInt = (int)(alpha * 255.0F);
            buffer.method_22918(matrix, (float)p.pos.field_1352, (float)(p.pos.field_1351 + mc.field_1724.method_17682()), (float)p.pos.field_1350)
               .method_1336((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), alphaInt);
            buffer.method_22918(matrix, (float)p.pos.field_1352, (float)p.pos.field_1351, (float)p.pos.field_1350)
               .method_1336((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), alphaInt);
            index++;
         }

         class_286.method_43433(buffer.method_60800());
         RenderSystem.lineWidth(2.0F);
         this.renderLineStrip(matrix, this.points, true, red, green, blue);
         this.renderLineStrip(matrix, this.points, false, red, green, blue);
         matrixStack.method_22909();
         this.stopRendering();
      }
   }

   private void renderLineStrip(Matrix4f matrix, List<Trails.Point> points, boolean withHeight, float red, float green, float blue) {
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29345, class_290.field_1576);
      int index = 0;

      for (Trails.Point p : points) {
         float alpha = Math.min((float)index / points.size() * 1.5F, 1.0F);
         int alphaInt = (int)(alpha * 255.0F);
         float y = withHeight ? (float)(p.pos.field_1351 + mc.field_1724.method_17682()) : (float)p.pos.field_1351;
         buffer.method_22918(matrix, (float)p.pos.field_1352, y, (float)p.pos.field_1350)
            .method_1336((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), alphaInt);
         index++;
      }

      class_286.method_43433(buffer.method_60800());
   }

   private void startRendering() {
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(class_10142.field_53876);
   }

   private void stopRendering() {
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   private static class Point {
      public class_243 pos;
      public long time;

      public Point(class_243 pos) {
         this.pos = pos;
         this.time = System.currentTimeMillis();
      }
   }
}
