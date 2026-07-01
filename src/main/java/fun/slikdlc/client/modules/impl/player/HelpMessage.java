package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;

public class HelpMessage extends Module {
   public static HelpMessage INSTANCE = new HelpMessage();
   private final BindSetting bind = new BindSetting("Бинд", -1);

   public HelpMessage() {
      super("HelpMessage", "Отправляет координаты в глобальный чат", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.bind});
   }

   @EventLink
   public void onBinding(EventBinding event) {
      if (mc.field_1724 != null && mc.method_1562() != null && mc.field_1755 == null) {
         if (event.getKey() == this.bind.getKey()) {
            int x = mc.field_1724.method_31477();
            int y = mc.field_1724.method_31478();
            int z = mc.field_1724.method_31479();
            mc.method_1562().method_45729("! " + x + " " + y + " " + z);
         }
      }
   }
}
