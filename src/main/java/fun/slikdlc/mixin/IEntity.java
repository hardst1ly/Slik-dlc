package fun.slikdlc.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_1297.class})
public interface IEntity {
   @Invoker("method_17835")
   class_243 invokeAdjustMovementForCollisions(class_243 var1);
}
