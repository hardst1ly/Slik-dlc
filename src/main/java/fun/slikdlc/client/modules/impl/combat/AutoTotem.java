package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventMoveInput;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.movement.Sprint;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2815;

public class AutoTotem extends Module {
   public static AutoTotem INSTANCE = new AutoTotem();
   private final ListSetting triggers = new ListSetting(
      "Брать от",
      new BooleanSetting("Кристалл рядом", true),
      new BooleanSetting("Кристалл в руке", true),
      new BooleanSetting("Обсидиан в руке", true),
      new BooleanSetting("Падения", true)
   );
   private final FloatSetting hp = new FloatSetting("Брать от хп", 6.0F, 1.0F, 20.0F, 0.5F);
   private final FloatSetting hpOnElytra = new FloatSetting("Хп на элитрах", 10.0F, 1.0F, 20.0F, 0.5F);
   private final FloatSetting crystalRadius = new FloatSetting("Радиус кристалла", 6.0F, 1.0F, 12.0F, 0.5F).visible(this::isCrystalRadiusVisible);
   private final FloatSetting fallHeight = new FloatSetting("Высота падения", 10.0F, 3.0F, 50.0F, 1.0F).visible(() -> this.triggers.is("Падения"));
   private final BooleanSetting saveEnchanted = new BooleanSetting("Сохранять зачар", true);
   private final BooleanSetting returnTotem = new BooleanSetting("Возвращать тотем", true);
   private final FloatSetting returnDelay = new FloatSetting("Задержка возврата", 20.0F, 5.0F, 100.0F, 5.0F).visible(this.returnTotem::isState);
   private final BooleanSetting bypassgrim = new BooleanSetting("Обходить Grim", true);
   private final ModeSetting swapVersion = new ModeSetting("Версия свапа", "1.21.4", "1.21.4", "1.16.5");
   private int bypassTicks;
   private boolean sprintPaused;
   private int swapCooldown;
   private int savedTotemSlot = -1;
   private class_1799 originalOffhandItem = class_1799.field_8037;
   private boolean totemTakenByUs = false;
   private boolean returnMode = false;
   private boolean needFastSwap = false;
   private int safeTicks = 0;

   public AutoTotem() {
      super("AutoTotem", "Автоматически берёт тотем в опасности", Module.ModuleCategory.COMBAT);
      this.addSettings(
         new Setting[]{
            this.hp,
            this.hpOnElytra,
            this.saveEnchanted,
            this.bypassgrim,
            this.returnTotem,
            this.swapVersion,
            this.returnDelay,
            this.triggers,
            this.crystalRadius,
            this.fallHeight
         }
      );
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
         boolean isCrystalDanger = this.isCrystalDanger();
         if (isCrystalDanger) {
            this.needFastSwap = true;
            this.safeTicks = 0;
         }

         if (this.swapCooldown > 0) {
            this.swapCooldown--;
         }

         if (this.bypassgrim.isState() && this.bypassTicks > 0) {
            mc.field_1724.method_5728(false);
            this.bypassTicks--;
            if (this.bypassTicks <= 0) {
               if (this.returnMode) {
                  this.performReturn();
               } else {
                  this.performSwap();
               }

               this.restoreSprint();
            }
         } else {
            boolean needTotem = this.shouldTakeTotem(isCrystalDanger);
            if (needTotem && !this.hasTotemInOffhand()) {
               int totemSlot = this.findTotemSlot();
               if (totemSlot == -1) {
                  return;
               }

               if (!this.needFastSwap && this.swapCooldown > 0) {
                  return;
               }

               if (this.originalOffhandItem.method_7960() && !this.totemTakenByUs) {
                  this.originalOffhandItem = mc.field_1724.method_6079().method_7972();
               }

               this.savedTotemSlot = totemSlot;
               this.returnMode = false;
               this.safeTicks = 0;
               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = this.needFastSwap ? 1 : 2;
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               } else {
                  this.performSwap();
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               }
            }

            boolean isSafe = !needTotem;
            if (isSafe) {
               this.safeTicks++;
            } else {
               this.safeTicks = 0;
            }

            if (this.returnTotem.isState()
               && !needTotem
               && this.hasTotemInOffhand()
               && this.totemTakenByUs
               && this.safeTicks >= this.returnDelay.getValue().intValue()) {
               if (!this.needFastSwap && this.swapCooldown > 0) {
                  return;
               }

               this.returnMode = true;
               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = this.needFastSwap ? 1 : 2;
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               } else {
                  this.performReturn();
                  this.swapCooldown = this.needFastSwap ? 0 : 2;
               }
            }

