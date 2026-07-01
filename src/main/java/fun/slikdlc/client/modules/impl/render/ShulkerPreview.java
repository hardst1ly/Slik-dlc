package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_465;
import net.minecraft.class_9288;
import net.minecraft.class_9334;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ShulkerPreview extends Module {
   public static ShulkerPreview INSTANCE = new ShulkerPreview();
   private static final float RECT_RADIUS = 5.0F;
   private static final int SLOT_SIZE = 18;
   private static final int PADDING = 7;
   private static final int ROWS = 3;
   private static final int COLS = 9;
   private static final int TITLE_HEIGHT = 14;
   private static final int SLOT_BG_COLOR = -7631989;
   private Field guiLeftField;
   private Field guiTopField;
   private static ShulkerPreview instance;

   public ShulkerPreview() {
      super("ShulkerPreview", "Показывает содержимое шалкера при наведении + CTRL", Module.ModuleCategory.RENDER);
      this.initReflection();
      instance = this;
   }

   public static ShulkerPreview getInstance() {
      return instance;
   }

   private void initReflection() {
      try {
         for (Field field : class_465.class.getDeclaredFields()) {
            if (field.getType() == int.class) {
               field.setAccessible(true);
               String name = field.getName();
               if (name.equals("x") || name.equals("field_2776") || name.contains("Left") || name.contains("guiLeft")) {
                  this.guiLeftField = field;
               } else if (name.equals("y") || name.equals("field_2800") || name.contains("Top") || name.contains("guiTop")) {
                  this.guiTopField = field;
               }
            }
         }
      } catch (Exception var6) {
      }
   }

   private int getGuiLeft(class_465<?> screen) {
      try {
         if (this.guiLeftField != null) {
            return this.guiLeftField.getInt(screen);
         }
      } catch (Exception var3) {
      }

      return (mc.method_22683().method_4486() - 176) / 2;
   }

   private int getGuiTop(class_465<?> screen) {
      try {
         if (this.guiTopField != null) {
            return this.guiTopField.getInt(screen);
         }
      } catch (Exception var3) {
      }

      return (mc.method_22683().method_4502() - 166) / 2;
   }

   public void renderFromMixin(class_332 context, int mouseX, int mouseY) {
      if (this.isEnable()) {
         if (mc != null && mc.field_1724 != null && mc.field_1755 != null) {
            if (mc.field_1755 instanceof class_465<?> handledScreen) {
               long handle = mc.method_22683().method_4490();
               boolean isCtrlPressed = GLFW.glfwGetKey(handle, 341) == 1;
               if (isCtrlPressed) {
                  class_1735 hoveredSlot = this.getHoveredSlot(handledScreen);
                  if (hoveredSlot != null) {
                     class_1799 stack = hoveredSlot.method_7677();
                     if (this.isShulkerBox(stack)) {
                        class_9288 container = (class_9288)stack.method_57824(class_9334.field_49622);
                        if (container != null) {
                           this.renderShulkerPreview(context, stack, container, mouseX, mouseY);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private class_1735 getHoveredSlot(class_465<?> screen) {
      try {
         class_1703 handler = screen.method_17577();
         if (handler == null || handler.field_7761 == null) {
            return null;
         }

         double mouseX = mc.field_1729.method_1603() * mc.method_22683().method_4486() / mc.method_22683().method_4480();
         double mouseY = mc.field_1729.method_1604() * mc.method_22683().method_4502() / mc.method_22683().method_4507();
         int guiLeft = this.getGuiLeft(screen);
         int guiTop = this.getGuiTop(screen);

         for (class_1735 slot : handler.field_7761) {
            int slotX = guiLeft + slot.field_7873;
            int slotY = guiTop + slot.field_7872;
            if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
               return slot;
            }
         }
      } catch (Exception var13) {
      }

      return null;
   }

   private boolean isShulkerBox(class_1799 stack) {
      return stack != null && !stack.method_7960()
         ? stack.method_7909() == class_1802.field_8545
            || stack.method_7909() == class_1802.field_8722
            || stack.method_7909() == class_1802.field_8380
            || stack.method_7909() == class_1802.field_8050
            || stack.method_7909() == class_1802.field_8829
            || stack.method_7909() == class_1802.field_8271
            || stack.method_7909() == class_1802.field_8548
            || stack.method_7909() == class_1802.field_8520
            || stack.method_7909() == class_1802.field_8627
            || stack.method_7909() == class_1802.field_8451
            || stack.method_7909() == class_1802.field_8213
            || stack.method_7909() == class_1802.field_8816
            || stack.method_7909() == class_1802.field_8350
            || stack.method_7909() == class_1802.field_8584
            || stack.method_7909() == class_1802.field_8461
            || stack.method_7909() == class_1802.field_8676
            || stack.method_7909() == class_1802.field_8268
         : false;
   }

   private int getShulkerColor(class_1799 stack) {
      if (stack.method_7909() == class_1802.field_8545) {
         return -6394435;
      } else if (stack.method_7909() == class_1802.field_8722) {
         return -1;
      } else if (stack.method_7909() == class_1802.field_8380) {
         return -425955;
      } else if (stack.method_7909() == class_1802.field_8050) {
         return -3715395;
      } else if (stack.method_7909() == class_1802.field_8829) {
         return -12930086;
      } else if (stack.method_7909() == class_1802.field_8271) {
         return -75715;
      } else if (stack.method_7909() == class_1802.field_8548) {
         return -8337633;
      } else if (stack.method_7909() == class_1802.field_8520) {
         return -816214;
      } else if (stack.method_7909() == class_1802.field_8627) {
         return -12103854;
      } else if (stack.method_7909() == class_1802.field_8451) {
         return -6447721;
      } else if (stack.method_7909() == class_1802.field_8213) {
         return -15295332;
      } else if (stack.method_7909() == class_1802.field_8816) {
         return -7785800;
      } else if (stack.method_7909() == class_1802.field_8350) {
         return -12827478;
      } else if (stack.method_7909() == class_1802.field_8584) {
         return -8170446;
      } else if (stack.method_7909() == class_1802.field_8461) {
         return -10585066;
      } else if (stack.method_7909() == class_1802.field_8676) {
         return -5231066;
      } else {
         return stack.method_7909() == class_1802.field_8268 ? -14869215 : -6394435;
      }
   }

   private void renderShulkerPreview(class_332 context, class_1799 shulkerItem, class_9288 container, float mouseX, float mouseY) {
      class_4587 matrices = context.method_51448();
      int screenWidth = context.method_51421();
      int screenHeight = context.method_51443();
      float contentWidth = 162.0F;
      float contentHeight = 54.0F;
      float totalWidth = contentWidth + 14.0F;
      float totalHeight = contentHeight + 14.0F + 14.0F;
      float x = mouseX + 12.0F;
      float y = mouseY - 12.0F;
      if (x + totalWidth > screenWidth) {
         x = mouseX - totalWidth - 4.0F;
      }

      if (y + totalHeight > screenHeight) {
         y = screenHeight - totalHeight - 4.0F;
      }

      if (y < 4.0F) {
         y = 4.0F;
      }

      if (x < 4.0F) {
         x = 4.0F;
      }

      int shulkerColor = this.getShulkerColor(shulkerItem);
      int bgColor = ColorUtils.applyAlpha(shulkerColor, 0.85F);
      int darkerColor = this.darkenColor(shulkerColor, 0.6F);
      int lighterColor = this.lightenColor(shulkerColor, 1.3F);
      matrices.method_22903();
      GL11.glClear(256);
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrices.method_46416(0.0F, 0.0F, 500.0F);
      RenderUtils.drawBlur(matrices, x - 2.0F, y - 2.0F, totalWidth + 4.0F, totalHeight + 4.0F, 7.0F, 8.0F, -1);
      context.method_25294((int)x, (int)y, (int)(x + totalWidth), (int)(y + totalHeight), bgColor);
      context.method_25294((int)x, (int)y, (int)(x + totalWidth), (int)(y + 2.0F), lighterColor);
      context.method_25294((int)x, (int)(y + totalHeight - 2.0F), (int)(x + totalWidth), (int)(y + totalHeight), darkerColor);
      context.method_25294((int)x, (int)y, (int)(x + 2.0F), (int)(y + totalHeight), lighterColor);
      context.method_25294((int)(x + totalWidth - 2.0F), (int)y, (int)(x + totalWidth), (int)(y + totalHeight), darkerColor);
      Font font = Fonts.getFont("sf_regular", 12);
      if (font != null) {
         String title = shulkerItem.method_7964().getString();
         float titleX = x + 7.0F;
         float titleY = y + 7.0F - 1.0F;
         int textColor = this.isColorDark(shulkerColor) ? -1 : -15066598;
         font.drawString(matrices, title, titleX, titleY, textColor);
      }

      float slotsX = x + 7.0F;
      float slotsY = y + 7.0F + 14.0F - 2.0F;
      int slotAreaBg = this.darkenColor(shulkerColor, 0.5F);
      context.method_25294((int)(slotsX - 1.0F), (int)(slotsY - 1.0F), (int)(slotsX + contentWidth + 1.0F), (int)(slotsY + contentHeight + 1.0F), slotAreaBg);
      List<class_1799> items = new ArrayList<>();
      container.method_57489().forEach(items::add);

      for (int i = 0; i < 27; i++) {
         int row = i / 9;
         int col = i % 9;
         int slotX = (int)(slotsX + col * 18);
         int slotY = (int)(slotsY + row * 18);
         context.method_25294(slotX, slotY, slotX + 18 - 2, slotY + 18 - 2, -7631989);
         context.method_25294(slotX, slotY, slotX + 18 - 2, slotY + 1, -11184811);
         context.method_25294(slotX, slotY, slotX + 1, slotY + 18 - 2, -11184811);
         context.method_25294(slotX, slotY + 18 - 3, slotX + 18 - 2, slotY + 18 - 2, -1);
         context.method_25294(slotX + 18 - 3, slotY, slotX + 18 - 2, slotY + 18 - 2, -1);
         if (i < items.size()) {
            class_1799 itemStack = items.get(i);
            if (!itemStack.method_7960()) {
               context.method_51427(itemStack, slotX, slotY);
               context.method_51431(mc.field_1772, itemStack, slotX, slotY);
            }
         }
      }

      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      matrices.method_22909();
   }

   private int darkenColor(int color, float factor) {
      int a = color >> 24 & 0xFF;
      int r = (int)((color >> 16 & 0xFF) * factor);
      int g = (int)((color >> 8 & 0xFF) * factor);
      int b = (int)((color & 0xFF) * factor);
      return a << 24 | Math.min(255, r) << 16 | Math.min(255, g) << 8 | Math.min(255, b);
   }

   private int lightenColor(int color, float factor) {
      int a = color >> 24 & 0xFF;
      int r = (int)Math.min(255.0F, (color >> 16 & 0xFF) * factor);
      int g = (int)Math.min(255.0F, (color >> 8 & 0xFF) * factor);
      int b = (int)Math.min(255.0F, (color & 0xFF) * factor);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private boolean isColorDark(int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
      return luminance < 0.5;
   }
}
