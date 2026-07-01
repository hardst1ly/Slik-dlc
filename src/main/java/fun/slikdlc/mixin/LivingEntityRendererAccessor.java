package fun.slikdlc.mixin;

import net.minecraft.class_10042;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_922.class})
public interface LivingEntityRendererAccessor {
   @Invoker("method_4046")
   boolean slikdlc$addFeature(class_3887<?, ?> var1);

   @Invoker("method_4058")
   void slikdlc$setupTransforms(class_10042 var1, class_4587 var2, float var3, float var4);

   @Invoker("method_4042")
   void slikdlc$scale(class_10042 var1, class_4587 var2);
}
