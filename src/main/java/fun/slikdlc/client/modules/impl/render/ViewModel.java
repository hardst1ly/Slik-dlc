package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_1306;
import net.minecraft.class_4587;

public class ViewModel extends Module {
   public static ViewModel INSTANCE = new ViewModel();
   public final FloatSetting mainHandX = new FloatSetting("Правая рука X", 0.0F, -2.0F, 2.0F, 0.01F);
   public final FloatSetting mainHandY = new FloatSetting("Правая рука Y", 0.0F, -2.0F, 2.0F, 0.01F);
   public final FloatSetting mainHandZ = new FloatSetting("Правая рука Z", 0.0F, -2.0F, 2.0F, 0.01F);
   public final FloatSetting offHandX = new FloatSetting("Левая рука X", 0.0F, -2.0F, 2.0F, 0.01F);
   public final FloatSetting offHandY = new FloatSetting("Левая рука Y", 0.0F, -2.0F, 2.0F, 0.01F);
   public final FloatSetting offHandZ = new FloatSetting("Левая рука Z", 0.0F, -2.0F, 2.0F, 0.01F);
   public final BooleanSetting onlyAura = new BooleanSetting("Только с аурой", false);

   public ViewModel() {
      super("ViewModel", "Оффсеты рук от первого лица", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.mainHandX, this.mainHandY, this.mainHandZ, this.offHandX, this.offHandY, this.offHandZ, this.onlyAura});
   }

   public void applyHandPosition(class_4587 matrices, class_1306 arm) {
      if (arm == class_1306.field_6183) {
         matrices.method_46416(this.mainHandX.get(), this.mainHandY.get(), this.mainHandZ.get());
      } else {
         matrices.method_46416(this.offHandX.get(), this.offHandY.get(), this.offHandZ.get());
      }
   }
}
