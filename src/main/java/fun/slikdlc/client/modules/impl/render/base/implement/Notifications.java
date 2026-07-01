package fun.slikdlc.client.modules.impl.render.base.implement;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.notification.NotificationManager;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_408;
import net.minecraft.class_4587;

public class Notifications extends InterfaceProcessing {
   private static final float DEFAULT_PAD_X = 7.0F;
   private static final float DEFAULT_ICON_TEXT_GAP = 1.0F;
   private static final float PREVIEW_ICON_TEXT_GAP = 2.0F;
   private final Map<NotificationManager.Entry, AnimationUtils> appearAnimations = new HashMap<>();
   private final Map<NotificationManager.Entry, Float> currentYPositions = new HashMap<>();
   private final Set<NotificationManager.Entry> activeEntriesScratch = new HashSet<>();
   private long lastRenderTime = System.currentTimeMillis();
   private float previewAlpha = 0.0F;

   public Notifications(Draggable draggable) {
      super(draggable);
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }

   private Font icons(int size) {
      return Fonts.getFont("icon", size);
   }

   private Font iconNew(int size) {
      return Fonts.getFont("icon", size);
   }

   private String getEntryText(NotificationManager.Entry entry) {
      if (entry.isCustom()) {
         return entry.customText;
      } else {
         String state = entry.enabled ? "Включен!" : "Выключен!";
         return entry.moduleName + " " + state;
      }
   }

   private String getWaveBodyText(NotificationManager.Entry entry) {
      return entry.isCustom() ? entry.customText : "Module '" + entry.moduleName + "' is " + (entry.enabled ? "enabled." : "disabled.");
   }

   private float getDefaultEntryWidth(NotificationManager.Entry entry, float padX) {
      String text = this.getEntryText(entry);
      String iconGlyph = entry.categoryIcon != null && !entry.categoryIcon.isEmpty() ? entry.categoryIcon : "?";
      return this.issue(13).getWidth(text) + this.icons(14).getWidth(iconGlyph) + padX * 2.0F + 1.0F;
   }

   private float getPreviewWidth(String previewText, String previewIconGlyph, float padX) {
      return this.issue(13).getWidth(previewText) + this.icons(16).getWidth(previewIconGlyph) + padX * 2.0F + 2.0F;
   }

