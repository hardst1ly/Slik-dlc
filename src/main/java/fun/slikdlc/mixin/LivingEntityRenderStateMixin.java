package fun.slikdlc.mixin;

import fun.slikdlc.client.modules.impl.render.SeeInvisiblesRenderState;
import net.minecraft.class_10042;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({class_10042.class})
public class LivingEntityRenderStateMixin implements SeeInvisiblesRenderState {
   @Unique
   private boolean slikdlc$seeInvisiblesTarget;

   public LivingEntityRenderStateMixin() {
   }

   @Override
   public boolean slikdlc$isSeeInvisiblesTarget() {
      return this.slikdlc$seeInvisiblesTarget;
   }

   @Override
   public void slikdlc$setSeeInvisiblesTarget(boolean value) {
      this.slikdlc$seeInvisiblesTarget = value;
   }
}
