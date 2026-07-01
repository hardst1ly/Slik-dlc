package fun.slikdlc.mixin;

import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventRotation;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.InterpolateF5;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({class_4184.class})
public abstract class CameraMixin {
   public CameraMixin() {
   }

   @Redirect(
      method = {"method_19321"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4184;method_19325(FF)V"
      )
   )
   private void redirectSetRotation(
      class_4184 instance, float yaw, float pitch, class_1922 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta
   ) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      EventRotation event = new EventRotation(yaw, pitch, tickDelta);
      EventInvoker.invoke(event);
      float newYaw = event.getYaw();
      float newPitch = event.getPitch();
      if (thirdPerson && inverseView) {
         newYaw += 180.0F;
         newPitch = -newPitch;
      }

      ((ICameraMixin)instance).setCustomRotation(newYaw, newPitch);
   }

   @Redirect(
      method = {"method_19321"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4184;method_19318(F)F"
      )
   )
   private float redirectClipToSpace(
      class_4184 instance, float distance, class_1922 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta
   ) {
      if (!thirdPerson) {
         return ((ICameraMixin)instance).setClipToSpace(distance);
      } else {
         InterpolateF5 module = ModuleClass.INSTANCE != null ? ModuleClass.interpolateF5 : null;
         return module != null && module.isEnable()
            ? ((ICameraMixin)instance).setClipToSpace(module.getInterpolatedDistance(tickDelta))
            : ((ICameraMixin)instance).setClipToSpace(distance);
      }
   }

   @Redirect(
      method = {"method_19321"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4184;method_19324(FFF)V"
      )
   )
   private void redirectMoveBy(
      class_4184 instance, float x, float y, float z, class_1922 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta
   ) {
      float newY = y;
      if (thirdPerson) {
         InterpolateF5 module = ModuleClass.INSTANCE != null ? ModuleClass.interpolateF5 : null;
         if (module != null && module.isEnable()) {
            newY = y + module.getInterpolatedHeightOffset(tickDelta);
         }
      }

      ((ICameraMixin)instance).setCustomMoveBy(x, newY, z);
   }
}
