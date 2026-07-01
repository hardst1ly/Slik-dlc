package fun.slikdlc.mixin;

import fun.slikdlc.client.modules.impl.render.Chams;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_3882;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_976.class})
public abstract class HeadFeatureRendererMixin<S extends class_10042, M extends class_583<S> & class_3882> extends class_3887<S, M> {
   public HeadFeatureRendererMixin(class_3883<S, M> context) {
      super(context);
   }

   @Inject(
      method = {"method_17159"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderHead(class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, S livingEntityRenderState, float f, float g, CallbackInfo ci) {
      if (livingEntityRenderState instanceof class_10055 playerState) {
         class_310 mc = class_310.method_1551();
         if (mc != null && mc.field_1687 != null) {
            if (mc.field_1687.method_8469(playerState.field_53528) instanceof class_1657 player) {
               if (Chams.INSTANCE != null && Chams.INSTANCE.shouldHideItemsAndCape(player)) {
                  ci.cancel();
               }
            }
         }
      }
   }
}
