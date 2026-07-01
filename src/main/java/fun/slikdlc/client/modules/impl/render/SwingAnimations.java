package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;

public class SwingAnimations extends Module {
   public static SwingAnimations INSTANCE = new SwingAnimations();
   public boolean swimmingAnimation = true;
   public boolean climbAndCrawl = true;
   public boolean mb3DCompat = false;
   public final BooleanSetting hmiEnable = new BooleanSetting("Мод на красивые руки", false);
   public final ModeSetting hmiAnimationType = new ModeSetting("Вид анимации", "Классик", "Классик", "Шарп").visible(this.hmiEnable::isState);
   public final FloatSetting hmiSmoothness = new FloatSetting("Плавность анимации", 1.0F, 0.35F, 2.5F, 0.05F).visible(this.hmiEnable::isState);
   public final BooleanSetting swingEnabled = new BooleanSetting("Анимация свинга", true).visible(() -> !this.hmiEnable.isState());
   public final ModeSetting swingType = new ModeSetting(
         "Тип свинга", "Smooth", "Smooth", "Static", "Down", "DropDown", "Poke", "SelfBack", "Feast", "ToBack", "Block", "Akrien", "Break", "Pander", "Slant"
      )
      .visible(() -> !this.hmiEnable.isState() && this.swingEnabled.isState());
   public final FloatSetting swingStrength = new FloatSetting("Сила анимации", 1.0F, 0.1F, 3.0F, 0.01F)
      .visible(() -> !this.hmiEnable.isState() && this.swingEnabled.isState() && !this.swingType.is("Pander"));
   public final FloatSetting corner = new FloatSetting("Угол DropDown", 12.0F, 1.0F, 360.0F, 1.0F)
      .visible(() -> !this.hmiEnable.isState() && this.swingEnabled.isState() && this.swingType.is("DropDown"));
   public final FloatSetting slant = new FloatSetting("Наклон DropDown", 12.0F, 1.0F, 360.0F, 1.0F)
      .visible(() -> !this.hmiEnable.isState() && this.swingEnabled.isState() && this.swingType.is("DropDown"));
   public final BooleanSetting smoothEnabled = new BooleanSetting("Плавная анимация", false).visible(() -> !this.hmiEnable.isState());
   public final FloatSetting slowAnimationSpeed = new FloatSetting("Скорость анимации", 12.0F, 1.0F, 50.0F, 1.0F)
      .visible(() -> !this.hmiEnable.isState() && this.smoothEnabled.isState());
   public final BooleanSetting auraTargetOnly = new BooleanSetting("Только при Aura", false).visible(() -> !this.hmiEnable.isState());
   public final BooleanSetting swapHands = new BooleanSetting("Свап рук", false).visible(() -> !this.hmiEnable.isState());
   public final BooleanSetting eatAnim = new BooleanSetting("Анимация еды", false).visible(() -> !this.hmiEnable.isState());

   public SwingAnimations() {
      super("SwingAnimations", "Кастомная анимация аттаки", Module.ModuleCategory.RENDER);
      this.addSettings(
         new Setting[]{
            this.hmiEnable,
            this.hmiAnimationType,
            this.hmiSmoothness,
            this.swingEnabled,
            this.swingType,
            this.swingStrength,
            this.corner,
            this.slant,
            this.smoothEnabled,
            this.slowAnimationSpeed,
            this.auraTargetOnly,
            this.swapHands,
            this.eatAnim
         }
      );
   }
}
