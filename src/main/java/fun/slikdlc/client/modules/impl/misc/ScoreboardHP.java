package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.client.modules.Module;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_266;
import net.minecraft.class_269;
import net.minecraft.class_746;
import net.minecraft.class_8646;
import net.minecraft.class_9013;

public class ScoreboardHP extends Module {
   public static final ScoreboardHP INSTANCE = new ScoreboardHP();

   public ScoreboardHP() {
      super("ScoreboardHP", "Обход показа HP для серверов", Module.ModuleCategory.MISC);
   }

   public static float getHealth(class_1309 entity) {
      if (entity == null) {
         return 0.0F;
      } else if (!INSTANCE.isEnable()) {
         return entity.method_6032();
      } else if (entity instanceof class_746) {
         return entity.method_6032();
      } else if (entity instanceof class_1657 player && mc.method_1558() != null) {
         try {
            class_269 scoreboard = player.method_7327();
            class_266 objective = scoreboard.method_1189(class_8646.field_45158);
            if (objective == null) {
               objective = scoreboard.method_1189(class_8646.field_45156);
            }

            if (objective == null) {
               return entity.method_6032();
            } else {
               class_9013 score = scoreboard.method_55430(player, objective);
               return score == null ? entity.method_6032() : score.method_55397();
            }
         } catch (Exception var5) {
            return entity.method_6032();
         }
      } else {
         return entity.method_6032();
      }
   }

   public static float getHealthWithAbsorption(class_1309 entity) {
      return Math.max(0.0F, getHealth(entity) + entity.method_6067());
   }
}
