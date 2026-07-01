package fun.slikdlc.client.modules.impl.render.base;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.draggable.Draggable;
import lombok.Generated;

public class InterfaceProcessing implements QClient {
   public final Draggable draggable;
   private boolean unusualRectType = true;

   public boolean isUnusualRectType() {
      return this.unusualRectType;
   }

   public void setUnusualRectType(boolean unusualRectType) {
      this.unusualRectType = unusualRectType;
   }

   public void onUpdate(EventUpdate eventUpdate) {
   }

   public void onRender(EventRender.Default eventRender) {
   }

   @Generated
   public InterfaceProcessing(Draggable draggable) {
      this.draggable = draggable;
   }
}
