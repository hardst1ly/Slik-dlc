package fun.slikdlc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.player.NoPush;
import fun.slikdlc.client.modules.impl.render.SeeInvisibles;
import fun.slikdlc.client.modules.impl.render.ShaderEsp;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1297.class})
public abstract class EntityMixin implements QClient {
   public EntityMixin() {
   }

   @ModifyExpressionValue(
      method = {"method_5784"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_65038()Z"
      )}
   )
   private boolean fixFallDistanceCalculation(boolean original) {
      return (class_1297)(Object)this == mc.field_1724 ? false : original;
   }

   @Inject(
      method = {"method_5697"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pushAwayFrom(CallbackInfo ci) {
      if ((class_1297)(Object)this == mc.field_1724 && ModuleClass.INSTANCE != null) {
         NoPush noPush = ModuleClass.noPush;
         if (noPush != null && noPush.isEnable() && noPush.getCollisionList().is("Игроки")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_5675"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void isPushedByFluids(CallbackInfoReturnable<Boolean> ci) {
      if ((class_1297)(Object)this == mc.field_1724 && ModuleClass.INSTANCE != null) {
         NoPush noPush = ModuleClass.noPush;
         if (noPush != null && noPush.isEnable() && noPush.getCollisionList().is("Вода")) {
            ci.setReturnValue(false);
         }
      }
   }

   @Inject(
      method = {"method_22861"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
      if (ModuleClass.INSTANCE != null) {
         ShaderEsp shaderEsp = ModuleClass.shaderEsp;
         if (shaderEsp != null && shaderEsp.shouldOutline((class_1297)(Object)this)) {
            cir.setReturnValue(shaderEsp.getOutlineColor());
         }
      }
   }

   @Inject(
      method = {"method_5756"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$allowSeeInvisibles(class_1657 player, CallbackInfoReturnable<Boolean> cir) {
      if ((class_1297)(Object)this instanceof class_1657 target && ModuleClass.INSTANCE != null) {
         SeeInvisibles seeInvisibles = ModuleClass.seeInvisibles;
         if (seeInvisibles != null && seeInvisibles.shouldRenderInvisible(target)) {
            cir.setReturnValue(false);
         }
      }
   }
}
