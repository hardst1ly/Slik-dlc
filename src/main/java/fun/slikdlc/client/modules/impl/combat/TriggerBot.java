package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventMoveInput;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.combat.IdealHitUtils;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.movement.Sprint;
import fun.slikdlc.client.modules.impl.player.AutoEat;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import lombok.Generated;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1296;
import net.minecraft.class_1309;
import net.minecraft.class_1431;
import net.minecraft.class_1531;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1675;
import net.minecraft.class_1743;
import net.minecraft.class_243;
import net.minecraft.class_3966;

public class TriggerBot extends Module {
   public static TriggerBot INSTANCE = new TriggerBot();
   private final FloatSetting range = new FloatSetting("Дистанция атаки", 3.0F, 0.0F, 6.0F, 0.05F);
   private final ListSetting options = new ListSetting(
      "Опции",
      new BooleanSetting("Умные криты", true),
      new BooleanSetting("Сброс спринта", true),
      new BooleanSetting("Бить через стены", false),
      new BooleanSetting("Проверка на наведение", true),
      new BooleanSetting("Отжимать щит", false),
      new BooleanSetting("Ломать щит", true)
   );
   private final ListSetting targets = new ListSetting(
      "Таргеты",
      new BooleanSetting("Игроки", true),
      new BooleanSetting("Невидимки", true),
      new BooleanSetting("Мирные", false),
      new BooleanSetting("Мобы", true)
   );
   private class_1309 target;
   private final TimerUtils attackTimer = new TimerUtils();
   private boolean needSprintReset = false;
   private boolean sprintResetDone = false;
   private int sprintResetTicks = 0;

   public TriggerBot() {
      super("TriggerBot", "Автоматически атакует при наведении на цель", Module.ModuleCategory.COMBAT);
      this.addSettings(new Setting[]{this.range, this.options, this.targets});
   }

   @EventLink
   public void onMoveInput(EventMoveInput event) {
      if (this.needSprintReset) {
         event.setForward(0.0F);
         event.setStrafe(0.0F);
         this.needSprintReset = false;
         this.sprintResetDone = true;
         this.sprintResetTicks = 0;
      }
   }

   @EventLink
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (AutoEat.shouldSuppressCombat()) {
            this.target = null;
            this.resetSprintState();
         } else {
            if (this.sprintResetDone) {
               this.sprintResetTicks++;
            }

            this.target = this.getTargetUnderCrosshair();
            if (this.target != null) {
               this.processAttack();
            } else {
               this.resetSprintState();
            }
         }
      }
   }

   private void processAttack() {
      if (this.shouldAttack()) {
         if (this.options.is("Сброс спринта") && mc.field_1724.method_5624() && !this.sprintResetDone && !this.shouldSkipSprintResetInWater()) {
            this.needSprintReset = true;
         } else if (!this.options.is("Сброс спринта") || !this.sprintResetDone || this.sprintResetTicks >= 1) {
            this.attack();
            this.sprintResetDone = false;
            this.sprintResetTicks = 0;
         }
      }
   }

   private class_1309 getTargetUnderCrosshair() {
      class_243 eyePos = mc.field_1724.method_5836(1.0F);
      class_243 lookVec = mc.field_1724.method_5828(1.0F);
      float rangeValue = this.range.getValue().floatValue();
      class_243 reachVec = eyePos.method_1019(lookVec.method_1021(rangeValue));
      class_3966 result = class_1675.method_18075(
         mc.field_1724,
         eyePos,
         reachVec,
         mc.field_1724.method_5829().method_1014(rangeValue),
         entity -> entity != mc.field_1724 && entity.method_5805() && entity instanceof class_1309,
         rangeValue * rangeValue
      );
      return result != null && result.method_17782() instanceof class_1309 living && this.isValidTarget(living) ? living : null;
   }

   private void attack() {
      if (this.options.is("Отжимать щит") && mc.field_1724.method_6039()) {
         mc.field_1761.method_2897(mc.field_1724);
      }

      if (this.target instanceof class_1657 player && player.method_6039() && this.options.is("Ломать щит")) {
         this.shieldBreak(player);
      } else {
         mc.field_1761.method_2918(mc.field_1724, this.target);
      }

      mc.field_1724.method_6104(class_1268.field_5808);
      this.attackTimer.reset();
   }

   private void shieldBreak(class_1657 entity) {
      int axeSlot = this.findAxeSlot();
      if (axeSlot != -1) {
         int prevSlot = mc.field_1724.method_31548().field_7545;
         mc.field_1724.method_31548().field_7545 = axeSlot;
         mc.field_1761.method_2918(mc.field_1724, entity);
         mc.field_1724.method_6104(class_1268.field_5808);
         mc.field_1724.method_31548().field_7545 = prevSlot;
      } else {
         mc.field_1761.method_2918(mc.field_1724, entity);
      }
   }

   private int findAxeSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909() instanceof class_1743) {
            return i;
         }
      }

      return -1;
   }

   private boolean isValidTarget(class_1309 entity) {
      if (entity != null && entity != mc.field_1724) {
         if (!entity.method_5805() || entity.method_6032() <= 0.0F) {
            return false;
         } else if (entity instanceof class_1531) {
            return false;
         } else {
            if (entity instanceof class_1657 player) {
               if (!this.targets.is("Игроки")) {
                  return false;
               }

               if (player.method_6059(class_1294.field_5905) && !this.targets.is("Невидимки")) {
                  return false;
               }

               if (SlikDlc.INSTANCE.friendStorage.isFriend(entity.method_5477().getString())) {
                  return false;
               }
            } else if (!(entity instanceof class_1296) && !(entity instanceof class_1431)) {
               if (entity instanceof class_1588 && !this.targets.is("Мобы")) {
                  return false;
               }
            } else if (!this.targets.is("Мирные")) {
               return false;
            }

            return mc.field_1724.method_33571().method_1022(entity.method_5829().method_1005()) > this.range.getValue().floatValue()
               ? false
               : this.options.is("Бить через стены") || mc.field_1724.method_6057(entity);
         }
      } else {
         return false;
      }
   }

   private boolean shouldAttack() {
      if (mc.field_1724.method_7261(1.5F) < IdealHitUtils.getAICooldown()) {
         return false;
      } else {
         if (this.options.is("Проверка на наведение")) {
            class_243 eyePos = mc.field_1724.method_5836(1.0F);
            class_243 lookVec = mc.field_1724.method_5828(1.0F);
            float rangeValue = this.range.getValue().floatValue();
            class_243 reachVec = eyePos.method_1019(lookVec.method_1021(rangeValue));
            class_3966 result = class_1675.method_18075(
               mc.field_1724,
               eyePos,
               reachVec,
               mc.field_1724.method_5829().method_1014(rangeValue),
               ex -> ex != mc.field_1724 && ex.method_5805(),
               rangeValue * rangeValue
            );
            if (result == null || result.method_17782() != this.target) {
               return false;
            }
         }

         return !this.options.is("Умные криты") || IdealHitUtils.canCritical(this.target);
      }
   }

   private void resetSprintState() {
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
   }

   private boolean shouldSkipSprintResetInWater() {
      return mc.field_1724 != null
         && (mc.field_1724.method_5799() || mc.field_1724.method_5869())
         && Sprint.INSTANCE != null
         && Sprint.INSTANCE.shouldKeepSprintInWater();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.target = null;
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.needSprintReset = false;
      this.sprintResetDone = false;
      this.sprintResetTicks = 0;
   }

   @Generated
   public class_1309 getTarget() {
      return this.target;
   }
}
