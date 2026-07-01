package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import net.minecraft.class_2828.class_2830;

public class GrimNoFall extends Module {
   public static GrimNoFall INSTANCE = new GrimNoFall();

   public GrimNoFall() {
      super("NoFall", "Убирает урон от падения", Module.ModuleCategory.MOVEMENT);
   }

   @EventLink
   public void onUpdate(EventUpdate ignored) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         if (!mc.field_1724.method_24828() && mc.field_1724.field_6017 > 1.0F) {
            mc.method_1562()
               .method_52787(
                  new class_2830(
                     mc.field_1724.method_23317(),
                     mc.field_1724.method_23318() + 1.0E-9,
                     mc.field_1724.method_23321(),
                     mc.field_1724.method_36454(),
                     mc.field_1724.method_36455(),
                     true,
                     false
                  )
               );
            mc.field_1724.method_38785();
         }
      }
   }
}
