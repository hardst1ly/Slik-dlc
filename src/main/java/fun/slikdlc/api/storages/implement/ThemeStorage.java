package fun.slikdlc.api.storages.implement;

import fun.slikdlc.api.storages.implement.helpertstorages.Theme;
import fun.slikdlc.api.utils.color.ColorUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import lombok.Generated;

public class ThemeStorage {
   private ObjectArrayList<ThemeStorage.Themes> themeList = new ObjectArrayList();
   private ThemeStorage.Themes themes;

   public ThemeStorage() {
      this.onInitialize();
   }

   private void onInitialize() {
      this.themeList
         .addAll(
            Arrays.asList(
               ThemeStorage.Themes.Custom,
               ThemeStorage.Themes.Purple,
               ThemeStorage.Themes.Red,
               ThemeStorage.Themes.Blue,
               ThemeStorage.Themes.Green,
               ThemeStorage.Themes.Pink,
               ThemeStorage.Themes.Orange,
               ThemeStorage.Themes.Blues,
               ThemeStorage.Themes.Yellows
            )
         );
      this.themes = (ThemeStorage.Themes)this.themeList.get(1);
   }

   @Generated
   public ObjectArrayList<ThemeStorage.Themes> getThemeList() {
      return this.themeList;
   }

   @Generated
   public ThemeStorage.Themes getThemes() {
      return this.themes;
   }

   @Generated
   public void setThemeList(ObjectArrayList<ThemeStorage.Themes> themeList) {
      this.themeList = themeList;
   }

   @Generated
   public void setThemes(ThemeStorage.Themes themes) {
      this.themes = themes;
   }

   public static enum Themes {
      Custom(new Theme("Rainbow", ColorUtils.rgba(255, 255, 255, 0))),
      Purple(new Theme("Lavender", ColorUtils.rgba(190, 143, 255, 255), ColorUtils.darken(ColorUtils.rgba(190, 143, 255, 255), 0.35F))),
      Red(new Theme("Blood", ColorUtils.rgba(230, 50, 57, 255), ColorUtils.darken(ColorUtils.rgba(230, 50, 57, 255), 0.35F))),
      Blue(new Theme("Ocean", ColorUtils.rgba(95, 113, 191, 255), ColorUtils.darken(ColorUtils.rgba(95, 113, 191, 255), 0.35F))),
      Green(new Theme("Emerald", ColorUtils.rgba(60, 220, 140, 255), ColorUtils.darken(ColorUtils.rgba(60, 220, 140, 255), 0.35F))),
      Pink(new Theme("Rose", ColorUtils.rgba(255, 120, 190, 255), ColorUtils.darken(ColorUtils.rgba(255, 120, 190, 255), 0.35F))),
      Orange(new Theme("Gold", ColorUtils.rgba(252, 192, 88, 255), ColorUtils.darken(ColorUtils.rgba(252, 192, 88, 255), 0.35F))),
      Blues(new Theme("Diamond", ColorUtils.rgba(125, 217, 250, 255), ColorUtils.darken(ColorUtils.rgba(125, 217, 250, 255), 0.35F))),
      Yellows(new Theme("Sun", ColorUtils.rgba(252, 231, 88, 255), ColorUtils.darken(ColorUtils.rgba(252, 231, 88, 255), 0.35F)));

      final Theme theme;

      @Generated
      private Themes(final Theme theme) {
         this.theme = theme;
      }

      @Generated
      public Theme getTheme() {
         return this.theme;
      }
   }
}
