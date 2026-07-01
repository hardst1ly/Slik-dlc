package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;

public class WorldTweaks extends Module {
   public static WorldTweaks INSTANCE = new WorldTweaks();
   private final ListSetting worldSettings = new ListSetting("Настройки мира", new BooleanSetting("Время", true), new BooleanSetting("Фог", true));
   private final FloatSetting timeSetting = new FloatSetting("Время", 12.0F, 0.0F, 24.0F, 1.0F).visible(() -> this.worldSettings.is("Время"));
   private final FloatSetting fogDistanceSetting = new FloatSetting("Дистанция фога", 100.0F, 20.0F, 200.0F, 1.0F).visible(() -> this.worldSettings.is("Фог"));

   public WorldTweaks() {
      super("CustomWorld", "Настройки мира", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.worldSettings, this.timeSetting, this.fogDistanceSetting});
   }

   public boolean isTimeEnabled() {
      return this.isEnable() && this.worldSettings.is("Время");
   }

   public boolean isFogEnabled() {
      return this.isEnable() && this.worldSettings.is("Фог");
   }

   public long getForcedTime() {
      return (long)this.timeSetting.get() * 1000L;
   }

   public float getFogDistance() {
      return this.fogDistanceSetting.get();
   }

   public int getFogColor() {
      return !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ColorUtils.getThemeColor();
   }
}
