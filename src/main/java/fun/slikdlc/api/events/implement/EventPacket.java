package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_2596;

public class EventPacket extends Event {
   private final class_2596<?> packet;
   private final EventPacket.Type type;

   @Generated
   public EventPacket(class_2596<?> packet, EventPacket.Type type) {
      this.packet = packet;
      this.type = type;
   }

   @Generated
   public class_2596<?> getPacket() {
      return this.packet;
   }

   @Generated
   public EventPacket.Type getType() {
      return this.type;
   }

   public static enum Type {
      SEND,
      RECEIVE;

      private Type() {
      }
   }
}
