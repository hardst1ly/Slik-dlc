package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;

public class EventBinding extends Event {
   private final int key;
   private final EventBinding.BindType bindType;

   public boolean isKeyDown(int button) {
      return this.key == button;
   }

   @Generated
   public EventBinding(int key, EventBinding.BindType bindType) {
      this.key = key;
      this.bindType = bindType;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public EventBinding.BindType getBindType() {
      return this.bindType;
   }

   public static enum BindType {
      KEYBOARD,
      MOUSE;

      private BindType() {
      }
   }
}
