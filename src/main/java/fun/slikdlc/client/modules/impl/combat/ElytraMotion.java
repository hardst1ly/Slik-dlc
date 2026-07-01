package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventMove;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import net.minecraft.class_243;

public class ElytraMotion extends Module {
   public static ElytraMotion INSTANCE = new ElytraMotion();
   public FloatSetting distance = new FloatSetting("Дистанция до игрока", 3.0F, 0.0F, 6.0F, 0.1F);
   public BooleanSetting bypass = new BooleanSetting("Обход", false);

   public ElytraMotion() {
      super("ElytraMotion", "Зависает рядом с игроком на эликах", Module.ModuleCategory.COMBAT);
      this.addSettings(new Setting[]{this.distance, this.bypass});
   }

   @EventLink
   public void onMove(EventMove e) {
      if (this.isEnable()) {
         Aura aura = ModuleClass.aura;
         if (mc.field_1724 != null && mc.field_1687 != null && aura.getTarget() != null) {
            if (mc.field_1724.method_6128() && mc.field_1724.method_5739(aura.getTarget()) < this.distance.getValue().floatValue()) {
               if (this.bypass.isState()) {
                  float yaw = mc.field_1724.method_36454();
                  double rad = Math.toRadians(yaw);
                  double forward = 0.01;
                  double down = -1.0E-4;
                  double moveX = -Math.sin(rad) * forward;
                  double moveZ = Math.cos(rad) * forward;
                  e.setMovePos(new class_243(moveX, down, moveZ));
               } else {
                  e.setMovePos(class_243.field_1353);
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
   }
}
