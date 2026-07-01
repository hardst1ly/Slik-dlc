package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

public class EventAttackEntity extends Event {
   private final class_1657 player;
   private final class_1297 target;

   @Generated
   public EventAttackEntity(class_1657 player, class_1297 target) {
      this.player = player;
      this.target = target;
   }

   @Generated
   public class_1657 getPlayer() {
      return this.player;
   }

   @Generated
   public class_1297 getTarget() {
      return this.target;
   }
}
