package fun.slikdlc.client.modules.settings.implement;

import fun.slikdlc.client.modules.settings.Setting;
import java.util.function.Supplier;
import lombok.Generated;

public class BindSetting extends Setting {
   private int key;

   public BindSetting(String name, int keyDefault) {
      super(name);
      this.key = keyDefault;
   }

   public BindSetting visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }
}
