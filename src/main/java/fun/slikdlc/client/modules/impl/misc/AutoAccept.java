package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import java.util.Locale;
import net.minecraft.class_7439;

public class AutoAccept extends Module {
   public static AutoAccept INSTANCE = new AutoAccept();
   private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", false);

   public AutoAccept() {
      super("AutoAccept", "Автоматически принимает телепорт", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.onlyFriend});
   }

   @EventLink
   public void onEvent(EventPacket event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getType() == EventPacket.Type.RECEIVE) {
            if (event.getPacket() instanceof class_7439 messagePacket) {
               String raw = messagePacket.comp_763().getString().toLowerCase(Locale.ROOT);
               if (raw.contains("телепортироваться") || raw.contains("has requested teleport") || raw.contains("просит к вам телепортироваться")) {
                  if (this.onlyFriend.isState()) {
                     boolean isFriend = false;
                     if (SlikDlc.INSTANCE.friendStorage != null) {
                        for (String friend : SlikDlc.INSTANCE.friendStorage.getFriends()) {
                           if (raw.contains(friend.toLowerCase(Locale.ROOT))) {
                              isFriend = true;
                              break;
                           }
                        }
                     }

                     if (!isFriend) {
                        return;
                     }
                  }

                  mc.field_1724.field_3944.method_45730("tpaccept");
               }
            }
         }
      }
   }
}
