package fun.slikdlc.mixin;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import net.minecraft.class_10055;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_972;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_972.class})
public class CapeFeatureRendererMixin implements QClient {
   public CapeFeatureRendererMixin() {
   }

   @Inject(
      method = {"method_4177"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$hideCape(
      class_4587 matrices, class_4597 vertexConsumers, int light, class_10055 playerState, float limbAngle, float limbDistance, CallbackInfo ci
   ) {
      if (ModuleClass.INSTANCE == null || mc.field_1687 == null) {
         ;
      }
   }
}
