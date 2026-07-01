package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2560;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2560.class})
public class CobwebBlockMixin {
   public CobwebBlockMixin() {
   }

   @Inject(
      method = {"method_9548"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onEntityCollision(class_2680 state, class_1937 world, class_2338 pos, class_1297 entity, CallbackInfo ci) {
      if (ModuleClass.noWeb.isEnable() && ModuleClass.noWeb.web.is("Коллизия")) {
         ci.cancel();
      }
   }
}
