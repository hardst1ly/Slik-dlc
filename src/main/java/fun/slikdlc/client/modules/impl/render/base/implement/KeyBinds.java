package fun.slikdlc.client.modules.impl.render.base.implement;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.input.KeyBoardUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.api.utils.scissor.ScissorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_4587;

public class KeyBinds extends InterfaceProcessing {
   private final Map<Module, AnimationUtils> animations = new HashMap<>();
   private final AnimationUtils widthAnimation = new AnimationUtils(60.0F, 10.5F, Easings.QUAD_OUT);
   private static final Map<Character, Character> RU_TO_EN = new HashMap<>();

   public KeyBinds(Draggable draggable) {
      super(draggable);
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }

   private Font icon(int size) {
      return Fonts.getFont("icon1", size);
   }

   private AnimationUtils getAnimation(Module module) {
      return this.animations.computeIfAbsent(module, m -> new AnimationUtils(0.0F, 10.5F, Easings.QUAD_OUT));
   }

   private String toEnglish(String text) {
      StringBuilder result = new StringBuilder();

      for (char c : text.toCharArray()) {
         result.append(RU_TO_EN.getOrDefault(c, c));
      }

      return result.toString();
   }

   private int getStaticThemeColor() {
      int[] colors = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getColor();
      if (colors != null && colors.length != 0) {
         int color = colors[0];
         if ((color >> 24 & 0xFF) == 0) {
            color = color & 16777215 | 0xFF000000;
         }

         return color;
      } else {
         return -1;
      }
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
      float baseX = this.draggable.getX();
      float y = this.draggable.getY();
      int colorTheme;
      if (!SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         colorTheme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         colorTheme = ColorUtils.getThemeColor();
      }

      int staticAccentColor = this.getStaticThemeColor();
      float targetWidth = 64.0F;
      float targetHeight = 16.0F;
      int visibleCount = 0;
      ObjectListIterator width = ModuleClass.INSTANCE.getObject().iterator();

      while (width.hasNext()) {
         Module module = (Module)width.next();
         if (module.getKey() != -1) {
            AnimationUtils anim = this.getAnimation(module);
            anim.update(module.isEnable() ? 1.0F : 0.0F);
         }
      }

      width = ModuleClass.INSTANCE.getObject().iterator();

      while (width.hasNext()) {
         Module module = (Module)width.next();
         if (module.getKey() != -1) {
            AnimationUtils anim = this.getAnimation(module);
            float animValue = anim.getValue();
            if (animValue > 0.01F) {
               visibleCount++;
               String keyName = this.toEnglish(KeyBoardUtils.getKeyName(module.getKey()));
               float keyWidth = this.issue(10).getWidth(keyName);
               float moduleWidth = this.issue(12).getWidth(module.getDisplayName()) + keyWidth + 25.0F;
               if (moduleWidth > targetWidth) {
                  targetWidth = moduleWidth;
               }

               targetHeight += 12.0F * animValue;
            }
         }
      }

      if (visibleCount > 0) {
         targetHeight += 2.0F;
      }

      this.widthAnimation.update(targetWidth);
      float widthx = this.widthAnimation.getValue() + 7.0F;
      float height = targetHeight;
      float rightEdge = baseX + 60.0F;
      float x = rightEdge - widthx;
      RenderUtils.drawDefaultHudElementRects(eventRender.getContext().method_51448(), x, y, widthx, targetHeight, colorTheme, this.isUnusualRectType());
      this.issue(14).draw(eventRender.getContext().method_51448(), "Binds", x + 5.0F, y + 6.0F, -1);
      this.icon(13).draw(eventRender.getContext().method_51448(), "f", rightEdge - 13.0F, y + 7.5F, colorTheme);
      float offsetY = 18.0F;
      ObjectListIterator var34 = ModuleClass.INSTANCE.getObject().iterator();

      while (var34.hasNext()) {
         Module module = (Module)var34.next();
         if (module.getKey() != -1) {
            AnimationUtils anim = this.getAnimation(module);
            float animValue = anim.getValue();
            if (animValue > 0.01F) {
               ScissorUtils.push();
               ScissorUtils.setFromComponentCoordinates((double)x, (double)y, (double)widthx, (double)height);
               String keyName = this.toEnglish(KeyBoardUtils.getBindName(module.getKey()));
               float keyBoxWidth = Math.max(this.issue(10).getWidth(keyName) + 4.0F, 9.0F);
               int alpha = (int)(255.0F * animValue);
               int textColor = ColorUtils.rgba(255, 255, 255, alpha);
               int accentColor = ColorUtils.setAlphaColor(this.getStableThemeColor(), alpha);
               int grayColor = ColorUtils.rgba(55, 55, 55, alpha);
               int darkColor = ColorUtils.rgba(35, 35, 35, alpha);
               this.issue(12).draw(eventRender.getContext().method_51448(), module.getDisplayName(), x + 12.0F, y + 2.0F + offsetY, textColor);
               RenderUtils.drawRoundedRect(eventRender.getContext().method_51448(), x + 5.2F, y + offsetY + 0.3F, 2.55F, 5.7F, 0.15F, accentColor);
               float keyBoxX = rightEdge - keyBoxWidth - 5.0F;
               RenderUtils.drawDefaultHudInfoBox(eventRender.getContext().method_51448(), keyBoxX, y + offsetY, keyBoxWidth, grayColor, darkColor);
               this.issue(10).drawCenteredString(eventRender.getContext().method_51448(), keyName, keyBoxX + keyBoxWidth / 2.0F, y + offsetY + 2.8F, textColor);
               offsetY += 12.0F * animValue;
               ScissorUtils.pop();
               ScissorUtils.unset();
            }
         }
      }

      this.draggable.setWidth(60.0F);
      this.draggable.setHeight(height);
   }

