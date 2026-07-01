package fun.slikdlc.mixin;

import net.minecraft.class_276;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_761.class})
public interface WorldRendererAccessor {
   @Accessor("field_53080")
   class_276 slikdlc$getEntityOutlineFramebufferRaw();
}
