package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.WorldTweaks;
import net.minecraft.class_638.class_5271;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_5271.class})
public class ClientWorldPropertiesMixin {
   @Shadow
   private long field_24439;

   public ClientWorldPropertiesMixin() {
   }

   @Inject(
      method = {"method_165"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$setTimeOfDay(long timeOfDay, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         WorldTweaks tweaks = ModuleClass.worldTweaks;
         if (tweaks != null && tweaks.isTimeEnabled()) {
            this.field_24439 = tweaks.getForcedTime();
            ci.cancel();
         }
      }
   }
}
