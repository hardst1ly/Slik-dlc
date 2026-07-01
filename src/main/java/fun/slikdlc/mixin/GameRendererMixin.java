package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.Removals;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_757.class})
public class GameRendererMixin {
   public GameRendererMixin() {
   }

   @Inject(
      method = {"method_3189"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$hideTotemAnimation(class_1799 stack, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null && stack != null && stack.method_31574(class_1802.field_8288)) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isTotemAnimationDisabled()) {
            ci.cancel();
         }
      }
   }
}
