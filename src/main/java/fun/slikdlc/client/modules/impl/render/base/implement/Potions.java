package fun.slikdlc.client.modules.impl.render.base.implement;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.api.utils.scissor.ScissorUtils;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1058;
import net.minecraft.class_1074;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_3532;
import net.minecraft.class_6880;

public class Potions extends InterfaceProcessing {
   private final Map<class_1291, AnimationUtils> animations = new LinkedHashMap<>();
   private final Map<class_1291, Potions.PotionSnapshot> snapshots = new HashMap<>();
   private final Map<class_1291, Integer> maxDurations = new HashMap<>();
   private final Set<class_1291> renderOrderSeen = new HashSet<>();
   private final AnimationUtils widthAnimation = new AnimationUtils(70.0F, 10.5F, Easings.QUAD_OUT);

   public Potions(Draggable draggable) {
      super(draggable);
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }

   private Font icon(int size) {
      return Fonts.getFont("icon", size);
   }

   private AnimationUtils getAnimation(class_1291 effect) {
      return this.animations.computeIfAbsent(effect, e -> new AnimationUtils(0.0F, 10.5F, Easings.QUAD_OUT));
   }

   private static String getLevelSuffix(int level) {
      return String.valueOf(Math.max(1, level));
   }

   private static String formatDuration(class_1293 effect) {
      return formatDuration(effect.method_5584(), effect.method_48559());
   }

   private static String formatDuration(int duration, boolean infinite) {
      if (infinite) {
         return "inf";
      } else {
         int seconds = Math.max(0, duration / 20);
         int minutes = seconds / 60;
         int secs = seconds % 60;
         return minutes + ":" + (secs < 10 ? "0" + secs : String.valueOf(secs));
      }
   }

   private void updateSnapshot(class_1293 effect) {
      class_1291 type = (class_1291)effect.method_5579().comp_349();
      Potions.PotionSnapshot snapshot = this.snapshots.computeIfAbsent(type, e -> new Potions.PotionSnapshot());
      snapshot.entry = effect.method_5579();
      snapshot.baseName = class_1074.method_4662(effect.method_5586(), new Object[0]);
      snapshot.amplifier = effect.method_5578() + 1;
      snapshot.duration = effect.method_5584();
      snapshot.infinite = effect.method_48559();
   }

   private List<class_1291> buildRenderOrder(Collection<class_1293> effects, Set<class_1291> active) {
      List<class_1291> order = new ArrayList<>();
      this.renderOrderSeen.clear();

      for (class_1293 effect : effects) {
         class_1291 type = (class_1291)effect.method_5579().comp_349();
         if (this.renderOrderSeen.add(type)) {
            order.add(type);
         }
      }

      for (class_1291 type : this.animations.keySet()) {
         if (!active.contains(type)) {
            order.add(type);
         }
      }

      return order;
   }

   private void drawEffectIcon(EventRender.Default eventRender, class_6880<class_1291> effect, float x, float y, int size, int alpha) {
      class_1058 sprite = mc.method_18505().method_18663(effect);
      int color = ColorUtils.rgba(255, 255, 255, alpha);
      RenderUtils.drawSprite(eventRender.getContext().method_51448(), sprite, x, y, size, color);
   }

   private void drawTextWithShadow(EventRender.Default eventRender, Font font, String text, float x, float y, int color) {
      int shadow = ColorUtils.rgba(20, 20, 20, 145);
      font.draw(eventRender.getContext().method_51448(), text, x + 0.8F, y + 0.8F, shadow);
      font.draw(eventRender.getContext().method_51448(), text, x, y, color);
   }

