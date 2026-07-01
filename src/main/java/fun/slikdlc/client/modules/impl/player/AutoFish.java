package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import net.minecraft.class_1268;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_2767;
import net.minecraft.class_2868;
import net.minecraft.class_3417;

public class AutoFish extends Module {
   public static AutoFish INSTANCE = new AutoFish();
   private final BooleanSetting takeRod = new BooleanSetting("Автоматически брать удочку", true);
   private boolean isCached = false;
   private boolean needCached = false;
   private int rodHotbarSlot = -1;
   private long lastActionTime = 0L;
   private long catchTime = 0L;

   public AutoFish() {
      super("AutoFish", "Автоматизирует процесс рыбалки", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.takeRod});
   }

   @Override
   public void onDisable() {
      this.isCached = false;
      this.needCached = false;
      this.rodHotbarSlot = -1;
      this.lastActionTime = 0L;
      this.catchTime = 0L;
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.takeRod.isState() && this.rodHotbarSlot == -1) {
            this.findBestFishingRodInHotbar();
         }

         if (this.rodHotbarSlot != -1 && mc.field_1724.method_31548().field_7545 != this.rodHotbarSlot) {
            mc.field_1724.method_31548().field_7545 = this.rodHotbarSlot;
            mc.field_1724.field_3944.method_52787(new class_2868(this.rodHotbarSlot));
         }

         long currentTime = System.currentTimeMillis();
         if (this.isCached && currentTime - this.catchTime >= 600L) {
            this.useFishingRod();
            this.isCached = false;
            this.needCached = true;
            this.lastActionTime = currentTime;
         }

         if (this.needCached && currentTime - this.lastActionTime >= 300L) {
            this.useFishingRod();
            this.needCached = false;
            this.lastActionTime = currentTime;
         }
      }
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getPacket() instanceof class_2767 packet && packet.method_11894().comp_349() == class_3417.field_14660) {
            this.isCached = true;
            this.catchTime = System.currentTimeMillis();
         }
      }
   }

   private void useFishingRod() {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.rodHotbarSlot != -1 && this.rodHotbarSlot < 9) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(this.rodHotbarSlot);
            if (stack.method_7909() instanceof class_1787) {
               if (mc.field_1724.method_31548().field_7545 != this.rodHotbarSlot) {
                  mc.field_1724.method_31548().field_7545 = this.rodHotbarSlot;
                  mc.field_1724.field_3944.method_52787(new class_2868(this.rodHotbarSlot));
               }

               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            }
         }
      }
   }

   private void findBestFishingRodInHotbar() {
      if (mc.field_1724 != null) {
         int bestRodSlot = -1;
         int maxEnchantments = -1;

         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() instanceof class_1787) {
               int enchantmentCount = class_1890.method_57532(stack).method_57541();
               if (enchantmentCount > maxEnchantments) {
                  maxEnchantments = enchantmentCount;
                  bestRodSlot = i;
               }
            }
         }

         if (bestRodSlot != -1) {
            this.rodHotbarSlot = bestRodSlot;
         }
      }
   }
}
