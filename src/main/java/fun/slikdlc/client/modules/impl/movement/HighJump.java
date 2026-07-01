package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_1690;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2480;
import net.minecraft.class_2680;
import net.minecraft.class_495;

public class HighJump extends Module {
   public static HighJump INSTANCE = new HighJump();
   private final ModeSetting mode = new ModeSetting("Режим", "Shulker", "Shulker", "Slime", "Boat");
   private final FloatSetting slimeMultiplier = new FloatSetting("Множитель", 2.0F, 1.1F, 5.0F, 0.1F);
   private boolean wasInBoat;
   private double lastVelY;
   private int cooldown;

   public HighJump() {
      super("HighJump", "Высокий прыжок от различных источников", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.slimeMultiplier});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.wasInBoat = false;
      this.lastVelY = 0.0;
      this.cooldown = 0;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.wasInBoat = false;
      this.lastVelY = 0.0;
      this.cooldown = 0;
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.cooldown > 0) {
            this.cooldown--;
         }

         if (this.mode.is("Shulker")) {
            this.handleShulker();
         }

         if (this.mode.is("Slime")) {
            this.handleSlime();
         }

         if (this.mode.is("Boat")) {
            this.handleBoat();
         }
      }
   }

   private void handleShulker() {
      if (mc.field_1755 instanceof class_495) {
         class_2338 playerPos = mc.field_1724.method_24515();
         class_2338[] checkPositions = new class_2338[]{
            playerPos.method_10074(), playerPos, playerPos.method_10095(), playerPos.method_10072(), playerPos.method_10078(), playerPos.method_10067()
         };
         boolean onShulker = false;

         for (class_2338 pos : checkPositions) {
            class_2680 state = mc.field_1687.method_8320(pos);
            if (state.method_26204() instanceof class_2480) {
               onShulker = true;
               break;
            }
         }

         if (onShulker) {
            mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 2.0, mc.field_1724.method_18798().field_1350);
            mc.field_1724.method_7346();
         }
      }
   }

   private void handleSlime() {
      double velY = mc.field_1724.method_18798().field_1351;
      class_2338 below = mc.field_1724.method_24515().method_10074();
      class_2338 belowTwo = mc.field_1724.method_24515().method_10087(2);
      boolean onSlime = mc.field_1687.method_8320(below).method_27852(class_2246.field_10030)
         || mc.field_1687.method_8320(belowTwo).method_27852(class_2246.field_10030);
      if (this.lastVelY < -0.1 && velY > 0.1 && onSlime && this.cooldown == 0) {
         double boostedVel = velY * this.slimeMultiplier.get();
         mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, boostedVel, mc.field_1724.method_18798().field_1350);
         this.cooldown = 5;
      }

      this.lastVelY = velY;
   }

   private void handleBoat() {
      boolean inBoat = mc.field_1724.method_5854() instanceof class_1690;
      if (this.wasInBoat && !inBoat && this.cooldown == 0) {
         mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 1.5, mc.field_1724.method_18798().field_1350);
         this.cooldown = 20;
      }

      this.wasInBoat = inBoat;
   }
}
