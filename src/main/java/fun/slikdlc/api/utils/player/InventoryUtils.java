package fun.slikdlc.api.utils.player;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import lombok.Generated;
import net.minecraft.class_10185;
import net.minecraft.class_10192;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1738;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2815;
import net.minecraft.class_2848;
import net.minecraft.class_2851;
import net.minecraft.class_2868;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import net.minecraft.class_2848.class_2849;

public final class InventoryUtils implements QClient {
   public static int getItemSlot(class_1792 input) {
      for (class_1799 stack : mc.field_1724.method_5661()) {
         if (stack.method_7909() == input) {
            return -2;
         }
      }

      int slot = -1;

      for (int i = 0; i < 36; i++) {
         class_1799 s = mc.field_1724.method_31548().method_5438(i);
         if (s.method_7909() == input) {
            slot = i;
            break;
         }
      }

      if (slot < 9 && slot != -1) {
         slot += 36;
      }

      return slot;
   }

   public static int getEnchantmentLevel(class_1799 stack, class_5321<class_1887> enchantmentKey) {
      class_9304 enchantments = (class_9304)stack.method_57825(class_9334.field_49633, class_9304.field_49385);

      for (class_6880<class_1887> enchantment : enchantments.method_57534()) {
         if (enchantment.method_40225(enchantmentKey)) {
            return enchantments.method_57536(enchantment);
         }
      }

      return 0;
   }

   public static int findBestElytraSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         int bestSlot = -1;
         double bestScore = -1.0;

