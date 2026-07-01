package fun.slikdlc.api.storages.implement;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventPopTotem;
import fun.slikdlc.api.events.implement.EventTickPre;
import java.lang.reflect.InvocationTargetException;
import lombok.Generated;
import net.minecraft.class_1657;
import net.minecraft.class_2663;
import net.minecraft.class_2828;
import net.minecraft.class_2848;
import net.minecraft.class_2868;

public class ServerStorage implements QClient {
   private int serverSlot;
   private float serverYaw;
   private float serverPitch;
   private float fallDistance;
   private double serverX;
   private double serverY;
   private double serverZ;
   private boolean serverOnGround;
   private boolean serverSprinting;
   private boolean serverSneaking;
   private boolean serverHorizontalCollision;

   public ServerStorage() {
   }

   public void ServerManager() {
      EventInvoker.register(this);
   }

   @EventLink
   public void onTick(EventTickPre e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         double y = mc.field_1724.field_6036 - mc.field_1724.method_23318();
         if (mc.field_1724.method_24828()) {
            this.fallDistance = 0.0F;
         } else if (y > 0.0) {
            this.fallDistance += (float)y;
         }
      }
   }

   @EventLink
   public void onPacketSend(EventPacket e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (e.getPacket() instanceof class_2828 packet) {
            if (packet.method_36171()) {
               this.serverX = packet.method_12269(mc.field_1724.method_23317());
               this.serverY = packet.method_12268(mc.field_1724.method_23318());
               this.serverZ = packet.method_12274(mc.field_1724.method_23321());
            }

            if (packet.method_36172()) {
               this.serverYaw = packet.method_12271(mc.field_1724.method_36454());
               this.serverPitch = packet.method_12270(mc.field_1724.method_36455());
            }

            this.serverOnGround = packet.method_12273();
            this.serverHorizontalCollision = packet.method_61225();
         }

         if (e.getPacket() instanceof class_2868 packet) {
            this.serverSlot = packet.method_12442();
         }

         if (e.getPacket() instanceof class_2848 packet) {
            switch (packet.method_12365()) {
               case field_12981:
                  e.setCancelled(this.serverSprinting);
                  if (!e.isCancelled()) {
                     this.serverSprinting = true;
                  }
                  break;
               case field_12985:
                  e.setCancelled(!this.serverSprinting);
                  if (!e.isCancelled()) {
                     this.serverSprinting = false;
                  }
                  break;
               case field_12979:
                  this.serverSneaking = true;
                  break;
               case field_12984:
                  this.serverSneaking = false;
            }
         }
      }
   }

   @EventLink
   public void onPacketReceive(EventPacket e) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (e.getPacket() instanceof class_2663 packet && packet.method_11470() == 35) {
            if (!(packet.method_11469(mc.field_1687) instanceof class_1657 player)) {
               return;
            }

            EventInvoker.invoke(new EventPopTotem(player));
         }
      }
   }

   @Generated
   public int getServerSlot() {
      return this.serverSlot;
   }

   @Generated
   public float getServerYaw() {
      return this.serverYaw;
   }

   @Generated
   public float getServerPitch() {
      return this.serverPitch;
   }

   @Generated
   public float getFallDistance() {
      return this.fallDistance;
   }

   @Generated
   public double getServerX() {
      return this.serverX;
   }

   @Generated
   public double getServerY() {
      return this.serverY;
   }

   @Generated
   public double getServerZ() {
      return this.serverZ;
   }

   @Generated
   public boolean isServerOnGround() {
      return this.serverOnGround;
   }

   @Generated
   public boolean isServerSprinting() {
      return this.serverSprinting;
   }

   @Generated
   public boolean isServerSneaking() {
      return this.serverSneaking;
   }

   @Generated
   public boolean isServerHorizontalCollision() {
      return this.serverHorizontalCollision;
   }
}
