package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.Removals;
import net.minecraft.class_10017;
import net.minecraft.class_4538;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_898.class})
public class EntityRenderDispatcherMixin {
   public EntityRenderDispatcherMixin() {
   }

   @Inject(
      method = {"method_23166"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void slikdlc$renderShadow(
      class_4587 matrices, class_4597 vertexConsumers, class_10017 renderState, float opacity, float tickDelta, class_4538 world, float radius, CallbackInfo ci
   ) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Тени")) {
            ci.cancel();
         }
      }
   }
}
