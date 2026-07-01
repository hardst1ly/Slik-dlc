package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1713;
import net.minecraft.class_1738;
import net.minecraft.class_1799;

public class AutoArmor extends Module {
   public static AutoArmor INSTANCE = new AutoArmor();
   private final FloatSetting delay = new FloatSetting("Задержка", 25.0F, 1.0F, 1000.0F, 1.0F);
   private long lastEquipTime = 0L;

   public AutoArmor() {
      super("AutoArmor", "Автоматически одевает броню", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.delay});
   }

   @EventLink
   public void onEvent(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!this.isMoving()) {
            long currentTime = System.currentTimeMillis();
            if (!((float)(currentTime - this.lastEquipTime) < this.delay.get())) {
               for (int i = 0; i < 4; i++) {
                  class_1799 currentArmor = mc.field_1724.method_31548().method_7372(i);
                  if (currentArmor.method_7960()) {
                     for (int j = 0; j < 36; j++) {
                        class_1799 stack = mc.field_1724.method_31548().method_5438(j);
                        if (!stack.method_7960() && stack.method_7909() instanceof class_1738 armorItem && this.getArmorSlotIndex(armorItem) == i) {
                           int slotToEquip = j;
                           if (j < 9) {
                              slotToEquip = j + 36;
                           }

                           mc.field_1761.method_2906(0, slotToEquip, 0, class_1713.field_7794, mc.field_1724);
                           this.lastEquipTime = currentTime;
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isMoving() {
      return mc.field_1724.field_3913.field_3905 != 0.0F || mc.field_1724.field_3913.field_3907 != 0.0F;
   }

   private int getArmorSlotIndex(class_1738 armor) {
      String itemName = armor.toString().toLowerCase();
      if (itemName.contains("helmet") || itemName.contains("skull")) {
         return 3;
      } else if (itemName.contains("chestplate") || itemName.contains("tunic")) {
         return 2;
      } else if (itemName.contains("leggings") || itemName.contains("pants")) {
         return 1;
      } else {
         return !itemName.contains("boots") && !itemName.contains("shoes") ? 0 : 0;
      }
   }
}
