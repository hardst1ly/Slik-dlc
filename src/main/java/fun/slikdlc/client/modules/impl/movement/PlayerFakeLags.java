package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import lombok.Generated;
import net.minecraft.class_2596;
import net.minecraft.class_2828;

public class PlayerFakeLags extends Module {
   public static PlayerFakeLags INSTANCE = new PlayerFakeLags();
   public final ModeSetting mode = new ModeSetting("Режим", "Blink", "Blink", "Pulse");
   public final FloatSetting delay = new FloatSetting("Задержка (MS)", 500.0F, 50.0F, 2000.0F, 50.0F);
   public final BooleanSetting onlyMovement = new BooleanSetting("Только движение", true);
   public final ObjectArrayList<class_2596<?>> packets = new ObjectArrayList();
   public final TimerUtils timer = new TimerUtils();
   public boolean releasing = false;

   public PlayerFakeLags() {
      super("PlayerFakeLags", "Фейковые лаги", Module.ModuleCategory.MOVEMENT);
      this.addSettings(new Setting[]{this.mode, this.delay, this.onlyMovement});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.packets.clear();
      this.timer.reset();
      this.releasing = false;
   }

   @EventLink
   void onEvent(EventUpdate ignored) {
      if (mc.field_1724 != null) {
         if (this.mode.is("Pulse") && this.timer.finished(this.delay.getValue().longValue())) {
            this.releasePackets();
            this.timer.reset();
         }
      }
   }

   @EventLink
   void onEvent(EventPacket event) {
      if (mc.field_1724 != null && !this.releasing) {
         if (event.getType() == EventPacket.Type.SEND) {
            if (this.onlyMovement.isState()) {
               if (event.getPacket() instanceof class_2828) {
                  event.cancel();
                  this.packets.add(event.getPacket());
               }
            } else {
               event.cancel();
               this.packets.add(event.getPacket());
            }
         }
      }
   }

   private void releasePackets() {
      if (!this.packets.isEmpty()) {
         this.releasing = true;
         ObjectListIterator var1 = this.packets.iterator();

         while (var1.hasNext()) {
            class_2596<?> packet = (class_2596<?>)var1.next();
            mc.field_1724.field_3944.method_52787(packet);
         }

         this.packets.clear();
         this.releasing = false;
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.releasePackets();
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public FloatSetting getDelay() {
      return this.delay;
   }

   @Generated
   public BooleanSetting getOnlyMovement() {
      return this.onlyMovement;
   }

   @Generated
   public ObjectArrayList<class_2596<?>> getPackets() {
      return this.packets;
   }

   @Generated
   public TimerUtils getTimer() {
      return this.timer;
   }

   @Generated
   public boolean isReleasing() {
      return this.releasing;
   }

   @Generated
   public void setReleasing(boolean releasing) {
      this.releasing = releasing;
   }
}
