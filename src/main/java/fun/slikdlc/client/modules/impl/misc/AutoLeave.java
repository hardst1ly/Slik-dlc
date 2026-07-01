package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_268;

public class AutoLeave extends Module {
   public static final AutoLeave INSTANCE = new AutoLeave();
   private static final Set<String> STAFF_PREFIXES = new HashSet<>(
      Arrays.asList("supp", "mod", "der", "adm", "wne", "curat", "dev", "yt", "мод", "помо", "адм", "владе", "курато", "сапп", "ютуб", "стажер", "сотрудник")
   );
   private final FloatSetting leaveDistance = new FloatSetting("Дистанция срабатывания", 5.0F, 3.0F, 50.0F, 1.0F);
   private final ListSetting leaveIfSeen = new ListSetting("Выходить если замечен", new BooleanSetting("Игрок", true), new BooleanSetting("Модератор", false));
   private final ModeSetting leaveType = new ModeSetting("Тип выхода", "В мейн меню", "В мейн меню", "/hub", "/home", "/spawn");
   private final BooleanSetting stopBaritone = new BooleanSetting("Выключать баритон", false);
   private final BooleanSetting leaveDisable = new BooleanSetting("Выключать после выхода", true);
   private int cooldownTicks;

   public AutoLeave() {
      super("AutoLeave", "Выходит с сервера, когда замечает поблизости игрока", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.leaveDistance, this.leaveIfSeen, this.leaveType, this.stopBaritone, this.leaveDisable});
   }

   @Override
   public void onEnable() {
      this.cooldownTicks = 0;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.cooldownTicks = 0;
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
         } else {
            float maxDistance = this.leaveDistance.get();

            for (class_1657 player : mc.field_1687.method_18456()) {
               if (player != null && player != mc.field_1724 && mc.field_1724.method_5739(player) <= maxDistance && this.shouldLeaveFor(player)) {
                  this.triggerLeave();
                  break;
               }
            }
         }
      }
   }

   private boolean shouldLeaveFor(class_1657 player) {
      return this.isModerator(player) ? this.leaveIfSeen.is("Модератор") : this.leaveIfSeen.is("Игрок");
   }

   private boolean isModerator(class_1657 player) {
      if (player == null) {
         return false;
      } else {
         String name = player.method_5477().getString();
         if (SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.staffStorage != null && SlikDlc.INSTANCE.staffStorage.isStaff(name)) {
            return true;
         } else {
            class_268 team = player.method_5781();
            if (team == null) {
               return false;
            } else {
               String prefix = team.method_1144().getString().toLowerCase(Locale.ROOT);

               for (String candidate : STAFF_PREFIXES) {
                  if (prefix.contains(candidate)) {
                     return true;
                  }
               }

               return false;
            }
         }
      }
   }

   private void triggerLeave() {
      this.tryStopBaritone();
      String var1 = this.leaveType.getCurrent();
      switch (var1) {
         case "В мейн меню":
            this.disconnectLeave();
            break;
         case "/hub":
            this.commandLeave("hub");
            break;
         case "/home":
            this.commandLeave("home home");
            break;
         case "/spawn":
            this.commandLeave("spawn");
      }
   }

   private void tryStopBaritone() {
      if (this.stopBaritone.isState() && mc.method_1562() != null) {
         mc.method_1562().method_45729("#stop");
      }
   }

   private void disconnectLeave() {
      if (mc.method_1562() == null) {
         ChatUtils.sendMessage("Модуль не работает в одиночном мире");
      } else {
         mc.method_1562().method_48296().method_10747(class_2561.method_43470("AutoLeave"));
         if (this.leaveDisable.isState()) {
            this.toggle();
         }
      }
   }

   private void commandLeave(String command) {
      if (mc.method_1562() == null) {
         ChatUtils.sendMessage("AutoLeave нельзя использовать в одиночной игре!");
      } else {
         mc.method_1562().method_45730(command);
         this.cooldownTicks = this.leaveDisable.isState() ? 10 : 30;
         if (this.leaveDisable.isState()) {
            this.toggle();
         }
      }
   }
}
