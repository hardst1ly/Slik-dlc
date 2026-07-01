package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import net.minecraft.class_2815;
import net.minecraft.class_490;

public class XCarry extends Module {
   public static XCarry INSTANCE = new XCarry();
   public BooleanSetting autoDisable = new BooleanSetting("Авто выкл", true);
   private boolean wasInInventory = false;

   public XCarry() {
      super("XCarry", "Дополнительные слоты", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.autoDisable});
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getPacket() instanceof class_2815 && mc.field_1755 instanceof class_490) {
            event.cancel();
            this.wasInInventory = true;
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.wasInInventory && mc.field_1755 == null) {
            if (this.autoDisable.isState()) {
               this.toggle();
            }

            this.wasInInventory = false;
         }
      }
   }
}
