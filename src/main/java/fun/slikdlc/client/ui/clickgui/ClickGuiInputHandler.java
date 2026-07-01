package fun.slikdlc.client.ui.clickgui;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.input.KeyBoardUtils;
import fun.slikdlc.api.utils.math.HoveringUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import fun.slikdlc.client.modules.settings.implement.TextSetting;
import java.util.List;
import net.minecraft.class_1041;

public class ClickGuiInputHandler implements QClient {
   private final ClickGuiState state;
   private final ClickGuiThemeSelector themeSelector;

   public ClickGuiInputHandler(ClickGuiState state, ClickGuiThemeSelector themeSelector) {
      this.state = state;
      this.themeSelector = themeSelector;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button, class_1041 window) {
      if (window != null && button == 0) {
         int categoryCount = Module.ModuleCategory.values().length;
         float searchW = this.getSearchWidth();
         float searchX = ClickGuiLayout.getSearchX(this.state.getX(), categoryCount, searchW);
         float searchY = ClickGuiLayout.getSearchY(this.state.getY() + this.state.getRenderOffsetY());
         boolean searchHovered = HoveringUtils.isHovered(mouseX, mouseY, searchX, searchY, searchW, 18.0);
         this.state.setSearchActive(searchHovered);
         if (searchHovered) {
            this.state.setEditingTextSetting(null);
            this.state.startSearchSelection(this.getSearchIndexAt(mouseX, searchX));
            return true;
         }
      }

      if (this.state.getBindingModule() != null && button >= 2) {
         this.state.getBindingModule().setKey(KeyBoardUtils.createMouseBind(button));
         this.state.setBindingModule(null);
         return true;
      } else if (this.state.getBindingSetting() != null && button >= 2) {
         this.state.getBindingSetting().setKey(KeyBoardUtils.createMouseBind(button));
         this.state.setBindingSetting(null);
         return true;
      } else {
         this.state.setEditingTextSetting(null);
         if (this.themeSelector.handleClick(window, mouseX, mouseY, button, this.state.getRenderOffsetY())) {
            return true;
         } else {
            Module.ModuleCategory[] categories = Module.ModuleCategory.values();

            for (int i = 0; i < categories.length; i++) {
               Module.ModuleCategory category = categories[i];
               float panelX = ClickGuiLayout.getCategoryPanelX(this.state.getX(), i);
               float contentY = ClickGuiLayout.getContentY(this.state.getY() + this.state.getRenderOffsetY());
               float contentHeight = ClickGuiLayout.getContentHeight();
               if (HoveringUtils.isHovered(mouseX, mouseY, panelX, contentY, 100.0, contentHeight)) {
                  float moduleY = contentY + this.state.getScroll(category);

                  for (Module module : this.state.getModules(category)) {
                     float openProgress = this.state.getOpenProgress(module);
                     float moduleHeight = ClickGuiLayout.getModuleHeight(module, openProgress);
                     if (HoveringUtils.isHovered(mouseX, mouseY, panelX + 3.0F, moduleY, 93.5, 20.0)) {
                        if (button == 0) {
                           module.toggle();
                           return true;
                        }

                        if (button == 1) {
                           module.setOpen(!module.isOpen());
                           this.state.clampScroll(category, contentHeight);
                           return true;
                        }

                        if (button == 2) {
                           this.state.setBindingModule(module);
                           return true;
                        }

                        return true;
                     }

                     if (module.isOpen() && openProgress > 0.1F) {
                        List<Setting> settings = module.getSettings();
                        if (settings != null && this.handleSettingClick(mouseX, mouseY, button, panelX, moduleY, settings)) {
                           return true;
                        }
                     }

                     moduleY += 4.0F + moduleHeight;
                  }
               }
            }

            return false;
         }
      }
   }

