package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.EntityESP;
import net.minecraft.class_10017;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_897.class})
public abstract class EntityRendererMixin<S extends class_10017> {
   public EntityRendererMixin() {
   }

   @Inject(
      method = {"method_3926"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$renderLabelIfPresent(S state, class_2561 text, class_4587 matrices, class_4597 vertexConsumers, int light, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         EntityESP esp = ModuleClass.entityESP;
         if (esp != null) {
            if (esp.shouldHideVanillaTags()) {
               ci.cancel();
            }
         }
      }
   }
}
