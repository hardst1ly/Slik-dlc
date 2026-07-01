package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.player.MoveUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2846;
import net.minecraft.class_3532;
import net.minecraft.class_2846.class_2847;

public class NoWeb extends Module {
   public static NoWeb INSTANCE = new NoWeb();
   public ModeSetting web = new ModeSetting("Мод", "Коллизия", "Коллизия", "Обычный", "Тест");

   public NoWeb() {
      super("NoWeb", "Убирает замедление от паутины", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.web});
   }

   @EventLink
   public void onUpdate(EventUpdate eventUpdate) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.web.is("Коллизия")) {
            class_2338 playerPos = mc.field_1724.method_24515();

            for (int x = -1; x <= 1; x++) {
               for (int y = 0; y <= 2; y++) {
                  for (int z = -1; z <= 1; z++) {
                     class_2338 pos = playerPos.method_10069(x, y, z);
                     if (mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10343) {
                        mc.field_1724.field_3944.method_52787(new class_2846(class_2847.field_12973, pos, class_2350.field_11036));
                     }
                  }
               }
            }
         }

         if (this.web.is("Обычный") && (!mc.field_1724.method_5715() || !mc.field_1724.method_24828())) {
            boolean headInWeb = false;
            boolean feetInWeb = false;

            for (double x = -0.295; x <= 0.295; x += 0.05) {
               for (double zx = -0.295; zx <= 0.295; zx += 0.05) {
                  for (double y = mc.field_1724.method_5751(); y >= 0.0; y -= 0.1) {
                     class_2338 headPos = class_2338.method_49637(
                        mc.field_1724.method_23317() + x, mc.field_1724.method_23318() + y, mc.field_1724.method_23321() + zx
                     );
                     if (mc.field_1687.method_8320(headPos).method_26204() == class_2246.field_10343) {
                        headInWeb = true;
                        break;
                     }
                  }
               }
            }

            if (!headInWeb) {
               for (double var20 = -0.295; var20 <= 0.295; var20 += 0.05) {
                  for (double zx = -0.295; zx <= 0.295; zx += 0.05) {
                     class_2338 pos = class_2338.method_49637(
                        mc.field_1724.method_23317() + var20, mc.field_1724.method_23318(), mc.field_1724.method_23321() + zx
                     );
                     if (mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10343) {
                        feetInWeb = true;
                        break;
                     }
                  }
               }
            }

            class_2338 aboveHeadPos = class_2338.method_49637(
               mc.field_1724.method_23317(), mc.field_1724.method_23318() + mc.field_1724.method_5751() + 0.2F, mc.field_1724.method_23321()
            );
            if (!headInWeb && !feetInWeb && mc.field_1687.method_8320(aboveHeadPos).method_26204() == class_2246.field_10343) {
               headInWeb = true;
            }

            if (headInWeb || feetInWeb) {
               if (mc.field_1690.field_1903.method_1434()) {
                  mc.field_1724.method_18800(0.0, 0.8, 0.0);
               } else if (mc.field_1690.field_1832.method_1434()) {
                  mc.field_1724.method_18800(0.0, -0.8, 0.0);
               } else {
                  mc.field_1724.method_18800(0.0, 0.0, 0.0);
               }

               MoveUtils.setMotion(0.21);
            }
         }

         if (this.web.is("Тест") && mc.field_1724 != null) {
            boolean cobweb = false;
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
                  cobweb = true;
               }
            }

            if (cobweb) {
               class_243 velocity = mc.field_1724.method_18798();
               float yaw = mc.field_1724.method_36454();
               double forward = 0.0;
               double strafe = 0.0;
               if (mc.field_1724.field_3913.field_54155.comp_3159()) {
                  forward++;
               }

               if (mc.field_1724.field_3913.field_54155.comp_3160()) {
                  forward--;
               }

               if (mc.field_1724.field_3913.field_54155.comp_3161()) {
                  strafe++;
               }

               if (mc.field_1724.field_3913.field_54155.comp_3162()) {
                  strafe--;
               }

               if (forward != 0.0 || strafe != 0.0) {
                  if (forward != 0.0) {
                     if (strafe > 0.0) {
                        yaw += forward > 0.0 ? -45 : 45;
                     } else if (strafe < 0.0) {
                        yaw += forward > 0.0 ? 45 : -45;
                     }

                     strafe = 0.0;
                     if (forward > 0.0) {
                        forward = 1.0;
                     } else {
                        forward = -1.0;
                     }
                  }

                  double movementYaw = Math.toDegrees(Math.atan2(strafe, forward)) + yaw;
                  yaw = (float)((movementYaw % 360.0 + 360.0) % 360.0);
               }

               float result = 0.63F;
               if ((!(yaw >= 313.0F) || !(yaw <= 317.0F))
                  && (!(yaw >= 223.0F) || !(yaw <= 227.0F))
                  && (!(yaw >= 133.0F) || !(yaw <= 137.0F))
                  && (!(yaw >= 43.0F) || !(yaw <= 47.0F))) {
                  if ((!(yaw >= 311.0F) || !(yaw <= 319.0F))
                     && (!(yaw >= 221.0F) || !(yaw <= 229.0F))
                     && (!(yaw >= 131.0F) || !(yaw <= 139.0F))
                     && (!(yaw >= 41.0F) || !(yaw <= 49.0F))) {
                     if ((!(yaw >= 310.8F) || !(yaw <= 320.8F))
                        && (!(yaw >= 220.8F) || !(yaw <= 230.8F))
                        && (!(yaw >= 130.8F) || !(yaw <= 140.8F))
                        && (!(yaw >= 40.8F) || !(yaw <= 50.8F))) {
                        if ((!(yaw >= 308.7F) || !(yaw <= 322.7F))
                           && (!(yaw >= 218.7F) || !(yaw <= 232.7F))
                           && (!(yaw >= 128.7F) || !(yaw <= 142.7F))
                           && (!(yaw >= 38.7F) || !(yaw <= 52.7F))) {
                           if ((!(yaw >= 306.5F) || !(yaw <= 324.5F))
                              && (!(yaw >= 216.5F) || !(yaw <= 234.5F))
                              && (!(yaw >= 126.5F) || !(yaw <= 144.5F))
                              && (!(yaw >= 36.5F) || !(yaw <= 54.5F))) {
                              if (yaw >= 304.0F && yaw <= 327.0F
                                 || yaw >= 214.0F && yaw <= 237.0F
                                 || yaw >= 124.0F && yaw <= 147.0F
                                 || yaw >= 34.0F && yaw <= 57.0F) {
                                 result = 0.75F;
                              }
                           } else {
                              result = 0.79F;
                           }
                        } else {
                           result = 0.81F;
                        }
                     } else {
                        result = 0.83F;
                     }
                  } else {
                     result = 0.85F;
                  }
               } else {
                  result = 0.88F;
               }

               if (!mc.field_1690.field_1903.method_1434()) {
                  if (mc.field_1690.field_1832.method_1434()) {
                     mc.field_1724.method_18800(velocity.field_1352, -3.6, velocity.field_1350);
                  } else {
                     mc.field_1724.method_18800(velocity.field_1352, 0.0, velocity.field_1350);
                  }
               } else {
                  mc.field_1724.method_18800(velocity.field_1352, forward == 0.0 && strafe == 0.0 ? 1.4 : 1.2, velocity.field_1350);
               }

               MoveUtils.setVelocity(result);
            }
         }
      }
   }
}
