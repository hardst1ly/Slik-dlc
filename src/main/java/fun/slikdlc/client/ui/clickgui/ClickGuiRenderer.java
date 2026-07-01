package fun.slikdlc.client.ui.clickgui;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.input.KeyBoardUtils;
import fun.slikdlc.api.utils.math.HoveringUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.api.utils.scissor.ScissorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1041;
import net.minecraft.class_332;
import net.minecraft.class_3532;

public class ClickGuiRenderer {
   private final ClickGuiState state;
   private final ClickGuiSettingRenderer settingRenderer;
   private final ClickGuiThemeSelector themeSelector;

   public ClickGuiRenderer(ClickGuiState state, ClickGuiSettingRenderer settingRenderer, ClickGuiThemeSelector themeSelector) {
      this.state = state;
      this.settingRenderer = settingRenderer;
      this.themeSelector = themeSelector;
   }

   public void render(class_332 context, int mouseX, int mouseY, class_1041 window, float animationProgress) {
      if (window != null) {
         float alphaMul = class_3532.method_15363(animationProgress, 0.0F, 1.0F);
         int shadeColor = this.getFadeShadeColor(alphaMul, 120);
         int colorTheme = this.getThemeColor();
         Module hoveredModule = null;
         Module.ModuleCategory[] categories = Module.ModuleCategory.values();

         for (int i = 0; i < categories.length; i++) {
            Module.ModuleCategory category = categories[i];
            float panelX = ClickGuiLayout.getCategoryPanelX(this.state.getX(), i);
            Module categoryHoveredModule = this.renderCategoryPanel(context, mouseX, mouseY, panelX, category, colorTheme, alphaMul, shadeColor);
            if (categoryHoveredModule != null) {
               hoveredModule = categoryHoveredModule;
            }
         }

         this.renderSearch(context, categories.length, colorTheme, alphaMul, this.getFadeShadeColor(alphaMul, 95));
         this.themeSelector.render(context, window, this.state.getRenderOffsetY(), alphaMul, this.getFadeShadeColor(alphaMul, 95));
         this.renderDescription(context, window, hoveredModule, colorTheme, animationProgress);
      }
   }

   private Module renderCategoryPanel(
      class_332 context, int mouseX, int mouseY, float panelX, Module.ModuleCategory category, int colorTheme, float alphaMul, int shadeColor
   ) {
      float panelY = this.state.getY() + this.state.getRenderOffsetY();
      RenderUtils.drawRoundedRect(context.method_51448(), panelX, panelY, 100.0F, 275.0F, 8.0F, ColorUtils.darken(colorTheme, 0.07F));
      RenderUtils.drawRoundedRect(context.method_51448(), panelX, panelY + 23.0F, 100.0F, 0.5F, 0.0F, ColorUtils.rgb(19, 18, 24));
      if ((shadeColor >> 24 & 0xFF) > 0) {
         RenderUtils.drawRoundedRect(context.method_51448(), panelX, panelY, 100.0F, 275.0F, 8.0F, shadeColor);
      }

      this.icons(14)
         .drawCenteredString(
            context.method_51448(),
            category.getIcons(),
            panelX + 50.0F - this.issue(15).getWidth(category.getName()) / 2.0F - 4.0F,
            panelY + 10.0F,
            this.alpha(colorTheme, alphaMul)
         );
      this.issue(15).drawCenteredString(context.method_51448(), category.getName(), panelX + 52.0F, panelY + 9.0F, this.alpha(-1, alphaMul));
      float contentY = ClickGuiLayout.getContentY(panelY);
      float contentHeight = ClickGuiLayout.getContentHeight();
      this.state.clampScroll(category, contentHeight);
      float moduleY = contentY + this.state.getScroll(category);
      Module hoveredModule = null;
      ScissorUtils.push();
      ScissorUtils.setFromComponentCoordinates((double)panelX, (double)contentY, 100.0, (double)contentHeight);

      for (Module module : this.state.getModules(category)) {
         float openProgress = this.state.getOpenProgress(module);
         float moduleHeight = ClickGuiLayout.getModuleHeight(module, openProgress);
         if (moduleY + moduleHeight + 4.0F >= contentY && moduleY <= contentY + contentHeight) {
            Module moduleHover = this.renderModule(
               context, mouseX, mouseY, panelX, moduleY, module, openProgress, moduleHeight, colorTheme, alphaMul, shadeColor
            );
            if (moduleHover != null) {
               hoveredModule = moduleHover;
            }
         }

         moduleY += 4.0F + moduleHeight;
      }

      ScissorUtils.pop();
      return hoveredModule;
   }

