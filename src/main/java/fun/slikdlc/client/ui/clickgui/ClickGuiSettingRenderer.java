package fun.slikdlc.client.ui.clickgui;

import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.input.KeyBoardUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.api.utils.scissor.ScissorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import fun.slikdlc.client.modules.settings.implement.TextSetting;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;

public class ClickGuiSettingRenderer {
   private static final float HOVER_SCROLL_OVERFLOW_THRESHOLD = 6.0F;

   public ClickGuiSettingRenderer() {
   }

   public void render(
      class_332 context, Module module, float panelX, float moduleY, float openProgress, int colorTheme, double mouseX, double mouseY, ClickGuiState state
   ) {
      List<Setting> settings = module.getSettings();
      if (settings != null && !settings.isEmpty() && !(openProgress <= 0.01F)) {
         float maxSettingHeight = ClickGuiLayout.calculateSettingsHeight(module);
         float settingsClipY = moduleY + 20.0F;
         float settingsClipHeight = maxSettingHeight * openProgress;
         ScissorUtils.push();
         ScissorUtils.setFromComponentCoordinates((double)(panelX + 3.0F), (double)settingsClipY, 93.5, (double)settingsClipHeight);
         float settingYoffset = 20.0F;

         for (Setting setting : settings) {
            if (setting != null && setting.visible()) {
               float settingY = moduleY + settingYoffset + 4.0F;
               int alpha = (int)(255.0F * openProgress);
               if (setting instanceof BooleanSetting booleanSetting) {
                  this.renderBooleanSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, booleanSetting, state);
                  settingYoffset += 12.0F;
               } else if (setting instanceof TextSetting textSetting) {
                  this.renderTextSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, textSetting, state);
                  settingYoffset += 22.0F;
               } else if (setting instanceof FloatSetting floatSetting) {
                  this.renderFloatSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, floatSetting, state);
                  settingYoffset += 22.0F;
               } else if (setting instanceof ModeSetting modeSetting) {
                  this.renderModeSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, modeSetting, state);
                  settingYoffset += ClickGuiLayout.calculateModeSettingHeight(modeSetting);
               } else if (setting instanceof ListSetting listSetting) {
                  this.renderListSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, listSetting, state);
                  settingYoffset += ClickGuiLayout.calculateListSettingHeight(listSetting);
               } else if (setting instanceof BindSetting bindSetting) {
                  this.renderBindSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, bindSetting, state);
                  settingYoffset += 12.0F;
               }
            }
         }

         ScissorUtils.pop();
      }
   }

   private void renderBooleanSetting(
      class_332 context,
      float panelX,
      float settingY,
      int alpha,
      int colorTheme,
      double mouseX,
      double mouseY,
      BooleanSetting booleanSetting,
      ClickGuiState state
   ) {
      AnimationUtils backgroundAnimation = state.getBooleanBackgroundAnimation(booleanSetting);
      AnimationUtils circleAnimation = state.getBooleanCircleAnimation(booleanSetting);
      backgroundAnimation.update(booleanSetting.isState() ? 1.0F : 0.0F);
      circleAnimation.update(booleanSetting.isState() ? 1.0F : 0.0F);
      float backgroundProgress = backgroundAnimation.getValue();
      float circleProgress = circleAnimation.getValue();
      int offColor = ColorUtils.darken(colorTheme, 0.05F);
      int r = (int)((offColor >> 16 & 0xFF) + ((colorTheme >> 16 & 0xFF) - (offColor >> 16 & 0xFF)) * backgroundProgress);
      int g = (int)((offColor >> 8 & 0xFF) + ((colorTheme >> 8 & 0xFF) - (offColor >> 8 & 0xFF)) * backgroundProgress);
      int b = (int)((offColor & 0xFF) + ((colorTheme & 0xFF) - (offColor & 0xFF)) * backgroundProgress);
      int a = (int)((offColor >> 24 & 0xFF) + ((colorTheme >> 24 & 0xFF) - (offColor >> 24 & 0xFF)) * backgroundProgress);
      int interpolatedColor = a << 24 | r << 16 | g << 8 | b;
      float maxWidth = panelX + 73.0F - (panelX + 10.0F);
      this.drawStringWithHoverScroll(
         this.issue(13),
         context.method_51448(),
         booleanSetting.name(),
         panelX + 10.0F,
         settingY,
         maxWidth,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(booleanSetting)
      );
      RenderUtils.drawRoundedRect(
         context.method_51448(),
         panelX + 75.0F,
         settingY - 2.0F,
         16.0F,
         9.0F,
         3.5F,
         ColorUtils.rgba(interpolatedColor >> 16 & 0xFF, interpolatedColor >> 8 & 0xFF, interpolatedColor & 0xFF, alpha)
      );
      float circleX = panelX + 79.5F + circleProgress * 6.2F;
      RenderUtils.drawRoundCircle(context.method_51448(), circleX + 0.5F, settingY + 2.5F, 7.0F, ColorUtils.rgba(255, 255, 255, alpha));
   }

   private void renderFloatSetting(
      class_332 context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, FloatSetting floatSetting, ClickGuiState state
   ) {
      if (floatSetting.isActive()) {
         floatSetting.setValue(state.updateActiveSliderValue(floatSetting, mouseX));
      }

      AnimationUtils sliderAnimation = state.getSliderAnimation(floatSetting);
      sliderAnimation.update(state.getSliderPos(floatSetting));
      float animatedPos = sliderAnimation.getValue();
      String valueString = this.formatSliderValue(floatSetting);
      float valueX = panelX + 89.0F - this.issue(12).getWidth(valueString);
      float nameMaxWidth = valueX - 4.0F - (panelX + 10.0F);
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.method_51448(),
         floatSetting.name(),
         panelX + 10.0F,
         settingY + 1.0F,
         nameMaxWidth,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(floatSetting)
      );
      this.issue(12).drawString(context.method_51448(), valueString, valueX, settingY + 1.0F, ColorUtils.setAlphaColor(colorTheme, alpha));
      int sliderBackgroundColor = ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.2F), alpha);
      RenderUtils.drawRoundedRect(context.method_51448(), panelX + 10.0F, settingY + 9.0F, 79.0F, 4.5F, 1.25F, sliderBackgroundColor);
      int sliderFillColor = ColorUtils.setAlphaColor(colorTheme, alpha);
      RenderUtils.drawRoundedRect(context.method_51448(), panelX + 10.0F, settingY + 9.0F, animatedPos * 79.0F, 4.5F, 1.25F, sliderFillColor);
      RenderUtils.drawRoundCircle(context.method_51448(), panelX + 10.0F + animatedPos * 79.0F, settingY + 11.25F, 6.0F, ColorUtils.setAlphaColor(-1, alpha));
   }

   private void renderTextSetting(
      class_332 context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, TextSetting textSetting, ClickGuiState state
   ) {
      String value = textSetting.get();
      boolean editing = state.getEditingTextSetting() == textSetting;
      String preview = value != null && !value.isEmpty() ? value : "...";
      String boxText = editing ? preview + "_" : preview;
      float boxWidth = 42.0F;
      float boxX = panelX + 49.0F;
      this.drawStringWithHoverScroll(
         this.issue(13),
         context.method_51448(),
         textSetting.name(),
         panelX + 10.0F,
         settingY,
         boxX - 1.0F - (panelX + 10.0F),
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(textSetting)
      );
      int background = ColorUtils.setAlphaColor(editing ? colorTheme : ColorUtils.darken(colorTheme, 0.15F), alpha);
      int textColor = ColorUtils.setAlphaColor(-1, alpha);
      float boxY = settingY - 2.5F;
      RenderUtils.drawRoundedRect(context.method_51448(), boxX, boxY, boxWidth, 9.0F, 1.5F, background);
      ScissorUtils.push();
      ScissorUtils.setFromComponentCoordinates((double)(boxX + 2.0F), (double)(boxY + 1.0F), (double)(boxWidth - 4.0F), 7.0);
      this.issue(12).drawString(context.method_51448(), boxText, boxX + 3.0F, settingY + 1.0F, textColor);
      ScissorUtils.pop();
   }

   private void renderModeSetting(
      class_332 context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, ModeSetting modeSetting, ClickGuiState state
   ) {
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.method_51448(),
         modeSetting.name(),
         panelX + 10.0F,
         settingY + 1.0F,
         79.0F,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(modeSetting)
      );
      float modeY = settingY + 10.0F;

      for (String mode : modeSetting.getMods()) {
         boolean selected = modeSetting.getCurrent().equals(mode);
         AnimationUtils animation = state.getModeAnimation(this.getModeKey(modeSetting, mode), selected);
         animation.update(selected ? 1.0F : 0.0F);
         float progress = animation.getValue();
         int outerColor = ColorUtils.setAlphaColor(colorTheme, (int)(alpha * (0.3F + 0.7F * progress)));
         int innerColor = selected ? ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.4F), alpha) : ColorUtils.rgba(255, 255, 255, alpha);
         this.issue(13).draw(context.method_51448(), mode, panelX + 10.0F, modeY, this.getSecondarySettingColor(alpha));
         RenderUtils.drawRoundCircle(context.method_51448(), panelX + 86.0F, modeY + 2.0F, 9.0F, outerColor);
         RenderUtils.drawRoundCircle(context.method_51448(), panelX + 86.0F, modeY + 2.0F, 6.0F - progress * 2.0F + 3.0F, innerColor);
         modeY += 10.0F;
      }
   }

   private void renderListSetting(
      class_332 context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, ListSetting listSetting, ClickGuiState state
   ) {
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.method_51448(),
         listSetting.name(),
         panelX + 10.0F,
         settingY + 1.0F,
         79.0F,
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(listSetting)
      );
      float listY = settingY + 10.0F;

      for (BooleanSetting entry : listSetting.getSettings()) {
         if (entry.visible()) {
            boolean selected = entry.isState();
            AnimationUtils animation = state.getListAnimation(this.getListKey(listSetting, entry), selected);
            animation.update(selected ? 1.0F : 0.0F);
            float progress = animation.getValue();
            int outerColor = ColorUtils.setAlphaColor(colorTheme, (int)(alpha * (0.3F + 0.7F * progress)));
            int innerColor = selected ? ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.4F), alpha) : ColorUtils.rgba(255, 255, 255, alpha);
            this.drawStringWithHoverScroll(
               this.issue(13),
               context.method_51448(),
               entry.name(),
               panelX + 10.0F,
               listY,
               panelX + 73.0F - (panelX + 10.0F),
               this.getSecondarySettingColor(alpha),
               mouseX,
               mouseY,
               state,
               this.getListKey(listSetting, entry) + "_text"
            );
            RenderUtils.drawRoundCircle(context.method_51448(), panelX + 86.0F, listY + 2.0F, 9.0F, outerColor);
            RenderUtils.drawRoundCircle(context.method_51448(), panelX + 86.0F, listY + 2.0F, 6.0F - progress * 2.0F + 3.0F, innerColor);
            listY += 10.0F;
         }
      }
   }

   private void renderBindSetting(
      class_332 context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, BindSetting bindSetting, ClickGuiState state
   ) {
      boolean binding = state.getBindingSetting() == bindSetting;
      AnimationUtils bindAnimation = state.getBindAnimation(this.getBindKey(bindSetting), binding);
      bindAnimation.update(binding ? 1.0F : 0.0F);
      float progress = bindAnimation.getValue();
      String bindString = binding ? "..." : state.toEnglish(KeyBoardUtils.getBindName(bindSetting.getKey()));
      float bindTextWidth = this.issue(12).getWidth(bindString);
      float bindWidth = bindTextWidth + 6.0F;
      float bindX = panelX + 89.0F - bindWidth;
      int bindBackgroundColor = ColorUtils.setAlphaColor(ColorUtils.interpolateColor(ColorUtils.darken(colorTheme, 0.15F), colorTheme, progress), alpha);
      int bindTextColor = ColorUtils.setAlphaColor(ColorUtils.interpolateColor(ColorUtils.rgb(140, 139, 145), -1, progress), alpha);
      RenderUtils.drawRoundedRect(context.method_51448(), bindX, settingY - 2.5F, bindWidth, 9.0F, 1.5F, bindBackgroundColor);
      this.issue(12).drawString(context.method_51448(), bindString, bindX + 3.0F, settingY + 1.0F, bindTextColor);
      this.drawStringWithHoverScroll(
         this.issue(12),
         context.method_51448(),
         bindSetting.name(),
         panelX + 10.0F,
         settingY + 1.0F,
         bindX - 4.0F - (panelX + 10.0F),
         this.getPrimarySettingColor(alpha),
         mouseX,
         mouseY,
         state,
         this.getSettingTextKey(bindSetting)
      );
   }

   private String getModeKey(ModeSetting setting, String mode) {
      return System.identityHashCode(setting) + "_mode_" + mode;
   }

   private String getListKey(ListSetting setting, BooleanSetting entry) {
      return setting.hashCode() + "_list_" + entry.name();
   }

   private String getBindKey(BindSetting setting) {
      return setting.hashCode() + "_bind";
   }

   private String formatSliderValue(FloatSetting setting) {
      float value = setting.get();
      float increment = setting.getIncrement();
      if (increment >= 1.0F) {
         return String.valueOf((int)value);
      } else {
         return increment >= 0.1F ? String.format("%.1f", value) : String.format("%.2f", value);
      }
   }

   private void drawStringWithHoverScroll(
      Font font,
      class_4587 matrix,
      String text,
      float x,
      float y,
      float maxWidth,
      int color,
      double mouseX,
      double mouseY,
      ClickGuiState state,
      String animationKey
   ) {
      if (text != null && !text.isEmpty() && !(maxWidth <= 0.0F)) {
         float totalWidth = font.getWidth(text);
         float overflow = totalWidth - maxWidth;
         if (overflow <= 6.0F) {
            font.draw(matrix, text, x, y, color);
         } else {
            boolean hovered = this.isTextHovered(x, y, maxWidth, font.getHeight(), mouseX, mouseY);
            float scrollPhase = state.advanceTextScrollPhase(animationKey, hovered);
            boolean scrollActive = state.isTextScrollActive(animationKey, hovered);
            AnimationUtils hoverAnimation = state.getTextHoverAnimation(animationKey, scrollActive);
            hoverAnimation.update(scrollActive ? 1.0F : 0.0F);
            float hoverProgress = hoverAnimation.getValue();
            float scrollOffset = this.getHoverScrollOffset(overflow, scrollPhase) * hoverProgress;
            ScissorUtils.push();
            ScissorUtils.setFromComponentCoordinates((double)x, (double)(y - 2.0F), (double)maxWidth, (double)(font.getHeight() + 4.0F));
            font.draw(matrix, text, x - scrollOffset, y, color);
            ScissorUtils.pop();
         }
      }
   }

   private int getPrimarySettingColor(int alpha) {
      return ColorUtils.rgba(245, 245, 248, alpha);
   }

   private int getSecondarySettingColor(int alpha) {
      return ColorUtils.rgba(186, 186, 194, alpha);
   }

   private boolean isTextHovered(float x, float y, float width, float height, double mouseX, double mouseY) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y - 2.0F && mouseY <= y + height + 2.0F;
   }

   private float getHoverScrollOffset(float maxOffset, float phase) {
      if (maxOffset <= 0.0F) {
         return 0.0F;
      } else {
         float pingPong = phase < 0.5F ? phase * 2.0F : 2.0F - phase * 2.0F;
         float eased = pingPong * pingPong * (3.0F - 2.0F * pingPong);
         return maxOffset * eased;
      }
   }

   private String getSettingTextKey(Setting setting) {
      return "setting_text_" + System.identityHashCode(setting);
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }
}
