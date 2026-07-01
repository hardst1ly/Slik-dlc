package fun.slikdlc.api.events;

import java.lang.reflect.InvocationTargetException;
import lombok.Generated;

public class Event {
   private boolean cancelled;

   public Event() {
   }

   public void cancel() {
      this.cancelled = true;
   }

   public void call() {
      try {
         EventInvoker.invoke(this);
      } catch (InvocationTargetException | InstantiationException | IllegalAccessException var2) {
         throw new RuntimeException("Failed to Invoke Method", var2);
      }
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }
}