   private Module renderModule(
      class_332 context,
      int mouseX,
      int mouseY,
      float panelX,
      float moduleY,
      Module module,
      float openProgress,
      float moduleHeight,
      int colorTheme,
      float alphaMul,
      int shadeColor
   ) {
      List<Setting> settings = module.getSettings();
      this.renderModuleBackground(context, panelX, moduleY, moduleHeight, module.isEnable(), colorTheme, shadeColor);
      String moduleName = module.getName();
      String bindText = "";
      if (this.state.getBindingModule() == module) {
         bindText = " [...]";
      } else if (module.getKey() != -1) {
         bindText = " [" + this.state.toEnglish(KeyBoardUtils.getBindName(module.getKey())) + "]";
      }

      int nameColor = module.isEnable() ? this.alpha(-1, alphaMul) : this.alpha(ColorUtils.rgba(255, 255, 255, 170), alphaMul);
      int bindColor = module.isEnable() ? this.alpha(ColorUtils.rgba(255, 255, 255, 150), alphaMul) : this.alpha(ColorUtils.rgba(255, 255, 255, 100), alphaMul);
      this.issue(14).draw(context.method_51448(), moduleName, panelX + 10.0F, moduleY + 8.0F, nameColor);
      if (!bindText.isEmpty()) {
         float nameWidth = this.issue(14).getWidth(moduleName);
         this.issue(11).draw(context.method_51448(), bindText, panelX + 10.0F + nameWidth, moduleY + 9.0F, bindColor);
      }

      if (settings != null && !settings.isEmpty() && ClickGuiLayout.hasVisibleSettings(settings)) {
         this.renderModuleDots(context, panelX, moduleY, module, module.isEnable(), alphaMul);
      }

      if (settings != null && !settings.isEmpty()) {
         this.settingRenderer.render(context, module, panelX, moduleY, openProgress, colorTheme, mouseX, mouseY, this.state);
      }

      return HoveringUtils.isHovered(mouseX, mouseY, panelX + 3.0F, moduleY, 93.5, moduleHeight) ? module : null;
   }

   private void renderModuleBackground(class_332 context, float panelX, float moduleY, float moduleHeight, boolean enabled, int colorTheme, int shadeColor) {
      if (enabled) {
         RenderUtils.drawRoundedRect(
            context.method_51448(), panelX + 3.0F, moduleY - 0.5F, 93.5F, moduleHeight + 1.0F, 5.0F, ColorUtils.darken(colorTheme, 0.17F)
         );
         RenderUtils.drawGradientRect(
            context.method_51448(),
            panelX + 3.0F + 0.5F,
            moduleY,
            92.5F,
            moduleHeight,
            4.0F,
            ColorUtils.darken(colorTheme, 0.15F),
            ColorUtils.darken(colorTheme, 0.1F),
            false
         );
         if ((shadeColor >> 24 & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.method_51448(), panelX + 3.0F + 0.5F, moduleY, 92.5F, moduleHeight, 4.0F, shadeColor);
         }
      } else {
         RenderUtils.drawRoundedRect(
            context.method_51448(), panelX + 3.0F, moduleY - 0.5F, 93.5F, moduleHeight + 1.0F, 5.0F, ColorUtils.darken(colorTheme, 0.1F)
         );
         RenderUtils.drawGradientRect(
            context.method_51448(),
            panelX + 3.0F + 0.5F,
            moduleY,
            92.5F,
            moduleHeight,
            4.0F,
            ColorUtils.darken(colorTheme, 0.09F),
            ColorUtils.darken(colorTheme, 0.08F),
            false
         );
         if ((shadeColor >> 24 & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.method_51448(), panelX + 3.0F + 0.5F, moduleY, 92.5F, moduleHeight, 4.0F, shadeColor);
         }
      }
   }

