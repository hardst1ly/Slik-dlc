package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.Module;
import java.lang.reflect.Field;
import java.util.Optional;
import net.minecraft.class_2172;
import org.lwjgl.glfw.GLFW;

public class BindCommand extends Command {
   public BindCommand() {
      super("bind");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      builder.then(
         this.literal("add")
            .then(
               this.arg("module", StringArgumentType.word())
                  .suggests(
                     (context, suggestionsBuilder) -> {
                        String remaining = suggestionsBuilder.getRemaining().toLowerCase();
                        ModuleClass.INSTANCE
                           .getObject()
                           .stream()
                           .map(Module::getName)
                           .filter(name -> name.toLowerCase().startsWith(remaining))
                           .forEach(suggestionsBuilder::suggest);
                        return suggestionsBuilder.buildFuture();
                     }
                  )
                  .then(this.arg("key", StringArgumentType.word()).suggests((context, suggestionsBuilder) -> {
                     String remaining = suggestionsBuilder.getRemaining().toUpperCase();

                     for (Field field : GLFW.class.getDeclaredFields()) {
                        String fieldName = field.getName();
                        if (fieldName.startsWith("GLFW_KEY_")) {
                           String keyName = fieldName.replace("GLFW_KEY_", "");
                           if (keyName.startsWith(remaining)) {
                              suggestionsBuilder.suggest(keyName);
                           }
                        }
                     }

                     if ("NONE".startsWith(remaining)) {
                        suggestionsBuilder.suggest("NONE");
                     }

                     return suggestionsBuilder.buildFuture();
                  }).executes(ctx -> {
                     String moduleName = (String)ctx.getArgument("module", String.class);
                     Optional<Module> optionalModule = this.findModuleByName(moduleName);
                     if (optionalModule.isEmpty()) {
                        ChatUtils.sendMessage("Модуль " + moduleName + " не найден");
                        return 1;
                     } else {
                        Module module = optionalModule.get();
                        String keyName = ((String)ctx.getArgument("key", String.class)).toUpperCase();
                        int keyCode = this.getKeyCode(keyName);
                        if (keyCode == -1) {
                           ChatUtils.sendMessage("Клавиша " + keyName + " не найдена");
                        } else {
                           module.setKey(keyCode);
                           ChatUtils.sendMessage("Модуль " + module.getName() + " привязан к клавише " + keyName);
                        }

                        return 1;
                     }
                  }))
            )
      );
      builder.then(this.literal("remove").then(this.arg("module", StringArgumentType.word()).executes(ctx -> {
         String moduleName = (String)ctx.getArgument("module", String.class);
         Optional<Module> optionalModule = this.findModuleByName(moduleName);
         if (optionalModule.isEmpty()) {
            ChatUtils.sendMessage("Модуль " + moduleName + " не найден");
            return 1;
         } else {
            Module module = optionalModule.get();
            module.setKey(-1);
            ChatUtils.sendMessage("Привязка клавиши для модуля " + module.getName() + " удалена");
            return 1;
         }
      })));
      builder.then(this.literal("clear").executes(ctx -> {
         ModuleClass.INSTANCE.getObject().forEach(module -> module.setKey(-1));
         ChatUtils.sendMessage("Все привязки клавиш удалены");
         return 1;
      }));
      builder.then(
         this.literal("list")
            .executes(
               ctx -> {
                  StringBuilder bindingsList = new StringBuilder("Список привязанных модулей: ");
                  boolean hasBinds = ModuleClass.INSTANCE
                     .getObject()
                     .stream()
                     .filter(module -> module.getKey() != -1)
                     .peek(module -> bindingsList.append("Модуль: ").append(module.getName()).append(" -> Клавиша: ").append(module.getKey()).append("\n"))
                     .findAny()
                     .isPresent();
                  if (!hasBinds) {
                     ChatUtils.sendMessage("Нет привязанных модулей");
                  } else {
                     ChatUtils.sendMessage(bindingsList.toString());
                  }

                  return 1;
               }
            )
      );
   }

   private Optional<Module> findModuleByName(String moduleName) {
      return ModuleClass.INSTANCE.getObject().stream().filter(module -> module.getName().equalsIgnoreCase(moduleName)).findFirst();
   }

   private int getKeyCode(String keyName) {
      if ("NONE".equalsIgnoreCase(keyName)) {
         return -1;
      } else {
         try {
            return GLFW.class.getField("GLFW_KEY_" + keyName).getInt(null);
         } catch (IllegalAccessException | NoSuchFieldException var3) {
            return -1;
         }
      }
   }
}
