package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_10185;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_2815;
import net.minecraft.class_2848;
import net.minecraft.class_2851;
import net.minecraft.class_2868;
import net.minecraft.class_2848.class_2849;

public class ServerHelper extends Module {
   public static ServerHelper INSTANCE = new ServerHelper();
   private final ModeSetting mode = new ModeSetting("Режим", "Lony", "Lony", "Spooky");
   private final BindSetting featherKey = new BindSetting("Перышко", -1).visible(() -> this.mode.is("Lony"));
   private final BindSetting magmaKey = new BindSetting("Ливалка", -1).visible(() -> this.mode.is("Lony"));
   private final BindSetting cryingObsidianKey = new BindSetting("Трапка", -1).visible(() -> this.mode.is("Lony"));
   private final BindSetting clayKey = new BindSetting("Ливалка с платформой", -1).visible(() -> this.mode.is("Lony"));
   private final BindSetting disorientationKey = new BindSetting("Дезориентация", -1).visible(() -> this.mode.is("Spooky"));
   private final BindSetting trapKey = new BindSetting("Трапка", -1).visible(() -> this.mode.is("Spooky"));
   private final BindSetting plastKey = new BindSetting("Пласт", -1).visible(() -> this.mode.is("Spooky"));
   private final BindSetting pilKey = new BindSetting("Явная пыль", -1).visible(() -> this.mode.is("Spooky"));
   private final BindSetting snegKey = new BindSetting("Снег заморозки", -1).visible(() -> this.mode.is("Spooky"));
   private final BindSetting auraKey = new BindSetting("Божья аура", -1).visible(() -> this.mode.is("Spooky"));
   private class_1792 pendingItem;
   private ServerHelper.Action pendingAction;

   public ServerHelper() {
      super("ServerHelper", "Помощник для серверов", Module.ModuleCategory.MISC);
      this.addSettings(
         new Setting[]{
            this.mode,
            this.featherKey,
            this.magmaKey,
            this.cryingObsidianKey,
            this.clayKey,
            this.disorientationKey,
            this.trapKey,
            this.plastKey,
            this.pilKey,
            this.snegKey,
            this.auraKey
         }
      );
   }

   public boolean isSpookyMode() {
      return this.mode.is("Spooky");
   }

   public boolean isLonyMode() {
      return this.mode.is("Lony");
   }

   public List<ServerHelper.HelperBind> getLonyHelperBinds() {
      return List.of(
         new ServerHelper.HelperBind("Перышко", class_1802.field_8153, this.featherKey),
         new ServerHelper.HelperBind("Ливалка", class_1802.field_8135, this.magmaKey),
         new ServerHelper.HelperBind("Трапка", class_1802.field_22421, this.cryingObsidianKey),
         new ServerHelper.HelperBind("Ливалка с платформой", class_1802.field_19060, this.clayKey)
      );
   }

   public List<ServerHelper.HelperBind> getSpookyHelperBinds() {
      return List.of(
         new ServerHelper.HelperBind("Дезориентация", class_1802.field_8449, this.disorientationKey),
         new ServerHelper.HelperBind("Трапка", class_1802.field_22021, this.trapKey),
         new ServerHelper.HelperBind("Пласт", class_1802.field_8551, this.plastKey),
         new ServerHelper.HelperBind("Явная пыль", class_1802.field_8479, this.pilKey),
         new ServerHelper.HelperBind("Снег заморозки", class_1802.field_8543, this.snegKey),
         new ServerHelper.HelperBind("Божья аура", class_1802.field_8614, this.auraKey)
      );
   }

   @Override
   public void onEnable() {
      this.pendingItem = null;
      this.pendingAction = null;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.pendingItem = null;
      this.pendingAction = null;
      super.onDisable();
   }

