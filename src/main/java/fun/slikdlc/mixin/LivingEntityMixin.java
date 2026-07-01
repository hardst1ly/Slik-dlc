package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.SwingAnimations;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1309.class})
public abstract class LivingEntityMixin {
   public LivingEntityMixin() {
   }

   @Inject(
      method = {"method_6028"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
      if ((class_1309)(Object)this == class_310.method_1551().field_1724) {
         if (ModuleClass.INSTANCE != null) {
            SwingAnimations tweaks = ModuleClass.swingAnimations;
            if (tweaks != null && tweaks.isEnable() && tweaks.smoothEnabled.isState()) {
               cir.setReturnValue((int)tweaks.slowAnimationSpeed.get());
            }
         }
      }
   }
}
