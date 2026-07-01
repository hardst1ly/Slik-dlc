package fun.slikdlc.api.utils.combat;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.mixin.IEntity;
import lombok.Generated;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1743;
import net.minecraft.class_1802;
import net.minecraft.class_1821;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3532;

public final class IdealHitUtils implements QClient {
   private static final int WATER_CRIT_INTENT_TICKS = 8;
   private static final int WATER_CRIT_CONTACT_TICKS = 10;
   private static final double WATER_CRIT_MIN_UPWARD_VELOCITY = 0.05;
   private static int lastWaterContactAge = -1000;
   private static int lastWaterCritIntentAge = -1000;

   public static float getAICooldown() {
      if (mc.field_1724.method_6047().method_7909() == class_1802.field_8162) {
         return 0.9F;
      } else {
         return !(mc.field_1724.method_6047().method_7909() instanceof class_1743) && !(mc.field_1724.method_6047().method_7909() instanceof class_1821)
            ? 0.93F
            : 0.95F;
      }
   }

   public static boolean canAIFall() {
      class_2338 posWater = class_2338.method_49638(mc.field_1724.method_19538().method_1031(0.0, -0.4F, 0.0));
      return mc.field_1687.method_8320(posWater).method_27852(class_2246.field_10382)
         ? true
         : getBlock(0.0, 3.0, 0.0) == class_2246.field_10124
               && getBlock(0.0, 2.0, 0.0) == class_2246.field_10124
               && getBlock(0.0, 1.0, 0.0) == class_2246.field_10124
            || mc.field_1724.field_6017 < (getBlock(0.0, 2.0, 0.0) != class_2246.field_10124 ? 0.08F : 0.6F)
            || mc.field_1724.field_6017 > 1.2F;
   }

   public static boolean canCritical(class_1309 target) {
      updateWaterCritState();
      boolean packetCrits = ModuleClass.packetCriticals.isEnable();
      boolean hasSlowFalling = mc.field_1724.method_6059(class_1294.field_5906);
      boolean inCobweb = isInCobweb();
      boolean smartCrit = ModuleClass.aura.smartCrit.isState();
      if (packetCrits && inCobweb) {
         return true;
      } else if (packetCrits && hasSlowFalling) {
         return mc.field_1724.method_18798().field_1351 < 0.0 && mc.field_1724.field_6017 > 0.0F;
      } else if (isTryingWaterCrit()) {
         return isWaterCritWindow();
      } else {
         boolean isCritPossible = !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 < 0.0 && mc.field_1724.field_6017 > 0.0F;
         if (isNoJumpDelayCeilingCritIntent()) {
            return isNoJumpDelayCeilingCritWindow();
         } else if (isNoJumpDelayJumpCritIntent()) {
            return isNoJumpDelayJumpCritWindow();
         } else if (cannotPerformCrit()) {
            return true;
         } else {
            return !smartCrit ? isCritPossible : mc.field_1724.method_24828() || isCritPossible;
         }
      }
   }

   private static boolean isNoJumpDelayCeilingCritIntent() {
      return ModuleClass.noJumpDelay.isEnable() && mc.field_1690 != null && mc.field_1690.field_1903.method_1434() && hasLowCeilingForJumpCrit();
   }

   private static boolean isNoJumpDelayJumpCritIntent() {
      return ModuleClass.noJumpDelay.isEnable() && mc.field_1690 != null && mc.field_1690.field_1903.method_1434();
   }

   private static boolean isNoJumpDelayCeilingCritWindow() {
      return mc.field_1724 != null
         && !mc.field_1724.method_24828()
         && mc.field_1724.method_18798().field_1351 <= 0.01
         && !mc.field_1724.method_5799()
         && !mc.field_1724.method_5869()
         && !mc.field_1724.method_5771()
         && !mc.field_1724.method_6101()
         && !mc.field_1724.method_5765()
         && !mc.field_1724.method_31549().field_7479;
   }

   public static boolean isNoJumpDelayJumpCritWindow() {
      return mc.field_1724 != null
         && mc.field_1687 != null
         && ModuleClass.noJumpDelay.isEnable()
         && mc.field_1690 != null
         && mc.field_1690.field_1903.method_1434()
         && !mc.field_1724.method_24828()
         && mc.field_1724.method_18798().field_1351 < 0.0
         && !mc.field_1724.method_5799()
         && !mc.field_1724.method_5869()
         && !mc.field_1724.method_5771()
         && !mc.field_1724.method_6101()
         && !mc.field_1724.method_5765()
         && !mc.field_1724.method_31549().field_7479
         && !mc.field_1724.method_6059(class_1294.field_5902)
         && !mc.field_1724.method_6059(class_1294.field_5906)
         && !mc.field_1724.method_6059(class_1294.field_5919)
         && !mc.field_1724.method_6128()
         && !isInCobweb();
   }

   private static boolean hasLowCeilingForJumpCrit() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_238 box = mc.field_1724.method_5829().method_1011(0.03);
         class_238 headBox = new class_238(box.field_1323, box.field_1325, box.field_1321, box.field_1320, box.field_1325 + 0.32, box.field_1324);

