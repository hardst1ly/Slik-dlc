package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.mixin.IMinecraftClientAccessor;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

public class FastExp extends Module {
   public static FastExp INSTANCE = new FastExp();

   public FastExp() {
      super("FastExp", "Позволяет бросать пузырьки опыта без задержки", Module.ModuleCategory.PLAYER);
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null) {
         class_1799 stack = mc.field_1724.method_6047();
         if (stack.method_31574(class_1802.field_8287)) {
            ((IMinecraftClientAccessor)mc).setItemUseCooldown(0);
         }
      }
   }
}
