package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.client.modules.Module;
import net.minecraft.class_3532;

public class TpsSync extends Module {
   public static TpsSync INSTANCE = new TpsSync();

   public TpsSync() {
      super("TpsSync", "Синхронизация с TPS сервера", Module.ModuleCategory.COMBAT);
   }

   public float getCurrentTPS() {
      if (SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.tpsCalc != null) {
         float tps = SlikDlc.INSTANCE.tpsCalc.getTPS();
         return class_3532.method_15363(tps, 0.1F, 20.0F);
      } else {
         return 20.0F;
      }
   }

   public long getAdjustedCooldown(long baseCooldown) {
      if (!this.isEnable()) {
         return baseCooldown;
      } else {
         float tps = this.getCurrentTPS();
         if (tps >= 20.0F) {
            return baseCooldown;
         } else {
            float multiplier = 20.0F / tps;
            float additionalFactor = 1.0F + (20.0F - tps) * 0.05F;
            long adjusted = (long)((float)baseCooldown * multiplier * additionalFactor);
            return Math.min(adjusted, 3000L);
         }
      }
   }

   public boolean canAttack(long lastAttackTime, long baseCooldown, long currentTime) {
      if (!this.isEnable()) {
         return currentTime >= lastAttackTime + baseCooldown;
      } else {
         long adjustedCooldown = this.getAdjustedCooldown(baseCooldown);
         return currentTime >= lastAttackTime + adjustedCooldown;
      }
   }
}
