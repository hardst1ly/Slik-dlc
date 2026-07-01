package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.misc.NameProtect;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({class_332.class})
public class DrawContextNameProtectMixin {
   public DrawContextNameProtectMixin() {
   }

   @ModifyVariable(
      method = {"method_25303"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private String slikdlc$patchStringShadow(String text) {
      return this.patch(text);
   }

   @ModifyVariable(
      method = {"method_51433"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private String slikdlc$patchString(String text) {
      return this.patch(text);
   }

   private String patch(String text) {
      if (ModuleClass.INSTANCE == null) {
         return text;
      } else {
         NameProtect nameProtect = ModuleClass.nameProtect;
         return nameProtect != null && nameProtect.isEnable() ? nameProtect.patchIncomingText(text) : text;
      }
   }
}
