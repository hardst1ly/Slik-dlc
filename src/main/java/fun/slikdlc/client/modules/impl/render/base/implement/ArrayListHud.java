package fun.slikdlc.client.modules.impl.render.base.implement;

import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_4587;

public class ArrayListHud extends InterfaceProcessing {
   private static final float LINE_HEIGHT = 9.5F;
   private static final float FLOW_SPEED = 1000.0F;
   private static final Comparator<ArrayListHud.ModuleEntry> MODULE_WIDTH_COMPARATOR = Comparator.<ArrayListHud.ModuleEntry>comparingDouble(
         entry -> entry.width
      )
      .reversed();
   private final List<ArrayListHud.ModuleEntry> visibleModules = new ArrayList<>();

   public ArrayListHud(Draggable draggable) {
      super(draggable);
   }

   private Font font() {
      return Fonts.getFont("suisse", 14);
   }

   private void drawFlowingText(class_4587 matrices, Font font, String text, float x, float y, int color, float alphaMul) {
      int textColor = ColorUtils.setAlphaColor(color, (int)(255.0F * alphaMul));
      font.draw(matrices, text, x, y, textColor);
   }

   @Override
   public void onRender(EventRender.Default eventRender) {
      class_4587 matrices = eventRender.getContext().method_51448();
      Font font = this.font();
      List<Module> modules = ModuleClass.INSTANCE.getObject();
      this.visibleModules.clear();

      for (Module module : modules) {
         module.getArrayAnimka().update(module.isEnable() ? 1.0F : 0.0F);
         float anim = module.getArrayAnimka().getValue();
         if (!(anim <= 0.03F)) {
            String displayName = module.getDisplayName();
            this.visibleModules.add(new ArrayListHud.ModuleEntry(displayName.toLowerCase(), font.getWidth(displayName), anim));
         }
      }

      this.visibleModules.sort(MODULE_WIDTH_COMPARATOR);
      long now = System.currentTimeMillis();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      float maxWidth = 0.0F;
      boolean leftHalf = x <= mc.method_22683().method_4486() * 0.5F;

      for (ArrayListHud.ModuleEntry entry : this.visibleModules) {
         maxWidth = Math.max(maxWidth, entry.width);
      }

      float yOffset = 0.0F;

      for (int i = 0; i < this.visibleModules.size(); i++) {
         ArrayListHud.ModuleEntry entry = this.visibleModules.get(i);
         float anim = entry.anim;
         float lineStep = 9.5F * anim;
         int indexShift = (int)((float)now * 1000.0F / 10.0F) + i * 42;
         int rowColor = ColorUtils.getThemeColor(indexShift);
         int rowColor2 = ColorUtils.getThemeColor(indexShift + 90);
         int glowAlpha = (int)((leftHalf ? 140 : 170) * anim);
         int glow1 = ColorUtils.setAlphaColor(rowColor, glowAlpha);
         int glow2 = ColorUtils.setAlphaColor(rowColor2, glowAlpha);
         float textWidth = entry.width;
         float drawX;
         if (leftHalf) {
            drawX = x - 3.0F;
         } else {
            drawX = x + (maxWidth - textWidth) - 3.0F;
         }

         float drawY = y + yOffset + (1.0F - anim) * 7.0F;
         float shadowX = leftHalf ? drawX - 0.6F : drawX - 1.5F;
         float shadowW = leftHalf ? textWidth - 4.0F : textWidth;
         RenderUtils.drawShadow(matrices, shadowX, drawY, shadowW, 6.0F, 5.0F, 11.0F, glow2, glow2, glow1, glow1);
         float textX = leftHalf ? drawX - 0.8F : drawX - 2.0F;
         this.drawFlowingText(matrices, font, entry.lowerName, textX, drawY + 1.5F, rowColor, anim);
         yOffset += lineStep;
      }

      if (yOffset > 0.5F) {
         float lineX = leftHalf ? x - 6.5F : x + maxWidth - 7.0F;
         float lineWidth = 2.5F;
         int topLineColor = ColorUtils.setAlphaColor(ColorUtils.getThemeColor(0), 220);
         int bottomLineColor = ColorUtils.setAlphaColor(ColorUtils.getThemeColor(180), 220);
         RenderUtils.drawGradientRect(matrices, lineX, y, lineWidth, yOffset - 2.0F, 0.0F, topLineColor, bottomLineColor);
      }

      this.draggable.setWidth(maxWidth + 4.0F);
      this.draggable.setHeight(yOffset);
      super.onRender(eventRender);
   }

   private record ModuleEntry(String lowerName, float width, float anim) {
   }
}
