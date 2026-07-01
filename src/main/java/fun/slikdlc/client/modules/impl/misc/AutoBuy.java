package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;

public class AutoBuy extends Module {
   public static AutoBuy INSTANCE = new AutoBuy();
   public BindSetting openKey = new BindSetting("Бинд гуи", -1);

   public AutoBuy() {
      super("AutoBuy", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.openKey});
   }
}
