package fun.slikdlc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fun.slikdlc.api.events.implement.EventFireWork;
import fun.slikdlc.api.utils.player.BoostUtils;
import fun.slikdlc.client.modules.impl.movement.ElytraBoost;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1671;
import net.minecraft.class_1676;
import net.minecraft.class_1937;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1671.class})
public abstract class FireWorkRocketEntityMixin extends class_1676 {
   @Unique
   private class_243 rotation;
   @Shadow
   private class_1309 field_7616;

   public FireWorkRocketEntityMixin(class_1299<? extends class_1676> entityType, class_1937 world) {
      super(entityType, world);
   }

   @Inject(
      method = {"method_5773"},
      at = {@At("HEAD")}
   )
   public void tick(CallbackInfo ci) {
      new EventFireWork((class_1671)(Object)this).call();
   }

   @ModifyExpressionValue(
      method = {"method_5773"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1309;method_5720()Lnet/minecraft/class_243;"
      )}
   )
   public class_243 captureRotation(class_243 original) {
      this.rotation = original;
      return this.rotation;
   }

   @Redirect(
      method = {"method_5773"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_243;method_1031(DDD)Lnet/minecraft/class_243;",
         ordinal = 0
      )
   )
   public class_243 modifyBoost(class_243 velocity, double x, double y, double z) {
      class_310 mc = class_310.method_1551();
      ElytraBoost elytraBoost = ElytraBoost.INSTANCE;
      if (mc.field_1724 == null || !mc.field_1724.method_6128()) {
         return this.defaultBoost(velocity);
      } else {
         return elytraBoost != null && elytraBoost.isEnable() ? this.handleElytraBoost(mc, elytraBoost, velocity) : this.defaultBoost(velocity);
      }
   }

   @Unique
   private class_243 handleElytraBoost(class_310 mc, ElytraBoost elytraBoost, class_243 velocity) {
      String modeName = elytraBoost.getMode().getCurrent();

      class_243 boost = switch (modeName) {
         case "LonyGrief" -> BoostUtils.getBoost(mc.field_1724);
         case "SlimeWorld" -> BoostUtils.getBoostslime(mc.field_1724);
         case "BravoHVH" -> BoostUtils.getBoostbravo(mc.field_1724);
         case "ReallyWorld" -> BoostUtils.getBoostrw(mc.field_1724);
         default -> {
            class_241 customBoost = elytraBoost.getBoostV2();
            yield new class_243(customBoost.field_1343, customBoost.field_1342, customBoost.field_1343);
         }
      };
      return velocity.method_1031(
         this.rotation.field_1352 * 0.1 + (this.rotation.field_1352 * boost.field_1352 - velocity.field_1352) * 0.5,
         this.rotation.field_1351 * 0.1 + (this.rotation.field_1351 * boost.field_1351 - velocity.field_1351) * 0.5,
         this.rotation.field_1350 * 0.1 + (this.rotation.field_1350 * boost.field_1350 - velocity.field_1350) * 0.5
      );
   }

   @Unique
   private class_243 defaultBoost(class_243 velocity) {
      return velocity.method_1031(
         this.rotation.field_1352 * 0.1 + (this.rotation.field_1352 * 1.5 - velocity.field_1352) * 0.5,
         this.rotation.field_1351 * 0.1 + (this.rotation.field_1351 * 1.5 - velocity.field_1351) * 0.5,
         this.rotation.field_1350 * 0.1 + (this.rotation.field_1350 * 1.5 - velocity.field_1350) * 0.5
      );
   }
}
