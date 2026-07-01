package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_243;

public class EventMove extends Event {
   private class_243 movePos;

   @Generated
   public class_243 getMovePos() {
      return this.movePos;
   }

   @Generated
   public void setMovePos(class_243 movePos) {
      this.movePos = movePos;
   }

   @Generated
   public EventMove() {
   }

   @Generated
   public EventMove(class_243 movePos) {
      this.movePos = movePos;
   }
}
