package fun.slikdlc.client.modules.impl.render.base.implement;

import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;

public class KeyStrokes extends InterfaceProcessing {
   private final class_310 mc = class_310.method_1551();
   private final List<Long> leftClicks = new ArrayList<>();
   private boolean wasLmbPressed = false;

   public KeyStrokes(Draggable draggable) {
      super(draggable);
   }

   @Override
   public void onRender(EventRender.Default eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      Font font = Fonts.getFont("suisse", 15);
      Font smallFont = Fonts.getFont("suisse", 10);
      float keySize = 20.0F;
      float gap = 2.0F;
      boolean wPressed = this.mc.field_1690.field_1894.method_1434();
      boolean aPressed = this.mc.field_1690.field_1913.method_1434();
      boolean sPressed = this.mc.field_1690.field_1881.method_1434();
      boolean dPressed = this.mc.field_1690.field_1849.method_1434();
      boolean spacePressed = this.mc.field_1690.field_1903.method_1434();
      boolean lmbPressed = this.mc.field_1690.field_1886.method_1434();
      boolean rmbPressed = this.mc.field_1690.field_1904.method_1434();
      long currentTime = System.currentTimeMillis();
      if (lmbPressed && !this.wasLmbPressed) {
         this.leftClicks.add(currentTime);
      }

      this.wasLmbPressed = lmbPressed;
      this.leftClicks.removeIf(timex -> currentTime - timex > 1000L);
      int lmbCps = this.leftClicks.size();
      float wX = x + keySize + gap;
      this.drawKey(eventRender, wX, y, keySize, keySize, "W", wPressed, font);
      float aY = y + keySize + gap;
      this.drawKey(eventRender, x, aY, keySize, keySize, "A", aPressed, font);
      float sX = x + keySize + gap;
      float sY = y + keySize + gap;
      this.drawKey(eventRender, sX, sY, keySize, keySize, "S", sPressed, font);
      float dX = x + (keySize + gap) * 2.0F;
      float dY = y + keySize + gap;
      this.drawKey(eventRender, dX, dY, keySize, keySize, "D", dPressed, font);
      float spaceWidth = keySize * 3.0F + gap * 2.0F;
      float spaceHeight = 20.0F;
      float spaceY = y + (keySize + gap) * 2.0F;
      this.drawKey(eventRender, x, spaceY, spaceWidth, spaceHeight, "Space", spacePressed, font);
      float mouseWidth = (spaceWidth - gap) / 2.0F;
      float mouseHeight = 20.0F;
      float lmbY = y + (keySize + gap) * 2.0F + spaceHeight + gap;
      float time = (float)(System.currentTimeMillis() % 2000L) / 2000.0F * 360.0F;
      int themeColor = ColorUtils.getThemeColor((int)time);
      this.drawKeyWithCps(eventRender, x, lmbY, mouseWidth, mouseHeight, "LMB", lmbPressed, font, smallFont, lmbCps, themeColor);
      float rmbX = x + mouseWidth + gap;
      float rmbY = y + (keySize + gap) * 2.0F + spaceHeight + gap;
      this.drawKey(eventRender, rmbX, rmbY, mouseWidth, mouseHeight, "RMB", rmbPressed, font);
      float totalWidth = keySize * 3.0F + gap * 2.0F;
      float totalHeight = keySize * 2.0F + gap + spaceHeight + gap + mouseHeight + gap;
      this.draggable.setWidth(totalWidth);
      this.draggable.setHeight(totalHeight);
      super.onRender(eventRender);
   }

   private void drawKey(EventRender.Default eventRender, float x, float y, float width, float height, String text, boolean pressed, Object font) {
      int bgColor = pressed ? ColorUtils.rgba(180, 180, 180, 200) : ColorUtils.rgba(25, 25, 25, 150);
      int textColor = pressed ? ColorUtils.rgba(0, 0, 0, 255) : ColorUtils.rgba(255, 255, 255, 255);
      RenderUtils.drawKeyStrokeRect(eventRender.getContext().method_51448(), x, y, width, height, 3.0F, bgColor);
      Font f = Fonts.getFont("suisse", 15);
      float textWidth = f.getWidth(text);
      float textHeight = 8.0F;
      float textX = x + (width - textWidth) / 2.0F;
      float textY = y + (height - textHeight) / 2.0F;
      f.draw(eventRender.getContext().method_51448(), text, textX - 0.5F, textY + 2.0F, textColor);
   }

   private void drawKeyWithCps(
      EventRender.Default eventRender,
      float x,
      float y,
      float width,
      float height,
      String text,
      boolean pressed,
      Object font,
      Object smallFont,
      int cps,
      int themeColor
   ) {
      int bgColor = pressed ? ColorUtils.rgba(180, 180, 180, 200) : ColorUtils.rgba(25, 25, 25, 150);
      int textColor = pressed ? ColorUtils.rgba(0, 0, 0, 255) : ColorUtils.rgba(255, 255, 255, 255);
      RenderUtils.drawKeyStrokeRect(eventRender.getContext().method_51448(), x, y, width, height, 3.0F, bgColor);
      Font f = Fonts.getFont("suisse", 15);
      Font sf = Fonts.getFont("suisse", 12);
      float textWidth = f.getWidth(text);
      float textX = x + (width - textWidth) / 2.0F;
      float textHeight = 8.0F;
      float textY = y + (height - textHeight) / 2.0F;
      f.draw(eventRender.getContext().method_51448(), text, textX - 0.5F, textY + 2.0F, textColor);
      String cpsText = "cps: " + cps;
      float cpsWidth = sf.getWidth(cpsText);
      float cpsX = x + (width - cpsWidth) / 2.0F;
      float cpsY = textY + 12.0F;
      sf.draw(eventRender.getContext().method_51448(), cpsText, cpsX, cpsY - 3.0F, themeColor);
   }
}
