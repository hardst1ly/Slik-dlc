package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;

public class TpBack extends Module {
   public static TpBack INSTANCE = new TpBack();
   private boolean isDead = false;
   private boolean waitingForRespawn = false;
   private int tickCounter = 0;
   public FloatSetting delay = new FloatSetting("Задержка", 5.0F, 1.0F, 20.0F, 1.0F);

   public TpBack() {
      super("TpBack", "Возвращает на точки смерти", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.delay});
   }

   @EventLink
   public void onEvent(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean playerDead = mc.field_1724.method_6032() <= 0.0F & mc.field_1724.field_6213 > 0;
         if (playerDead && !this.isDead) {
            this.isDead = true;
            mc.field_1724.field_3944.method_45729("/sethome slikdlc");
            mc.field_1724.method_7331();
            this.waitingForRespawn = true;
            this.tickCounter = 0;
         }

         if (this.waitingForRespawn && !playerDead) {
            this.tickCounter++;
            if (this.tickCounter >= this.delay.get()) {
               mc.field_1724.field_3944.method_45729("/home slikdlc");
               this.waitingForRespawn = false;
               this.tickCounter = 0;
            }
         }

         if (!playerDead && !this.waitingForRespawn) {
            this.isDead = false;
         }
      }
   }
}
