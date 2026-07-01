package fun.slikdlc.mixin;

import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventAttackEntity;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_636.class})
public abstract class ClientPlayerInteractionManagerMixin {
   public ClientPlayerInteractionManagerMixin() {
   }

   @Inject(
      method = {"method_2918"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void attackEntity(class_1657 player, class_1297 target, CallbackInfo ci) {
      try {
         if (player != null && target != null) {
            EventAttackEntity event = new EventAttackEntity(player, target);
            EventInvoker.invoke(event);
            if (event.isCancelled()) {
               ci.cancel();
            }
         }
      } catch (Exception var5) {
      }
   }
}
