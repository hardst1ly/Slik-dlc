package fun.slikdlc.mixin;

import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_4184.class})
public interface ICameraMixin {
   @Invoker("method_19325")
   void setCustomRotation(float var1, float var2);

   @Invoker("method_19318")
   float setClipToSpace(float var1);

   @Invoker("method_19324")
   void setCustomMoveBy(float var1, float var2, float var3);
}
