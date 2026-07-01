package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;

public class EventChunkUpdate extends Event {
   private final int chunkX;
   private final int chunkZ;

   @Generated
   public int getChunkX() {
      return this.chunkX;
   }

   @Generated
   public int getChunkZ() {
      return this.chunkZ;
   }

   @Generated
   public EventChunkUpdate(int chunkX, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }
}
