package fun.slikdlc.client.modules.impl.combat.components;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.implement.EventMove;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.utils.network.NetworkUtils;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2708;
import net.minecraft.class_2815;
import net.minecraft.class_2828.class_2830;

public class CatlavanElytraBypass implements QClient {
   private static final double MAX_POSITION_DRIFT = 1.0E-4D;

   private final BooleanSetting swapChestplate;
   private final FloatSetting attackDistance;
   private final FloatSetting rangeSetting;

   private class_243 freezePosition;
   private int swapCooldownTicks;
   private float savedAttackRange;
   private boolean rangeOverridden;

   public CatlavanElytraBypass(BooleanSetting swapChestplate, FloatSetting attackDistance, FloatSetting rangeSetting) {
      this.swapChestplate = swapChestplate;
      this.attackDistance = attackDistance;
      this.rangeSetting = rangeSetting;
   }

   public void onEnable() {
      this.applyAttackDistance();
      this.swapCooldownTicks = 0;
      this.freezePosition = null;
      if (mc.field_1724 == null) {
         return;
      }

      boolean wearingElytra = mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
      if (wearingElytra && this.swapChestplate.isState()) {
         this.equipBestChestplate();
         this.swapCooldownTicks = 1;
      } else {
         this.freezePosition = mc.field_1724.method_19538();
      }
   }

   public void onDisable() {
      this.freezePosition = null;
      this.swapCooldownTicks = 0;
      this.restoreAttackDistance();
   }

   public void onUpdate() {
      this.applyAttackDistance();
      if (this.swapCooldownTicks > 0) {
         this.swapCooldownTicks--;
         if (this.swapCooldownTicks == 0 && this.freezePosition == null && mc.field_1724 != null) {
            this.freezePosition = mc.field_1724.method_19538();
         }
      }

      if (this.freezePosition != null && mc.field_1724 != null && this.swapChestplate.isState()) {
         mc.field_1724.method_18800(0.0, 0.0, 0.0);
         if (mc.field_1724.method_19538().method_1025(this.freezePosition) > MAX_POSITION_DRIFT) {
            this.freezePosition = mc.field_1724.method_19538();
         }

         if (mc.field_1724.field_6012 % 2 == 0 && mc.field_1724.field_3944 != null) {
            NetworkUtils.sendSilentPacket(
               new class_2830(
                  this.freezePosition.field_1352,
                  this.freezePosition.field_1351,
                  this.freezePosition.field_1350,
                  mc.field_1724.method_36454(),
                  mc.field_1724.method_36455(),
                  mc.field_1724.method_24828(),
                  mc.field_1724.field_5976
               )
            );
         }
      }
   }

   public void onMove(EventMove event) {
      if (this.freezePosition != null && this.swapChestplate.isState()) {
         event.setMovePos(class_243.field_1353);
      }
   }

   public void onPacket(EventPacket event) {
      if (this.freezePosition == null || !this.swapChestplate.isState()) {
         return;
      }

      if (event.getType() == EventPacket.Type.RECEIVE && event.getPacket() instanceof class_2708) {
         event.cancel();
      }
   }

   private void applyAttackDistance() {
      if (!this.swapChestplate.isState()) {
         return;
      }

      if (!this.rangeOverridden) {
         this.savedAttackRange = this.rangeSetting.get();
         this.rangeOverridden = true;
      }

      this.rangeSetting.setValue(this.attackDistance.get());
   }

   private void restoreAttackDistance() {
      if (this.rangeOverridden) {
         this.rangeSetting.setValue(this.savedAttackRange);
         this.rangeOverridden = false;
      }
   }

   private void equipBestChestplate() {
      int slot = InventoryUtils.findBestChestplateSlot();
      if (slot == -1 || mc.field_1724 == null || mc.field_1761 == null) {
         return;
      }

      if (slot >= 0 && slot < 9) {
         mc.field_1761.method_2906(0, 6, slot, class_1713.field_7791, mc.field_1724);
      } else {
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, 6, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
      }

      if (mc.field_1724.field_3944 != null) {
         mc.field_1724.field_3944.method_52787(new class_2815(0));
      }
   }
}
