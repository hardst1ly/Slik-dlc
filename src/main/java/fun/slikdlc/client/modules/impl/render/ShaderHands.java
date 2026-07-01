package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.utils.render.hands.ShaderHandsRenderer;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;

public class ShaderHands extends Module {
   public static ShaderHands INSTANCE = new ShaderHands();
   private static final ShaderHandsRenderer RENDERER = ShaderHandsRenderer.getInstance();
   public final ModeSetting mode = new ModeSetting("Режим", "Свечение", "Свечение", "Красивый");
   public final FloatSetting waveSpeed = new FloatSetting("Скорость волн", 1.2F, 0.1F, 5.0F, 0.1F).visible(() -> this.mode.is("Красивый"));
   public final FloatSetting waveScale = new FloatSetting("Частота волн", 1.0F, 1.0F, 3.0F, 0.1F).visible(() -> this.mode.is("Красивый"));
   public final FloatSetting outline = new FloatSetting("Ширина обводки", 1.2F, 0.1F, 5.0F, 0.1F);
   public final FloatSetting glow = new FloatSetting("Сила свечения", 1.0F, 0.0F, 5.0F, 0.1F);
   public final FloatSetting fill = new FloatSetting("Заливка", 0.6F, 0.0F, 1.0F, 0.01F);
   public final FloatSetting alpha = new FloatSetting("Прозрачность", 1.0F, 0.0F, 1.0F, 0.05F);

   public ShaderHands() {
      super("ShaderHands", "Красивый Шейдер на руки и предметы", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.mode, this.waveSpeed, this.waveScale, this.outline, this.glow, this.fill, this.alpha});
   }

   @Override
   public void onDisable() {
      RENDERER.invalidateState();
      super.onDisable();
   }

   @EventLink(
      priority = 0
   )
   public void onRender2D(EventRender.Default event) {
      if (this.isEnable()) {
         RENDERER.renderOverlayIfPending();
      }
   }
}
