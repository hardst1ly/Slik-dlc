package fun.slikdlc.mixin;

import fun.slikdlc.SlikDlc;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Main.class})
public class MainMixin {
   public MainMixin() {
   }

   @Inject(
      method = {"main"},
      at = {@At("HEAD")}
   )
   private static void onMain(String[] args, CallbackInfo ci) {
      if (SlikDlc.INSTANCE.isServer) {
         try {
            SlikDlc.INSTANCE.closeMinecraft();
         } catch (Exception var3) {
            var3.printStackTrace();
         }

         SlikDlc.INSTANCE.isServer = false;
      }
   }
}
