package fun.slikdlc.client.modules.settings.implement;

import fun.slikdlc.client.modules.settings.Setting;
import java.util.function.Supplier;
import lombok.Generated;

public class BooleanSetting extends Setting {
   private boolean state;

   public BooleanSetting(String name, boolean state) {
      super(name);
      this.state = state;
   }

   public static BooleanSetting of(String name, boolean state) {
      return new BooleanSetting(name, state);
   }

   public BooleanSetting visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public boolean isState() {
      return this.state;
   }

   @Generated
   public void setState(boolean state) {
      this.state = state;
   }
}