   @Override
   public void onRender(EventRender.Default eventRender) {
      if (!ModuleClass.interfaceModule.style.is("Wave")) {
         this.DefaultStyle(eventRender);
      } else {
         this.WaveStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   private void DefaultStyle(EventRender.Default eventRender) {
      if (mc != null) {
         long currentTime = System.currentTimeMillis();
         float deltaTime = (float)(currentTime - this.lastRenderTime) / 1000.0F;
         this.lastRenderTime = currentTime;
         List<NotificationManager.Entry> entries = NotificationManager.getActive();
         boolean isChatOpen = mc.field_1755 instanceof class_408;
         boolean shouldRender = !entries.isEmpty() || isChatOpen;
         float targetPreviewAlpha = isChatOpen ? 0.7F : 0.0F;
         float alphaSpeed = 8.0F;
         this.previewAlpha = this.previewAlpha + (targetPreviewAlpha - this.previewAlpha) * Math.min(1.0F, alphaSpeed * deltaTime);
         if (!shouldRender && this.previewAlpha < 0.01F) {
            this.appearAnimations.clear();
            this.currentYPositions.clear();
            this.previewAlpha = 0.0F;
         } else {
            float baseX = this.draggable.getX();
            float baseY = this.draggable.getY();
            int colorTheme;
            if (!SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
               colorTheme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0];
            } else {
               colorTheme = ColorUtils.getThemeColor();
            }

            boolean drawSquares = this.isUnusualRectType();
            long now = System.currentTimeMillis();
            float height = 16.0F;
            float spacing = 3.0F;
            float lerpSpeed = 12.0F;
            float padX = 7.0F;
            String previewText = "Кликни на меня для открытия настроек!";
            String previewIconGlyph = "A";
            float previewIconW = this.icons(16).getWidth(previewIconGlyph);
            float previewWidth = this.getPreviewWidth(previewText, previewIconGlyph, padX);
            float maxWidth = previewWidth;

            for (NotificationManager.Entry entry : entries) {
               float width = this.getDefaultEntryWidth(entry, padX);
               if (width > maxWidth) {
                  maxWidth = width;
               }
            }

            float targetY = baseY;
            if (this.previewAlpha > 0.01F) {
               float x = baseX + (maxWidth - previewWidth) * 0.5F;
               float alpha = this.previewAlpha;
               float scale = 0.86F + 0.14F * alpha;
               int base = ColorUtils.setAlphaColor(ColorUtils.rgba(50, 50, 50, 255), (int)(255.0F * alpha));
               int top = ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.15F), (int)(255.0F * alpha));
               int bottom = ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.05F), (int)(255.0F * alpha));
               float cx = x + previewWidth * 0.5F;
               float cy = baseY + height * 0.5F;
               class_4587 ms = eventRender.getContext().method_51448();
               ms.method_22903();
               ms.method_46416(cx, cy, 0.0F);
               ms.method_22905(scale, scale, 1.0F);
               ms.method_46416(-cx, -cy, 0.0F);
               RenderUtils.drawDefaultHudPanel(ms, x, baseY, previewWidth, height, 3.0F, 3.5F, base, top, bottom);
               float squareAlpha = alpha * alpha * (3.0F - 2.0F * alpha);
               if (drawSquares && squareAlpha > 0.08F) {
                  RenderUtils.drawHudSquarePattern(ms, x, baseY, previewWidth, height, ColorUtils.setAlphaColor(colorTheme, (int)(255.0F * squareAlpha)));
               }

               int textColor = ColorUtils.setAlphaColor(-1, (int)(255.0F * alpha));
               int iconColor = ColorUtils.setAlphaColor(colorTheme, (int)(255.0F * alpha));
               this.icons(16).draw(ms, previewIconGlyph, x + padX - 3.5F, baseY + 6.6F, iconColor);
               this.issue(13).draw(ms, previewText, x + padX + previewIconW + 5.5F, baseY + 6.6F, textColor);
               ms.method_22909();
               targetY = baseY + (height + spacing);
            }

