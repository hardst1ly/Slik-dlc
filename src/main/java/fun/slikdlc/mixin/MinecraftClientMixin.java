package fun.slikdlc.mixin;

import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventGameUpdate;
import fun.slikdlc.api.events.implement.EventTickPost;
import fun.slikdlc.api.events.implement.EventTickPre;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.baritone.BaritoneAntiStuck;
import fun.slikdlc.api.utils.player.Counter;
import fun.slikdlc.client.modules.impl.render.ShaderEsp;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.class_1297;
import net.minecraft.class_156;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_310.class})
public abstract class MinecraftClientMixin {
   @Unique
   private long lastHookTime = class_156.method_648();
   @Unique
   private int accumulatedCalls = 0;

   public MinecraftClientMixin() {
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("HEAD")}
   )
   public void tick(CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (EventInvoker.hasListeners(EventTickPre.class)) {
         EventTickPre event = new EventTickPre();
         EventInvoker.invoke(event);
      }

      Counter.updateFPS();
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("RETURN")}
   )
   public void tickEnd(CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (EventInvoker.hasListeners(EventTickPost.class)) {
         EventTickPost event = new EventTickPost();
         EventInvoker.invoke(event);
      }

      BaritoneAntiStuck.tick();
   }

   @Inject(
      method = {"method_1523"},
      at = {@At("HEAD")}
   )
   private void render(boolean tick, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (!EventInvoker.hasListeners(EventGameUpdate.class)) {
         this.lastHookTime = class_156.method_648();
         this.accumulatedCalls = 0;
      } else {
         long now = class_156.method_648();
         long delta = now - this.lastHookTime;
         this.accumulatedCalls += (int)(delta / 4166666L);
         this.lastHookTime = this.lastHookTime + this.accumulatedCalls * 4166666L;

         for (this.accumulatedCalls = Math.min(this.accumulatedCalls, 240); this.accumulatedCalls > 0; this.accumulatedCalls--) {
            EventInvoker.invoke(new EventGameUpdate());
         }
      }
   }

   @Inject(
      method = {"method_27022"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$hasOutline(class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
      if (ModuleClass.INSTANCE != null) {
         ShaderEsp shaderEsp = ModuleClass.shaderEsp;
         if (shaderEsp != null && shaderEsp.shouldOutline(entity)) {
            cir.setReturnValue(true);
         }
      }
   }
}
