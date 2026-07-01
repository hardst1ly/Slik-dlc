package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;

public class ClientSounds extends Module {
   public static ClientSounds INSTANCE = new ClientSounds();
   public final ModeSetting stateSounds = new ModeSetting("Режим", "Нет", "Первый", "Второй", "Третий", "Четвертый", "Пятый", "Шестой");
   public final FloatSetting volume = new FloatSetting("Громкость", 50.0F, 1.0F, 100.0F, 0.5F);

   public ClientSounds() {
      super("ClientSounds", "Добавляет звуки клиента", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.stateSounds, this.volume});
   }
}
