package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.mixin.ILivingEntity;

public class NoJumpDelay extends Module {
   public static NoJumpDelay INSTANCE = new NoJumpDelay();

   public NoJumpDelay() {
      super("NoJumpDelay", "Убирает задержку на прыжок", Module.ModuleCategory.PLAYER);
   }

   @EventLink
   public void onEvent(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         ((ILivingEntity)mc.field_1724).setJumpingCooldown(0);
      }
   }
}
