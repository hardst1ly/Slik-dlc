package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1708;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3965;

public class AutoBrew extends Module {
   public static AutoBrew INSTANCE = new AutoBrew();
   private final ModeSetting potionType = new ModeSetting("Тип зелья", "Огнестойкость", "Огнестойкость", "Невидимость", "Скорость", "Сила");
   private final BooleanSetting redstoneExtend = new BooleanSetting("Удлинение (Редстоун)", true);
   private final BooleanSetting glowstoneUpgrade = new BooleanSetting("Усиление (Светопыль)", false);
   private final BooleanSetting infiniteMode = new BooleanSetting("Бесконечный режим", false);
   private final BooleanSetting searchChest = new BooleanSetting("Искать сундук", true);
   private final FloatSetting searchRadius = new FloatSetting("Радиус поиска", 5.0F, 3.0F, 10.0F, 0.5F);
   private final FloatSetting brewTime = new FloatSetting("Время варки (сек)", 20.0F, 10.0F, 40.0F, 1.0F);
   private final TimerUtils brewTimer = new TimerUtils();
   private final TimerUtils actionTimer = new TimerUtils();
   private AutoBrew.BrewState currentState = AutoBrew.BrewState.IDLE;
   private class_2338 brewingStandPos = null;
   private class_2338 chestPos = null;
   private int cycles = 0;
   private boolean isWorking = false;

