package fun.slikdlc.mixin;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.implement.EventChunkReload;
import fun.slikdlc.api.utils.input.KeyBoardUtils;
import net.minecraft.class_309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_309.class})
public class KeyboardMixin implements QClient {
   public KeyboardMixin() {
   }

   @Inject(
      method = {"method_1466"},
      at = {@At("HEAD")}
   )
   public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      if (mc.field_1755 == null) {
         KeyBoardUtils.call(key, action);
      }
   }

   @Inject(
      method = {"method_1468"},
      at = {@At("RETURN")}
   )
   private void processF3(int key, CallbackInfoReturnable<Boolean> cir) {
      if (key == 65 && (Boolean)cir.getReturnValue()) {
         new EventChunkReload().call();
      }
   }
}
