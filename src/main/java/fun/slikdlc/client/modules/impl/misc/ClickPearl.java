package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1802;

public class ClickPearl extends Module {
   public static ClickPearl INSTANCE = new ClickPearl();
   private final BindSetting keyToPearl = new BindSetting("Кнопка", -1);
   private final BooleanSetting bypass = new BooleanSetting("Обход", true);
   private boolean use;

   public ClickPearl() {
      super("ClickPearl", "Кидает перку по внутреннему бинду", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.keyToPearl, this.bypass});
   }

   @Override
   public void onEnable() {
      this.use = false;
      super.onEnable();
   }

   @EventLink
   public void onEvent(EventBinding event) {
      if (mc.field_1755 == null) {
         if (event.getKey() == this.keyToPearl.getKey()) {
            this.use = true;
         }
      }
   }

   @EventLink
   public void onEvent(EventUpdate event) {
      if (this.use) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            int oldSlot = mc.field_1724.method_31548().field_7545;
            int pearlSlot = InventoryUtils.find(class_1802.field_8634, 0, 36);
            if (pearlSlot == -1) {
               this.use = false;
            } else {
               if (pearlSlot > 9) {
                  mc.field_1724.method_5728(false);
               }

               if (this.bypass.isState()) {
                  mc.field_1724.method_31548().field_7545 = pearlSlot;
                  mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                  mc.field_1724.method_31548().field_7545 = oldSlot;
               } else {
                  InventoryUtils.swapAndUseHvH(class_1802.field_8634);
               }

               this.use = false;
            }
         } else {
            this.use = false;
         }
      }
   }
}