   @EventLink
   public void onBinding(EventBinding event) {
      if (mc.field_1755 == null) {
         if (this.mode.is("Lony")) {
            if (event.getKey() == this.featherKey.getKey()) {
               this.pendingItem = class_1802.field_8153;
            } else if (event.getKey() == this.magmaKey.getKey()) {
               this.pendingItem = class_1802.field_8135;
            } else if (event.getKey() == this.cryingObsidianKey.getKey()) {
               this.pendingItem = class_1802.field_22421;
            } else if (event.getKey() == this.clayKey.getKey()) {
               this.pendingItem = class_1802.field_8696;
            }
         } else {
            if (event.getKey() == this.disorientationKey.getKey()) {
               this.pendingAction = ServerHelper.Action.DISORIENTATION;
            } else if (event.getKey() == this.trapKey.getKey()) {
               this.pendingAction = ServerHelper.Action.TRAP;
            } else if (event.getKey() == this.plastKey.getKey()) {
               this.pendingAction = ServerHelper.Action.PLAST;
            } else if (event.getKey() == this.pilKey.getKey()) {
               this.pendingAction = ServerHelper.Action.DUST;
            } else if (event.getKey() == this.snegKey.getKey()) {
               this.pendingAction = ServerHelper.Action.FREEZE_SNOW;
            } else if (event.getKey() == this.auraKey.getKey()) {
               this.pendingAction = ServerHelper.Action.AURA;
            }
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 == null || mc.field_1687 == null) {
         this.pendingItem = null;
         this.pendingAction = null;
      } else if (this.mode.is("Lony")) {
         if (this.pendingItem != null) {
            InventoryUtils.swapAndUseHvH(this.pendingItem);
            this.pendingItem = null;
         }
      } else if (this.pendingAction != null && mc.field_1761 != null) {
         this.useAction(this.pendingAction);
         this.pendingAction = null;
      }
   }

   private void useAction(ServerHelper.Action action) {
      if (mc.field_1724.method_7357().method_7904(new class_1799(action.item))) {
         ChatUtils.sendMessage("У предмета " + action.cooldownName + " есть кд");
      } else if (this.matchesAction(mc.field_1724.method_6047(), action)) {
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         mc.field_1724.method_6104(class_1268.field_5808);
         ChatUtils.sendMessage(action.successText);
      } else if (this.matchesAction(mc.field_1724.method_6079(), action)) {
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5810);
         mc.field_1724.method_6104(class_1268.field_5810);
         ChatUtils.sendMessage(action.successText);
      } else {
         int hotbarSlot = this.findMatchingSlot(action, 0, 8);
         if (hotbarSlot != -1) {
            int previousSlot = mc.field_1724.method_31548().field_7545;
            mc.field_1724.field_3944.method_52787(new class_2868(hotbarSlot));
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            mc.field_1724.method_6104(class_1268.field_5808);
            mc.field_1724.field_3944.method_52787(new class_2868(previousSlot));
            ChatUtils.sendMessage(action.successText);
         } else {
            int inventorySlot = this.findMatchingSlot(action, 9, 35);
            if (inventorySlot != -1) {
               this.useFromInventory(inventorySlot);
               ChatUtils.sendMessage(action.successText);
            } else {
               ChatUtils.sendMessage(action.failText);
            }
         }
      }
   }

   private void useFromInventory(int inventorySlot) {
      int previousSlot = mc.field_1724.method_31548().field_7545;
      int hotbarSlot = this.findTemporaryHotbarSlot();
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

      mc.field_1761.method_2906(0, inventorySlot, hotbarSlot, class_1713.field_7791, mc.field_1724);
      mc.field_1724.field_3944.method_52787(new class_2815(0));
      mc.field_1724.field_3944.method_52787(new class_2868(hotbarSlot));
      mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
      mc.field_1724.method_6104(class_1268.field_5808);
      mc.field_1724.field_3944.method_52787(new class_2868(previousSlot));
      mc.field_1761.method_2906(0, inventorySlot, hotbarSlot, class_1713.field_7791, mc.field_1724);
      mc.field_1724.field_3944.method_52787(new class_2815(0));
      if (wasSprinting) {
         mc.field_1724.field_3944.method_52787(new class_2851(mc.field_1724.field_3913.field_54155));
      }
   }

   private int findTemporaryHotbarSlot() {
      int fallback = 8;

      for (int slot = 0; slot < 9; slot++) {
         if (slot != mc.field_1724.method_31548().field_7545) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
            if (stack.method_7960()) {
               return slot;
            }

            if (stack.method_7976() == class_1839.field_8952) {
               fallback = slot;
            }
         }
      }

      return fallback;
   }

   private int findMatchingSlot(ServerHelper.Action action, int start, int end) {
      for (int slot = start; slot <= end; slot++) {
         if (this.matchesAction(mc.field_1724.method_31548().method_5438(slot), action)) {
            return slot;
         }
      }

      return -1;
   }

   private boolean matchesAction(class_1799 stack, ServerHelper.Action action) {
      return stack != null && !stack.method_7960() && stack.method_7909() == action.item
         ? stack.method_7964().getString().toLowerCase(Locale.ROOT).contains(action.query)
         : false;
   }

   private static enum Action {
      DISORIENTATION("дезориентация", "дезориентации", class_1802.field_8449, "Использовал дезориентацию!", "Дезориентация не найдена!"),
      TRAP("трапка", "трапки", class_1802.field_22021, "Использовал трапку!", "Трапка не найдена!"),
      PLAST("пласт", "пласта", class_1802.field_8551, "Использовал пласт!", "Пласт не найден!"),
      DUST("явная пыль", "пыли", class_1802.field_8479, "Использовал пыль!", "Пыль не найдена!"),
      FREEZE_SNOW("заморозка", "снега", class_1802.field_8543, "Использовал снег!", "Снег не найден!"),
      AURA("божья", "ауры", class_1802.field_8614, "Использовал ауру!", "Аура не найдена!");

      private final String query;
      private final String cooldownName;
      private final class_1792 item;
      private final String successText;
      private final String failText;

      private Action(String query, String cooldownName, class_1792 item, String successText, String failText) {
         this.query = query;
         this.cooldownName = cooldownName;
         this.item = item;
         this.successText = successText;
         this.failText = failText;
      }
   }

   public record HelperBind(String name, class_1792 item, BindSetting bind) {
   }
}
