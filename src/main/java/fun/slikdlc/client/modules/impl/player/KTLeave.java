package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;

public class KTLeave extends Module {
   public static KTLeave INSTANCE = new KTLeave();
   private boolean hasGM;
   private double lastX;
   private double lastY;
   private double lastZ;
   private BindSetting bind = new BindSetting("Кнопка лива", -1);

   public KTLeave() {
      super("KTLeave", "Позволяет ливнуть с пвп прямо в кт", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.bind});
   }

   @EventLink
   public void onKey(EventBinding e) {
      if (mc.field_1724 != null) {
         if (e.getKey() == this.bind.getKey()) {
            this.hasGM = !this.hasGM;
            if (this.hasGM) {
               this.lastX = mc.field_1724.method_23317();
               this.lastY = mc.field_1724.method_23318();
               this.lastZ = mc.field_1724.method_23321();
               mc.field_1724.method_5814(mc.field_1724.method_23317() + 10.0, mc.field_1724.method_23318() + 10.0, mc.field_1724.method_23321() + 10.0);
            } else {
               mc.field_1724.method_5814(this.lastX, this.lastY, this.lastZ);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.hasGM = false;
   }
}
