package fun.slikdlc.api.utils.cmd.macro;

import fun.slikdlc.client.modules.settings.implement.BindSetting;
import lombok.Generated;

public class Macro {
   private String name;
   private String command;
   private BindSetting bind;

   @Generated
   public Macro(String name, String command, BindSetting bind) {
      this.name = name;
      this.command = command;
      this.bind = bind;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getCommand() {
      return this.command;
   }

   @Generated
   public BindSetting getBind() {
      return this.bind;
   }
}
