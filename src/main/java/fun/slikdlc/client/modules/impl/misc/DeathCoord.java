package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import net.minecraft.class_2338;
import net.minecraft.class_2561;

public class DeathCoord extends Module {
   public static DeathCoord INSTANCE = new DeathCoord();
   private final BooleanSetting copyToClipboard = new BooleanSetting("Копировать в буфер", true);
   private class_2338 deathPos = null;
   private boolean isDead = false;

   public DeathCoord() {
      super("DeathCoord", "Показывает координаты смерти", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.copyToClipboard});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.isDead = false;
      this.deathPos = null;
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1724.method_6032() <= 0.0F && !this.isDead) {
            this.isDead = true;
            this.deathPos = mc.field_1724.method_24515();
            String coords = "X: " + this.deathPos.method_10263() + " Y: " + this.deathPos.method_10264() + " Z: " + this.deathPos.method_10260();
            String dimension = this.getDimension();
            String message = "§cВы умерли! §f" + coords + " §7(" + dimension + ")";
            mc.field_1724.method_7353(class_2561.method_43470(message), false);
            if (this.copyToClipboard.isState()) {
               mc.field_1774.method_1455(this.deathPos.method_10263() + " " + this.deathPos.method_10264() + " " + this.deathPos.method_10260());
            }
         }

         if (mc.field_1724.method_6032() > 0.0F && this.isDead) {
            this.isDead = false;
         }
      }
   }

   private String getDimension() {
      if (mc.field_1687 == null) {
         return "Unknown";
      } else {
         String dimension = mc.field_1687.method_27983().method_29177().toString();
         if (dimension.contains("overworld")) {
            return "Overworld";
         } else if (dimension.contains("nether")) {
            return "Nether";
         } else {
            return dimension.contains("end") ? "End" : dimension;
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.isDead = false;
      this.deathPos = null;
   }
}
