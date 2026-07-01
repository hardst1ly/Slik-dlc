package fun.slikdlc.mixin;

import fun.slikdlc.api.utils.render.fonts.ttf.Fonts;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_310.class})
public class FontInitMixin {
   public FontInitMixin() {
   }

   @Inject(
      method = {"method_53465"},
      at = {@At("TAIL")}
   )
   private void onFinishedLoading(CallbackInfo ci) {
      Fonts.init();
      fun.slikdlc.api.utils.render.fonts.msdf.Fonts.init();
   }
}
