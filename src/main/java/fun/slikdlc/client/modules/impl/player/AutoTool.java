package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_3965;

public class AutoTool extends Module {
   public static AutoTool INSTANCE = new AutoTool();
   private final BooleanSetting packet = new BooleanSetting("Пакетный", false);
   private final BooleanSetting silent = new BooleanSetting("Видно только для других людей", false);
   private int previousSlot = -1;

   public AutoTool() {
      super("AutoTool", "При копании берет лучший предмет", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.packet, this.silent});
   }

   @EventLink
   public void onEvent(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null && !mc.field_1724.method_7337()) {
         if (mc.field_1761.method_2923()) {
            if (this.previousSlot == -1) {
               this.previousSlot = mc.field_1724.method_31548().field_7545;
            }

            int toolSlot = this.findOptimalTool();
            if (toolSlot != -1) {
               this.switchToSlot(toolSlot);
            }
         } else if (this.previousSlot != -1) {
            this.switchToSlot(this.previousSlot);
            this.previousSlot = -1;
         }
      } else {
         this.previousSlot = -1;
      }
   }

   private void switchToSlot(int slot) {
      if (slot >= 0 && slot <= 8) {
         if (mc.field_1724.method_31548().field_7545 != slot) {
            if (this.silent.isState()) {
               mc.method_1562().method_52787(new class_2868(slot));
            } else if (this.packet.isState()) {
               mc.field_1724.method_31548().field_7545 = slot;
               mc.method_1562().method_52787(new class_2868(slot));
            } else {
               mc.field_1724.method_31548().field_7545 = slot;
            }
         }
      }
   }

   private int findOptimalTool() {
      if (mc.field_1765 instanceof class_3965 blockHitResult) {
         class_2680 blockState = mc.field_1687.method_8320(blockHitResult.method_17777());
         return this.findBestToolSlot(blockState);
      } else {
         return -1;
      }
   }

   private int findBestToolSlot(class_2680 blockState) {
      int bestSlot = -1;
      float bestSpeed = 1.0F;

      for (int i = 0; i < 9; i++) {
         float speed = mc.field_1724.method_31548().method_5438(i).method_7924(blockState);
         if (speed > bestSpeed) {
            bestSpeed = speed;
            bestSlot = i;
         }
      }

      return bestSlot;
   }

   @Override
   public void onDisable() {
      this.previousSlot = -1;
      super.onDisable();
   }
}
