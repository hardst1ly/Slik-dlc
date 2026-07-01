package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventCloseInv;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.player.MoveUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2813;
import net.minecraft.class_2815;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_498;

public class InventoryWalk extends Module {
   public static InventoryWalk INSTANCE = new InventoryWalk();
   public ModeSetting mode = new ModeSetting("Обход", "Обычный", "Обычный", "Grim");
   public ModeSetting grimVersion = new ModeSetting("Версия свапа", "1.21.4", "1.21.4", "1.16.5").visible(() -> this.mode.is("Grim"));
   public int tick = 0;
   private final List<class_2813> pendingPackets = new ArrayList<>();
   private class_2815 pendingClosePacket = null;
   private boolean sprintPaused = false;
   private boolean waitingToClose = false;
   private int delayedFlushTicks = -1;
   private boolean flushingPackets = false;

   public InventoryWalk() {
      super("InventoryWalk", "Ходьба с открытым инвентарём", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.grimVersion});
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null) {
         class_304[] pressedKeys = new class_304[]{
            mc.field_1690.field_1894,
            mc.field_1690.field_1881,
            mc.field_1690.field_1913,
            mc.field_1690.field_1849,
            mc.field_1690.field_1903,
            mc.field_1690.field_1867
         };
         if (this.mode.is("Grim") && this.grimVersion.is("1.21.4") && this.waitingToClose && !MoveUtils.isMoving()) {
            this.flushQueuedPackets(true);
            this.waitingToClose = false;
            this.tick = 3;
         }

         if (this.mode.is("Grim") && this.grimVersion.is("1.16.5") && this.delayedFlushTicks >= 0) {
            if (this.delayedFlushTicks == 0) {
               this.flushQueuedPackets(true);
               this.delayedFlushTicks = -1;
               this.tick = 1;
            } else {
               this.delayedFlushTicks--;
            }
         }

         if (this.tick == 0 && !this.pendingPackets.isEmpty() && mc.field_1755 == null && !this.waitingToClose) {
            this.sendPendingPackets();
         }

         if (this.tick != 0) {
            for (class_304 keyBinding : pressedKeys) {
               keyBinding.method_23481(false);
            }

            this.tick--;
            if (this.tick == 0 && this.sprintPaused) {
               this.sprintPaused = false;
               Sprint.popPause();
            }
         } else if (!(mc.field_1755 instanceof class_408) && !(mc.field_1755 instanceof class_498)) {
            if (!this.mode.is("Grim") || !(mc.field_1755 instanceof class_465) || mc.field_1755 instanceof class_490) {
               if (this.waitingToClose) {
                  for (class_304 keyBinding : pressedKeys) {
                     keyBinding.method_23481(false);
                  }
               } else {
                  for (class_304 keyBinding : pressedKeys) {
                     boolean isKeyPressed = class_3675.method_15987(mc.method_22683().method_4490(), keyBinding.method_1429().method_1444());
                     keyBinding.method_23481(isKeyPressed);
                  }
               }
            }
         }
      }
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (event.getType() == EventPacket.Type.SEND && !this.flushingPackets) {
         Object packet = event.getPacket();
         if (this.mode.is("Grim") && MoveUtils.isMoving() && mc.field_1755 instanceof class_490) {
            if (packet instanceof class_2813 clickPacket) {
               this.pendingPackets.add(clickPacket);
               event.cancel();
            } else {
               if (packet instanceof class_2815 closePacket) {
                  this.pendingClosePacket = closePacket;
                  if (this.grimVersion.is("1.16.5")) {
                     this.delayedFlushTicks = 1;
                     this.waitingToClose = false;
                  } else {
                     this.waitingToClose = true;
                  }

                  this.pauseSprint();
                  event.cancel();
               }
            }
         }
      }
   }

   @EventLink
   public void onCloseInv(EventCloseInv eventCloseInv) {
      if (this.mode.is("Grim") && this.grimVersion.is("1.16.5") && MoveUtils.isMoving() && mc.field_1755 instanceof class_490) {
         this.pendingClosePacket = new class_2815(eventCloseInv.windowId);
         this.delayedFlushTicks = 1;
         this.pauseSprint();
         this.tick = 1;
         eventCloseInv.cancel();
      } else {
         if (this.mode.is("Grim") && !this.waitingToClose) {
            this.pauseSprint();
            this.tick = 1;
         }
      }
   }

   private void pauseSprint() {
      if (!this.sprintPaused) {
         Sprint.pushPause(0L);
         this.sprintPaused = true;
      }
   }

   private void sendPendingPackets() {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         this.flushingPackets = true;

         try {
            for (class_2813 packet : this.pendingPackets) {
               mc.method_1562().method_52787(packet);
            }
         } finally {
            this.flushingPackets = false;
         }

         this.pendingPackets.clear();
      } else {
         this.pendingPackets.clear();
      }
   }

   private void flushQueuedPackets(boolean includeClose) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         this.sendPendingPackets();
         if (includeClose && this.pendingClosePacket != null) {
            this.flushingPackets = true;

            try {
               mc.method_1562().method_52787(this.pendingClosePacket);
            } finally {
               this.flushingPackets = false;
            }

            this.pendingClosePacket = null;
         }
      } else {
         this.pendingPackets.clear();
         this.pendingClosePacket = null;
      }
   }

   public static void stopTick(int ticks) {
      InventoryWalk inventoryWalk = ModuleClass.inventoryWalk;
      if (inventoryWalk != null && inventoryWalk.isEnable()) {
         inventoryWalk.tick = Math.max(inventoryWalk.tick, ticks);
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.flushQueuedPackets(true);
      if (this.sprintPaused) {
         this.sprintPaused = false;
         Sprint.popPause();
      }

      this.waitingToClose = false;
      this.delayedFlushTicks = -1;
      this.flushingPackets = false;
      this.tick = 0;
   }
}
