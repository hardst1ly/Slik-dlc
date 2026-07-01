package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;

public class AntiBot extends Module {
   public static AntiBot INSTANCE = new AntiBot();
   public static final List<class_1297> isBot = new ArrayList<>();

   public AntiBot() {
      super("AntiBot", "Определяет ботов на сервере", Module.ModuleCategory.COMBAT);
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      this.newMatrix();
   }

   public void newMatrix() {
      if (mc.field_1687 != null) {
         for (class_1657 player : mc.field_1687.method_18456()) {
            if (mc.field_1724 != player
               && ((class_1799)player.method_31548().field_7548.get(0)).method_7909() != class_1802.field_8162
               && ((class_1799)player.method_31548().field_7548.get(1)).method_7909() != class_1802.field_8162
               && ((class_1799)player.method_31548().field_7548.get(2)).method_7909() != class_1802.field_8162
               && ((class_1799)player.method_31548().field_7548.get(3)).method_7909() != class_1802.field_8162
               && ((class_1799)player.method_31548().field_7548.get(0)).method_7923()
               && ((class_1799)player.method_31548().field_7548.get(1)).method_7923()
               && ((class_1799)player.method_31548().field_7548.get(2)).method_7923()
               && ((class_1799)player.method_31548().field_7548.get(3)).method_7923()
               && player.method_6079().method_7909() == class_1802.field_8162
               && (
                  ((class_1799)player.method_31548().field_7548.get(0)).method_7909() == class_1802.field_8370
                     || ((class_1799)player.method_31548().field_7548.get(1)).method_7909() == class_1802.field_8570
                     || ((class_1799)player.method_31548().field_7548.get(2)).method_7909() == class_1802.field_8577
                     || ((class_1799)player.method_31548().field_7548.get(3)).method_7909() == class_1802.field_8267
                     || ((class_1799)player.method_31548().field_7548.get(0)).method_7909() == class_1802.field_8660
                     || ((class_1799)player.method_31548().field_7548.get(1)).method_7909() == class_1802.field_8396
                     || ((class_1799)player.method_31548().field_7548.get(2)).method_7909() == class_1802.field_8523
                     || ((class_1799)player.method_31548().field_7548.get(3)).method_7909() == class_1802.field_8743
               )
               && player.method_6047().method_7909() != class_1802.field_8162
               && !((class_1799)player.method_31548().field_7548.get(0)).method_7986()
               && !((class_1799)player.method_31548().field_7548.get(1)).method_7986()
               && !((class_1799)player.method_31548().field_7548.get(2)).method_7986()
               && !((class_1799)player.method_31548().field_7548.get(3)).method_7986()
               && player.method_7344().method_7586() == 20) {
               if (!isBot.contains(player)) {
                  isBot.add(player);
               }

               return;
            }

            isBot.remove(player);
         }
      }
   }

   public static boolean checkBot(class_1309 entity) {
      return entity instanceof class_1657 && isBot.contains(entity);
   }

   @Override
   public void onDisable() {
      super.onDisable();
      isBot.clear();
   }
}
