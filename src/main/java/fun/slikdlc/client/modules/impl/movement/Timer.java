package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;

public class Timer extends Module {
   public static Timer INSTANCE = new Timer();
   public FloatSetting speed = new FloatSetting("Скорость", 2.0F, 0.1F, 10.0F, 0.1F);

   public Timer() {
      super("Timer", "Ускоряет время в игре", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.speed});
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      mc.field_1724.field_28627 = this.speed.getValue().floatValue();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      mc.field_1724.field_28627 = 1.0F;
   }
}
