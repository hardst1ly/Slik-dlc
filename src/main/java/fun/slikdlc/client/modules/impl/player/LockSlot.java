package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.mixin.SlotAccessor;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_2813;
import net.minecraft.class_2846;
import net.minecraft.class_465;
import net.minecraft.class_2846.class_2847;

public class LockSlot extends Module {
   public static LockSlot INSTANCE = new LockSlot();
   private final ListSetting slots = new ListSetting(
      "Слоты",
      new BooleanSetting("1", false),
      new BooleanSetting("2", false),
      new BooleanSetting("3", false),
      new BooleanSetting("4", false),
      new BooleanSetting("5", false),
      new BooleanSetting("6", false),
      new BooleanSetting("7", false),
      new BooleanSetting("8", false),
      new BooleanSetting("9", false)
   );

   public LockSlot() {
      super("LockSlot", "Блокирует выброс предметов из выбранных слотов", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.slots});
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && event.getType() == EventPacket.Type.SEND) {
         if (!(mc.field_1755 instanceof class_465)) {
            if (event.getPacket() instanceof class_2846 packet) {
               if (packet.method_12363() == class_2847.field_12975 || packet.method_12363() == class_2847.field_12970) {
                  if (this.isCurrentSlotLockedForDrop()) {
                     event.cancel();
                     this.sendLockedMessage(mc.field_1724.method_31548().field_7545);
                  }
               }
            } else {
               if (event.getPacket() instanceof class_2813 packetx && packetx.method_12195() == class_1713.field_7795) {
                  int hotbarSlot = this.getHotbarSlotFromClick(packetx.method_12192());
                  if (hotbarSlot >= 0 && this.isHotbarSlotLocked(hotbarSlot)) {
                     event.cancel();
                     this.sendLockedMessage(hotbarSlot);
                  }
               }
            }
         }
      }
   }

   public boolean isCurrentSlotLockedForDrop() {
      if (!this.isEnable() || mc.field_1724 == null || mc.field_1724.method_6047().method_7960()) {
         return false;
      } else {
         return mc.field_1755 instanceof class_465 ? false : this.isHotbarSlotLocked(mc.field_1724.method_31548().field_7545);
      }
   }

   private boolean isHotbarSlotLocked(int slot) {
      return slot >= 0 && slot < this.slots.getSettings().size() ? this.slots.getSettings().get(slot).isState() : false;
   }

   private int getHotbarSlotFromClick(int slotId) {
      if (mc.field_1724 != null && slotId >= 0 && slotId < mc.field_1724.field_7512.field_7761.size()) {
         class_1735 slot = mc.field_1724.field_7512.method_7611(slotId);
         SlotAccessor accessor = (SlotAccessor)slot;
         int inventoryIndex = accessor.slikdlc$getIndex();
         return accessor.slikdlc$getInventory() == mc.field_1724.method_31548() && inventoryIndex >= 0 && inventoryIndex <= 8 ? inventoryIndex : -1;
      } else {
         return -1;
      }
   }

   private void sendLockedMessage(int slot) {
      ChatUtils.sendMessage("Выброс предмета из слота " + (slot + 1) + " заблокирован");
   }
}
