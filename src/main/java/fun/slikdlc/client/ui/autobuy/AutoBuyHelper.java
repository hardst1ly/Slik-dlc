package fun.slikdlc.client.ui.autobuy;

import lombok.Generated;

public class AutoBuyHelper {
   private AutoBuyHelper.Group group;

   public AutoBuyHelper(AutoBuyHelper.Group group) {
      this.group = group;
   }

   public static enum Group {
      RW("RW"),
      HW("HW"),
      FT("FT"),
      SP("SP");

      private final String server;

      @Generated
      private Group(final String server) {
         this.server = server;
      }

      @Generated
      public String getServer() {
         return this.server;
      }
   }
}
