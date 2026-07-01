package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.utils.bot.BotSessionManager;
import fun.slikdlc.client.modules.Module;
import java.util.UUID;
import net.minecraft.class_2720;
import net.minecraft.class_2856;
import net.minecraft.class_2856.class_2857;

public class RPSpoofer extends Module {
   public static RPSpoofer INSTANCE = new RPSpoofer();

   public RPSpoofer() {
      super("RPSpoofer", "Убирает ресурс-пак сервера", Module.ModuleCategory.PLAYER);
   }

   @EventLink
   public void onReceivePacket(EventPacket e) {
      if (e.getPacket() instanceof class_2720 packet && (this.isEnable() || BotSessionManager.shouldBypassResourcePacks())) {
         UUID packId = packet.comp_2158();
         mc.method_1562().method_52787(new class_2856(packId, class_2857.field_13016));
         mc.method_1562().method_52787(new class_2856(packId, class_2857.field_13017));
         e.setCancelled(true);
      }
   }
}
