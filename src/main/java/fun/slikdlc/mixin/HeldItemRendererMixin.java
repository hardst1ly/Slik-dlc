package fun.slikdlc.mixin;

import com.google.common.base.MoreObjects;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.render.BeautifulHandsRenderer;
import fun.slikdlc.api.utils.render.hands.ShaderHandsRenderer;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.render.BeautifulHands;
import fun.slikdlc.client.modules.impl.render.ShaderHands;
import fun.slikdlc.client.modules.impl.render.SwingAnimations;
import fun.slikdlc.client.modules.impl.render.ViewModel;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_759.class})
public abstract class HeldItemRendererMixin {
   @Shadow
   private class_1799 field_4047;
   @Shadow
   private float field_4043;
   @Shadow
   private float field_4053;
   @Shadow
   private float field_4051;
   @Shadow
   private float field_4052;
   @Shadow
   private class_1799 field_4048;

   public HeldItemRendererMixin() {
   }

   private static HeldItemRendererHandRenderTypeAccessor method_33303(class_746 player) {
      try {
         return (HeldItemRendererHandRenderTypeAccessor)class_759.class.getDeclaredMethod("method_33303", class_746.class).invoke(null, player);
      } catch (ReflectiveOperationException exception) {
         throw new RuntimeException(exception);
      }
   }

   @Shadow
   protected abstract void method_3228(
      class_742 var1, float var2, float var3, class_1268 var4, float var5, class_1799 var6, float var7, class_4587 var8, class_4597 var9, int var10
   );

   @Inject(
      method = {"method_22976"},
      at = {@At("HEAD")}
   )
   private void onRenderItemHead(float tickProgress, class_4587 matrices, class_4598 immediate, class_746 player, int light, CallbackInfo ci) {
      BeautifulHands beautifulHands = this.getBeautifulHands();
      if (beautifulHands != null && beautifulHands.isEnable()) {
         BeautifulHandsRenderer.getInstance().updateDelta();
      }

      ShaderHands shaderHands = this.getShaderHands();
      if (shaderHands != null && shaderHands.isEnable()) {
         ShaderHandsRenderer.getInstance().captureBeforeHands();
      }
   }

   @Inject(
      method = {"method_22976"},
      at = {@At("TAIL")}
   )
   private void onRenderItemTail(float tickProgress, class_4587 matrices, class_4598 immediate, class_746 player, int light, CallbackInfo ci) {
      ShaderHands shaderHands = this.getShaderHands();
      if (shaderHands != null && shaderHands.isEnable()) {
         ShaderHandsRenderer.getInstance().captureAfterHands();
      }
   }

   @Redirect(
      method = {"method_22976"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      )
   )
   private void onRenderFirstPersonItemCall(
      class_759 instance,
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light
   ) {
      class_1268 renderHand = hand;
      SwingAnimations tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.swapHands.isState()) {
         renderHand = hand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
      }

