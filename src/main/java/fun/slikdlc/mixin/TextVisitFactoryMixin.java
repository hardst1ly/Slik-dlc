package fun.slikdlc.mixin;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.misc.NameProtect;
import net.minecraft.class_5223;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({class_5223.class})
public class TextVisitFactoryMixin {
   public TextVisitFactoryMixin() {
   }

   @ModifyArg(
      method = {"method_27472"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_5223;method_27473(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z",
         ordinal = 0
      ),
      index = 0
   )
   private static String slikdlc$patchVisitedText(String text) {
      if (ModuleClass.INSTANCE == null) {
         return text;
      } else {
         NameProtect nameProtect = ModuleClass.nameProtect;
         return nameProtect != null && nameProtect.isEnable() ? nameProtect.patchIncomingText(text) : text;
      }
   }
}