         for (int slot = 0; slot < 36; slot++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7909() == class_1802.field_8833) {
               int protection = getEnchantmentLevel(stack, class_1893.field_9111);
               int unbreaking = getEnchantmentLevel(stack, class_1893.field_9119);
               int mending = getEnchantmentLevel(stack, class_1893.field_9101);
               int maxDurability = stack.method_7936();
               int currentDamage = stack.method_7919();
               double durabilityRatio = (double)(maxDurability - currentDamage) / maxDurability;
               double score = protection * 100 + unbreaking * 10 + (mending > 0 ? 1 : 0) + durabilityRatio * 10.0;
               if (score > bestScore) {
                  bestScore = score;
                  bestSlot = slot;
               }
            }
         }

         return bestSlot;
      }
   }

   public static int findBestChestplateSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         int bestSlot = -1;
         double bestScore = -1.0;

         for (int slot = 0; slot < 36; slot++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7909() instanceof class_1738 armor) {
               class_10192 equippable = (class_10192)stack.method_57824(class_9334.field_54196);
               if (equippable != null && equippable.comp_3174() == class_1304.field_6174) {
                  int protection = getEnchantmentLevel(stack, class_1893.field_9111);
                  int unbreaking = getEnchantmentLevel(stack, class_1893.field_9119);
                  int mending = getEnchantmentLevel(stack, class_1893.field_9101);
                  int priority = getChestplatePriority(armor);
                  int maxDamage = stack.method_7936();
                  int damage = stack.method_7919();
                  double durabilityRatio = maxDamage == 0 ? 1.0 : (double)(maxDamage - damage) / maxDamage;
                  double score = priority * 10000.0 + protection * 100.0 + unbreaking * 10.0 + (mending > 0 ? 1 : 0) + durabilityRatio * 10.0;
                  if (score > bestScore) {
                     bestScore = score;
                     bestSlot = slot;
                  }
               }
            }
         }

         return bestSlot;
      }
   }

   public static int getChestplatePriority(class_1792 item) {
      if (item == class_1802.field_22028) {
         return 5;
      } else if (item == class_1802.field_8058) {
         return 4;
      } else if (item == class_1802.field_8523) {
         return 3;
      } else if (item == class_1802.field_8678) {
         return 2;
      } else if (item == class_1802.field_8873) {
         return 2;
      } else {
         return item == class_1802.field_8577 ? 1 : 0;
      }
   }

   public static int find(class_1792 item, int start, int end) {
      if (mc.field_1724 != null) {
         for (int i = end; i >= start; i--) {
            if (mc.field_1724.field_7512.field_7763 != 0 && mc.field_1724.field_7512.method_7611(i).method_7677().method_7909() == item) {
               return i;
            }

            if (mc.field_1724.field_7512.field_7763 == 0 && mc.field_1724.method_31548().method_5438(i).method_7909() == item) {
               return i;
            }
         }
      }

      return -1;
   }

   public static void swapAndUseHvH(class_1792 item) {
      int slot = find(item, 9, 45);
      int slotHotbar = find(item, 0, 8);
      int previousSlot = mc.field_1724.method_31548().field_7545;
      boolean isUsingItem = mc.field_1724.method_6115();
      if (mc.field_1724.method_6047().method_7909() == item) {
         if (!isUsingItem) {
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         }
      } else if (mc.field_1724.method_6079().method_7909() == item) {
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5810);
      } else if (isUsingItem) {
         if (slotHotbar != -1) {
            mc.field_1761.method_2906(0, 36 + slotHotbar, 40, class_1713.field_7791, mc.field_1724);
            mc.field_1724.field_3944.method_52787(new class_2815(0));
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5810);
            mc.field_1761.method_2906(0, 36 + slotHotbar, 40, class_1713.field_7791, mc.field_1724);
            mc.field_1724.field_3944.method_52787(new class_2815(0));
         } else if (slot != -1) {
            mc.field_1761.method_2906(0, slot, 40, class_1713.field_7791, mc.field_1724);
            mc.field_1724.field_3944.method_52787(new class_2815(0));
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5810);
            mc.field_1761.method_2906(0, slot, 40, class_1713.field_7791, mc.field_1724);
            mc.field_1724.field_3944.method_52787(new class_2815(0));
         }
      } else if (slotHotbar != -1) {
         mc.field_1724.field_3944.method_52787(new class_2868(slotHotbar));
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         mc.field_1724.field_3944.method_52787(new class_2868(previousSlot));
      } else {
         if (slot != -1) {
            int slotCorrectable = -1;

            for (int slotNone = 0; slotNone < 8; slotNone++) {
               class_1799 stack = mc.field_1724.method_31548().method_5438(slotNone);
               if (stack.method_7960()) {
                  slotCorrectable = slotNone;
                  break;
               }

               class_1839 action = stack.method_7976();
               if (action == class_1839.field_8952) {
                  slotCorrectable = slotNone;
               }
            }

            boolean wasSprinting = false;
            if (mc.field_1724.method_5624()) {
               mc.field_1724.field_3944.method_52787(new class_2851(new class_10185(false, false, false, false, false, false, false)));
               mc.field_1724.method_5728(false);
               mc.field_1724.field_3944.method_52787(new class_2848(mc.field_1724, class_2849.field_12985));
               if (!ModuleClass.sprint.isEnable()) {
                  mc.field_1690.field_1867.method_23481(false);
               }

               wasSprinting = true;
            }

            if (slotCorrectable == -1) {
               mc.field_1761.method_2906(0, slot, 8, class_1713.field_7791, mc.field_1724);
               mc.field_1724.field_3944.method_52787(new class_2815(0));
               mc.field_1724.field_3944.method_52787(new class_2868(8));
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               mc.field_1724.field_3944.method_52787(new class_2868(previousSlot));
            } else {
               mc.field_1761.method_2906(0, slot, slotCorrectable, class_1713.field_7791, mc.field_1724);
               mc.field_1724.field_3944.method_52787(new class_2815(0));
               mc.field_1724.field_3944.method_52787(new class_2868(slotCorrectable));
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               mc.field_1724.field_3944.method_52787(new class_2868(previousSlot));
               mc.field_1761.method_2906(0, slot, slotCorrectable, class_1713.field_7791, mc.field_1724);
               mc.field_1724.field_3944.method_52787(new class_2815(0));
            }

            if (wasSprinting) {
               mc.field_1724.field_3944.method_52787(new class_2851(mc.field_1724.field_3913.field_54155));
            }
         }
      }
   }

   @Generated
   private InventoryUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
