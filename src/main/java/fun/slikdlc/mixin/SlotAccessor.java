package fun.slikdlc.mixin;

import net.minecraft.class_1263;
import net.minecraft.class_1735;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({class_1735.class})
public interface SlotAccessor {
   @Accessor("field_7871")
   class_1263 slikdlc$getInventory();

   @Accessor("field_7875")
   int slikdlc$getIndex();
}
