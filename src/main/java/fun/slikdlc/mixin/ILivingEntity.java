package fun.slikdlc.mixin;

import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_1309.class})
public interface ILivingEntity {
   @Accessor("field_6273")
   int getLastAttackedTicks();

   @Accessor("field_6228")
   void setJumpingCooldown(int var1);

   @Accessor("field_6284")
   double getResolveYaw();

   @Accessor("field_6221")
   double getResolvePitch();
}
