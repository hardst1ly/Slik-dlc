package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;

public class ItemScroller extends Module {
   public static ItemScroller INSTANCE = new ItemScroller();
   public final FloatSetting delay = new FloatSetting("Задержка", 50.0F, 0.0F, 200.0F, 1.0F);
   private long lastQuickMoveAt;

   public ItemScroller() {
      super("ItemScroller", "Убирает задержку перемещения предметов", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.delay});
   }

   public boolean canQuickMove() {
      long now = System.currentTimeMillis();
      if (now - this.lastQuickMoveAt < (long)this.delay.get()) {
         return false;
      } else {
         this.lastQuickMoveAt = now;
         return true;
      }
   }

   public void resetTimer() {
      this.lastQuickMoveAt = 0L;
   }

   @Override
   public void onDisable() {
      this.resetTimer();
      super.onDisable();
   }
}
