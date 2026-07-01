package fun.slikdlc.client.modules.settings.implement;

import fun.slikdlc.client.modules.settings.Setting;
import java.util.function.Supplier;
import lombok.Generated;
import net.minecraft.class_3532;

public class FloatSetting extends Setting {
   private float value;
   private final float min;
   private final float max;
   private final float increment;
   private boolean active;

   public FloatSetting(String name, float value, float min, float max, float increment) {
      super(name);
      this.value = value;
      this.min = min;
      this.max = max;
      this.increment = increment;
   }

   public Number getValue() {
      return class_3532.method_15363(this.value, this.getMin(), this.getMax());
   }

   public void setValue(float value) {
      this.value = class_3532.method_15363(value, this.getMin(), this.getMax());
   }

   public float get() {
      return this.getValue().floatValue();
   }

   public FloatSetting visible(Supplier<Boolean> state) {
      this.visible = state;
      return this;
   }

   @Generated
   public float getMin() {
      return this.min;
   }

   @Generated
   public float getMax() {
      return this.max;
   }

   @Generated
   public float getIncrement() {
      return this.increment;
   }

   @Generated
   public boolean isActive() {
      return this.active;
   }

   @Generated
   public void setActive(boolean active) {
      this.active = active;
   }
}
