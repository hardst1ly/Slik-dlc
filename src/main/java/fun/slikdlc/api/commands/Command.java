package fun.slikdlc.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fun.slikdlc.api.QClient;
import lombok.Generated;
import net.minecraft.class_2172;

public abstract class Command implements QClient {
   private final String command;

   public Command(String command) {
      this.command = command;
   }

   public abstract void execute(LiteralArgumentBuilder<class_2172> var1);

   public void register(CommandDispatcher<class_2172> dispatcher) {
      LiteralArgumentBuilder<class_2172> builder = LiteralArgumentBuilder.literal(this.command);
      this.execute(builder);
      dispatcher.register(builder);
   }

   protected <T> RequiredArgumentBuilder<class_2172, T> arg(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   protected LiteralArgumentBuilder<class_2172> literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   @Generated
   public String getCommand() {
      return this.command;
   }
}