   @Override
   public void onRender(EventRender.Default eventRender) {
      if (ModuleClass.interfaceModule.style.is("Обычный")) {
         this.DefaultStyle(eventRender);
      } else {
         this.WaveStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   public void DefaultStyle(EventRender.Default eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int colorTheme;
      if (!SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         colorTheme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         colorTheme = ColorUtils.getThemeColor();
      }

      float targetWidth = 70.0F;
      float targetHeight = 16.0F;
      int visibleCount = 0;
      Collection<class_1293> effects = (Collection<class_1293>)(mc != null && mc.field_1724 != null ? mc.field_1724.method_6026() : List.of());
      Set<class_1291> active = new HashSet<>();

      for (class_1293 effect : effects) {
         class_1291 type = (class_1291)effect.method_5579().comp_349();
         active.add(type);
         this.getAnimation(type).update(1.0F);
         this.updateSnapshot(effect);
         int duration = effect.method_5584();
         Integer prevMax = this.maxDurations.get(type);
         if (prevMax == null || duration > prevMax) {
            this.maxDurations.put(type, duration);
         }
      }

      for (Entry<class_1291, AnimationUtils> entry : this.animations.entrySet()) {
         if (!active.contains(entry.getKey())) {
            entry.getValue().update(0.0F);
         }
      }

      List<class_1291> renderOrder = this.buildRenderOrder(effects, active);

      for (class_1291 type : renderOrder) {
         AnimationUtils anim = this.getAnimation(type);
         float animValue = anim.getValue();
         Potions.PotionSnapshot snapshot = this.snapshots.get(type);
         if (animValue > 0.01F && snapshot != null) {
            visibleCount++;
            String baseName = snapshot.baseName != null ? snapshot.baseName : class_1074.method_4662(type.method_5567(), new Object[0]);
            String levelSuffix = getLevelSuffix(snapshot.amplifier);
            String time = formatDuration(snapshot.duration, snapshot.infinite);
            float nameWidth = this.issue(12).getWidth(baseName);
            if (!levelSuffix.isEmpty()) {
               nameWidth += this.issue(11).getWidth(" LVL") + this.issue(12).getWidth(levelSuffix);
            }

            float timeWidth = this.issue(10).getWidth(time) + 6.0F;
            float rowWidth = nameWidth + timeWidth + 25.0F + 9.0F + 9.0F;
            if (rowWidth > targetWidth) {
               targetWidth = rowWidth;
            }

            targetHeight += 12.0F * animValue;
         }
      }

      if (visibleCount > 0) {
         targetHeight += 2.0F;
      }

      this.widthAnimation.update(targetWidth);
      float width = this.widthAnimation.getValue();
      float height = targetHeight;
      RenderUtils.drawDefaultHudElementRects(eventRender.getContext().method_51448(), x, y, width, targetHeight, colorTheme, this.isUnusualRectType());
      this.issue(14).draw(eventRender.getContext().method_51448(), "Effects", x + 5.0F, y + 6.0F, -1);
      this.icon(13).draw(eventRender.getContext().method_51448(), "d", x + width - 12.5F, y + 7.5F, colorTheme);
      float offsetY = 18.0F;

      for (class_1291 typex : renderOrder) {
         AnimationUtils anim = this.getAnimation(typex);
         float animValue = anim.getValue();
         Potions.PotionSnapshot snapshot = this.snapshots.get(typex);
         if (animValue > 0.01F && snapshot != null) {
            ScissorUtils.push();
            ScissorUtils.setFromComponentCoordinates((double)x, (double)y, (double)width, (double)height);
            int alpha = (int)(255.0F * animValue);
            int textColor = ColorUtils.rgba(255, 255, 255, alpha);
            int grayColor = ColorUtils.rgba(55, 55, 55, alpha);
            int darkColor = ColorUtils.rgba(35, 35, 35, alpha);
            float iconSize = 7.0F;
            float iconX = x + 5.0F;
            float iconY = y + offsetY;
            if (snapshot.entry != null) {
               this.drawEffectIcon(eventRender, snapshot.entry, iconX, iconY, (int)iconSize, alpha);
            }

            String baseNamex = snapshot.baseName != null ? snapshot.baseName : class_1074.method_4662(typex.method_5567(), new Object[0]);
            String levelSuffixx = getLevelSuffix(snapshot.amplifier);
            float textX = iconX + iconSize + 3.0F;
            float textY = y + 2.0F + offsetY;
            this.issue(12).draw(eventRender.getContext().method_51448(), baseNamex, textX, textY, textColor);
            if (!levelSuffixx.isEmpty()) {
               float baseWidth = this.issue(12).getWidth(baseNamex);
               int levelThemeColor = ColorUtils.setAlphaColor(colorTheme, alpha);
               float lvlX = textX + baseWidth;
               this.issue(10).draw(eventRender.getContext().method_51448(), " LVL", lvlX, textY + 1.0F, levelThemeColor);
               this.issue(11)
                  .draw(eventRender.getContext().method_51448(), levelSuffixx, (double)(lvlX + this.issue(11).getWidth(" LVL")), textY + 0.5, levelThemeColor);
            }

            String timex = formatDuration(snapshot.duration, snapshot.infinite);
            float timeBoxWidth = Math.max(this.issue(10).getWidth(timex) + 4.0F, 12.0F);
            float ringSize = 6.0F;
            float ringGap = 3.0F;
            float timeBoxX = x + width - timeBoxWidth - 5.0F;
            float ringX = timeBoxX - ringGap - ringSize;
            float ringY = y + offsetY + 0.3F;
            RenderUtils.drawDefaultHudInfoBox(eventRender.getContext().method_51448(), timeBoxX, y + offsetY, timeBoxWidth, grayColor, darkColor);
            this.issue(10).drawCenteredString(eventRender.getContext().method_51448(), timex, timeBoxX + timeBoxWidth / 2.0F, y + offsetY + 3.0F, textColor);
            float progress = 1.0F;
            if (!snapshot.infinite) {
               int currentDuration = snapshot.duration;
               int maxDuration = this.maxDurations.getOrDefault(typex, currentDuration);
               if (maxDuration > 0) {
                  progress = class_3532.method_15363((float)currentDuration / maxDuration, 0.0F, 1.0F);
               } else {
                  progress = 0.0F;
               }
            }

            int ringColor = ColorUtils.setAlphaColor(colorTheme, alpha);
            float thickness = 1.75F;
            RenderUtils.drawRingArc(eventRender.getContext().method_51448(), ringX, ringY, ringSize, thickness, -90.0F, 270.0F, grayColor);
            if (progress > 0.0F) {
               float endAngle = -90.0F + 360.0F * progress;
               RenderUtils.drawRingArc(eventRender.getContext().method_51448(), ringX, ringY, ringSize, thickness, -90.0F, endAngle, ringColor);
            }

            offsetY += 12.0F * animValue;
            ScissorUtils.pop();
            ScissorUtils.unset();
         }
      }

      this.animations.entrySet().removeIf(entryx -> !active.contains(entryx.getKey()) && entryx.getValue().getValue() <= 0.01F);
      this.snapshots.keySet().removeIf(typexx -> !this.animations.containsKey(typexx));
      this.maxDurations.keySet().removeIf(typexx -> !this.animations.containsKey(typexx));
      this.draggable.setWidth(width);
      this.draggable.setHeight(height);
   }

   public void WaveStyle(EventRender.Default eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int time = (int)((float)(System.currentTimeMillis() % 2000L) / 2000.0F * 360.0F);
      int leftTop = ColorUtils.getThemeColor(time);
      int leftBottom = ColorUtils.getThemeColor(time + 30);
      int centerTop = ColorUtils.getThemeColor(time + 90);
      int centerBottom = ColorUtils.getThemeColor(time + 120);
      int rightTop = ColorUtils.getThemeColor(time + 180);
      int rightBottom = ColorUtils.getThemeColor(time + 210);
      Collection<class_1293> effects = (Collection<class_1293>)(mc != null && mc.field_1724 != null ? mc.field_1724.method_6026() : List.of());
      Set<class_1291> active = new HashSet<>();

      for (class_1293 effect : effects) {
         class_1291 type = (class_1291)effect.method_5579().comp_349();
         active.add(type);
         this.getAnimation(type).update(1.0F);
      }

      for (Entry<class_1291, AnimationUtils> entry : this.animations.entrySet()) {
         if (!active.contains(entry.getKey())) {
            entry.getValue().update(0.0F);
         }
      }

      float width = 84.0F;
      float height = 18.0F;
      int visibleEffects = 0;

      for (class_1293 effect : effects) {
         AnimationUtils anim = this.getAnimation((class_1291)effect.method_5579().comp_349());
         float animValue = anim.getValue();
         if (!(animValue <= 0.01F)) {
            visibleEffects++;
            String baseName = class_1074.method_4662(effect.method_5586(), new Object[0]);
            String levelSuffix = getLevelSuffix(effect.method_5578() + 1);
            String line = baseName + (levelSuffix.isEmpty() ? "" : " > " + levelSuffix);
            width = Math.max(width, this.issue(16).getWidth(line) + 38.0F);
            width = Math.max(width, this.issue(15).getWidth(formatDuration(effect)) + 38.0F);
            height += 18.0F * animValue;
         }
      }

      if (visibleEffects == 0) {
         float headerHeight = 18.0F;
         RenderUtils.drawWaveHudHeader(
            eventRender.getContext().method_51448(),
            x,
            y,
            width,
            15.0F,
            0.0F,
            10.0F,
            10.0F,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
         String title = "potions";
         float titleX = x + (width - this.issue(16).getWidth(title)) / 2.0F;
         this.drawTextWithShadow(eventRender, this.issue(16), title, titleX, y + 5.0F, -1);
         this.draggable.setWidth(width);
         this.draggable.setHeight(headerHeight);
      } else {
         RenderUtils.drawWaveHudPanel(
            eventRender.getContext().method_51448(),
            x,
            y,
            width,
            height,
            ColorUtils.rgba(25, 25, 25, 150),
            15.0F,
            0.0F,
            10.0F,
            10.0F,
            leftTop,
            leftBottom,
            centerTop,
            centerBottom,
            rightTop,
            rightBottom
         );
         String title = "potions";
         float titleX = x + (width - this.issue(16).getWidth(title)) / 1.9F;
         this.drawTextWithShadow(eventRender, this.issue(16), title, titleX, y + 5.0F, -1);
         float yOffset = 20.0F;

         for (class_1293 effectx : effects) {
            AnimationUtils anim = this.getAnimation((class_1291)effectx.method_5579().comp_349());
            float animValue = anim.getValue();
            if (!(animValue <= 0.01F)) {
               ScissorUtils.push();
               ScissorUtils.setFromComponentCoordinates((double)x, (double)y, (double)width, (double)height);
               int alpha = (int)(255.0F * animValue);
               int textColor = ColorUtils.rgba(255, 255, 255, alpha);
               int levelColor = ColorUtils.rgba(20, 185, 45, alpha);
               float iconX = x + 5.0F;
               float iconY = y + yOffset;
               this.drawEffectIcon(eventRender, effectx.method_5579(), iconX, iconY, 11, alpha);
               String baseName = class_1074.method_4662(effectx.method_5586(), new Object[0]).toLowerCase();
               String levelSuffix = getLevelSuffix(effectx.method_5578() + 1);
               float textX = iconX + 14.0F;
               this.issue(15).draw(eventRender.getContext().method_51448(), baseName + " >", textX, y + yOffset - 1.0F, textColor);
               if (!levelSuffix.isEmpty()) {
                  float nameW = this.issue(14).getWidth(baseName + " >");
                  this.issue(14)
                     .draw(eventRender.getContext().method_51448(), " " + levelSuffix, (double)(textX + nameW + 2.0F), y + yOffset - 0.5, levelColor);
               }

               this.issue(14).draw(eventRender.getContext().method_51448(), formatDuration(effectx), (double)textX, y + yOffset + 7.5, textColor);
               yOffset += 18.0F * animValue;
               ScissorUtils.pop();
               ScissorUtils.unset();
            }
         }

         this.draggable.setWidth(width);
         this.draggable.setHeight(height);
      }
   }

   private static final class PotionSnapshot {
      class_6880<class_1291> entry;
      String baseName;
      int amplifier;
      int duration;
      boolean infinite;

      private PotionSnapshot() {
      }
   }
}