            if (!isCrystalDanger) {
               this.needFastSwap = false;
            }
         }
      }
   }

   private boolean isCrystalDanger() {
      float radius = this.crystalRadius.getValue().floatValue();
      double radiusSq = radius * radius;
      if (this.triggers.is("Кристалл рядом")) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1511 && mc.field_1724.method_5858(entity) <= radiusSq) {
               return true;
            }
         }
      }

      if (this.triggers.is("Кристалл в руке")) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player != mc.field_1724
               && mc.field_1724.method_5858(player) <= radiusSq
               && (player.method_6047().method_31574(class_1802.field_8301) || player.method_6079().method_31574(class_1802.field_8301))) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean shouldTakeTotem(boolean isCrystalDanger) {
      float currentHp = mc.field_1724.method_6032() + mc.field_1724.method_6067();
      boolean isGliding = mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833) && mc.field_1724.method_6128();
      float hpThreshold = isGliding ? this.hpOnElytra.getValue().floatValue() : this.hp.getValue().floatValue();
      if (currentHp <= hpThreshold) {
         return true;
      } else if (isCrystalDanger) {
         return true;
      } else {
         float radius = this.crystalRadius.getValue().floatValue();
         double radiusSq = radius * radius;
         if (this.triggers.is("Обсидиан в руке")) {
            for (class_1657 player : mc.field_1687.method_18456()) {
               if (player != mc.field_1724
                  && mc.field_1724.method_5858(player) <= radiusSq
                  && (player.method_6047().method_31574(class_1802.field_8281) || player.method_6079().method_31574(class_1802.field_8281))) {
                  return true;
               }
            }
         }

         return this.triggers.is("Падения") && mc.field_1724.field_6017 >= this.fallHeight.getValue().floatValue() && !isGliding;
      }
   }

   private boolean hasTotemInOffhand() {
      return mc.field_1724.method_6079().method_31574(class_1802.field_8288);
   }

   private int findTotemSlot() {
      int normalTotem = -1;
      int enchantedTotem = -1;

      for (int i = 9; i < 45; i++) {
         class_1799 stack = mc.field_1724.field_7498.method_7611(i).method_7677();
         if (stack.method_31574(class_1802.field_8288)) {
            boolean isEnchanted = stack.method_7942();
            if (isEnchanted) {
               if (enchantedTotem == -1) {
                  enchantedTotem = i;
               }
            } else if (normalTotem == -1) {
               normalTotem = i;
            }
         }
      }

      if (this.saveEnchanted.isState()) {
         return normalTotem != -1 ? normalTotem : enchantedTotem;
      } else {
         return enchantedTotem != -1 ? enchantedTotem : normalTotem;
      }
   }

   private void performSwap() {
      int totemSlot = this.findTotemSlot();
      if (totemSlot != -1) {
         this.savedTotemSlot = totemSlot;
         this.doSwap(totemSlot);
         this.totemTakenByUs = true;
         mc.field_1724.field_3944.method_52787(new class_2815(0));
      }
   }

   private void performReturn() {
      if (!this.hasTotemInOffhand()) {
         this.totemTakenByUs = false;
      } else {
         if (!this.originalOffhandItem.method_7960()) {
            int slotToReturn = this.findSlotForItem(this.originalOffhandItem);
            if (slotToReturn != -1) {
               this.doSwap(slotToReturn);
            } else {
               if (this.savedTotemSlot == -1) {
                  this.savedTotemSlot = 9;
               }

               this.doSwap(this.savedTotemSlot);
            }
         } else {
            if (this.savedTotemSlot == -1) {
               this.savedTotemSlot = 9;
            }

            this.doSwap(this.savedTotemSlot);
         }

         this.totemTakenByUs = false;
         this.savedTotemSlot = -1;
         this.originalOffhandItem = class_1799.field_8037;
         mc.field_1724.field_3944.method_52787(new class_2815(0));
      }
   }

   private int findSlotForItem(class_1799 item) {
      if (item.method_7960()) {
         return -1;
      } else {
         for (int i = 9; i < 45; i++) {
            class_1799 stack = mc.field_1724.field_7498.method_7611(i).method_7677();
            if (class_1799.method_7984(stack, item) && class_1799.method_7973(stack, item)) {
               return i;
            }
         }

         return -1;
      }
   }

   private void doSwap(int slot) {
      if (this.swapVersion.is("1.16.5")) {
         this.doSwap1165(slot);
      } else {
         this.doSwap1214(slot);
      }
   }

   private void doSwap1214(int slot) {
      if (slot >= 36 && slot <= 44) {
         int hotbarSlot = slot - 36;
         mc.field_1761.method_2906(0, 45, hotbarSlot, class_1713.field_7791, mc.field_1724);
      } else {
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, 45, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
      }
   }

   private void doSwap1165(int slot) {
      mc.field_1761.method_2906(0, slot, 40, class_1713.field_7791, mc.field_1724);
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

   private boolean isCrystalRadiusVisible() {
      return this.triggers.is("Кристалл рядом") || this.triggers.is("Кристалл в руке") || this.triggers.is("Обсидиан в руке");
   }

   @Override
   public void onDisable() {
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      this.savedTotemSlot = -1;
      this.originalOffhandItem = class_1799.field_8037;
      this.totemTakenByUs = false;
      this.returnMode = false;
      this.needFastSwap = false;
      this.safeTicks = 0;
      this.restoreSprint();
      super.onDisable();
   }
}
