package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;

public class EventSlowWalking extends Event {
   private boolean cancelled;

   public EventSlowWalking() {
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
