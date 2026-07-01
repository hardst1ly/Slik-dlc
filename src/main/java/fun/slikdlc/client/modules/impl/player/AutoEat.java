package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.movement.Sprint;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_2815;
import net.minecraft.class_2868;
import net.minecraft.class_746;

public class AutoEat extends Module {
   public static final AutoEat INSTANCE = new AutoEat();
   private static final String BARITONE_API_CLASS = "baritone.api.BaritoneAPI";
   private final FloatSetting hungerBars = new FloatSetting("Плашки голода", 6.0F, 1.0F, 10.0F, 1.0F);
   private boolean eating;
   private boolean sprintPaused;
   private boolean swappedFromInventory;
   private int originalSlot = -1;
   private int swappedInventorySlot = -1;

   public AutoEat() {
      super("AutoEat", "Автоматически ест при низком голоде", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.hungerBars});
   }

   public static boolean shouldSuppressCombat() {
      return INSTANCE != null && INSTANCE.isEnable() && INSTANCE.eating;
   }

   @Override
   public void onDisable() {
      this.stopEating();
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1761 == null) {
         this.stopEating();
      } else if (mc.field_1755 != null) {
         this.stopEating();
      } else if (!mc.field_1724.method_31549().field_7477 && !mc.field_1724.method_7325()) {
         if (!this.eating) {
            if (!this.shouldStartEating()) {
               return;
            }

            this.eating = true;
            this.originalSlot = mc.field_1724.method_31548().field_7545;
         }

         this.tickEating();
      } else {
         this.stopEating();
      }
   }

   private void tickEating() {
      class_746 player = mc.field_1724;
      if (player == null) {
         this.stopEating();
      } else {
         this.pauseBaritone();
         if (!this.sprintPaused) {
            Sprint.pushPause(0L);
            this.sprintPaused = true;
         }

         mc.field_1690.field_1886.method_23481(false);
         if (!this.needsFood()) {
            if (!player.method_6115()) {
               this.stopEating();
            }
         } else if (!this.ensureFoodReady()) {
            this.stopEating();
         } else {
            class_1268 eatingHand = this.getEatingHand(player);
            if (eatingHand == null) {
               this.stopEating();
            } else {
               mc.field_1690.field_1904.method_23481(true);
               if (!player.method_6115() || player.method_6058() != eatingHand) {
                  mc.field_1761.method_2919(player, eatingHand);
               }
            }
         }
      }
   }

   private boolean shouldStartEating() {
      return this.needsFood() && !mc.field_1724.method_6115() && (this.isValidFood(mc.field_1724.method_6079()) || this.findFoodSlot() != -1);
   }

   private boolean needsFood() {
      return mc.field_1724 != null && mc.field_1724.method_7344().method_7586() < 20 && mc.field_1724.method_7344().method_7586() <= this.getFoodThreshold();
   }

   private int getFoodThreshold() {
      return Math.round(this.hungerBars.get()) * 2;
   }

   private boolean ensureFoodReady() {
      class_746 player = mc.field_1724;
      if (player == null) {
         return false;
      } else if (this.isValidFood(player.method_6079())) {
         return true;
      } else if (this.isValidFood(player.method_6047())) {
         return true;
      } else {
         int foodSlot = this.findFoodSlot();
         if (foodSlot == -1) {
            return false;
         } else if (foodSlot < 9) {
            this.swappedFromInventory = false;
            this.swappedInventorySlot = -1;
            this.selectHotbarSlot(foodSlot);
            return this.isValidFood(player.method_6047());
         } else {
            this.selectHotbarSlot(this.originalSlot == -1 ? player.method_31548().field_7545 : this.originalSlot);
            this.swapInventorySlotWithHotbar(foodSlot, player.method_31548().field_7545);
            this.swappedFromInventory = true;
            this.swappedInventorySlot = foodSlot;
            return this.isValidFood(player.method_6047());
         }
      }
   }

   private class_1268 getEatingHand(class_746 player) {
      if (player == null) {
         return null;
      } else if (this.isValidFood(player.method_6079())) {
         return class_1268.field_5810;
      } else {
         return this.isValidFood(player.method_6047()) ? class_1268.field_5808 : null;
      }
   }

   private int findFoodSlot() {
      class_746 player = mc.field_1724;
      if (player == null) {
         return -1;
      } else {
         int selected = player.method_31548().field_7545;
         if (this.isValidFood(player.method_31548().method_5438(selected))) {
            return selected;
         } else {
            for (int slot = 0; slot < 9; slot++) {
               if (slot != selected && this.isValidFood(player.method_31548().method_5438(slot))) {
                  return slot;
               }
            }

            for (int slotx = 9; slotx < 36; slotx++) {
               if (this.isValidFood(player.method_31548().method_5438(slotx))) {
                  return slotx;
               }
            }

            return -1;
         }
      }
   }

   private boolean isValidFood(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      } else {
         return !stack.method_31574(class_1802.field_8463) && !stack.method_31574(class_1802.field_8367) && !stack.method_31574(class_1802.field_8233)
            ? stack.method_7976() == class_1839.field_8950
            : false;
      }
   }

   private void selectHotbarSlot(int slot) {
      if (mc.field_1724 != null && slot >= 0 && slot <= 8 && mc.field_1724.method_31548().field_7545 != slot) {
         mc.field_1724.method_31548().field_7545 = slot;
         if (mc.method_1562() != null) {
            mc.method_1562().method_52787(new class_2868(slot));
         }
      }
   }

   private void swapInventorySlotWithHotbar(int inventorySlot, int hotbarSlot) {
      if (mc.field_1724 != null && mc.field_1761 != null && inventorySlot >= 9 && inventorySlot <= 35 && hotbarSlot >= 0 && hotbarSlot <= 8) {
         mc.field_1761.method_2906(0, inventorySlot, hotbarSlot, class_1713.field_7791, mc.field_1724);
         if (mc.method_1562() != null) {
            mc.method_1562().method_52787(new class_2815(0));
         }
      }
   }

   private void stopEating() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1904.method_23481(false);
      }

      if (this.sprintPaused) {
         Sprint.popPause();
         this.sprintPaused = false;
      }

      this.restoreHeldItem();
      this.eating = false;
   }

   private void restoreHeldItem() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.swappedFromInventory && this.swappedInventorySlot != -1) {
            int hotbarSlot = this.originalSlot == -1 ? mc.field_1724.method_31548().field_7545 : this.originalSlot;
            this.selectHotbarSlot(hotbarSlot);
            this.swapInventorySlotWithHotbar(this.swappedInventorySlot, hotbarSlot);
         }

         if (this.originalSlot != -1) {
            this.selectHotbarSlot(this.originalSlot);
         }

         this.resetSwapState();
      } else {
         this.resetSwapState();
      }
   }

   private void resetSwapState() {
      this.swappedFromInventory = false;
      this.swappedInventorySlot = -1;
      this.originalSlot = -1;
   }

   private void pauseBaritone() {
      try {
         Object baritone = getPrimaryBaritone();
         if (baritone == null) {
            this.cancelVanillaBreaking();
            return;
         }

         Object pathing = invoke(baritone, "getPathingBehavior");
         if (pathing == null || !Boolean.TRUE.equals(invoke(pathing, "hasPath"))) {
            this.cancelVanillaBreaking();
            return;
         }

         Object input = invoke(baritone, "getInputOverrideHandler");
         if (input != null) {
            input.getClass().getMethod("clearAllKeys").invoke(input);
            Object blockBreakHelper = input.getClass().getMethod("getBlockBreakHelper").invoke(input);
            if (blockBreakHelper != null) {
               blockBreakHelper.getClass().getMethod("stopBreakingBlock").invoke(blockBreakHelper);
            }
         }

         pathing.getClass().getMethod("requestPause").invoke(pathing);
         this.cancelVanillaBreaking();
      } catch (Throwable var5) {
         this.cancelVanillaBreaking();
      }
   }

   private void cancelVanillaBreaking() {
      try {
         if (mc.field_1761 != null) {
            mc.field_1761.method_2925();
         }
      } catch (Throwable var2) {
      }
   }

   private static Object getPrimaryBaritone() throws ReflectiveOperationException {
      Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
      Object provider = apiClass.getMethod("getProvider").invoke(null);
      return provider == null ? null : provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
   }

   private static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
      return target.getClass().getMethod(methodName).invoke(target);
   }
}
