package fun.slikdlc.mixin;

import net.minecraft.class_342;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_408.class})
public interface ChatScreenAccessor {
   @Accessor("field_2382")
   class_342 slikdlc$getChatField();
}
