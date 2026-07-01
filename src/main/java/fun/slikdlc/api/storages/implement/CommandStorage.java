package fun.slikdlc.api.storages.implement;

import com.mojang.brigadier.CommandDispatcher;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.commands.impl.AutoLesCommand;
import fun.slikdlc.api.commands.impl.BindCommand;
import fun.slikdlc.api.commands.impl.BlockESPCommand;
import fun.slikdlc.api.commands.impl.BotCommand;
import fun.slikdlc.api.commands.impl.ConfigCommand;
import fun.slikdlc.api.commands.impl.DataCommand;
import fun.slikdlc.api.commands.impl.FriendCommand;
import fun.slikdlc.api.commands.impl.GPSCommand;
import fun.slikdlc.api.commands.impl.MacroCommand;
import fun.slikdlc.api.commands.impl.NukerCommand;
import fun.slikdlc.api.commands.impl.StaffCommand;
import fun.slikdlc.api.commands.impl.VClipCommand;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.class_2172;
import net.minecraft.class_310;
import net.minecraft.class_637;

public class CommandStorage {
   private final CommandDispatcher<class_2172> dispatcher = new CommandDispatcher();
   private final List<Command> commands = new ArrayList<>();
   private String prefix = ".";

   public CommandStorage() {
      this.registry();
   }

   private void registry() {
      this.addCommands(
         new AutoLesCommand(),
         new FriendCommand(),
         new ConfigCommand(),
         new MacroCommand(),
         new BotCommand(),
         new BlockESPCommand(),
         new NukerCommand(),
         new NukerCommand("nuk"),
         new GPSCommand(),
         new BindCommand(),
         new StaffCommand(),
         new VClipCommand(),
         new DataCommand()
      );
   }

   public class_2172 getSource() {
      return new class_637(null, class_310.method_1551());
   }

   private void addCommands(Command... command) {
      for (Command cmd : command) {
         cmd.register(this.dispatcher);
         this.commands.add(cmd);
      }
   }

   @Generated
   public CommandDispatcher<class_2172> getDispatcher() {
      return this.dispatcher;
   }

   @Generated
   public List<Command> getCommands() {
      return this.commands;
   }

   @Generated
   public String getPrefix() {
      return this.prefix;
   }

   @Generated
   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }
}
