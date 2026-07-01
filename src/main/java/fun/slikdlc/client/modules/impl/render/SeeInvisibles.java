package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import net.minecraft.class_1294;
import net.minecraft.class_1657;

public class SeeInvisibles extends Module {
   public static final float INVISIBLE_ALPHA = 0.7F;
   public static final int INVISIBLE_COLOR = Math.round(178.5F) << 24 | 16777215;
   public static SeeInvisibles INSTANCE = new SeeInvisibles();

   public SeeInvisibles() {
      super("SeeInvisibles", "Показывает невидимых игроков", Module.ModuleCategory.RENDER);
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (this.shouldRenderInvisible(player)) {
               player.method_5648(false);
            }
         }
      }
   }

   public boolean shouldRenderInvisible(class_1657 player) {
      return this.isEnable()
         && mc.field_1724 != null
         && player != null
         && player != mc.field_1724
         && (player.method_5767() || player.method_6059(class_1294.field_5905));
   }
}
