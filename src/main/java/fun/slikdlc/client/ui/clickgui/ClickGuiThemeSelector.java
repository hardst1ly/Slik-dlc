package fun.slikdlc.client.ui.clickgui;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.storages.implement.ThemeStorage;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.math.HoveringUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import java.util.List;
import net.minecraft.class_1041;
import net.minecraft.class_332;

public class ClickGuiThemeSelector {
   public ClickGuiThemeSelector() {
   }

   public void render(class_332 context, class_1041 window, float offsetY, float alphaMul, int shadeColor) {
      if (context != null && window != null) {
         List<ThemeStorage.Themes> themes = SlikDlc.INSTANCE.themeStorage.getThemeList();
         if (themes != null && !themes.isEmpty()) {
            float totalWidth = themes.size() * 8.0F + (themes.size() - 1) * 4.0F;
            float panelWidth = totalWidth + 8.0F;
            float panelX = this.getThemePanelX(window, panelWidth);
            float panelY = 100.0F + offsetY;
            float startX = panelX + 4.0F;
            float startY = panelY + 3.5F;
            RenderUtils.drawGradientRect(
               context.method_51448(),
               panelX,
               panelY,
               panelWidth,
               15.0F,
               3.5F,
               ColorUtils.darken(ColorUtils.getThemeColor(), 0.12F),
               ColorUtils.darken(ColorUtils.getThemeColor(), 0.1F),
               false
            );
            if ((shadeColor >> 24 & 0xFF) > 0) {
               RenderUtils.drawRoundedRect(context.method_51448(), panelX, panelY, panelWidth, 15.0F, 3.5F, shadeColor);
            }

            ThemeStorage.Themes selected = SlikDlc.INSTANCE.themeStorage.getThemes();

            for (int i = 0; i < themes.size(); i++) {
               ThemeStorage.Themes theme = themes.get(i);
               float boxX = startX + i * 12.0F;
               if (theme == selected) {
                  RenderUtils.drawRoundedRect(
                     context.method_51448(), boxX - 0.5F, startY - 0.5F, 9.0F, 9.0F, 2.5F, ColorUtils.setAlphaColor(-1, Math.max(1, (int)(200.0F * alphaMul)))
                  );
               }

               RenderUtils.drawRoundedRect(
                  context.method_51448(), boxX, startY, 8.0F, 8.0F, 2.0F, ColorUtils.applyAlpha(this.getThemeDisplayColor(theme), Math.max(0.55F, alphaMul))
               );
            }
         }
      }
   }

   public boolean handleClick(class_1041 window, double mouseX, double mouseY, int button, float offsetY) {
      if (window != null && button == 0) {
         List<ThemeStorage.Themes> themes = SlikDlc.INSTANCE.themeStorage.getThemeList();
         if (themes != null && !themes.isEmpty()) {
            float totalWidth = themes.size() * 8.0F + (themes.size() - 1) * 4.0F;
            float panelWidth = totalWidth + 8.0F;
            float panelX = this.getThemePanelX(window, panelWidth);
            float panelY = 100.0F + offsetY;
            float startX = panelX + 4.0F;
            float startY = panelY + 3.5F;
            if (!HoveringUtils.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, 15.0)) {
               return false;
            } else {
               for (int i = 0; i < themes.size(); i++) {
                  float boxX = startX + i * 12.0F;
                  if (HoveringUtils.isHovered(mouseX, mouseY, boxX, startY, 8.0, 8.0)) {
                     SlikDlc.INSTANCE.themeStorage.setThemes(themes.get(i));
                     return true;
                  }
               }

               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int getThemeDisplayColor(ThemeStorage.Themes theme) {
      int color = theme.getTheme().getColor(0);
      return ColorUtils.alpha(color) == 0 ? ColorUtils.rgba(220, 220, 220, 180) : color;
   }

   private float getThemePanelX(class_1041 window, float panelWidth) {
      return window.method_4486() / 2.0F - panelWidth / 2.0F;
   }
}
