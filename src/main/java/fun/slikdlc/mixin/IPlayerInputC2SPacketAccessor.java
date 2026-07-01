package fun.slikdlc.mixin;

import net.minecraft.class_10185;
import net.minecraft.class_2851;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_2851.class})
public interface IPlayerInputC2SPacketAccessor {
   @Mutable
   @Accessor("comp_3139")
   void setInput(class_10185 var1);
}
