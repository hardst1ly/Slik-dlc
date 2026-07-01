package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;

public class Removals extends Module {
   public static Removals INSTANCE = new Removals();
   private final ListSetting elements = new ListSetting(
      "Элементы",
      new BooleanSetting("Огонь", false),
      new BooleanSetting("Плохие эффекты", false),
      new BooleanSetting("Оверлей в блоке", false),
      new BooleanSetting("Частицы", false),
      new BooleanSetting("Погода", false),
      new BooleanSetting("Облака", false),
      new BooleanSetting("Блок-сущности", false),
      new BooleanSetting("Тени", false),
      new BooleanSetting("Анимацию тотема", false)
   );

   public Removals() {
      super("Removals", "Убирает выбранные элементы рендера", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.elements});
   }

   public boolean isEnabled(String element) {
      return this.isEnable() && this.elements.is(element);
   }

   public boolean isTotemAnimationDisabled() {
      return this.isEnabled("Анимацию тотема");
   }
}
