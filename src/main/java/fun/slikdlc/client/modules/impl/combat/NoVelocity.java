package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import net.minecraft.class_2664;
import net.minecraft.class_2743;

public class NoVelocity extends Module {
   public static NoVelocity INSTANCE = new NoVelocity();
   private final ModeSetting mode = new ModeSetting("Мод", "Vanilla", "Vanilla", "Grim", "Jump Reset");
   private final BooleanSetting explosions = new BooleanSetting("Взрывы", true);
   private boolean needJump;
   private int hurtTicks;

   public NoVelocity() {
      super("NoVelocity", "Отключает отдачу от урона", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.explosions});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.needJump = false;
      this.hurtTicks = 0;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.needJump = false;
      this.hurtTicks = 0;
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getType() == EventPacket.Type.RECEIVE) {
            if (event.getPacket() instanceof class_2743 packet) {
               if (packet.method_11818() != mc.field_1724.method_5628()) {
                  return;
               }

               if (this.mode.is("Vanilla")) {
                  event.cancel();
               }

               if (this.mode.is("Grim")) {
                  event.cancel();
                  double velY = packet.method_11816() / 8000.0;
                  if (mc.field_1724.method_24828() && velY > 0.0) {
                     mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.0, mc.field_1724.method_18798().field_1350);
                  } else if (velY > 0.0) {
                     mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.0, mc.field_1724.method_18798().field_1350);
                  }
               }

               if (this.mode.is("Jump Reset")) {
                  double velY = packet.method_11816() / 8000.0;
                  if (velY > 0.1) {
                     this.needJump = true;
                     this.hurtTicks = 0;
                  }
               }
            }

            if (this.explosions.isState() && event.getPacket() instanceof class_2664 && (this.mode.is("Vanilla") || this.mode.is("Grim"))) {
               event.cancel();
            }
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null) {
         if (this.mode.is("Jump Reset") && this.needJump) {
            this.hurtTicks++;
            if (mc.field_1724.method_24828()) {
               mc.field_1724.method_6043();
               this.needJump = false;
               this.hurtTicks = 0;
            }

            if (this.hurtTicks > 5) {
               this.needJump = false;
               this.hurtTicks = 0;
            }
         }
      }
   }
}
