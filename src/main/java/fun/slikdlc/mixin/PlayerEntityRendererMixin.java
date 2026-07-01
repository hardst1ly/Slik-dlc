package fun.slikdlc.mixin;

import fun.slikdlc.client.modules.impl.render.SatelliteFeatureRenderer;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_3883;
import net.minecraft.class_591;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1007.class})
public abstract class PlayerEntityRendererMixin {
   public PlayerEntityRendererMixin() {
   }

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void slikdlc$addShoulderPetFeature(class_5618 context, boolean slim, CallbackInfo ci) {
      class_3883<class_10055, class_591> rendererContext = (class_3883<class_10055, class_591>)this;
      ((LivingEntityRendererAccessor)this).slikdlc$addFeature(new SatelliteFeatureRenderer(rendererContext, context));
   }
}