   private void renderModuleDots(class_332 context, float panelX, float moduleY, Module module, boolean enabled, float alphaMul) {
      int dotsColor = enabled ? this.alpha(ColorUtils.rgba(255, 255, 255, 220), alphaMul) : this.alpha(ColorUtils.rgba(255, 255, 255, 100), alphaMul);
      float dotsX = panelX + 87.5F;
      float baseY = moduleY + 10.0F;
      float spacing = 2.0F;
      float radius = 2.1F;
      float bottomXOffset = 2.1F;
      float angle = this.state.updateDotsRotation(module, module.isOpen() ? (float) (Math.PI / 2) : 0.0F);
      float cos = (float)Math.cos(angle);
      float sin = (float)Math.sin(angle);
      float[][] offsets = new float[][]{{0.0F, -spacing}, {-bottomXOffset, spacing}, {bottomXOffset, spacing}};

      for (float[] offset : offsets) {
         float rx = offset[0] * cos - offset[1] * sin;
         float ry = offset[0] * sin + offset[1] * cos;
         RenderUtils.drawRoundCircle(context.method_51448(), dotsX + rx, baseY + ry, radius, dotsColor);
      }
   }

   private int getThemeColor() {
      return !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ColorUtils.getThemeColor();
   }

   private void renderSearch(class_332 context, int categoryCount, int colorTheme, float alphaMul, int shadeColor) {
      float searchY = ClickGuiLayout.getSearchY(this.state.getY() + this.state.getRenderOffsetY());
      float searchW = this.getSearchWidth();
      float searchX = ClickGuiLayout.getSearchX(this.state.getX(), categoryCount, searchW);
      float searchH = 18.0F;
      float selectionPaddingLeft = 3.0F;
      float selectionPaddingRight = 1.5F;
      int borderColor = ColorUtils.darken(colorTheme, 0.12F);
      RenderUtils.drawRoundedRect(context.method_51448(), searchX - 0.5F, searchY - 0.5F, searchW + 1.0F, searchH + 1.0F, 5.5F, borderColor);
      RenderUtils.drawGradientRect(
         context.method_51448(), searchX, searchY, searchW, searchH, 5.0F, ColorUtils.darken(colorTheme, 0.12F), ColorUtils.darken(colorTheme, 0.08F), false
      );
      if ((shadeColor >> 24 & 0xFF) > 0) {
         RenderUtils.drawRoundedRect(context.method_51448(), searchX, searchY, searchW, searchH, 5.0F, shadeColor);
      }

      String query = this.state.getSearchText();
      String text = query.isEmpty() ? "Search..." : query;
      int textColor = query.isEmpty() ? this.alpha(ColorUtils.rgba(255, 255, 255, 110), alphaMul) : this.alpha(ColorUtils.rgba(255, 255, 255, 230), alphaMul);
      float iconX = searchX + 3.5F;
      float textX = searchX + 19.0F;
      float textY = searchY + 6.2F;
      this.iconsNew(18)
         .drawGradientStringHorizontal(
            context.method_51448(), "l", iconX + 2.0F, searchY + 6.5F, this.alpha(colorTheme, alphaMul), this.alpha(colorTheme, alphaMul)
         );
      ScissorUtils.push();
      ScissorUtils.setFromComponentCoordinates(
         (double)(textX - selectionPaddingLeft), (double)searchY, (double)(searchW - 19.0F - 8.0F + selectionPaddingLeft), (double)searchH
      );
      if (!query.isEmpty() && this.state.hasSearchSelection()) {
         int selectionStart = this.state.getSearchSelectionStart();
         int selectionEnd = this.state.getSearchSelectionEnd();
         float selectedX = textX + this.issue(14).getWidth(query.substring(0, selectionStart)) - selectionPaddingLeft;
         float selectedW = this.issue(14).getWidth(query.substring(selectionStart, selectionEnd)) + selectionPaddingLeft + selectionPaddingRight;
         RenderUtils.drawRoundedRect(
            context.method_51448(), selectedX, searchY + 3.8F, selectedW, 10.5F, 1.5F, this.alpha(ColorUtils.rgba(42, 115, 255, 155), alphaMul)
         );
      }

      this.issue(14).draw(context.method_51448(), text, textX, textY + 1.0F, textColor);
      if (this.state.isSearchActive() && System.currentTimeMillis() / 500L % 2L == 0L) {
         float cursorX = textX + this.issue(14).getWidth(query.substring(0, Math.min(this.state.getSearchCursor(), query.length())));
         RenderUtils.drawRoundedRect(
            context.method_51448(), cursorX + 1.0F, searchY + 4.5F, 0.8F, 9.0F, 0.0F, this.alpha(ColorUtils.applyAlpha(colorTheme, 0.9F), alphaMul)
         );
      }

      ScissorUtils.pop();
   }

