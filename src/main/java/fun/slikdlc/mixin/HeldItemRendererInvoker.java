package fun.slikdlc.mixin;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_759.class})
public interface HeldItemRendererInvoker {
   @Accessor("field_4047")
   class_1799 whylol$getMainHand();

   @Accessor("field_4048")
   class_1799 whylol$getOffHand();

   @Invoker("method_3228")
   void whylol$callRenderFirstPersonItem(
      class_742 var1, float var2, float var3, class_1268 var4, float var5, class_1799 var6, float var7, class_4587 var8, class_4597 var9, int var10
   );

   @Invoker("method_3224")
   void whylol$applyEquipOffset(class_4587 var1, class_1306 var2, float var3);

   @Invoker("method_65816")
   void whylol$callSwingArm(float var1, float var2, class_4587 var3, int var4, class_1306 var5);

   @Invoker("method_3219")
   void whylol$renderArmHoldingItem(class_4587 var1, class_4597 var2, int var3, float var4, float var5, class_1306 var6);

   @Invoker("method_3231")
   void whylol$renderMapInBothHands(class_4587 var1, class_4597 var2, int var3, float var4, float var5, float var6);

   @Invoker("method_3222")
   void whylol$renderMapInOneHand(class_4587 var1, class_4597 var2, int var3, float var4, class_1306 var5, float var6, class_1799 var7);

   @Invoker("method_3233")
   void whylol$renderItem(class_1309 var1, class_1799 var2, class_811 var3, boolean var4, class_4587 var5, class_4597 var6, int var7);
}

@Mixin(targets = "net/minecraft/class_759$class_5773")
interface HeldItemRendererHandRenderTypeAccessor {
   @Accessor("field_28387")
   boolean field_28387();

   @Accessor("field_28388")
   boolean field_28388();
}