         for (class_2338 pos : class_2338.method_10094(
            class_3532.method_15357(headBox.field_1323),
            class_3532.method_15357(headBox.field_1322),
            class_3532.method_15357(headBox.field_1321),
            class_3532.method_15357(headBox.field_1320),
            class_3532.method_15357(headBox.field_1325),
            class_3532.method_15357(headBox.field_1324)
         )) {
            class_2680 state = mc.field_1687.method_8320(pos);
            if (!state.method_26215() && !state.method_26220(mc.field_1687, pos).method_1110()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean canPacketCrit() {
      return isInCobweb() || mc.field_1724.method_6059(class_1294.field_5906);
   }

   private static void updateWaterCritState() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean nearWaterSurface = isNearWaterSurface();
         if (nearWaterSurface) {
            lastWaterContactAge = mc.field_1724.field_6012;
            if (isWaterCritIntentState()) {
               lastWaterCritIntentAge = mc.field_1724.field_6012;
            }
         }
      } else {
         lastWaterContactAge = -1000;
         lastWaterCritIntentAge = -1000;
      }
   }

   private static boolean isWaterCritIntentState() {
      return mc.field_1724 != null && mc.field_1690 != null
         ? mc.field_1690.field_1903.method_1434()
            && !mc.field_1724.method_24828()
            && !mc.field_1724.method_5869()
            && mc.field_1724.method_18798().field_1351 > 0.05
         : false;
   }

   private static boolean isTryingWaterCrit() {
      return mc.field_1724 != null && mc.field_1690 != null && mc.field_1690.field_1903.method_1434()
         ? mc.field_1724.field_6012 - lastWaterCritIntentAge <= 8 && mc.field_1724.field_6012 - lastWaterContactAge <= 10
         : false;
   }

   private static boolean isWaterCritWindow() {
      return mc.field_1724 != null
         && !mc.field_1724.method_24828()
         && !mc.field_1724.method_5799()
         && !mc.field_1724.method_5869()
         && mc.field_1724.field_6017 > 0.0F
         && mc.field_1724.method_18798().field_1351 < 0.0;
   }

   private static boolean isNearWaterSurface() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_2338 below = class_2338.method_49638(mc.field_1724.method_19538().method_1031(0.0, -0.4F, 0.0));
         return mc.field_1724.method_5799() || mc.field_1724.method_5869() || mc.field_1687.method_8320(below).method_27852(class_2246.field_10382);
      } else {
         return false;
      }
   }

   private static boolean cannotPerformCrit() {
      double effectiveJumpHeight = mc.field_1724.method_49476();
      class_243 jumpVec = new class_243(0.0, effectiveJumpHeight, 0.0);
      class_243 allowedMovement = ((IEntity)mc.field_1724).invokeAdjustMovementForCollisions(jumpVec);
      boolean cobweb = isInCobweb();
      class_2338 posWater = class_2338.method_49638(mc.field_1724.method_19538().method_1031(0.0, mc.field_1724.method_17682() / 2.0F, 0.0));
      return mc.field_1724.method_5771()
         || mc.field_1724.method_6101()
         || mc.field_1687.method_8320(posWater).method_27852(class_2246.field_10382)
         || mc.field_1724.method_6059(class_1294.field_5902)
         || mc.field_1724.method_6059(class_1294.field_5906)
         || mc.field_1724.method_6059(class_1294.field_5919)
         || cobweb
         || mc.field_1724.method_6128()
         || mc.field_1724.method_5765()
         || mc.field_1724.method_31549().field_7479
         || mc.field_1724.method_5799()
         || allowedMovement.field_1351 < mc.field_1724.method_49476() - 0.5 && mc.field_1724.method_24828();
   }

   public static boolean isInCobweb() {
      class_238 box = mc.field_1724.method_5829();

      for (class_2338 pos : class_2338.method_10094(
         class_3532.method_15357(box.field_1323),
         class_3532.method_15357(box.field_1322),
         class_3532.method_15357(box.field_1321),
         class_3532.method_15357(box.field_1320),
         class_3532.method_15357(box.field_1325),
         class_3532.method_15357(box.field_1324)
      )) {
         if (mc.field_1687.method_8320(pos).method_27852(class_2246.field_10343)) {
            return true;
         }
      }

      return false;
   }

   public static class_2248 getBlock(double x, double y, double z) {
      return mc.field_1687.method_8320(mc.field_1724.method_24515().method_10069((int)x, (int)y, (int)z)).method_26204();
   }

   public static boolean findFall(float fallDistance) {
      class_243 rotationVec = mc.field_1724.method_5720();
      double tempVelocityX = mc.field_1724.method_18798().field_1352;
      double tempVelocityY = mc.field_1724.method_18798().field_1351;
      double tempVelocityZ = mc.field_1724.method_18798().field_1350;
      float n = class_3532.method_15362(mc.field_1724.method_36455() * (float) (Math.PI / 180.0));
      n = (float)(n * n * Math.min(rotationVec.method_1033() / 0.4, 1.0));
      class_243 vec3d = new class_243(tempVelocityX, tempVelocityY, tempVelocityZ).method_1031(0.0, 0.08 * (-1.0 + n * 0.75), 0.0);
      tempVelocityY = vec3d.field_1351 * 0.98F;
      return tempVelocityY < fallDistance;
   }

   @Generated
   private IdealHitUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
