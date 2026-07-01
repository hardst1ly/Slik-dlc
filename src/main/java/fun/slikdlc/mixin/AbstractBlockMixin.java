package fun.slikdlc.mixin;

import fun.slikdlc.api.events.Event;
import fun.slikdlc.api.events.implement.EventBlockCollide;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3726;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_4970.class})
public class AbstractBlockMixin {
   public AbstractBlockMixin() {
   }

   @Inject(
      method = {"method_9530"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getOutlineShape(class_2680 state, class_1922 world, class_2338 pos, class_3726 context, CallbackInfoReturnable<class_265> cir) {
      Event event = new EventBlockCollide(pos);
      event.call();
      if (event.isCancelled()) {
         cir.setReturnValue(class_259.method_1073());
      }
   }

   @Inject(
      method = {"method_9549"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getCollisionShape(class_2680 state, class_1922 world, class_2338 pos, class_3726 context, CallbackInfoReturnable<class_265> cir) {
      Event event = new EventBlockCollide(pos);
      event.call();
      if (event.isCancelled()) {
         cir.setReturnValue(class_259.method_1073());
      }
   }

   @Inject(
      method = {"method_9584"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getRaycastShape(class_2680 state, class_1922 world, class_2338 pos, CallbackInfoReturnable<class_265> cir) {
      Event event = new EventBlockCollide(pos);
      event.call();
      if (event.isCancelled()) {
         cir.setReturnValue(class_259.method_1073());
      }
   }
}
