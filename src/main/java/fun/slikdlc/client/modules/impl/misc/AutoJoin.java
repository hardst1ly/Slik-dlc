package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_2678;
import net.minecraft.class_2868;
import net.minecraft.class_476;
import net.minecraft.class_7439;

public final class AutoJoin extends Module {
   public static AutoJoin INSTANCE = new AutoJoin();
   private static final long CLICK_DELAY_MS = 30L;
   private static final int NEXT_PAGE_SLOT = 44;
   private static final int MAX_PAGE_SWITCHES = 5;
   private final FloatSetting grief = new FloatSetting("Гриф", 5.0F, 1.0F, 64.0F, 1.0F);
   private final TimerUtils clickTimer = new TimerUtils();
   private final TimerUtils compassTimer = new TimerUtils();
   private boolean joining;
   private int pageSwitches;
   private int targetGrief;

   public AutoJoin() {
      super("AutoJoin", "Автоматически заходит на выбранный гриф", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.grief});
   }

   public void startJoinTo(int griefId) {
      this.grief.setValue(griefId);
      if (!this.isEnable()) {
         this.toggle();
      } else {
         this.startJoin();
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.startJoin();
   }

   @Override
   public void onDisable() {
      this.joining = false;
      this.pageSwitches = 0;
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (this.joining && mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (!(mc.field_1755 instanceof class_476)) {
            this.openServerSelector(false);
         } else {
            this.handleServerMenu();
         }
      }
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (this.joining && mc.field_1724 != null && mc.field_1687 != null && event.getType() == EventPacket.Type.RECEIVE) {
         if (event.getPacket() instanceof class_2678) {
            ChatUtils.sendMessage("Вход на гриф #" + this.targetGrief + ": успешно");
            this.joining = false;
            this.pageSwitches = 0;
         } else if (event.getPacket() instanceof class_7439 packet) {
            String message = packet.comp_763().getString();
            if (message.contains("Подождите несколько секунд перед повторным подключением")) {
               event.cancel();
            } else if (message.contains("К сожалению сервер переполнен")) {
               event.cancel();
               ChatUtils.sendMessage("Вход на гриф #" + this.targetGrief + ": неудачно");
            } else {
               this.openServerSelector(false);
            }
         }
      }
   }

   private void startJoin() {
      this.joining = true;
      this.pageSwitches = 0;
      this.targetGrief = Math.round(this.grief.get());
      this.clickTimer.reset();
      this.compassTimer.reset();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.openServerSelector(true);
      }
   }

   private void openServerSelector(boolean force) {
      if (force || this.compassTimer.finished(30L)) {
         if (mc.field_1724 != null && mc.field_1761 != null && mc.method_1562() != null) {
            int previousSlot = mc.field_1724.method_31548().field_7545;
            int slot = this.findCompassSlot();
            if (slot != -1) {
               this.pageSwitches = 0;
               mc.field_1724.method_31548().field_7545 = slot;
               mc.method_1562().method_52787(new class_2868(slot));
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               mc.field_1724.method_31548().field_7545 = previousSlot;
               mc.method_1562().method_52787(new class_2868(previousSlot));
               this.compassTimer.reset();
            }
         }
      }
   }

   private int findCompassSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8251) {
               return i;
            }
         }

         return -1;
      }
   }

   private void handleServerMenu() {
      if (mc.field_1755 instanceof class_476 screen) {
         if (this.clickTimer.finished(30L)) {
            String title = screen.method_25440().getString();
            class_1703 handler = screen.method_17577();
            if (title.contains("Выбор сервера")) {
               this.clickSlot(handler, 21);
               this.pageSwitches = 0;
               this.clickTimer.reset();
            } else if (!this.clickTargetGriefIfVisible(handler)) {
               if (this.targetGrief > 36 && this.pageSwitches < 5) {
                  class_1735 nextPageSlot = this.getSlot(handler, 44);
                  if (nextPageSlot != null && nextPageSlot.method_7681()) {
                     this.clickSlot(handler, 44);
                     this.pageSwitches++;
                     this.clickTimer.reset();
                  }
               }
            }
         }
      }
   }

   private boolean clickTargetGriefIfVisible(class_1703 handler) {
      String targetName = "ГРИФ #" + this.targetGrief + " (1.16.5+)";
      String targetPrefix = "ГРИФ #" + this.targetGrief;

      for (int slot = 0; slot < handler.field_7761.size(); slot++) {
         class_1735 containerSlot = handler.method_7611(slot);
         if (containerSlot != null && containerSlot.method_7681()) {
            String itemName = containerSlot.method_7677().method_7964().getString();
            if (itemName.equalsIgnoreCase(targetName) || itemName.toUpperCase().contains(targetPrefix)) {
               this.clickSlot(handler, slot);
               this.pageSwitches = 0;
               this.clickTimer.reset();
               return true;
            }
         }
      }

      return false;
   }

   private void clickSlot(class_1703 handler, int slot) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (slot >= 0 && slot < handler.field_7761.size()) {
            mc.field_1761.method_2906(handler.field_7763, slot, 0, class_1713.field_7790, mc.field_1724);
         }
      }
   }

   private class_1735 getSlot(class_1703 handler, int slot) {
      return slot >= 0 && slot < handler.field_7761.size() ? handler.method_7611(slot) : null;
   }
}