   private int getStableThemeColor() {
      return !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ColorUtils.getThemeColor();
   }

   public void WaveStyle(EventRender.Default eventRender) {
      class_4587 context = eventRender.getContext().method_51448();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      int time = (int)((float)(System.currentTimeMillis() % 2000L) / 2000.0F * 360.0F);
      int leftTop = ColorUtils.getThemeColor(time);
      int leftBottom = ColorUtils.getThemeColor(time + 30);
      int centerTop = ColorUtils.getThemeColor(time + 90);
      int centerBottom = ColorUtils.getThemeColor(time + 120);
      int rightTop = ColorUtils.getThemeColor(time + 180);
      int rightBottom = ColorUtils.getThemeColor(time + 210);
      List<Module> activeModules = new ArrayList<>();
      ObjectListIterator targetWidth = ModuleClass.INSTANCE.getObject().iterator();

      while (targetWidth.hasNext()) {
         Module module = (Module)targetWidth.next();
         if (module.getKey() <= 0) {
            module.getAnimka().update(0.0F);
         } else {
            module.getAnimka().update(module.isEnable() ? 1.0F : 0.0F);
            if (module.getAnimka().getValue() > 0.01F) {
               activeModules.add(module);
            }
         }
      }

      float targetWidthx = 84.0F;
      float height = 18.0F;
      int visibleModules = 0;

      for (Module module : activeModules) {
         float animValue = module.getAnimka().getValue();
         if (!(animValue <= 0.01F)) {
            visibleModules++;
            String line = module.getDisplayName().toLowerCase() + " >> toggle";
            targetWidthx = Math.max(targetWidthx, this.issue(14).getWidth(line) + 7.0F);
            height += 12.0F * animValue;
         }
      }

      this.widthAnimation.update(targetWidthx);
      float animatedWidth = this.widthAnimation.getValue();
      if (visibleModules == 0) {
         float headerHeight = 18.0F;
         RenderUtils.drawWaveHudHeader(
            context, x, y, animatedWidth, 15.0F, 0.0F, 10.0F, 10.0F, leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom
         );
         String title = "keybinds";
         float titleX = x + (animatedWidth - this.issue(15).getWidth(title)) / 2.0F;
         this.issue(15).drawStringWithShadow(eventRender.getContext().method_51448(), title, titleX, y + 5.0F, -1);
         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(headerHeight);
      } else {
         RenderUtils.drawWaveHudPanel(
            context,
            x,
            y,
            animatedWidth,
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
         String title = "keybinds";
         float titleX = x + (animatedWidth - this.issue(15).getWidth(title)) / 2.0F;
         this.issue(15).drawStringWithShadow(eventRender.getContext().method_51448(), title, titleX, y + 5.0F, -1);
         float yOffset = 18.0F;

         for (Module modulex : activeModules) {
            float animValue = modulex.getAnimka().getValue();
            if (!(animValue <= 0.01F)) {
               ScissorUtils.push();
               ScissorUtils.setFromComponentCoordinates((double)x, (double)y, (double)animatedWidth, (double)height);
               int alpha = (int)(255.0F * animValue);
               int textColor = ColorUtils.rgba(255, 255, 255, alpha);
               String text = modulex.getDisplayName().toLowerCase() + " >> toggle";
               float textX = x + 5.5F;
               this.issue(14).draw(context, text, textX, y + yOffset + 2.0F, textColor);
               yOffset += 12.0F * animValue;
               ScissorUtils.unset();
               ScissorUtils.pop();
            }
         }

         this.draggable.setWidth(animatedWidth);
         this.draggable.setHeight(height);
      }
   }

   static {
      String ru = "йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ";
      String en = "qwertyuiop[]asdfghjkl;'zxcvbnm,.QWERTYUIOP[]ASDFGHJKL;'ZXCVBNM,.";
      int length = Math.min(ru.length(), en.length());

      for (int i = 0; i < length; i++) {
         RU_TO_EN.put(ru.charAt(i), en.charAt(i));
      }
   }
}
