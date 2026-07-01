package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.events.implement.EventMoveInput;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.movement.Sprint;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import java.util.Set;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2815;
import net.minecraft.class_2848;
import net.minecraft.class_2868;
import net.minecraft.class_2848.class_2849;

public class ElytraSwap extends Module {
   public static ElytraSwap INSTANCE = new ElytraSwap();
   private final BindSetting elytraBind = new BindSetting("Бинд элитры", -1);
   private final BindSetting fireworkBind = new BindSetting("Бинд фейерверка", -1);
   private final BooleanSetting autofly = new BooleanSetting("Авто-взлёт", true);
   private final BooleanSetting bypassgrim = new BooleanSetting("Обходить Grim", true);
   private final BooleanSetting bypassGround = new BooleanSetting("Обходить Граунд", true);
   private boolean swapElytraQueued;
   private boolean useFirework;
   private int bypassTicks;
   private boolean sprintPaused;
   private int swapCooldown;
   private int fireworkReturnSlot = -1;
   private int fireworkReturnTicks = -1;
   private boolean packetSwapActive;
   private int packetSwapStage;
   private int packetSwapSlot;

   public ElytraSwap() {
      super("ElytraSwap", "Автоматический свап элитр", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.elytraBind, this.fireworkBind, this.autofly, this.bypassgrim, this.bypassGround});
   }

   @EventLink
   public void onInput(EventMoveInput e) {
      if (this.bypassgrim.isState() && this.bypassTicks > 0) {
         if (mc.field_1724 == null) {
            return;
         }

         mc.field_1724.method_5728(false);
         e.setForward(0.0F);
         e.setStrafe(0.0F);
         e.setJump(false);
         e.setSneak(false);
      }
   }

   @EventLink
   public void onEvent(EventUpdate ignored) {
      if (mc.field_1724 != null) {
         if (this.swapCooldown > 0) {
            this.swapCooldown--;
         }

         this.handleFireworkReturn();
         this.handlePacketSwap();
         if (this.bypassTicks > 0) {
            mc.field_1724.method_5728(false);
            this.bypassTicks--;
            if (this.bypassTicks == 1) {
               this.performSwap();
            }

            if (this.bypassTicks == 0) {
               this.restoreSprint();
            }
         } else {
            if (this.swapElytraQueued) {
               if (this.swapCooldown > 0) {
                  this.swapElytraQueued = false;
                  return;
               }

               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = 3;
                  this.swapCooldown = 1;
               } else {
                  this.performSwap();
                  this.swapCooldown = 1;
               }

               this.swapElytraQueued = false;
            }

            if (this.useFirework) {
               int slotFirework = InventoryUtils.getItemSlot(class_1802.field_8639);
               if (mc.field_1724.method_6128()) {
                  if (slotFirework != -1) {
                     if (this.bypassGround.isState()) {
                        this.executePacketFireworkSwap(slotFirework);
                     } else {
                        InventoryUtils.swapAndUseHvH(class_1802.field_8639);
                     }
                  } else {
                     ChatUtils.sendMessage("" + class_124.field_1061 + class_124.field_1067 + "Нет Фейерверков!");
                  }
               }

               this.useFirework = false;
            }

            if (this.autofly.isState() && this.bypassTicks == 0) {
               class_1799 chestStack = mc.field_1724.method_6118(class_1304.field_6174);
               if (chestStack.method_31574(class_1802.field_8833)
                  && !mc.field_1724.method_5799()
                  && !mc.field_1724.method_5771()
                  && mc.field_1724.method_24828()
                  && !mc.field_1690.field_1903.method_1434()) {
                  mc.field_1724.method_6043();
               } else if (chestStack.method_31574(class_1802.field_8833)
                  && this.isElytraUsable(chestStack)
                  && !mc.field_1724.method_6128()
                  && !mc.field_1724.method_24828()) {
                  mc.field_1724.method_23669();
                  mc.field_1724.field_3944.method_52787(new class_2848(mc.field_1724, class_2849.field_12982));
               }
            }
         }
      }
   }

   private void handlePacketSwap() {
      if (this.packetSwapActive && mc.field_1724 != null) {
         if (this.packetSwapStage == 0) {
            int currentSlot = mc.field_1724.method_31548().field_7545;
            int nextSlot = (currentSlot + 1) % 9;
            mc.field_1724.field_3944.method_52787(new class_2868(nextSlot));
            this.packetSwapStage = 1;
         } else if (this.packetSwapStage == 1) {
            mc.field_1724.field_3944.method_52787(new class_2868(this.packetSwapSlot));
            this.packetSwapActive = false;
            this.packetSwapStage = 0;
         }
      }
   }

   private void executePacketFireworkSwap(int fireworkSlot) {
      int currentSlot = mc.field_1724.method_31548().field_7545;
      this.packetSwapSlot = currentSlot;
      if (fireworkSlot < 9) {
         mc.field_1724.field_3944.method_52787(new class_2868(fireworkSlot));
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         mc.field_1724.field_3944.method_52787(new class_2868(currentSlot));
      } else {
         if (fireworkSlot >= 36) {
            int var10000 = fireworkSlot - 36;
         }

         mc.field_1761.method_2906(0, fireworkSlot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, 36 + currentSlot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         mc.field_1761.method_2906(0, 36 + currentSlot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, fireworkSlot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1724.field_3944.method_52787(new class_2815(0));
      }

      this.packetSwapActive = true;
      this.packetSwapStage = 0;
   }

   private void performSwap() {
      int slotElytra = InventoryUtils.findBestElytraSlot();
      int chestSlot = InventoryUtils.findBestChestplateSlot();
      boolean needChestplate = mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)
         || mc.field_1724.method_6118(class_1304.field_6174).method_7960()
         || !Set.of(class_1802.field_22028, class_1802.field_8058, class_1802.field_8523, class_1802.field_8678, class_1802.field_8873, class_1802.field_8577)
            .contains(mc.field_1724.method_6118(class_1304.field_6174).method_7909());
      if (needChestplate) {
         if (chestSlot == -1) {
            ChatUtils.sendMessage("" + class_124.field_1061 + class_124.field_1067 + "Нет нагрудника!");
            this.bypassTicks = 0;
            this.restoreSprint();
            return;
         }

         class_1799 chestItem = mc.field_1724.field_7498.method_7611(chestSlot).method_7677();
         this.doSwap(chestSlot);
      } else {
         if (slotElytra == -1) {
            ChatUtils.sendMessage("" + class_124.field_1061 + class_124.field_1067 + "Нет элитры!");
            this.bypassTicks = 0;
            this.restoreSprint();
            return;
         }

         class_1799 elytraItem = mc.field_1724.field_7498.method_7611(slotElytra).method_7677();
         this.doSwap(slotElytra);
      }

      mc.field_1724.field_3944.method_52787(new class_2815(0));
   }

   private void doSwap(int slot) {
      if (slot >= 0 && slot < 9) {
         mc.field_1761.method_2906(0, 6, slot, class_1713.field_7791, mc.field_1724);
      } else {
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, 6, 0, class_1713.field_7791, mc.field_1724);
         mc.field_1761.method_2906(0, slot, 0, class_1713.field_7791, mc.field_1724);
      }
   }

   private void handleFireworkReturn() {
      if (this.fireworkReturnTicks >= 0) {
         if (this.fireworkReturnTicks > 0) {
            this.fireworkReturnTicks--;
         } else {
            if (this.fireworkReturnSlot != -1) {
               this.swapSlotToOffhand(this.fireworkReturnSlot);
               mc.field_1724.field_3944.method_52787(new class_2815(0));
            }

            this.fireworkReturnSlot = -1;
            this.fireworkReturnTicks = -1;
         }
      }
   }

   private int findScreenSlot(class_1792 item) {
      for (int slot = 9; slot < 45; slot++) {
         class_1799 stack = mc.field_1724.field_7498.method_7611(slot).method_7677();
         if (stack.method_31574(item)) {
            return slot;
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

   private void disableSprint() {
      if (!this.sprintPaused) {
         Sprint.pushPause(1000L);
         this.sprintPaused = true;
      }
   }

   private void restoreSprint() {
      if (this.sprintPaused) {
         this.sprintPaused = false;
         Sprint.popPause();
      }
   }

   private boolean isElytraUsable(class_1799 stack) {
      return stack.method_7919() < stack.method_7936() - 1;
   }

   @EventLink
   public void onEvent(EventBinding event) {
      if (event.getKey() == this.elytraBind.getKey()) {
         this.swapElytraQueued = true;
      }

      if (event.getKey() == this.fireworkBind.getKey()) {
         this.useFirework = true;
      }
   }

   @Override
   public void onDisable() {
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      this.fireworkReturnSlot = -1;
      this.fireworkReturnTicks = -1;
      this.packetSwapActive = false;
      this.packetSwapStage = 0;
      this.restoreSprint();
      super.onDisable();
   }
}
