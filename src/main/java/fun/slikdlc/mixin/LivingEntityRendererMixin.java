package fun.slikdlc.mixin;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.SeeInvisibles;
import fun.slikdlc.client.modules.impl.render.SeeInvisiblesRenderState;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_922.class})
public abstract class LivingEntityRendererMixin<T extends class_1309, S extends class_10042, M extends class_583<? super S>> implements QClient {
   public LivingEntityRendererMixin() {
   }

   @Inject(
      method = {"method_62355"},
      at = {@At("TAIL")}
   )
   private void slikdlc$updateSeeInvisiblesState(T entity, S state, float tickDelta, CallbackInfo ci) {
      boolean shouldRenderInvisible = this.slikdlc$shouldRenderInvisible(entity);
      ((SeeInvisiblesRenderState)state).slikdlc$setSeeInvisiblesTarget(shouldRenderInvisible);
      if (shouldRenderInvisible) {
         state.field_53333 = true;
         state.field_53461 = false;
      }
   }

   @ModifyConstant(
      method = {"method_4054"},
      constant = {@Constant(
         intValue = 654311423
      )}
   )
   private int slikdlc$changeInvisibleAlpha(int original, S state, class_4587 matrices, class_4597 vertexConsumers, int light) {
      return ((SeeInvisiblesRenderState)state).slikdlc$isSeeInvisiblesTarget() ? SeeInvisibles.INVISIBLE_COLOR : original;
   }

   @Unique
   private boolean slikdlc$shouldRenderInvisible(T entity) {
      if (entity instanceof class_1657 player && ModuleClass.INSTANCE != null) {
         SeeInvisibles seeInvisibles = ModuleClass.seeInvisibles;
         return seeInvisibles != null && seeInvisibles.shouldRenderInvisible(player);
      } else {
         return false;
      }
   }

   @Unique
   private class_1657 slikdlc$resolvePlayer(S state) {
      if (state instanceof class_10055 playerState && mc.field_1687 != null) {
         return mc.field_1687.method_8469(playerState.field_53528) instanceof class_1657 player ? player : null;
      } else {
         return null;
      }
   }
}
