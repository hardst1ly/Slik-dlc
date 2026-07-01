package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.impl.render.Removals;
import fun.slikdlc.client.modules.impl.render.WorldTweaks;
import net.minecraft.class_1297;
import net.minecraft.class_4184;
import net.minecraft.class_6854;
import net.minecraft.class_758;
import net.minecraft.class_9958;
import net.minecraft.class_758.class_4596;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_758.class})
public class BackgroundRendererMixin {
   public BackgroundRendererMixin() {
   }

   @Inject(
      method = {"method_42588"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void slikdlc$getFogModifier(class_1297 entity, float tickDelta, CallbackInfoReturnable<Object> cir) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Плохие эффекты")) {
            cir.setReturnValue(null);
         }
      }
   }

   @Inject(
      method = {"method_3211"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void slikdlc$applyFog(
      class_4184 camera, class_4596 fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<class_9958> cir
   ) {
      if (ModuleClass.INSTANCE != null) {
         WorldTweaks tweaks = ModuleClass.worldTweaks;
         if (tweaks != null && tweaks.isFogEnabled()) {
            float fogDistance = Math.max(12.0F, tweaks.getFogDistance());
            float fogEnd = Math.min(viewDistance, fogDistance);
            float fogStart = Math.max(0.0F, fogEnd * 0.05F);
            int color1 = tweaks.getFogColor();
            cir.setReturnValue(
               new class_9958(fogStart, fogEnd, class_6854.field_36350, ColorUtils.redf(color1), ColorUtils.greenf(color1), ColorUtils.bluef(color1), 1.0F)
            );
         }
      }
   }
}