   public boolean mouseReleased(int button) {
      this.state.stopSearchSelection();
      if (button == 0) {
         for (Module module : this.state.getAllModules()) {
            List<Setting> settings = module.getSettings();
            if (settings != null) {
               for (Setting setting : settings) {
                  if (setting instanceof FloatSetting floatSetting) {
                     floatSetting.setActive(false);
                     this.state.endSliderDrag(floatSetting);
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button) {
      if (button == 0 && this.state.isSearchActive() && this.state.isSearchDragging()) {
         int categoryCount = Module.ModuleCategory.values().length;
         float searchX = ClickGuiLayout.getSearchX(this.state.getX(), categoryCount, this.getSearchWidth());
         this.state.updateSearchSelection(this.getSearchIndexAt(mouseX, searchX));
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
      Module.ModuleCategory[] categories = Module.ModuleCategory.values();

      for (int i = 0; i < categories.length; i++) {
         Module.ModuleCategory category = categories[i];
         float panelX = ClickGuiLayout.getCategoryPanelX(this.state.getX(), i);
         float contentY = ClickGuiLayout.getContentY(this.state.getY() + this.state.getRenderOffsetY());
         float contentHeight = ClickGuiLayout.getContentHeight();
         if (HoveringUtils.isHovered(mouseX, mouseY, panelX, contentY, 100.0, contentHeight)) {
            this.state.addScroll(category, verticalAmount, contentHeight);
            return true;
         }
      }

      return false;
   }

   public boolean keyPressed(int keyCode, int modifiers) {
      if (this.state.getEditingTextSetting() != null) {
         TextSetting textSetting = this.state.getEditingTextSetting();
         if (keyCode == 256 || keyCode == 257 || keyCode == 335) {
            this.state.setEditingTextSetting(null);
            return true;
         } else if (keyCode == 259) {
            String current = textSetting.get();
            if (current != null && !current.isEmpty()) {
               textSetting.setText(current.substring(0, current.length() - 1));
            }

            return true;
         } else {
            return true;
         }
      } else {
         if (this.state.isSearchActive()) {
            if ((modifiers & 2) != 0) {
               if (keyCode == 65) {
                  this.state.selectAllSearchText();
                  return true;
               }

               if (keyCode == 67) {
                  if (this.state.hasSearchSelection() && mc != null && mc.field_1774 != null) {
                     mc.field_1774.method_1455(this.state.getSelectedSearchText());
                  }

                  return true;
               }

               if (keyCode == 86) {
                  if (mc != null && mc.field_1774 != null) {
                     this.state.replaceSearchSelection(mc.field_1774.method_1460());
                  }

                  return true;
               }

               if (keyCode == 90) {
                  this.state.restoreSearchUndo();
                  return true;
               }
            }

            if (keyCode == 256 || keyCode == 257 || keyCode == 335) {
               this.state.setSearchActive(false);
               return true;
            }

            if (keyCode == 259) {
               this.state.removeLastSearchChar();
               return true;
            }

            if (keyCode == 261) {
               this.state.clearSearchText();
               return true;
            }

            if (keyCode == 263) {
               this.state.setSearchCursor(this.state.getSearchCursor() - 1, (modifiers & 1) != 0);
               return true;
            }

            if (keyCode == 262) {
               this.state.setSearchCursor(this.state.getSearchCursor() + 1, (modifiers & 1) != 0);
               return true;
            }
         }

         if (this.state.getBindingModule() != null) {
            if (keyCode == 256) {
               this.state.setBindingModule(null);
            } else if (keyCode != 261 && keyCode != 259) {
               this.state.getBindingModule().setKey(keyCode);
               this.state.setBindingModule(null);
            } else {
               this.state.getBindingModule().setKey(-1);
               this.state.setBindingModule(null);
            }

            return true;
         } else if (this.state.getBindingSetting() == null) {
            return false;
         } else {
            if (keyCode == 256) {
               this.state.setBindingSetting(null);
            } else if (keyCode != 261 && keyCode != 259) {
               this.state.getBindingSetting().setKey(keyCode);
               this.state.setBindingSetting(null);
            } else {
               this.state.getBindingSetting().setKey(-1);
               this.state.setBindingSetting(null);
            }

            return true;
         }
      }
   }

   public boolean charTyped(char chr) {
      if (this.state.getEditingTextSetting() != null) {
         if (!Character.isISOControl(chr)) {
            TextSetting textSetting = this.state.getEditingTextSetting();
            textSetting.setText(textSetting.get() + chr);
         }

         return true;
      } else if (!this.state.isSearchActive()) {
         return false;
      } else {
         this.state.appendSearchChar(chr);
         return true;
      }
   }

   private int getSearchIndexAt(double mouseX, float searchX) {
      String text = this.state.getSearchText();
      float textX = searchX + 19.0F;
      float localX = (float)mouseX - textX;
      if (!(localX <= 0.0F) && !text.isEmpty()) {
         for (int i = 1; i <= text.length(); i++) {
            float previousWidth = this.issue(14).getWidth(text.substring(0, i - 1));
            float currentWidth = this.issue(14).getWidth(text.substring(0, i));
            float midpoint = previousWidth + (currentWidth - previousWidth) * 0.5F;
            if (localX < midpoint) {
               return i - 1;
            }
         }

         return text.length();
      } else {
         return 0;
      }
   }

   private float getSearchWidth() {
      String query = this.state.getSearchText();
      String text = query.isEmpty() ? "Search..." : query;
      float contentWidth = 19.0F + this.issue(14).getWidth(text) + 8.0F;
      return Math.max(75.0F, contentWidth);
   }

   private boolean handleSettingClick(double mouseX, double mouseY, int button, float panelX, float moduleY, List<Setting> settings) {
      float settingYoffset = 20.0F;

      for (Setting setting : settings) {
         if (setting != null && setting.visible()) {
            float settingY = moduleY + settingYoffset + 4.0F;
            if (setting instanceof BooleanSetting booleanSetting) {
               if (button == 0 && HoveringUtils.isHovered(mouseX, mouseY, panelX + 75.0F, settingY - 2.0F, 16.0, 10.0)) {
                  booleanSetting.setState(!booleanSetting.isState());
                  return true;
               }

               settingYoffset += 12.0F;
            } else if (setting instanceof TextSetting textSetting) {
               float boxWidth = 42.0F;
               float boxX = panelX + 49.0F;
               if (button == 0 && HoveringUtils.isHovered(mouseX, mouseY, boxX, settingY - 2.5F, boxWidth, 9.0)) {
                  this.state.setSearchActive(false);
                  this.state.stopSearchSelection();
                  this.state.setEditingTextSetting(textSetting);
                  return true;
               }

               settingYoffset += 12.0F;
            } else if (setting instanceof FloatSetting floatSetting) {
               if (button == 0 && HoveringUtils.isHovered(mouseX, mouseY, panelX + 10.0F, settingY + 9.0F, 79.0, 6.0)) {
                  floatSetting.setActive(true);
                  floatSetting.setValue(this.state.getSliderValue(floatSetting, panelX + 10.0F, mouseX));
                  this.state.beginSliderDrag(floatSetting, mouseX);
                  return true;
               }

               settingYoffset += 22.0F;
            } else if (setting instanceof ModeSetting modeSetting) {
               float modeY = settingY + 10.0F;

               for (String mode : modeSetting.getMods()) {
                  if (button == 0 && HoveringUtils.isHovered(mouseX, mouseY, panelX + 10.0F, modeY - 2.0F, 79.0, 10.0)) {
                     modeSetting.set(mode);
                     return true;
                  }

                  modeY += 10.0F;
               }

               settingYoffset += ClickGuiLayout.calculateModeSettingHeight(modeSetting);
            } else if (!(setting instanceof ListSetting listSetting)) {
               if (setting instanceof BindSetting bindSetting) {
                  String bindString = this.state.getBindingSetting() == bindSetting
                     ? "..."
                     : this.state.toEnglish(KeyBoardUtils.getBindName(bindSetting.getKey()));
                  float bindWidth = this.issue(12).getWidth(bindString) + 6.0F;
                  float bindX = panelX + 89.0F - bindWidth;
                  if (button == 0 && HoveringUtils.isHovered(mouseX, mouseY, bindX, settingY - 2.5F, bindWidth, 9.0)) {
                     this.state.setBindingSetting(bindSetting);
                     return true;
                  }

                  settingYoffset += 12.0F;
               }
            } else {
               float listY = settingY + 10.0F;

               for (BooleanSetting entry : listSetting.getSettings()) {
                  if (entry.visible()) {
                     if (button == 0 && HoveringUtils.isHovered(mouseX, mouseY, panelX + 10.0F, listY - 2.0F, 79.0, 10.0)) {
                        entry.setState(!entry.isState());
                        return true;
                     }

                     listY += 10.0F;
                  }
               }

               settingYoffset += ClickGuiLayout.calculateListSettingHeight(listSetting);
            }
         }
      }

      return false;
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }
}
