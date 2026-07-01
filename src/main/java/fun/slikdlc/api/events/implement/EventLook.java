package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;

public class EventLook extends Event {
   private double yaw;
   private double pitch;

   @Generated
   public double getYaw() {
      return this.yaw;
   }

   @Generated
   public double getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(double yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(double pitch) {
      this.pitch = pitch;
   }

   @Generated
   public EventLook(double yaw, double pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
}
