package fun.slikdlc.mixin;

import net.minecraft.class_310;
import net.minecraft.class_320;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_310.class})
public interface IMinecraftClientAccessor {
   @Mutable
   @Accessor("field_1726")
   void setSession(class_320 var1);

   @Mutable
   @Accessor("field_1752")
   void setItemUseCooldown(int var1);
}
