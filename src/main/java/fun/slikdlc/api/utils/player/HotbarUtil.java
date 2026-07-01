package fun.slikdlc.api.utils.player;

import fun.slikdlc.api.QClient;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.class_1268;
import net.minecraft.class_1280;
import net.minecraft.class_1294;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1748;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1810;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_2248;
import net.minecraft.class_2868;
import net.minecraft.class_5134;
import net.minecraft.class_7923;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import net.minecraft.class_9285.class_9287;
import org.jetbrains.annotations.NotNull;

public final class HotbarUtil implements QClient {
   private static int cachedSlot = -1;

   public static int getItemCount(class_1792 item) {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int counter = 0;

         for (int i = 0; i < mc.field_1724.method_31548().method_5439(); i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_31574(item)) {
               counter += stack.method_7947();
            }
         }

         return counter;
      }
   }

   public static SlotSearchResult getAxe() {
      return findBest(itemStack -> itemStack.method_7909() instanceof class_1743, false);
   }

   public static SlotSearchResult getAxeHotBar() {
      return findBest(itemStack -> itemStack.method_7909() instanceof class_1743, true);
   }

   public static SlotSearchResult getPickAxe() {
      return findBest(itemStack -> itemStack.method_7909() instanceof class_1810, false);
   }

   public static SlotSearchResult getPickAxeHotbar() {
      return getPickAxeHotBar();
   }

   public static SlotSearchResult getPickAxeHotBar() {
      return findBest(itemStack -> itemStack.method_7909() instanceof class_1810, true);
   }

   public static SlotSearchResult getSword() {
      return findBest(itemStack -> itemStack.method_7909() instanceof class_1829, false);
   }

   public static SlotSearchResult getSwordHotBar() {
      return findBest(itemStack -> itemStack.method_7909() instanceof class_1829, true);
   }

   public static SlotSearchResult getSkull() {
      return findInHotBar(
         stack -> stack.method_31574(class_1802.field_8398)
            || stack.method_31574(class_1802.field_8791)
            || stack.method_31574(class_1802.field_8681)
            || stack.method_31574(class_1802.field_8575)
            || stack.method_31574(class_1802.field_8470)
      );
   }

   public static int getElytra() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (class_1799 stack : mc.field_1724.method_31548().field_7548) {
            if (stack.method_31574(class_1802.field_8833) && stack.method_7919() < stack.method_7936() - 1) {
               return -2;
            }
         }

         for (int i = 0; i < 36; i++) {
            class_1799 stackx = mc.field_1724.method_31548().method_5438(i);
            if (stackx.method_31574(class_1802.field_8833) && stackx.method_7919() < stackx.method_7936() - 1) {
               return i < 9 ? i + 36 : i;
            }
         }

         return -1;
      }
   }

   public static SlotSearchResult findInHotBar(HotbarUtil.Searcher searcher) {
      if (mc.field_1724 != null) {
         if (searcher.isValid(mc.field_1724.method_6079())) {
            return SlotSearchResult.inOffhand(mc.field_1724.method_6079());
         }

         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (searcher.isValid(stack)) {
               return new SlotSearchResult(i, true, stack);
            }
         }
      }

      return SlotSearchResult.notFound();
   }

   public static SlotSearchResult findItemInHotBar(List<class_1792> items) {
      return findInHotBar(stack -> items.contains(stack.method_7909()));
   }

   public static SlotSearchResult findItemInHotBar(class_1792... items) {
      return findItemInHotBar(Arrays.asList(items));
   }

   public static SlotSearchResult findInInventory(HotbarUtil.Searcher searcher) {
      if (mc.field_1724 != null) {
         for (int i = 35; i >= 0; i--) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (searcher.isValid(stack)) {
               return new SlotSearchResult(i, true, stack);
            }
         }
      }

      return SlotSearchResult.notFound();
   }

   public static SlotSearchResult findItemInInventory(List<class_1792> items) {
      return findInInventory(stack -> items.contains(stack.method_7909()));
   }

   public static SlotSearchResult findItemInInventory(class_1792... items) {
      return findItemInInventory(Arrays.asList(items));
   }

   public static SlotSearchResult findBlockInHotBar(@NotNull List<class_2248> blocks) {
      return findItemInHotBar(blocks.stream().<class_1792>map(class_2248::method_8389).toList());
   }

   public static SlotSearchResult findBlockInHotBar(class_2248... blocks) {
      return findItemInHotBar(Arrays.stream(blocks).<class_1792>map(class_2248::method_8389).toList());
   }

   public static SlotSearchResult findBlockInInventory(@NotNull List<class_2248> blocks) {
      return findItemInInventory(blocks.stream().<class_1792>map(class_2248::method_8389).toList());
   }

   public static SlotSearchResult findBlockInInventory(class_2248... blocks) {
      return findItemInInventory(Arrays.stream(blocks).<class_1792>map(class_2248::method_8389).toList());
   }

   public static void saveSlot() {
      if (mc.field_1724 != null) {
         cachedSlot = mc.field_1724.method_31548().field_7545;
      }
   }

   public static void returnSlot() {
      if (cachedSlot != -1) {
         switchTo(cachedSlot);
      }

      cachedSlot = -1;
   }

   public static void saveAndSwitchTo(int slot) {
      saveSlot();
      switchTo(slot);
   }

   public static void switchTo(int slot) {
      if (mc.field_1724 != null && mc.method_1562() != null && slot >= 0 && slot <= 8) {
         if (mc.field_1724.method_31548().field_7545 != slot) {
            mc.field_1724.method_31548().field_7545 = slot;
            mc.method_1562().method_52787(new class_2868(slot));
         }
      }
   }

   public static void switchToSilent(int slot) {
      if (mc.field_1724 != null && mc.method_1562() != null && slot >= 0 && slot <= 8) {
         mc.method_1562().method_52787(new class_2868(slot));
      }
   }

   public static SlotSearchResult getAntiWeaknessItem() {
      if (mc.field_1724 == null) {
         return SlotSearchResult.notFound();
      } else {
         class_1792 mainHand = mc.field_1724.method_6047().method_7909();
         return !(mainHand instanceof class_1829)
               && !(mainHand instanceof class_1810)
               && !(mainHand instanceof class_1743)
               && !(mainHand instanceof class_1821)
            ? findInHotBar(
               stack -> stack.method_7909() instanceof class_1829
                  || stack.method_7909() instanceof class_1810
                  || stack.method_7909() instanceof class_1743
                  || stack.method_7909() instanceof class_1821
            )
            : new SlotSearchResult(mc.field_1724.method_31548().field_7545, true, mc.field_1724.method_6047());
      }
   }

   public static float getHitDamage(@NotNull class_1799 weapon, class_1657 entity) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float baseDamage = getBaseAttackDamage(weapon);
         if (mc.field_1724.field_6017 > 0.0F) {
            baseDamage += baseDamage / 2.0F;
         }

         if (mc.field_1724.method_6059(class_1294.field_5910)) {
            int strength = Objects.requireNonNull(mc.field_1724.method_6112(class_1294.field_5910)).method_5578() + 1;
            baseDamage += 3.0F * strength;
         }

         return class_1280.method_5496(
            entity, baseDamage, mc.field_1687.method_48963().method_48830(), entity.method_6096(), (float)entity.method_45325(class_5134.field_23725)
         );
      } else {
         return 0.0F;
      }
   }

   public static SlotSearchResult findBedInHotBar() {
      return findInHotBar(stack -> stack.method_7909() instanceof class_1748);
   }

   public static SlotSearchResult findBed() {
      return findInInventory(stack -> stack.method_7909() instanceof class_1748);
   }

   public static class_1792 getItem(String name) {
      if (name == null) {
         return class_1802.field_8162;
      } else {
         String normalized = name.toLowerCase();

         for (class_2248 block : class_7923.field_41175) {
            if (block.method_63499().replace("block.minecraft.", "").equals(normalized)) {
               return class_1792.method_7867(block);
            }
         }

         for (class_1792 item : class_7923.field_41178) {
            if (item.method_7876().replace("item.minecraft.", "").equals(normalized)) {
               return item;
            }
         }

         return class_1802.field_8831;
      }
   }

   public static int getBedsCount() {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int counter = 0;

         for (int i = 0; i < mc.field_1724.method_31548().method_5439(); i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() instanceof class_1748) {
               counter += stack.method_7947();
            }
         }

         return counter;
      }
   }

   private static SlotSearchResult findBest(HotbarUtil.Searcher searcher, boolean hotbarOnly) {
      if (mc.field_1724 == null) {
         return SlotSearchResult.notFound();
      } else {
         int bestSlot = -1;
         float bestDamage = 0.0F;
         int end = hotbarOnly ? 8 : 35;

         for (int i = 0; i <= end; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (searcher.isValid(stack)) {
               float damage = getBaseAttackDamage(stack);
               if (damage > bestDamage) {
                  bestDamage = damage;
                  bestSlot = i;
               }
            }
         }

         return bestSlot == -1 ? SlotSearchResult.notFound() : new SlotSearchResult(bestSlot, true, mc.field_1724.method_31548().method_5438(bestSlot));
      }
   }

   private static float getBaseAttackDamage(class_1799 stack) {
      class_9285 component = (class_9285)stack.method_57825(class_9334.field_49636, class_9285.field_49326);
      double damage = 1.0;

      for (class_9287 entry : component.comp_2393()) {
         if (entry.comp_2395().equals(class_5134.field_23721)) {
            damage += entry.comp_2396().comp_2449();
         }
      }

      return (float)damage;
   }

   public static boolean isHolding(class_1792 item) {
      return mc.field_1724 != null && (mc.field_1724.method_6047().method_31574(item) || mc.field_1724.method_6079().method_31574(item));
   }

   public static class_1268 getHand(class_1792 item) {
      if (mc.field_1724 == null) {
         return null;
      } else if (mc.field_1724.method_6079().method_31574(item)) {
         return class_1268.field_5810;
      } else {
         return mc.field_1724.method_6047().method_31574(item) ? class_1268.field_5808 : null;
      }
   }

   @Generated
   private HotbarUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public interface Searcher {
      boolean isValid(class_1799 var1);
   }
}
