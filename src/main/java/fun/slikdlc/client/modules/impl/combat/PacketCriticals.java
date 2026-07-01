package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventAttackEntity;
import fun.slikdlc.api.utils.combat.IdealHitUtils;
import fun.slikdlc.client.modules.Module;
import net.minecraft.class_2828.class_2829;

public class PacketCriticals extends Module {
   public static PacketCriticals INSTANCE = new PacketCriticals();

   public PacketCriticals() {
      super("PacketCriticals", "Бьет критами под эффект плавного падения / в паутине", Module.ModuleCategory.COMBAT);
   }

   @EventLink
   public void onAttack(EventAttackEntity event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean inWeb = IdealHitUtils.isInCobweb();
         double x = mc.field_1724.method_23317();
         double y = mc.field_1724.method_23318();
         double z = mc.field_1724.method_23321();
         if (inWeb) {
            mc.field_1724.field_3944.method_52787(new class_2829(x, y + 0.003, z, false, false));
            mc.field_1724.field_3944.method_52787(new class_2829(x, y, z, false, false));
         }
      }
   }
}
