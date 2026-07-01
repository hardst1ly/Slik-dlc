package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.events.implement.EventMoveInput;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.movement.Sprint;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2815;

public class AutoSwap extends Module {
   public static AutoSwap INSTANCE = new AutoSwap();
   private final ModeSetting firstItem = new ModeSetting("Первый предмет", "Руна", "Руна", "Тотем", "Шар", "Гепл", "Щит");
   private final ModeSetting secondItem = new ModeSetting("Второй предмет", "Тотем", "Руна", "Тотем", "Шар", "Гепл", "Щит");
   private final BindSetting swapKey = new BindSetting("Кнопка свапа", -98);
   private final BooleanSetting bypassgrim = new BooleanSetting("Обходить Grim", true);
   private int bypassTicks;
   private boolean sprintPaused;
   private int swapCooldown;
   private int targetSlot = -1;
   private boolean needSwap = false;

   public AutoSwap() {
      super("AutoSwap", "Быстрая смена предметов в офф-хенде", Module.ModuleCategory.COMBAT);
      this.addSettings(new Setting[]{this.firstItem, this.secondItem, this.swapKey, this.bypassgrim});
   }

   @Override
   public void onEnable() {
      this.needSwap = false;
      this.targetSlot = -1;
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      super.onEnable();
   }

   @EventLink
   public void onBinding(EventBinding event) {
      if (mc.field_1755 == null) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            if (event.getKey() == this.swapKey.getKey() && this.swapCooldown == 0) {
               this.needSwap = true;
            }
         }
      }
   }

   @EventLink
   public void onInput(EventMoveInput e) {
      if (this.bypassgrim.isState() && this.bypassTicks > 0) {
         if (mc.field_1724 == null) {
            return;
         }

         mc.field_1724.method_5728(false);
         e.setForward(0.0F);
         e.setStrafe(0.0F);
         e.setJump(false);
         e.setSneak(false);
      }
   }

   @EventLink
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.swapCooldown > 0) {
            this.swapCooldown--;
         }

         if (this.bypassgrim.isState() && this.bypassTicks > 0) {
            mc.field_1724.method_5728(false);
            this.bypassTicks--;
            if (this.bypassTicks == 1) {
               this.performSwap();
            }

            if (this.bypassTicks == 0) {
               this.restoreSprint();
            }
         } else {
            if (this.needSwap && this.targetSlot == -1) {
               this.needSwap = false;
               class_1792 offhand = mc.field_1724.method_6079().method_7909();
               class_1792 first = this.getItem(this.firstItem.getCurrent());
               class_1792 second = this.getItem(this.secondItem.getCurrent());
               int firstSlot = this.findItemSlot(first);
               int secondSlot = this.findItemSlot(second);
               if (firstSlot == -1 && secondSlot == -1) {
                  return;
               }

               int slot;
               if (offhand == first && secondSlot != -1) {
                  slot = secondSlot;
               } else if (firstSlot != -1) {
                  slot = firstSlot;
               } else {
                  slot = secondSlot;
               }

               if (slot == -1) {
                  return;
               }

               this.targetSlot = slot;
               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = 2;
                  this.swapCooldown = 2;
               } else {
                  this.performSwap();
                  this.swapCooldown = 2;
               }
            }
         }
      }
   }

   private void performSwap() {
      if (this.targetSlot != -1) {
         this.doSwap(this.targetSlot);
         mc.field_1724.field_3944.method_52787(new class_2815(0));
         this.targetSlot = -1;
      }
   }

   private void doSwap(int slot) {
      if (slot >= 36 && slot <= 44) {
         int hotbarSlot = slot - 36;
         mc.field_1761.method_2906(0, 45, hotbarSlot, class_1713.field_7791, mc.field_1724);
      } else {
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, 45, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
      }
   }

   private int findItemSlot(class_1792 item) {
      for (int i = 9; i < 45; i++) {
         class_1799 stack = mc.field_1724.field_7498.method_7611(i).method_7677();
         if (stack.method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private class_1792 getItem(String name) {
      return switch (name) {
         case "Руна" -> class_1802.field_8450;
         case "Тотем" -> class_1802.field_8288;
         case "Шар" -> class_1802.field_8575;
         case "Гепл" -> class_1802.field_8463;
         case "Щит" -> class_1802.field_8255;
         default -> class_1802.field_8162;
      };
   }

   private void disableSprint() {
      if (!this.sprintPaused) {
         Sprint.pushPause(1000L);
         this.sprintPaused = true;
      }
   }

   private void restoreSprint() {
      if (this.sprintPaused) {
         this.sprintPaused = false;
         Sprint.popPause();
      }
   }

   @Override
   public void onDisable() {
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      this.needSwap = false;
      this.targetSlot = -1;
      this.restoreSprint();
      super.onDisable();
   }
}
