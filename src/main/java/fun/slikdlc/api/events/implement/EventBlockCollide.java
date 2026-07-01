package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_2338;

public class EventBlockCollide extends Event {
   private final class_2338 pos;

   public EventBlockCollide(class_2338 pos) {
      this.pos = pos;
   }

   @Generated
   public class_2338 getPos() {
      return this.pos;
   }
}
