package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBlockCollide;
import fun.slikdlc.client.modules.Module;
import net.minecraft.class_2246;

public class NoControllerWeb extends Module {
   public static NoControllerWeb INSTANCE = new NoControllerWeb();

   public NoControllerWeb() {
      super("NoControllerWeb", "Позволяет ломать и бить сквозь паутину", Module.ModuleCategory.COMBAT);
   }

   @EventLink
   public void onBlockCollide(EventBlockCollide e) {
      if (mc.field_1687 != null && e.getPos() != null) {
         if (mc.field_1687.method_8320(e.getPos()).method_26204() == class_2246.field_10343) {
            e.setCancelled(true);
         }
      }
   }
}
