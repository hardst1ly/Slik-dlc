package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventSlowWalking;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.player.ViaProtocolUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_1268;
import net.minecraft.class_2886;

public class NoSlow extends Module {
   public static NoSlow INSTANCE = new NoSlow();
   private final ModeSetting mode = new ModeSetting("Мод", "Grim Old", "Grim Old", "Grim Last");
   private final BooleanSetting sprint = new BooleanSetting("Спринт", true);

   public NoSlow() {
      super("NoSlow", "Убирает замедление во время еды", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.sprint});
   }

   @EventLink
   public void onSlowDown(EventSlowWalking event) {
      if (mc.field_1724 != null && mc.field_1724.method_6115()) {
         if (this.mode.is("Grim Last") && mc.field_1724.method_6048() % 2 == 0) {
            event.setCancelled(true);
         }

         if (this.mode.is("Grim Old")) {
            class_1268 activeHand = mc.field_1724.method_6058();
            boolean legacyProtocol = ViaProtocolUtils.isTargetProtocolBelowOneNineteen();
            if (this.sprint.isState()) {
               mc.field_1724
                  .method_5728(
                     (ModuleClass.sprint.isEnable() && Sprint.isSprinting() || mc.field_1690.field_1867.method_1434())
                        && mc.field_1724.field_3913.field_3905 > 0.0F
                        && (!legacyProtocol || !mc.field_1724.field_5976 && !mc.field_1724.field_34927)
                        && !mc.field_1724.method_6128()
                  );
            }

            class_1268 otherHand = activeHand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
            mc.method_1562().method_52787(new class_2886(otherHand, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
            event.setCancelled(true);
         }
      }
   }
}