      ((HeldItemRendererInvoker)instance)
         .whylol$callRenderFirstPersonItem(player, tickDelta, pitch, renderHand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light);
   }

   @ModifyArg(
      method = {"method_3228"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3219(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;IFFLnet/minecraft/class_1306;)V"
      ),
      index = 5
   )
   private class_1306 swapEmptyHandArm(class_1306 arm) {
      SwingAnimations tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.swapHands.isState()) {
         return arm == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
      } else {
         return arm;
      }
   }

   @Inject(
      method = {"method_3228"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4587;method_22903()V",
         shift = Shift.AFTER
      )}
   )
   private void onRenderFirstPersonItem(
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      ViewModel viewModel = this.getViewModel();
      if (viewModel != null && viewModel.isEnable()) {
         if (hand == class_1268.field_5808) {
            matrices.method_46416(viewModel.mainHandX.get(), viewModel.mainHandY.get(), viewModel.mainHandZ.get());
         } else {
            matrices.method_46416(viewModel.offHandX.get(), viewModel.offHandY.get(), viewModel.offHandZ.get());
         }
      }

      BeautifulHands beautifulHands = this.getBeautifulHands();
      if (beautifulHands != null && beautifulHands.isEnable()) {
         if (hand == class_1268.field_5808) {
            matrices.method_46416(
               beautifulHands.rightX.getValue().floatValue(), beautifulHands.rightY.getValue().floatValue(), beautifulHands.rightZ.getValue().floatValue()
            );
         } else {
            matrices.method_46416(
               beautifulHands.leftX.getValue().floatValue(), beautifulHands.leftY.getValue().floatValue(), beautifulHands.leftZ.getValue().floatValue()
            );
         }
      }
   }

   @Redirect(
      method = {"method_3228"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_65816(FFLnet/minecraft/class_4587;ILnet/minecraft/class_1306;)V",
         ordinal = 2
      )
   )
   private void onSwingArm(class_759 instance, float swingProgress, float equipProgress, class_4587 matrices, int armX, class_1306 arm) {
      SwingAnimations tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.swingEnabled.isState()) {
         Aura aura = ModuleClass.INSTANCE != null ? ModuleClass.aura : null;
         if (!tweaks.auraTargetOnly.isState() || aura != null && aura.isEnable() && aura.getTarget() != null && aura.getTarget().method_5805()) {
            if (class_310.method_1551().field_1724 != null) {
               class_1306 expectedSwingArm = class_310.method_1551().field_1724.method_6068();
               if (tweaks.swapHands.isState()) {
                  expectedSwingArm = expectedSwingArm == class_1306.field_6183 ? class_1306.field_6182 : class_1306.field_6183;
               }

               if (arm != expectedSwingArm) {
                  this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
                  return;
               }
            }

            int i = arm == class_1306.field_6183 ? 1 : -1;
            float strength = tweaks.swingStrength.get();
            float sin1 = class_3532.method_15374(swingProgress * swingProgress * (float) Math.PI);
            float sin2 = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
            String var13 = tweaks.swingType.getCurrent();
            switch (var13) {
               case "Down":
                  matrices.method_46416(i * 0.56F, -0.32F, -0.72F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(76 * i));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(sin2 * -5.0F * strength));
                  matrices.method_22907(class_7833.field_40713.rotationDegrees(sin2 * -100.0F * strength));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -155.0F * strength));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(-100.0F));
                  break;
               case "Poke": {
                  float anim = (float)Math.sin(swingProgress * (Math.PI / 2) * 2.0);
                  float tilt = strength / 3.0F;
                  matrices.method_46416(i * 0.56F, -0.52F, -0.72F);
                  matrices.method_46416(0.0F, 0.0F, tilt * -anim);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(75.0F * i));
                  matrices.method_22907(class_7833.field_40718.rotationDegrees((-75.0F * (strength / 4.0F) * anim - 60.0F) * i));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(-75.0F));
                  break;
               }
               case "Static":
                  matrices.method_46416(i * 0.56F, -0.42F, -0.72F);
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -60.0F * strength));
                  matrices.method_22904(0.0, -0.1, 0.0);
                  break;
               case "Feast":
                  matrices.method_46416(i * 0.56F, -0.32F, -0.72F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(30 * i));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(sin2 * 75.0F * i * strength));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -65.0F * strength));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(30 * i));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(35 * i));
                  break;
               case "Akrien":
                  matrices.method_46416(i * 0.65F, -0.32F, -0.72F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(76 * i));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(sin2 * -5.0F * strength));
                  matrices.method_22907(class_7833.field_40713.rotationDegrees(sin2 * -100.0F * strength));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -155.0F * strength));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(-100.0F));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(sin2 * 25.0F * strength));
                  matrices.method_22907(class_7833.field_40713.rotationDegrees(sin2 * -25.0F * strength));
                  matrices.method_22907(class_7833.field_40713.rotationDegrees(sin1 * 15.0F * strength));
                  matrices.method_46416(sin2 * 0.18F * strength, sin2 * 0.59F * strength, 0.0F);
                  break;
               case "Smooth":
                  this.applySwingOffset(matrices, i, swingProgress, strength);
                  break;
               case "Block":
                  if (swingProgress > 0.0F) {
                     float gx = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
                     matrices.method_46416(0.56F * i, equipProgress * -0.2F - 0.5F, -0.7F);
                     matrices.method_22907(class_7833.field_40716.rotationDegrees(45 * i));
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(gx * -85.0F * strength));
                     matrices.method_46416(-0.1F * i, 0.28F, 0.2F);
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(-85.0F));
                  } else {
                     float n = -0.4F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
                     float m = 0.2F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) (Math.PI * 2));
                     float f1 = -0.2F * class_3532.method_15374(swingProgress * (float) Math.PI);
                     matrices.method_46416(n * i * strength, m * strength, f1 * strength);
                     this.applyEquipOffset(matrices, i, equipProgress);
                     this.applySwingOffset(matrices, i, swingProgress, strength);
                  }
                  break;
               case "ToBack":
                  float g = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
                  matrices.method_46416(0.65F * i, -0.45F, -0.9F);
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(50.0F));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees((-30.0F * (1.0F - g * strength) - 30.0F) * i));
                  matrices.method_22907(class_7833.field_40718.rotationDegrees(110.0F * i));
                  break;
               case "SelfBack": {
                  float anim = (float)Math.sin(swingProgress * (Math.PI / 2) * 2.0);
                  matrices.method_46416(0.65F * i, -0.3F, -0.8F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(90 * i));
                  matrices.method_22907(class_7833.field_40718.rotationDegrees(-70 * i));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(-100.0F - 60.0F * strength * anim));
                  break;
               }
               case "Break":
               case "Брик":
                  matrices.method_46416(0.66F * i, -0.3F, -0.38F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(270 * i));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(sin2 * 10.0F * strength));
                  matrices.method_22905(0.5F, 0.5F, 0.5F);
                  matrices.method_46416(-0.1F * i, 0.2F, 0.0F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(-10.0F * i));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(-105.0F * i));
                  break;
               case "DropDown": {
                  float anim = (float)Math.sin(swingProgress * (Math.PI / 2) * 2.0);
                  this.applyEquipOffset(matrices, i, 0.0F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(80.0F));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(tweaks.corner.get()));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(-tweaks.slant.get() * anim * strength));
                  break;
               }
               case "Pander":
                  float panderAnim = class_3532.method_15374(swingProgress * (float) Math.PI);
                  float panderF = 1.0F - equipProgress;
                  matrices.method_46416(i * 0.56F, -0.52F, -0.72F);
                  matrices.method_46416((0.3F - panderAnim * 0.15F) * i, 0.2F - panderF * 0.12F, -0.15F - panderAnim * 0.13F);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees((76.0F - 10.0F * panderAnim) * i));
                  matrices.method_22907(class_7833.field_40718.rotationDegrees((-16.0F - 8.0F * panderAnim) * i));
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(-83.0F - 26.0F * panderAnim));
                  break;
               case "Slant": {
                  float anim = (float)Math.sin(swingProgress * (Math.PI / 2) * 2.0);
                  float rotate = 35.0F * strength;
                  matrices.method_46416(i * 0.56F, -0.52F, -0.72F);
                  matrices.method_46416(0.0F, 0.0F, -0.3F * anim * strength);
                  matrices.method_22907(class_7833.field_40714.rotationDegrees(anim * -rotate));
                  matrices.method_22907(class_7833.field_40718.rotationDegrees(anim * rotate));
                  break;
               }
               default:
                  this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
            }
         } else {
            this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
         }
      } else {
         this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
      }
   }

   @Overwrite
   public void method_22976(float tickDelta, class_4587 matrices, class_4598 vertexConsumers, class_746 player, int light) {
      float f = player.method_6055(tickDelta);
      class_1268 hand = (class_1268)MoreObjects.firstNonNull(player.field_6266, class_1268.field_5808);
      float g = player.method_61414(tickDelta);
      HeldItemRendererHandRenderTypeAccessor handRenderType = method_33303(player);
      float h = class_3532.method_16439(tickDelta, player.field_3914, player.field_3916);
      float i = class_3532.method_16439(tickDelta, player.field_3931, player.field_3932);
      if (handRenderType.field_28387()) {
         float j = hand == class_1268.field_5808 ? f : 0.0F;
         float k = 1.0F - class_3532.method_16439(tickDelta, this.field_4053, this.field_4043);
         this.method_3228(player, tickDelta, g, class_1268.field_5808, j, this.field_4047, k, matrices, vertexConsumers, light);
      }

      if (handRenderType.field_28388()) {
         float j = hand == class_1268.field_5810 ? f : 0.0F;
         float k = 1.0F - class_3532.method_16439(tickDelta, this.field_4051, this.field_4052);
         this.method_3228(player, tickDelta, g, class_1268.field_5810, j, this.field_4048, k, matrices, vertexConsumers, light);
      }

      vertexConsumers.method_22993();
   }

   @Inject(
      method = {"method_3218"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onApplyEatOrDrinkTransformation(class_4587 matrices, float tickDelta, class_1306 arm, class_1799 stack, class_1657 player, CallbackInfo ci) {
      SwingAnimations tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.eatAnim.isState() && player.method_6115()) {
         this.applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, stack);
         ci.cancel();
      }
   }

   private void applyEatOrDrinkTransformationCustom(class_4587 matrices, float tickDelta, class_1306 arm, class_1799 stack) {
      if (class_310.method_1551().field_1724 != null) {
         float f = class_310.method_1551().field_1724.method_6014() - tickDelta + 1.0F;
         float g = f / stack.method_7935(class_310.method_1551().field_1724);
         if (g < 0.8F) {
            float h = class_3532.method_15379(class_3532.method_15362(f / 4.0F * (float) Math.PI) * 0.005F);
            matrices.method_46416(0.0F, h, 0.0F);
         }

         float h = 1.0F - (float)Math.pow(g, 27.0);
         int i = arm == class_1306.field_6183 ? 1 : -1;
         float offsetX = 0.0F;
         float offsetY = 0.0F;
         float offsetZ = 0.0F;
         ViewModel viewModel = this.getViewModel();
         if (viewModel != null && viewModel.isEnable()) {
            if (arm == class_1306.field_6183) {
               offsetX = viewModel.mainHandX.get();
               offsetY = viewModel.mainHandY.get();
               offsetZ = viewModel.mainHandZ.get();
            } else {
               offsetX = viewModel.offHandX.get();
               offsetY = viewModel.offHandY.get();
               offsetZ = viewModel.offHandZ.get();
            }
         }

         matrices.method_46416(h * 0.6F * i + offsetX, h * -0.5F + offsetY, offsetZ);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(i * h * 90.0F));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(h * 10.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(i * h * 30.0F));
      }
   }

   private void applyEquipOffset(class_4587 matrices, int i, float equipProgress) {
      matrices.method_46416(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }

   private void applySwingOffset(class_4587 matrices, int i, float swingProgress, float strength) {
      float f = class_3532.method_15374(swingProgress * swingProgress * (float) Math.PI);
      matrices.method_46416(0.56F * i, -0.52F, -0.72F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(i * (45.0F + f * -20.0F * strength)));
      float g = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
      matrices.method_22907(class_7833.field_40718.rotationDegrees(i * g * -20.0F * strength));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(g * -80.0F * strength));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(i * -45.0F));
   }

   private void callSwingArm(class_759 instance, float swingProgress, float equipProgress, class_4587 matrices, int armX, class_1306 arm) {
      ((HeldItemRendererInvoker)instance).whylol$callSwingArm(swingProgress, equipProgress, matrices, armX, arm);
   }

   private SwingAnimations getTweaks() {
      return ModuleClass.INSTANCE == null ? null : ModuleClass.swingAnimations;
   }

   private ViewModel getViewModel() {
      return ModuleClass.INSTANCE == null ? null : ModuleClass.viewModel;
   }

   private ShaderHands getShaderHands() {
      return ModuleClass.INSTANCE == null ? null : ModuleClass.shaderHands;
   }

   private BeautifulHands getBeautifulHands() {
      return ModuleClass.INSTANCE == null ? null : ModuleClass.beautifulHands;
   }
}
