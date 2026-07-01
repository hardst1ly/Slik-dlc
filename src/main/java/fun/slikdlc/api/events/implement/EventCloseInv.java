package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;

public class EventCloseInv extends Event {
   public int windowId;

   @Generated
   public EventCloseInv(int windowId) {
      this.windowId = windowId;
   }
}
