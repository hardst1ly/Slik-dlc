package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.class_1309;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class ElytraResolver extends Module {
   public static ElytraResolver INSTANCE = new ElytraResolver();

   private final FloatSetting distance = new FloatSetting("Дистанция отлета", 6.0F, 4.0F, 8.0F, 0.1F);
   private final BooleanSetting autoFirework = new BooleanSetting("Авто-Фейерверк", true);

   private boolean escaping;
   private class_243 escapePos;
   private long escapeStartTime;
   private int returnFireworkTicks = -1;
   private class_243 lastEscapeDirection;
   private long lastEscapeTime = 0L;

   public ElytraResolver() {
      super("ElytraResolver", "Отлет на элитрах", Module.ModuleCategory.COMBAT);
      this.addSettings(new Setting[]{this.distance, this.autoFirework});
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.escaping = false;
      this.escapePos = null;
      this.returnFireworkTicks = -1;
      this.lastEscapeDirection = null;
      this.lastEscapeTime = 0L;
   }

   public void onAuraAttack(class_1309 target) {
      if (!this.isEnable() || mc.field_1724 == null || mc.field_1687 == null
            || target == null || target.method_6128()) {
         return;
      }

      if (this.escaping && this.escapePos != null) {
         return;
      }

      class_243 playerPos = mc.field_1724.method_19538();
      class_243 targetPos = target.method_19538();

      class_243 bestPos = this.calculateSmartEscape(playerPos, targetPos, this.distance.get());
      if (bestPos != null) {
         this.escapePos = bestPos;
         this.escaping = true;
         this.escapeStartTime = System.currentTimeMillis();

         if (this.autoFirework.isState() && mc.field_1724.method_6128()) {
            this.useFirework();
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.returnFireworkTicks > 0) {
            this.returnFireworkTicks--;
         } else if (this.returnFireworkTicks == 0) {
            if (this.autoFirework.isState() && mc.field_1724.method_6128()) {
               this.useFirework();
            }
            this.returnFireworkTicks = -1;
         }

         if (this.escaping && this.escapePos != null) {
            double dist = mc.field_1724.method_19538().method_1022(this.escapePos);

            if (dist < 2.0D || System.currentTimeMillis() - this.escapeStartTime > 1500L) {
               this.escaping = false;
               this.lastEscapeTime = System.currentTimeMillis();

               if (this.autoFirework.isState() && mc.field_1724.method_6128()) {
                  this.returnFireworkTicks = 4;
               }
            }
         }
      } else {
         this.escaping = false;
         this.returnFireworkTicks = -1;
      }
   }

   public boolean isEscaping() {
      return this.isEnable()
            && this.escaping
            && this.escapePos != null
            && mc.field_1724 != null
            && mc.field_1724.method_6128();
   }

   public class_243 getEscapePos() {
      return this.escapePos;
   }

   private class_243 calculateSmartEscape(class_243 pPos, class_243 targetPos, float d) {
      class_243 awayFromTarget = new class_243(
            pPos.field_1352 - targetPos.field_1352,
            0.0D,
            pPos.field_1350 - targetPos.field_1350
      );

      if (awayFromTarget.method_1027() < 0.01D) {
         awayFromTarget = new class_243(1.0D, 0.0D, 0.0D);
      } else {
         awayFromTarget = awayFromTarget.method_1029();
      }

      List<class_243> directions = this.generateUpwardDirections(awayFromTarget);
      List<EscapePoint> validPoints = new ArrayList<>();

      class_243 eyePos = mc.field_1724.method_33571();
      double eyeHeight = eyePos.field_1351 - pPos.field_1351;
      class_243 playerVelocity = mc.field_1724.method_18798();

      for (class_243 dir : directions) {
         class_243 target = pPos.method_1019(dir.method_1021(d));

         if (target.field_1351 < pPos.field_1351 + 4.0D) {
            continue;
         }

         class_243 targetEye = target.method_1019(new class_243(0.0D, eyeHeight, 0.0D));
         class_3959 context = new class_3959(
               eyePos,
               targetEye,
               class_3960.field_17558,
               class_242.field_1348,
               mc.field_1724
         );

         class_3965 hit = mc.field_1687.method_17742(context);

         double actualDistance = d;
         class_243 finalPos = target;

         if (hit.method_17783() != class_240.field_1333 && hit.method_17784() != null) {
            double hitDist = hit.method_17784().method_1022(eyePos);
            if (hitDist <= 2.0D) {
               continue;
            }

            actualDistance = hitDist;
            finalPos = hit.method_17784()
                  .method_1019(dir.method_1021(-1.0D))
                  .method_1019(new class_243(0.0D, -eyeHeight, 0.0D));
         }

         if (!this.isPositionSafe(finalPos)) {
            continue;
         }

         double score = this.calculateScore(dir, actualDistance, finalPos, awayFromTarget, playerVelocity);
         validPoints.add(new EscapePoint(finalPos, actualDistance, score));
      }

      if (validPoints.isEmpty()) {
         class_243 upPos = pPos.method_1019(new class_243(0.0D, d, 0.0D));
         return this.isPositionSafe(upPos) ? upPos : null;
      }

      validPoints.sort(Comparator.comparingDouble(p -> -p.score));
      this.lastEscapeDirection = validPoints.get(0).pos.method_1020(pPos).method_1029();
      return validPoints.get(0).pos;
   }

   private List<class_243> generateUpwardDirections(class_243 awayFromTarget) {
      class_243 up = new class_243(0.0D, 1.0D, 0.0D);
      class_243 right = new class_243(-awayFromTarget.field_1350, 0.0D, awayFromTarget.field_1352);
      class_243 left = right.method_1021(-1.0D);

      List<class_243> dirs = new ArrayList<>();

      dirs.add(awayFromTarget.method_1019(up).method_1029());
      dirs.add(awayFromTarget.method_1019(right).method_1019(up).method_1029());
      dirs.add(awayFromTarget.method_1019(left).method_1019(up).method_1029());
      dirs.add(right.method_1019(up).method_1029());
      dirs.add(left.method_1019(up).method_1029());
      dirs.add(awayFromTarget.method_1019(right.method_1021(0.5D)).method_1019(up.method_1021(1.5D)).method_1029());
      dirs.add(awayFromTarget.method_1019(left.method_1021(0.5D)).method_1019(up.method_1021(1.5D)).method_1029());
      dirs.add(awayFromTarget.method_1019(up.method_1021(2.0D)).method_1029());
      dirs.add(right.method_1019(up.method_1021(1.5D)).method_1029());
      dirs.add(left.method_1019(up.method_1021(1.5D)).method_1029());
      dirs.add(awayFromTarget.method_1021(0.7D).method_1019(right.method_1021(0.3D)).method_1019(up.method_1021(1.2D)).method_1029());
      dirs.add(awayFromTarget.method_1021(0.7D).method_1019(left.method_1021(0.3D)).method_1019(up.method_1021(1.2D)).method_1029());
      dirs.add(awayFromTarget.method_1021(0.5D).method_1019(up.method_1021(1.8D)).method_1029());
      dirs.add(right.method_1021(0.8D).method_1019(up.method_1021(1.3D)).method_1029());
      dirs.add(left.method_1021(0.8D).method_1019(up.method_1021(1.3D)).method_1029());

      class_243 velocity = mc.field_1724.method_18798();
      if (velocity.method_1027() > 0.01D) {
         class_243 perpendicular = new class_243(-velocity.field_1350, 0.0D, velocity.field_1352).method_1029();
         dirs.add(perpendicular.method_1019(up).method_1029());
         dirs.add(perpendicular.method_1021(-1.0D).method_1019(up).method_1029());
         dirs.add(perpendicular.method_1019(up.method_1021(1.5D)).method_1029());
         dirs.add(perpendicular.method_1021(-1.0D).method_1019(up.method_1021(1.5D)).method_1029());
      }

      return dirs;
   }

   private boolean isPositionSafe(class_243 pos) {
      if (mc.field_1687 == null || mc.field_1724 == null) {
         return false;
      }

      class_243 head = pos.method_1019(new class_243(0.0D, 1.8D, 0.0D));
      class_3959 context = new class_3959(
            pos,
            head,
            class_3960.field_17558,
            class_242.field_1348,
            mc.field_1724
      );

      class_3965 hit = mc.field_1687.method_17742(context);
      return hit.method_17783() == class_240.field_1333;
   }

   private double calculateScore(class_243 dir, double dist, class_243 pos, class_243 awayFromTarget, class_243 playerVelocity) {
      double score = 0.0D;

      double awayBonus = dir.method_1026(awayFromTarget);
      score += awayBonus * 40.0D;
      score += dir.field_1351 * 25.0D;
      score += dist * 2.0D;

      if (playerVelocity.method_1027() > 0.01D) {
         class_243 velNorm = playerVelocity.method_1029();
         double velocityBonus = dir.method_1026(velNorm);
         score += velocityBonus * 20.0D;
      }

      if (mc.field_1687 != null) {
         double groundY = this.getGroundY(pos);
         double groundDist = pos.field_1351 - groundY;
         if (groundDist < 10.0D) {
            score -= (10.0D - groundDist) * 5.0D;
         }
      }

      if (this.lastEscapeDirection != null && (System.currentTimeMillis() - this.lastEscapeTime) < 2000L) {
         double similarity = dir.method_1026(this.lastEscapeDirection);
         if (similarity > 0.8D) {
            score -= 40.0D;
         }
      }

      return score;
   }

   private double getGroundY(class_243 from) {
      class_243 to = new class_243(from.field_1352, from.field_1351 - 30.0D, from.field_1350);
      class_3959 context = new class_3959(from, to, class_3960.field_17558, class_242.field_1348, mc.field_1724);
      class_3965 hit = mc.field_1687.method_17742(context);

      if (hit.method_17783() != class_240.field_1333 && hit.method_17784() != null) {
         return hit.method_17784().field_1351;
      }

      return mc.field_1687.method_31607();
   }

   private void useFirework() {
      if (mc.field_1724 != null) {
         int slotFirework = InventoryUtils.getItemSlot(class_1802.field_8639);
         if (slotFirework != -1) {
            InventoryUtils.swapAndUseHvH(class_1802.field_8639);
         }
      }
   }

   private static class EscapePoint {
      class_243 pos;
      double distance;
      double score;

      EscapePoint(class_243 pos, double distance, double score) {
         this.pos = pos;
         this.distance = distance;
         this.score = score;
      }
   }
}