package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventMove;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.utils.network.NetworkUtils;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2815;
import net.minecraft.class_2828;
import net.minecraft.class_2828.class_2829;
import net.minecraft.class_2828.class_2830;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_2828.class_5911;

public class AirStuck extends Module {
   public static AirStuck INSTANCE = new AirStuck();
   private final ModeSetting mode = new ModeSetting("Мод", "Обычный", "Обычный", "LonyGrief");
   private final BooleanSetting cancelPackets = new BooleanSetting("Отменять пакеты", true);
   private final BooleanSetting swapElytra = new BooleanSetting("Свапать элитру", true);
   private class_243 freezePosition = class_243.field_1353;
   private boolean frozen = false;

   public AirStuck() {
      super("AirStuck", "Зависает в воздухе", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.cancelPackets, this.swapElytra});
   }

   @Override
   public void onEnable() {
      this.frozen = false;
      if (mc.field_1724 != null && this.swapElytra.isState()) {
         this.swapChestEquipment();
      }

      if (mc.field_1724 != null && this.mode.is("Обычный")) {
         this.freezePosition = mc.field_1724.method_19538();
         this.frozen = true;
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.frozen = false;
      super.onDisable();
   }

   private void swapChestEquipment() {
      class_1799 chestStack = mc.field_1724.method_6118(class_1304.field_6174);
      if (chestStack.method_31574(class_1802.field_8833)) {
         int chestplateSlot = InventoryUtils.findBestChestplateSlot();
         if (chestplateSlot != -1) {
            this.doSwap(chestplateSlot);
         }
      }
   }

   private void doSwap(int slot) {
      if (slot >= 0 && slot < 9) {
         mc.field_1761.method_2906(0, 6, slot, class_1713.field_7791, mc.field_1724);
      } else {
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, 6, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
      }

      mc.field_1724.field_3944.method_52787(new class_2815(0));
   }

   @EventLink
   public void onMove(EventMove e) {
      if (mc.field_1724 != null) {
         if (this.mode.is("LonyGrief") && !this.frozen && mc.field_1724.field_6017 > 0.0F && mc.field_1724.method_18798().field_1351 < 0.0) {
            this.freezePosition = mc.field_1724.method_19538();
            this.frozen = true;
         }

         if (this.frozen) {
            e.setMovePos(class_243.field_1353);
            mc.field_1724.method_5814(this.freezePosition.field_1352, this.freezePosition.field_1351, this.freezePosition.field_1350);
            mc.field_1724.method_18800(0.0, 0.0, 0.0);
         }
      }
   }

   @EventLink
   public void onPacket(EventPacket e) {
      if (this.frozen && e.getType() == EventPacket.Type.SEND) {
         if (e.getPacket() instanceof class_2828 packet) {
            if (this.cancelPackets.isState()) {
               e.cancel();
            } else {
               e.cancel();
               NetworkUtils.sendSilentPacket(this.createFrozenPacket(packet));
            }
         }
      }
   }

   private class_2828 createFrozenPacket(class_2828 packet) {
      boolean onGround = packet.method_12273();
      boolean horizontalCollision = packet.method_61225();
      if (packet.method_36171() && packet.method_36172()) {
         return new class_2830(
            this.freezePosition.field_1352,
            this.freezePosition.field_1351,
            this.freezePosition.field_1350,
            packet.method_12271(mc.field_1724.method_36454()),
            packet.method_12270(mc.field_1724.method_36455()),
            onGround,
            horizontalCollision
         );
      } else if (packet.method_36171()) {
         return new class_2829(this.freezePosition.field_1352, this.freezePosition.field_1351, this.freezePosition.field_1350, onGround, horizontalCollision);
      } else {
         return (class_2828)(packet.method_36172()
            ? new class_2831(
               packet.method_12271(mc.field_1724.method_36454()), packet.method_12270(mc.field_1724.method_36455()), onGround, horizontalCollision
            )
            : new class_5911(onGround, horizontalCollision));
      }
   }
}