   private void renderDescription(class_332 context, class_1041 window, Module hoveredModule, int colorTheme, float alphaMul) {
      if (hoveredModule != null) {
         String description = hoveredModule.getDisplayDescription();
         if (description != null && !description.isBlank() && !"NULLABLE".equalsIgnoreCase(description) && !"desc".equalsIgnoreCase(description)) {
            Font descriptionFont = this.issue(16);
            float maxWidth = window.method_4486() - 40.0F;
            List<String> lines = this.wrapDescription(descriptionFont, description, maxWidth);
            if (!lines.isEmpty()) {
               float lineHeight = descriptionFont.getHeight() - 2.0F;
               float boxHeight = lines.size() * lineHeight;
               float centerX = window.method_4486() * 0.5F;
               float startY = 100.0F - boxHeight - 6.0F;

               for (int i = 0; i < lines.size(); i++) {
                  descriptionFont.drawCenteredString(
                     context.method_51448(), lines.get(i), centerX, startY + i * lineHeight, ColorUtils.applyAlpha(-1, alphaMul)
                  );
               }
            }
         }
      }
   }

   private List<String> wrapDescription(Font font, String text, float maxWidth) {
      List<String> lines = new ArrayList<>();
      String[] words = text.trim().split("\\s+");
      if (words.length == 0) {
         return lines;
      } else {
         StringBuilder currentLine = new StringBuilder();

         for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (!(font.getWidth(candidate) <= maxWidth) && !currentLine.isEmpty()) {
               lines.add(currentLine.toString());
               currentLine.setLength(0);
               currentLine.append(word);
            } else {
               currentLine.setLength(0);
               currentLine.append(candidate);
            }
         }

         if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
         }

         return lines;
      }
   }

   private float getSearchWidth() {
      String query = this.state.getSearchText();
      String text = query.isEmpty() ? "Search..." : query;
      float contentWidth = 19.0F + this.issue(14).getWidth(text) + 8.0F;
      return Math.max(75.0F, contentWidth);
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }

   private Font icons(int size) {
      return Fonts.getFont("icon", size);
   }

   private Font iconsNew(int size) {
      return Fonts.getFont("icon1", size);
   }

   private int alpha(int color, float alphaMul) {
      return ColorUtils.applyAlpha(color, alphaMul);
   }

   private int getFadeShadeColor(float alphaMul, int maxAlpha) {
      int alpha = class_3532.method_15340((int)((1.0F - alphaMul) * maxAlpha), 0, 255);
      return ColorUtils.rgba(0, 0, 0, alpha);
   }
}
