package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import net.minecraft.class_1293;
import net.minecraft.class_1294;

public class FullBright extends Module {
   public static FullBright INSTANCE = new FullBright();

   public FullBright() {
      super("FullBright", "Всегда светло", Module.ModuleCategory.RENDER);
   }

   @EventLink
   public void onUpdate(EventUpdate ignored) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         mc.field_1724.method_6092(new class_1293(class_1294.field_5925, 777, 1));
      }
   }

   @Override
   public void onDisable() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         mc.field_1724.method_6016(class_1294.field_5925);
         super.onDisable();
      }
   }
}