            for (NotificationManager.Entry entryx : entries) {
               AnimationUtils anim = this.appearAnimations.computeIfAbsent(entryx, e -> new AnimationUtils(0.0F, 12.0F, Easings.QUAD_OUT));
               long age = now - entryx.startTime;
               anim.update(1.0F);
               float appear = anim.getValue();
               float alpha = appear;
               if (age > 2300L) {
                  alpha = (1.0F - (float)(age - 2300L) / 200.0F) * appear;
               }

               if (alpha <= 0.0F) {
                  targetY += height + spacing;
               } else {
                  Float currentY = this.currentYPositions.get(entryx);
                  if (currentY == null) {
                     currentY = targetY;
                  }

                  float diff = targetY - currentY;
                  if (Math.abs(diff) > 0.01F) {
                     currentY = currentY + diff * Math.min(1.0F, lerpSpeed * deltaTime);
                  } else {
                     currentY = targetY;
                  }

                  this.currentYPositions.put(entryx, currentY);
                  String text = this.getEntryText(entryx);
                  String iconGlyph = entryx.categoryIcon != null && !entryx.categoryIcon.isEmpty() ? entryx.categoryIcon : "?";
                  float iconW = this.icons(14).getWidth(iconGlyph);
                  float width = this.getDefaultEntryWidth(entryx, padX);
                  float x = baseX + (maxWidth - width) * 0.5F;
                  float slide = 6.0F * (1.0F - appear);
                  float renderY = currentY + slide;
                  float scale = 0.86F + 0.14F * alpha;
                  boolean disabled = !entryx.isCustom() && !entryx.enabled;
                  int disabledRed = ColorUtils.rgba(200, 55, 55, 255);
                  int base = ColorUtils.setAlphaColor(ColorUtils.rgba(50, 50, 50, 255), (int)(255.0F * alpha));
                  int top = ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.15F), (int)(255.0F * alpha));
                  int bottom = ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.05F), (int)(255.0F * alpha));
                  float cx = x + width * 0.5F;
                  float cy = renderY + height * 0.5F;
                  class_4587 ms = eventRender.getContext().method_51448();
                  ms.method_22903();
                  ms.method_46416(cx, cy, 0.0F);
                  ms.method_22905(scale, scale, 1.0F);
                  ms.method_46416(-cx, -cy, 0.0F);
                  RenderUtils.drawDefaultHudPanel(ms, x, renderY, width, height, 3.0F, 3.5F, base, top, bottom);
                  float squareAlpha = alpha * alpha * (3.0F - 2.0F * alpha);
                  if (drawSquares && squareAlpha > 0.08F) {
                     RenderUtils.drawHudSquarePattern(ms, x, renderY, width, height, ColorUtils.setAlphaColor(colorTheme, (int)(255.0F * squareAlpha)));
                  }

                  int textColor = ColorUtils.setAlphaColor(-1, (int)(255.0F * alpha));
                  int iconColor = ColorUtils.setAlphaColor(colorTheme, (int)(255.0F * alpha));
                  this.icons(14).draw(ms, iconGlyph, x + padX - 1.5F, renderY + 7.3F, iconColor);
                  float textX = x + padX + iconW + 1.0F;
                  if (!entryx.isCustom()) {
                     String modulePart = entryx.moduleName + " ";
                     String statePart = text.length() > modulePart.length() ? text.substring(modulePart.length()) : "";
                     int stateColor = disabled ? disabledRed : iconColor;
                     this.issue(13).draw(ms, modulePart, textX + 2.0F, renderY + 6.8F, textColor);
                     this.issue(13).draw(ms, statePart, textX + this.issue(13).getWidth(modulePart) - 0.5F + 2.0F, renderY + 7.0F, stateColor);
                  } else {
                     this.issue(13).draw(ms, text, textX, renderY + 6.8F, textColor);
                  }

                  ms.method_22909();
                  targetY += height + spacing;
               }
            }

            this.activeEntriesScratch.clear();
            this.activeEntriesScratch.addAll(entries);
            this.appearAnimations.keySet().removeIf(entryx -> !this.activeEntriesScratch.contains(entryx));
            this.currentYPositions.keySet().removeIf(entryx -> !this.activeEntriesScratch.contains(entryx));
            this.draggable.setWidth(maxWidth);
            this.draggable.setHeight(Math.max(1.0F, targetY - baseY));
         }
      }
   }

   private void WaveStyle(EventRender.Default eventRender) {
      if (mc != null) {
         long currentTime = System.currentTimeMillis();
         float deltaTime = (float)(currentTime - this.lastRenderTime) / 1000.0F;
         this.lastRenderTime = currentTime;
         List<NotificationManager.Entry> entries = NotificationManager.getActive();
         if (entries.isEmpty()) {
            this.appearAnimations.clear();
            this.currentYPositions.clear();
         } else {
            int time = (int)((float)(System.currentTimeMillis() % 2000L) / 2000.0F * 360.0F);
            int leftTop = ColorUtils.getThemeColor(time);
            int leftBottom = ColorUtils.getThemeColor(time + 30);
            int centerTop = ColorUtils.getThemeColor(time + 90);
            int centerBottom = ColorUtils.getThemeColor(time + 120);
            int rightTop = ColorUtils.getThemeColor(time + 180);
            int rightBottom = ColorUtils.getThemeColor(time + 210);
            long now = System.currentTimeMillis();
            float spacing = 4.0F;
            float lerpSpeed = 14.0F;
            float screenW = mc.method_22683().method_4486();
            float screenH = mc.method_22683().method_4502();
            float rightPadding = 5.0F;
            float bottomPadding = 5.0F;
            float stackOffset = 0.0F;
            float maxWidth = 120.0F;

            for (NotificationManager.Entry entry : entries) {
               String title = "Notify";
               String body = this.getWaveBodyText(entry);
               float iconW = this.iconNew(14).getWidth("j");
               float titleW = this.issue(15).getWidth(title);
               float bodyW = this.issue(13).getWidth(body);
               float width = Math.max(120.0F, Math.max(titleW + iconW + 18.0F, bodyW + 14.0F));
               maxWidth = Math.max(maxWidth, width);
            }

            for (NotificationManager.Entry entry : entries) {
               AnimationUtils anim = this.appearAnimations.computeIfAbsent(entry, e -> new AnimationUtils(0.0F, 12.0F, Easings.QUAD_OUT));
               anim.update(1.0F);
               float appear = anim.getValue();
               long age = now - entry.startTime;
               float alphaMul = appear;
               if (age > 2300L) {
                  alphaMul = (1.0F - (float)(age - 2300L) / 200.0F) * appear;
               }

               if (!(alphaMul <= 0.01F)) {
                  String title = "Notify";
                  String body = this.getWaveBodyText(entry);
                  String warningGlyph = "j";
                  float iconW = this.iconNew(14).getWidth(warningGlyph);
                  float titleW = this.issue(15).getWidth(title);
                  float bodyW = this.issue(13).getWidth(body);
                  float width = Math.max(120.0F, Math.max(titleW + iconW + 18.0F, bodyW + 14.0F));
                  float height = 24.0F;
                  float x = screenW - width - rightPadding;
                  float targetY = screenH - bottomPadding - height - stackOffset;
                  Float currentY = this.currentYPositions.get(entry);
                  if (currentY == null) {
                     currentY = targetY;
                  }

                  currentY = currentY + (targetY - currentY) * Math.min(1.0F, lerpSpeed * deltaTime);
                  this.currentYPositions.put(entry, currentY);
                  float y = currentY;
                  float scale = 0.86F + 0.14F * alphaMul;
                  int bg = ColorUtils.rgba(25, 25, 25, (int)(150.0F * alphaMul));
                  int txt = ColorUtils.setAlphaColor(-1, (int)(255.0F * alphaMul));
                  int iconCol = ColorUtils.setAlphaColor(ColorUtils.rgba(235, 0, 0, 255), (int)(255.0F * alphaMul));
                  float cx = x + width * 0.5F;
                  float cy = y + height * 0.5F;
                  class_4587 ms = eventRender.getContext().method_51448();
                  ms.method_22903();
                  ms.method_46416(cx, cy, 0.0F);
                  ms.method_22905(scale, scale, 1.0F);
                  ms.method_46416(-cx, -cy, 0.0F);
                  RenderUtils.drawWaveHudPanel(
                     ms, x, y, width, height - 1.5F, bg, 3.5F, 0.0F, 10.0F, 10.0F, leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom
                  );
                  this.iconNew(28).draw(ms, warningGlyph, x + 3.0F, y + 8.0F, iconCol);
                  this.issue(15).draw(ms, title, x + 19.0F, y + 6.5F, txt);
                  this.issue(13).draw(ms, body, x + 19.0F, y + 15.0F, txt);
                  ms.method_22909();
                  stackOffset += (height + spacing) * appear;
               }
            }

            this.appearAnimations.keySet().removeIf(entryx -> !entries.contains(entryx));
            this.currentYPositions.keySet().removeIf(entryx -> !entries.contains(entryx));
            this.draggable.setWidth(0.0F);
            this.draggable.setHeight(0.0F);
         }
      }
   }
}
