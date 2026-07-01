package fun.slikdlc.mixin;

import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventLook;
import fun.slikdlc.api.utils.input.KeyBoardUtils;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_3540;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_312.class})
public abstract class MouseMixin {
   @Shadow
   @Final
   private class_310 field_1779;
   @Shadow
   private double field_1789;
   @Shadow
   private double field_1787;
   @Shadow
   private class_3540 field_1793;
   @Shadow
   private class_3540 field_1782;

   public MouseMixin() {
   }

   @Inject(
      method = {"method_1601"},
      at = {@At("HEAD")},
      cancellable = false
   )
   private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
      try {
         if (this.field_1779.field_1724 == null) {
            return;
         }

         int actionId = action == 1 ? 1 : 0;
         KeyBoardUtils.callMouse(button, actionId);
      } catch (Exception var9) {
      }
   }

   @Inject(
      method = {"method_1606"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
      try {
         if (this.field_1779.field_1724 == null) {
            return;
         }

         double sensitivity = (Double)this.field_1779.field_1690.method_42495().method_41753() * 0.6 + 0.2;
         double scaled = sensitivity * sensitivity * sensitivity * 8.0;
         double i;
         double j;
         if (this.field_1779.field_1690.field_1914) {
            i = this.field_1793.method_15429(this.field_1789 * scaled, timeDelta * scaled);
            j = this.field_1782.method_15429(this.field_1787 * scaled, timeDelta * scaled);
         } else if (this.field_1779.field_1690.method_31044().method_31034() && this.field_1779.field_1724.method_31550()) {
            this.field_1793.method_15428();
            this.field_1782.method_15428();
            i = this.field_1789 * sensitivity * sensitivity * sensitivity;
            j = this.field_1787 * sensitivity * sensitivity * sensitivity;
         } else {
            this.field_1793.method_15428();
            this.field_1782.method_15428();
            i = this.field_1789 * scaled;
            j = this.field_1787 * scaled;
         }

         int invert = this.field_1779.field_1690.method_42438().method_41753() ? -1 : 1;
         EventLook event = new EventLook(i, j * invert);
         EventInvoker.invoke(event);
         if (!event.isCancelled()) {
            this.field_1779.method_1577().method_4908(event.getYaw(), event.getPitch());
            this.field_1779.field_1724.method_5872(event.getYaw(), event.getPitch());
         }

         this.field_1789 = 0.0;
         this.field_1787 = 0.0;
         ci.cancel();
      } catch (Exception var14) {
      }
   }
}
