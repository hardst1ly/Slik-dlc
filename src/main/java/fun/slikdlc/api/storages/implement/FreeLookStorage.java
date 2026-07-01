package fun.slikdlc.api.storages.implement;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventLook;
import fun.slikdlc.api.events.implement.EventRotation;
import lombok.Generated;
import net.minecraft.class_3532;

public class FreeLookStorage implements QClient {
   private static boolean active;
   private static float freeYaw;
   private static float freePitch;

   public FreeLookStorage() {
      EventInvoker.register(this);
   }

   public static boolean isActive() {
      return active;
   }

   @EventLink
   public void onLook(EventLook event) {
      if (active) {
         this.rotateTowards(event.getYaw(), event.getPitch());
         event.cancel();
      }
   }

   @EventLink
   public void onRotation(EventRotation event) {
      if (active) {
         event.setYaw(freeYaw);
         event.setPitch(freePitch);
      } else {
         freeYaw = event.getYaw();
         freePitch = event.getPitch();
      }
   }

   private void rotateTowards(double targetYaw, double targetPitch) {
      freePitch = class_3532.method_15363((float)(freePitch + targetPitch * 0.15), -90.0F, 90.0F);
      freeYaw = (float)(freeYaw + targetYaw * 0.15);
   }

   @Generated
   public static void setActive(boolean active) {
      FreeLookStorage.active = active;
   }

   @Generated
   public static float getFreeYaw() {
      return freeYaw;
   }

   @Generated
   public static float getFreePitch() {
      return freePitch;
   }

   @Generated
   public static void setFreeYaw(float freeYaw) {
      FreeLookStorage.freeYaw = freeYaw;
   }

   @Generated
   public static void setFreePitch(float freePitch) {
      FreeLookStorage.freePitch = freePitch;
   }
}
