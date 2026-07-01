package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_1657;

public class EventPopTotem extends Event {
   private final class_1657 player;

   @Generated
   public EventPopTotem(class_1657 player) {
      this.player = player;
   }

   @Generated
   public class_1657 getPlayer() {
      return this.player;
   }
}
