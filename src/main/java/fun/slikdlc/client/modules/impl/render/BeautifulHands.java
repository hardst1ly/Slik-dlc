package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;

public class BeautifulHands extends Module {
   public static final BeautifulHands INSTANCE = new BeautifulHands();
   public final ModeSetting attackMode = new ModeSetting("Режим атаки", "Swing", "Swing", "Forward", "Normal");
   public final FloatSetting rightX = new FloatSetting("Правая X", 0.0F, -2.0F, 2.0F, 0.1F);
   public final FloatSetting rightY = new FloatSetting("Правая Y", 0.0F, -2.0F, 2.0F, 0.1F);
   public final FloatSetting rightZ = new FloatSetting("Правая Z", 0.0F, -2.0F, 2.0F, 0.1F);
   public final FloatSetting leftX = new FloatSetting("Левая X", 0.0F, -2.0F, 2.0F, 0.1F);
   public final FloatSetting leftY = new FloatSetting("Левая Y", 0.0F, -2.0F, 2.0F, 0.1F);
   public final FloatSetting leftZ = new FloatSetting("Левая Z", 0.0F, -2.0F, 2.0F, 0.1F);

   public BeautifulHands() {
      super("BeautifulHands", "Красивая анимация рук", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.attackMode, this.rightX, this.rightY, this.rightZ, this.leftX, this.leftY, this.leftZ});
   }

   public boolean useSwingAttack() {
      return this.isEnable() && this.attackMode.is("Swing");
   }

   public boolean useForwardAttack() {
      return this.isEnable() && this.attackMode.is("Forward");
   }

   public boolean useNormalAttack() {
      return this.isEnable() && this.attackMode.is("Normal");
   }

   public boolean hasAuraTarget() {
      return Aura.INSTANCE.getTarget() != null && Aura.INSTANCE.getTarget().method_5805();
   }
}
