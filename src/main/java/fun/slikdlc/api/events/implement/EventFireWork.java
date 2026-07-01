package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_1671;

public class EventFireWork extends Event {
   private final class_1671 firework;

   @Generated
   public class_1671 getFirework() {
      return this.firework;
   }

   @Generated
   public EventFireWork(class_1671 firework) {
      this.firework = firework;
   }
}
