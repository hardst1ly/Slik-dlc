package fun.slikdlc.api.storages.implement.helpertstorages;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.color.ColorUtils;
import lombok.Generated;

public class Theme implements QClient {
   private String name;
   public int[] color;

   public Theme(String name, int... color) {
      this.name = name;
      this.color = color;
   }

   public int getColor(int index) {
      return this.name.equals("Rainbow") ? ColorUtils.rainbow(10, index, 0.6F, 1.0F, 1.0F) : ColorUtils.gradient(5, index, this.color);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int[] getColor() {
      return this.color;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setColor(int[] color) {
      this.color = color;
   }
}
