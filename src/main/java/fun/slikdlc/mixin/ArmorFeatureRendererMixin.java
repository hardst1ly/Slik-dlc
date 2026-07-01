package fun.slikdlc.mixin;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import net.minecraft.class_10017;
import net.minecraft.class_10055;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_970.class})
public class ArmorFeatureRendererMixin implements QClient {
   public ArmorFeatureRendererMixin() {
   }

   @Inject(
      method = {"method_4199"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$hideArmor(
      class_4587 matrices, class_4597 vertexConsumers, int light, class_10017 state, float limbAngle, float limbDistance, CallbackInfo ci
   ) {
      if (!(state instanceof class_10055 playerState && ModuleClass.INSTANCE != null && mc.field_1687 != null)) {
         ;
      }
   }
}
