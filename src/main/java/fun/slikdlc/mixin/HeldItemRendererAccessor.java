package fun.slikdlc.mixin;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_759.class})
public interface HeldItemRendererAccessor {
   @Accessor("field_4047")
   class_1799 getMainHand();

   @Accessor("field_4048")
   class_1799 getOffHand();

   @Accessor("field_4043")
   float getEquipProgressMainHand();

   @Accessor("field_4053")
   float getPrevEquipProgressMainHand();

   @Accessor("field_4052")
   float getEquipProgressOffHand();

   @Accessor("field_4051")
   float getPrevEquipProgressOffHand();

   @Accessor("field_4050")
   class_310 getClient();

   @Invoker("method_3228")
   void invokeRenderFirstPersonItem(
      class_742 var1, float var2, float var3, class_1268 var4, float var5, class_1799 var6, float var7, class_4587 var8, class_4597 var9, int var10
   );

   @Invoker("method_3224")
   void invokeApplyEquipOffset(class_4587 var1, class_1306 var2, float var3);

   @Invoker("method_65816")
   void invokeSwingArm(float var1, float var2, class_4587 var3, int var4, class_1306 var5);

   @Invoker("method_3219")
   void invokeRenderArmHoldingItem(class_4587 var1, class_4597 var2, int var3, float var4, float var5, class_1306 var6);

   @Invoker("method_3231")
   void invokeRenderMapInBothHands(class_4587 var1, class_4597 var2, int var3, float var4, float var5, float var6);

   @Invoker("method_3222")
   void invokeRenderMapInOneHand(class_4587 var1, class_4597 var2, int var3, float var4, class_1306 var5, float var6, class_1799 var7);

   @Invoker("method_3217")
   void invokeApplySwingOffset(class_4587 var1, class_1306 var2, float var3);

   @Invoker("method_3233")
   void invokeRenderItem(class_1309 var1, class_1799 var2, class_811 var3, boolean var4, class_4587 var5, class_4597 var6, int var7);
}
