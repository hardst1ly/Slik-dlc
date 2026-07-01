package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.combat.Aura;

public class AutoJump extends Module {
   public static AutoJump INSTANCE = new AutoJump();

   public AutoJump() {
      super("AutoJump", "Прыгает автоматически при ауре", Module.ModuleCategory.MOVEMENT);
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         Aura aura = ModuleClass.aura;
         if (aura != null && aura.isEnable()) {
            if (aura.getTarget() != null && mc.field_1724.method_24828()) {
               mc.field_1724.method_6043();
            }
         }
      }
   }
}
