package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.input.KeyBoardUtils;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.api.utils.rotate.RotationUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import lombok.Generated;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1713;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2815;
import net.minecraft.class_2824;
import net.minecraft.class_2868;
import net.minecraft.class_2885;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public final class AutoExplosion extends Module {
   public static AutoExplosion INSTANCE = new AutoExplosion();
   private final ModeSetting modeBaxa = new ModeSetting("Режим взрыва", "Авто", "Авто", "По бинду");
   private final BindSetting bind = new BindSetting("Бинд", -1).visible(() -> this.modeBaxa.is("По бинду"));
   private final BooleanSetting explosionOnRightClick = new BooleanSetting("Взрыв по ПКМ", true);
   private final BooleanSetting keepCrystal = new BooleanSetting("Оставлять кристалл", false);
   private static final double INTERACT_RANGE = 4.5;
   private class_2338 targetPos;
   private int targetSlot = -1;
   private int oldSlot = -1;
   private boolean needSync;
   private class_238 crystalArea;
   private boolean blocked;
   private boolean internalInteract;

   public AutoExplosion() {
      super("AutoExplosion", "Автоматически взрывает кристалл", Module.ModuleCategory.COMBAT);
      this.addSettings(new Setting[]{this.modeBaxa, this.bind, this.explosionOnRightClick, this.keepCrystal});
   }

   @EventLink
   public void onBinding(EventBinding event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null) {
         if (this.modeBaxa.is("По бинду")) {
            boolean pressed = this.bind.getKey() == -1 ? event.getKey() == KeyBoardUtils.createMouseBind(2) : event.getKey() == this.bind.getKey();
            if (pressed) {
               this.placeObsidianByCrosshair();
            }
         }
      }
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getType() == EventPacket.Type.SEND) {
            if (!this.internalInteract) {
               if (event.getPacket() instanceof class_2885 packet) {
                  class_3965 hit = packet.method_12543();
                  class_2338 clickedPos = hit.method_17777();
                  class_2338 placePos = clickedPos.method_10093(hit.method_17780());
                  if (this.isHoldingObsidian() && this.isInRange(placePos) && !mc.field_1724.method_7357().method_7904(new class_1799(class_1802.field_8301))) {
                     int crystalSlot = this.findCrystalSlot();
                     if (crystalSlot != -1) {
                        this.targetPos = placePos;
                        this.targetSlot = crystalSlot;
                        this.blocked = true;
                     }
                  }

                  if (this.explosionOnRightClick.isState() && this.shouldPlaceByRightClick(clickedPos) && this.placeCrystalFromOffhand(hit, clickedPos)) {
                     event.cancel();
                  }
               }
            }
         }
      }
   }

   @EventLink
   public void onTick(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.needSync) {
            this.needSync = false;
            this.restoreSelectedSlot();
         }

         if (this.targetPos != null) {
            if (mc.field_1687.method_8320(this.targetPos).method_26215()) {
               this.targetPos = null;
            } else if (this.blocked) {
               this.blocked = false;
            } else {
               this.tryPlaceCrystalFast(this.targetPos);
            }
         }

         this.processCrystalArea();
      } else {
         this.reset();
      }
   }

   private void tryPlaceCrystalFast(class_2338 pos) {
      if (this.targetSlot >= 0 && this.targetSlot <= 8 && this.canPlaceCrystal(pos)) {
         this.rotateTo(class_243.method_24953(pos));
         this.oldSlot = mc.field_1724.method_31548().field_7545;
         mc.method_1562().method_52787(new class_2868(this.targetSlot));
         mc.field_1724.method_31548().field_7545 = this.targetSlot;
         class_243 hitVec = class_243.method_24953(pos).method_1031(0.0, 0.5, 0.0);
         class_3965 result = new class_3965(hitVec, class_2350.field_11036, pos, false);
         this.sendInteract(class_1268.field_5808, result);
         mc.field_1724.method_6104(class_1268.field_5808);
         this.needSync = true;
         this.crystalArea = this.boxFromBlock(pos.method_10084()).method_1014(0.1);
         this.targetPos = null;
      }
   }

   private void processCrystalArea() {
      if (this.crystalArea != null) {
         for (class_1297 entity : mc.field_1687.method_8335(null, this.crystalArea)) {
            if (entity instanceof class_1511 crystal && crystal.method_5805()) {
               if (!crystal.method_5829().method_1006(mc.field_1724.method_33571())) {
                  this.rotateTo(crystal.method_5829().method_1005());
               }

               this.attackCrystal(crystal);
               this.crystalArea = null;
               if (!this.keepCrystal.isState()) {
                  this.restoreSelectedSlot();
               }

               return;
            }
         }
      }
   }

   private boolean shouldPlaceByRightClick(class_2338 clickedPos) {
      if (mc.field_1724.method_7357().method_7904(new class_1799(class_1802.field_8301))) {
         return false;
      } else if (this.isHoldingBlockForPlace()) {
         return false;
      } else {
         class_2248 block = mc.field_1687.method_8320(clickedPos).method_26204();
         return block != class_2246.field_10540 && block != class_2246.field_9987 ? false : mc.field_1687.method_8320(clickedPos.method_10084()).method_26215();
      }
   }

   private boolean placeCrystalFromOffhand(class_3965 hit, class_2338 clickedPos) {
      int slot = this.findScreenSlot(class_1802.field_8301);
      if (slot == -1 && mc.field_1724.method_6079().method_7909() != class_1802.field_8301) {
         return false;
      } else {
         boolean swapped = false;
         if (mc.field_1724.method_6079().method_7909() != class_1802.field_8301) {
            this.swapSlotToOffhand(slot);
            swapped = true;
         }

         this.sendInteract(class_1268.field_5810, hit);
         mc.field_1724.method_6104(class_1268.field_5810);
         this.crystalArea = this.boxFromBlock(clickedPos.method_10084()).method_1014(0.1);
         if (swapped) {
            this.swapSlotToOffhand(slot);
            mc.field_1724.field_3944.method_52787(new class_2815(0));
         }

         return true;
      }
   }

   private void placeObsidianByCrosshair() {
      int obsidianSlot = this.findScreenSlot(class_1802.field_8281);
      int crystalSlot = this.findCrystalSlot();
      if (obsidianSlot != -1 && crystalSlot != -1) {
         if (mc.field_1765 instanceof class_3965 hit) {
            if (hit.method_17783() == class_240.field_1332) {
               if (!mc.field_1687.method_8320(hit.method_17777()).method_26215()) {
                  class_2338 placePos = hit.method_17777().method_10093(hit.method_17780());
                  this.targetPos = placePos;
                  this.targetSlot = crystalSlot;
                  this.blocked = true;
                  this.swapSlotToOffhand(obsidianSlot);
                  this.sendInteract(class_1268.field_5810, hit);
                  mc.field_1724.method_6104(class_1268.field_5810);
                  this.swapSlotToOffhand(obsidianSlot);
                  mc.field_1724.field_3944.method_52787(new class_2815(0));
               }
            }
         }
      }
   }

   private void attackCrystal(class_1511 crystal) {
      mc.method_1562().method_52787(class_2824.method_34206(crystal, false));
      mc.field_1724.method_6104(class_1268.field_5808);
   }

   private void sendInteract(class_1268 hand, class_3965 hitResult) {
      this.internalInteract = true;

      try {
         mc.method_1562().method_52787(new class_2885(hand, hitResult, 0));
      } finally {
         this.internalInteract = false;
      }
   }

   private void rotateTo(class_243 vec) {
      class_241 rotation = RotationUtils.getRotations(vec);
      RotationStorage.update(new Rotation(rotation.field_1343, rotation.field_1342), 360.0F, 360.0F, 360.0F, 360.0F, 1, 2, false);
   }

   private boolean canPlaceCrystal(class_2338 pos) {
      class_2338 up1 = pos.method_10084();
      class_2338 up2 = pos.method_10086(2);
      if (!mc.field_1687.method_8320(up1).method_26215()) {
         return false;
      } else if (!mc.field_1687.method_8320(up2).method_26215()) {
         return false;
      } else {
         class_238 box = new class_238(
            up1.method_10263(), up1.method_10264(), up1.method_10260(), up1.method_10263() + 1.0, up1.method_10264() + 2.0, up1.method_10260() + 1.0
         );

         for (class_1297 entity : mc.field_1687.method_8335(null, box)) {
            if (!(entity instanceof class_1511)) {
               return false;
            }
         }

         return true;
      }
   }

   private int findCrystalSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8301) {
            return i;
         }
      }

      return -1;
   }

   private int findScreenSlot(class_1792 item) {
      for (int i = 9; i < 45; i++) {
         class_1799 stack = mc.field_1724.field_7498.method_7611(i).method_7677();
         if (stack.method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   private void swapSlotToOffhand(int slot) {
      if (slot >= 36 && slot <= 44) {
         mc.field_1761.method_2906(0, 45, slot - 36, class_1713.field_7791, mc.field_1724);
      } else {
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, 45, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
      }
   }

   private void restoreSelectedSlot() {
      if (this.oldSlot != -1) {
         mc.method_1562().method_52787(new class_2868(this.oldSlot));
         mc.field_1724.method_31548().field_7545 = this.oldSlot;
         this.oldSlot = -1;
      }
   }

   private class_238 boxFromBlock(class_2338 pos) {
      return new class_238(
         pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1.0, pos.method_10264() + 1.0, pos.method_10260() + 1.0
      );
   }

   private boolean isHoldingObsidian() {
      return mc.field_1724.method_6047().method_7909() == class_1802.field_8281 || mc.field_1724.method_6079().method_7909() == class_1802.field_8281;
   }

   private boolean isHoldingBlockForPlace() {
      class_1792 main = mc.field_1724.method_6047().method_7909();
      class_1792 off = mc.field_1724.method_6079().method_7909();
      return main instanceof class_1747 && main != class_1802.field_8575 || off instanceof class_1747 && off != class_1802.field_8575;
   }

   private boolean isInRange(class_2338 pos) {
      return mc.field_1724.method_33571().method_1022(class_243.method_24953(pos)) <= 4.5;
   }

   private void reset() {
      if (this.oldSlot != -1 && mc.field_1724 != null && mc.method_1562() != null) {
         this.restoreSelectedSlot();
      }

      this.targetPos = null;
      this.targetSlot = -1;
      this.needSync = false;
      this.crystalArea = null;
      this.blocked = false;
      this.internalInteract = false;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.reset();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.reset();
   }

   @Generated
   public ModeSetting getModeBaxa() {
      return this.modeBaxa;
   }

   @Generated
   public BindSetting getBind() {
      return this.bind;
   }

   @Generated
   public BooleanSetting getExplosionOnRightClick() {
      return this.explosionOnRightClick;
   }

   @Generated
   public BooleanSetting getKeepCrystal() {
      return this.keepCrystal;
   }

   @Generated
   public class_2338 getTargetPos() {
      return this.targetPos;
   }

   @Generated
   public int getTargetSlot() {
      return this.targetSlot;
   }

   @Generated
   public int getOldSlot() {
      return this.oldSlot;
   }

   @Generated
   public boolean isNeedSync() {
      return this.needSync;
   }

   @Generated
   public class_238 getCrystalArea() {
      return this.crystalArea;
   }

   @Generated
   public boolean isBlocked() {
      return this.blocked;
   }

   @Generated
   public boolean isInternalInteract() {
      return this.internalInteract;
   }

   @Generated
   public void setTargetPos(class_2338 targetPos) {
      this.targetPos = targetPos;
   }

   @Generated
   public void setTargetSlot(int targetSlot) {
      this.targetSlot = targetSlot;
   }

   @Generated
   public void setOldSlot(int oldSlot) {
      this.oldSlot = oldSlot;
   }

   @Generated
   public void setNeedSync(boolean needSync) {
      this.needSync = needSync;
   }

   @Generated
   public void setCrystalArea(class_238 crystalArea) {
      this.crystalArea = crystalArea;
   }

   @Generated
   public void setBlocked(boolean blocked) {
      this.blocked = blocked;
   }

   @Generated
   public void setInternalInteract(boolean internalInteract) {
      this.internalInteract = internalInteract;
   }
}
