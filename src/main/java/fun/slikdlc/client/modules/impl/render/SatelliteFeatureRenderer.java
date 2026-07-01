package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import net.minecraft.class_10055;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5602;
import net.minecraft.class_591;
import net.minecraft.class_7308;
import net.minecraft.class_7833;
import net.minecraft.class_9996;
import net.minecraft.class_5617.class_5618;

public class SatelliteFeatureRenderer extends class_3887<class_10055, class_591> {
   private static final class_2960 ALLAY_TEXTURE = class_2960.method_60656("textures/entity/allay/allay.png");
   private final class_7308 model;
   private final class_9996 allayState = new class_9996();

   public SatelliteFeatureRenderer(class_3883<class_10055, class_591> context, class_5618 rendererContext) {
      super(context);
      this.model = new class_7308(rendererContext.method_32167(class_5602.field_38455));
   }

   public void method_4199(class_4587 matrices, class_4597 vertexConsumers, int light, class_10055 playerState, float yawDegrees, float pitch) {
      Satellite sattelite = ModuleClass.sattelite;
      if (sattelite != null && sattelite.shouldRender(playerState)) {
         matrices.method_22903();
         float baseX = sattelite.isLeftShoulder() ? 0.4F : -0.4F;
         float baseY = playerState.field_53410 ? -1.3F : -1.5F;
         float idleBob = 0.0F;
         float idleYaw = 0.0F;
         float idleRoll = 0.0F;
         float idlePitch = 0.0F;
         float animationAge = playerState.field_53328;
         if (sattelite.idleAnimation.isState()) {
            float speed = sattelite.idleSpeed.get();
            float strength = sattelite.idleStrength.get();
            float time = playerState.field_53328 * (0.7F + speed * 0.65F);
            idleBob = class_3532.method_15374(time * 0.42F) * 0.06F * strength;
            idleYaw = class_3532.method_15374(time * 0.16F) * 9.0F * strength;
            idleRoll = class_3532.method_15362(time * 0.24F) * 7.0F * strength;
            idlePitch = class_3532.method_15374(time * 0.31F) * 5.0F * strength;
            animationAge = playerState.field_53328 * (0.85F + speed * 0.45F);
         }

         matrices.method_46416(baseX + sattelite.offsetX.get(), baseY + sattelite.offsetY.get() + idleBob, sattelite.offsetZ.get());
         float scale = sattelite.scale.get();
         matrices.method_22905(scale, scale, scale);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(sattelite.rotateX.get() + idlePitch));
         matrices.method_22907(class_7833.field_40716.rotationDegrees(sattelite.rotateY.get() + idleYaw));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(sattelite.rotateZ.get() + idleRoll));
         this.allayState.field_53328 = animationAge;
         this.allayState.field_53450 = playerState.field_53450;
         this.allayState.field_53451 = playerState.field_53451;
         this.allayState.field_53447 = yawDegrees;
         this.allayState.field_53448 = pitch;
         this.allayState.field_53333 = playerState.field_53333;
         this.allayState.field_53461 = playerState.field_53461;
         this.allayState.field_53462 = playerState.field_53462;
         this.allayState.field_53456 = playerState.field_53456;
         this.allayState.field_53457 = false;
         this.allayState.field_53458 = playerState.field_53458;
         this.allayState.field_53446 = playerState.field_53446;
         this.allayState.field_53453 = 1.0F;
         this.allayState.field_53454 = 1.0F;
         this.allayState.field_53465 = playerState.field_53465;
         this.allayState.field_53449 = 0.0F;
         this.allayState.field_53460 = playerState.field_53460;
         this.allayState.field_53237 = false;
         this.allayState.field_53238 = false;
         this.allayState.field_53239 = 0.0F;
         this.allayState.field_53240 = 0.0F;
         this.model.method_42732(this.allayState);
         class_4588 vertexConsumer = vertexConsumers.getBuffer(this.model.method_23500(ALLAY_TEXTURE));
         this.model.method_60879(matrices, vertexConsumer, light, class_4608.field_21444);
         matrices.method_22909();
      }
   }
}