   public AutoBrew() {
      super("AutoBrew", "Автоматическая варка зелий", Module.ModuleCategory.MISC);
      this.addSettings(
         new Setting[]{this.potionType, this.redstoneExtend, this.glowstoneUpgrade, this.infiniteMode, this.searchChest, this.searchRadius, this.brewTime}
      );
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.currentState = AutoBrew.BrewState.SEARCHING_STAND;
         this.brewingStandPos = null;
         this.chestPos = null;
         this.cycles = 0;
         this.isWorking = true;
         this.sendMessage("§a[AutoBrew] Запуск! Тип: " + this.potionType.getCurrent());
      } else {
         this.toggle();
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.isWorking = false;
      this.currentState = AutoBrew.BrewState.IDLE;
      this.sendMessage("§c[AutoBrew] Остановлено! Циклов: " + this.cycles);
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.isWorking) {
         switch (this.currentState) {
            case SEARCHING_STAND:
               this.searchBrewingStand();
               break;
            case SEARCHING_CHEST:
               if (this.searchChest.isState()) {
                  this.searchChestWithItems();
               } else {
                  this.currentState = AutoBrew.BrewState.OPENING_STAND;
               }
               break;
            case OPENING_STAND:
               this.openBrewingStand();
               break;
            case PLACING_INGREDIENTS:
               this.placeIngredients();
               break;
            case BREWING:
               if (this.brewTimer.finished((long)(this.brewTime.getValue().floatValue() * 1000.0F))) {
                  this.currentState = AutoBrew.BrewState.COLLECTING;
               }
               break;
            case COLLECTING:
               this.collectPotions();
               break;
            case FINISHED:
               this.cycles++;
               this.sendMessage("§a[AutoBrew] Цикл #" + this.cycles + " завершён!");
               if (this.infiniteMode.isState()) {
                  this.currentState = AutoBrew.BrewState.OPENING_STAND;
                  this.actionTimer.reset();
               } else {
                  this.toggle();
               }
         }
      }
   }

   private void searchBrewingStand() {
      if (this.brewingStandPos != null) {
         this.currentState = AutoBrew.BrewState.SEARCHING_CHEST;
      } else {
         int radius = (int)this.searchRadius.getValue().floatValue();
         class_2338 playerPos = mc.field_1724.method_24515();

         for (int x = -radius; x <= radius; x++) {
            for (int y = -3; y <= 3; y++) {
               for (int z = -radius; z <= radius; z++) {
                  class_2338 pos = playerPos.method_10069(x, y, z);
                  if (mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10333) {
                     this.brewingStandPos = pos;
                     this.sendMessage("§a[AutoBrew] Найдена варочная стойка: " + pos.method_23854());
                     this.currentState = AutoBrew.BrewState.SEARCHING_CHEST;
                     return;
                  }
               }
            }
         }

         this.sendMessage("§c[AutoBrew] Варочная стойка не найдена!");
         this.toggle();
      }
   }

   private void searchChestWithItems() {
      if (this.chestPos != null) {
         this.currentState = AutoBrew.BrewState.OPENING_STAND;
      } else {
         int radius = (int)this.searchRadius.getValue().floatValue() + 5;
         class_2338 playerPos = mc.field_1724.method_24515();

         for (int x = -radius; x <= radius; x++) {
            for (int y = -5; y <= 5; y++) {
               for (int z = -radius; z <= radius; z++) {
                  class_2338 pos = playerPos.method_10069(x, y, z);
                  class_2248 block = mc.field_1687.method_8320(pos).method_26204();
                  if (block == class_2246.field_10034 || block == class_2246.field_10380 || block == class_2246.field_16328) {
                     this.chestPos = pos;
                     this.sendMessage("§a[AutoBrew] Найден сундук: " + pos.method_23854());
                     this.currentState = AutoBrew.BrewState.OPENING_STAND;
                     return;
                  }
               }
            }
         }

         this.sendMessage("§e[AutoBrew] Сундук не найден, продолжаем с инвентарём");
         this.currentState = AutoBrew.BrewState.OPENING_STAND;
      }
   }

   private void openBrewingStand() {
      if (this.brewingStandPos == null) {
         this.currentState = AutoBrew.BrewState.SEARCHING_STAND;
      } else if (this.actionTimer.finished(500L)) {
         if (mc.field_1724.field_7512 instanceof class_1708) {
            this.currentState = AutoBrew.BrewState.PLACING_INGREDIENTS;
            this.actionTimer.reset();
         } else {
            class_243 hitVec = class_243.method_24953(this.brewingStandPos);
            class_3965 hitResult = new class_3965(hitVec, class_2350.field_11036, this.brewingStandPos, false);
            mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hitResult);
            this.actionTimer.reset();
         }
      }
   }

   private void placeIngredients() {
      if (!(mc.field_1724.field_7512 instanceof class_1708)) {
         this.currentState = AutoBrew.BrewState.OPENING_STAND;
      } else if (this.actionTimer.finished(250L)) {
         class_1708 handler = (class_1708)mc.field_1724.field_7512;
         AutoBrew.PotionRecipe recipe = this.getRecipe();
         if (recipe == null) {
            this.sendMessage("§c[AutoBrew] Неизвестный рецепт!");
            this.toggle();
         } else {
            for (int i = 0; i < 3; i++) {
               class_1799 slotStack = handler.method_7611(i).method_7677();
               if (slotStack.method_7960() || !slotStack.method_31574(class_1802.field_8574)) {
                  int bottleSlot = this.findItemInInventory(class_1802.field_8574);
                  if (bottleSlot != -1) {
                     mc.field_1761.method_2906(handler.field_7763, bottleSlot, 0, class_1713.field_7794, mc.field_1724);
                  }
               }
            }

            class_1799 fuelSlot = handler.method_7611(3).method_7677();
            if (fuelSlot.method_7960() || !fuelSlot.method_31574(recipe.mainIngredient)) {
               int ingredientSlot = this.findItemInInventory(recipe.mainIngredient);
               if (ingredientSlot != -1) {
                  mc.field_1761.method_2906(handler.field_7763, ingredientSlot, 0, class_1713.field_7794, mc.field_1724);
               }
            }

            if (recipe.modifier != null) {
               int modifierSlot = this.findItemInInventory(recipe.modifier);
               if (modifierSlot != -1) {
                  mc.field_1761.method_2906(handler.field_7763, modifierSlot, 0, class_1713.field_7794, mc.field_1724);
               }
            }

            this.currentState = AutoBrew.BrewState.BREWING;
            this.brewTimer.reset();
            this.sendMessage("§e[AutoBrew] Варка началась...");
            mc.field_1724.method_7346();
            this.actionTimer.reset();
         }
      }
   }

   private void collectPotions() {
      if (this.actionTimer.finished(500L)) {
         if (!(mc.field_1724.field_7512 instanceof class_1708 handler)) {
            this.openBrewingStand();
         } else {
            for (int i = 0; i < 3; i++) {
               class_1799 slotStack = handler.method_7611(i).method_7677();
               if (!slotStack.method_7960()) {
                  mc.field_1761.method_2906(handler.field_7763, i, 0, class_1713.field_7794, mc.field_1724);
               }
            }

            mc.field_1724.method_7346();
            this.currentState = AutoBrew.BrewState.FINISHED;
            this.actionTimer.reset();
         }
      }
   }

   private int findItemInInventory(class_1792 item) {
      for (int i = 0; i < mc.field_1724.method_31548().method_5439(); i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (stack.method_31574(item)) {
            return i < 9 ? i + 36 : i;
         }
      }

      return -1;
   }

   private AutoBrew.PotionRecipe getRecipe() {
      String type = this.potionType.getCurrent();
      class_1792 modifier = null;
      if (this.redstoneExtend.isState()) {
         modifier = class_1802.field_8725;
      } else if (this.glowstoneUpgrade.isState()) {
         modifier = class_1802.field_8601;
      }

      switch (type) {
         case "Огнестойкость":
            return new AutoBrew.PotionRecipe(class_1802.field_8135, modifier);
         case "Невидимость":
            return new AutoBrew.PotionRecipe(class_1802.field_8711, modifier);
         case "Скорость":
            return new AutoBrew.PotionRecipe(class_1802.field_8479, modifier);
         case "Сила":
            return new AutoBrew.PotionRecipe(class_1802.field_8183, modifier);
         default:
            return null;
      }
   }

   private void sendMessage(String message) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_7353(class_2561.method_43470(message), false);
      }
   }

   private static enum BrewState {
      IDLE,
      SEARCHING_STAND,
      SEARCHING_CHEST,
      OPENING_STAND,
      PLACING_INGREDIENTS,
      BREWING,
      COLLECTING,
      FINISHED;

      private BrewState() {
      }
   }

   private static class PotionRecipe {
      final class_1792 mainIngredient;
      final class_1792 modifier;

      PotionRecipe(class_1792 mainIngredient, class_1792 modifier) {
         this.mainIngredient = mainIngredient;
         this.modifier = modifier;
      }
   }
}
