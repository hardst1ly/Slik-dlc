package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1753;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1835;
import net.minecraft.class_2868;
import net.minecraft.class_2886;

public class Spider extends Module {
   public static Spider INSTANCE = new Spider();
   private final ModeSetting mode = new ModeSetting("Мод", "Вода", "Вода", "SpookyTime");
   private final BooleanSetting legit = new BooleanSetting("Легит", false);
   private int lastSlot = -1;
   private boolean isClimbing = false;
   private int swapBackSlot = -1;
   private int spookyTicks;
   private int chargeSlot = -1;
   private boolean charging;

   public Spider() {
      super("Spider", "Позволяет взбираться по стенам", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.legit});
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.field_1724 != null) {
         if (this.lastSlot != -1 && this.legit.isState()) {
            mc.field_1724.method_31548().field_7545 = this.lastSlot;
         }

         this.lastSlot = -1;
         this.swapBackSlot = -1;
         this.isClimbing = false;
         this.spookyTicks = 0;
         this.chargeSlot = -1;
         this.charging = false;
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!mc.field_1724.field_5976) {
            this.stopClimbing();
         } else {
            this.isClimbing = true;
            RotationStorage.update(new Rotation(mc.field_1724.method_36454(), 0.0F), 360.0F, 360.0F, 360.0F, 360.0F, 1, 1, false);
            if (this.mode.is("SpookyTime")) {
               this.processSpookyTime();
            } else {
               int bucketSlot = this.getBucketSlot(false);
               if (bucketSlot != -1) {
                  this.useBucket(bucketSlot, this.legit.isState());
                  mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.36, mc.field_1724.method_18798().field_1350);
               }
            }
         }
      }
   }

   private void stopClimbing() {
      if (this.lastSlot != -1 && this.legit.isState()) {
         mc.field_1724.method_31548().field_7545 = this.lastSlot;
         this.lastSlot = -1;
      }

      if (this.swapBackSlot != -1) {
         mc.field_1761.method_2906(0, this.swapBackSlot, 0, class_1713.field_7794, mc.field_1724);
         this.swapBackSlot = -1;
      }

      this.isClimbing = false;
      this.spookyTicks = 0;
      this.chargeSlot = -1;
      this.charging = false;
   }

   private void processSpookyTime() {
      int bucketSlot = this.getBucketSlot(true);
      boolean bucketPulse = this.spookyTicks % 5 == 0;
      boolean boostPulse = this.spookyTicks % 4 != 3;
      this.keepChargeHeld();
      if (bucketSlot != -1 && bucketPulse) {
         this.useBucket(bucketSlot, false);
         this.keepChargeHeld();
      }

      double y = boostPulse ? 0.18 : 0.03;
      mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, y, mc.field_1724.method_18798().field_1350);
      this.spookyTicks++;
   }

   private void useBucket(int bucketSlot, boolean legitMode) {
      if (!legitMode) {
         int currentSlot = mc.field_1724.method_31548().field_7545;
         boolean isInventorySwap = bucketSlot >= 9 && bucketSlot <= 35;
         if (isInventorySwap) {
            mc.field_1761.method_2906(0, bucketSlot, currentSlot, class_1713.field_7791, mc.field_1724);
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            mc.field_1761.method_2906(0, bucketSlot, currentSlot, class_1713.field_7791, mc.field_1724);
         } else {
            mc.field_1724.field_3944.method_52787(new class_2868(bucketSlot));
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            mc.field_1724.field_3944.method_52787(new class_2868(currentSlot));
         }
      } else {
         boolean isInventorySwap = bucketSlot >= 9 && bucketSlot <= 35;
         if (isInventorySwap) {
            mc.field_1761.method_2906(0, bucketSlot, mc.field_1724.method_31548().field_7545, class_1713.field_7791, mc.field_1724);
            this.swapBackSlot = bucketSlot;
         } else if (mc.field_1724.method_31548().field_7545 != bucketSlot) {
            if (this.lastSlot == -1) {
               this.lastSlot = mc.field_1724.method_31548().field_7545;
            }

            mc.field_1724.method_31548().field_7545 = bucketSlot;
         }

         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
      }
   }

   private void keepChargeHeld() {
      if (this.isChargeItem(mc.field_1724.method_6079())) {
         if (!this.charging || this.spookyTicks % 12 == 0) {
            this.sendChargeUsePacket(class_1268.field_5810);
         }

         this.charging = true;
      } else {
         if (this.chargeSlot == -1 || !this.isChargeItem(mc.field_1724.method_31548().method_5438(this.chargeSlot))) {
            this.chargeSlot = this.getChargeHotbarSlot();
            this.charging = false;
         }

         if (this.chargeSlot != -1) {
            if (mc.field_1724.method_31548().field_7545 != this.chargeSlot) {
               mc.field_1724.field_3944.method_52787(new class_2868(this.chargeSlot));
               mc.field_1724.method_31548().field_7545 = this.chargeSlot;
               this.charging = false;
            }

            if (!this.charging || this.spookyTicks % 12 == 0) {
               this.sendChargeUsePacket(class_1268.field_5808);
            }

            this.charging = true;
         }
      }
   }

   private void sendChargeUsePacket(class_1268 hand) {
      mc.field_1724.field_3944.method_52787(new class_2886(hand, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
   }

   private int getBucketSlot(boolean allowLava) {
      for (int i = 0; i < 9; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (this.isBucket(stack, allowLava)) {
            return i;
         }
      }

      if (!this.legit.isState() || this.mode.is("SpookyTime")) {
         for (int ix = 9; ix < 36; ix++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(ix);
            if (this.isBucket(stack, allowLava)) {
               return ix;
            }
         }
      }

      return -1;
   }

   private int getChargeHotbarSlot() {
      for (int i = 0; i < 9; i++) {
         if (this.isChargeItem(mc.field_1724.method_31548().method_5438(i))) {
            return i;
         }
      }

      return -1;
   }

   private boolean isBucket(class_1799 stack, boolean allowLava) {
      return stack.method_7909() == class_1802.field_8705 || allowLava && stack.method_7909() == class_1802.field_8187;
   }

   private boolean isChargeItem(class_1799 stack) {
      return stack.method_7909() instanceof class_1753 || stack.method_7909() instanceof class_1835;
   }
}
