package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.Removals;
import net.minecraft.class_1058;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4603.class})
public class InGameOverlayRendererMixin {
   public InGameOverlayRendererMixin() {
   }

   @Inject(
      method = {"method_23070"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void slikdlc$renderFireOverlay(class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Огонь")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_23068"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void slikdlc$renderInWallOverlay(class_1058 sprite, class_4587 matrices, class_4597 vertexConsumers, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Оверлей в блоке")) {
            ci.cancel();
         }
      }
   }
}
