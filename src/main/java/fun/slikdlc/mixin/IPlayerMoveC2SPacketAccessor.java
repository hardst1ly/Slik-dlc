package fun.slikdlc.mixin;

import net.minecraft.class_2828;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_2828.class})
public interface IPlayerMoveC2SPacketAccessor {
   @Accessor("field_12889")
   void setX(double var1);

   @Accessor("field_12886")
   void setY(double var1);

   @Accessor("field_12884")
   void setZ(double var1);

   @Mutable
   @Accessor("field_52335")
   void setHorizontalCollision(boolean var1);
}
