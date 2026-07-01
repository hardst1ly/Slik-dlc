package fun.slikdlc.mixin;

import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventMoveInput;
import net.minecraft.class_10185;
import net.minecraft.class_310;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_743.class})
public abstract class KeyboardInputMixin extends class_744 {
   private static final class_310 mc = class_310.method_1551();

   public KeyboardInputMixin() {
   }

   @Inject(
      method = {"method_3129"},
      at = {@At("TAIL")}
   )
   private void onTickTail(CallbackInfo ci) {
      if (EventInvoker.hasListeners(EventMoveInput.class)) {
         EventMoveInput eventInput = new EventMoveInput(this.field_3905, this.field_3907, this.field_54155.comp_3163(), this.field_54155.comp_3164());
         eventInput.call();
         float forward = eventInput.getForward();
         float strafe = eventInput.getStrafe();
         this.field_54155 = new class_10185(
            forward > 0.0F, forward < 0.0F, strafe > 0.0F, strafe < 0.0F, eventInput.isJump(), eventInput.isSneak(), this.field_54155.comp_3165()
         );
         this.field_3905 = forward;
         this.field_3907 = strafe;
      }
   }
}
