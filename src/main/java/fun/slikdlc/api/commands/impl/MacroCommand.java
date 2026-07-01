package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.api.utils.cmd.macro.Macro;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import java.lang.reflect.Field;
import net.minecraft.class_2172;
import org.lwjgl.glfw.GLFW;

public class MacroCommand extends Command {
   public MacroCommand() {
      super("macro");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add")
                     .then(this.arg("name", StringArgumentType.word()).then(this.arg("bind", StringArgumentType.word()).suggests((context, builder1) -> {
                        for (Field field : GLFW.class.getDeclaredFields()) {
                           String name = field.getName();
                           if (name.startsWith("GLFW_KEY_")) {
                              String bind = name.replace("GLFW_KEY_", "");
                              if (bind.startsWith(builder1.getRemaining())) {
                                 builder1.suggest(bind);
                              }
                           }
                        }

                        if ("NONE".startsWith(builder1.getRemaining().toUpperCase())) {
                           builder1.suggest("NONE");
                        }

                        return builder1.buildFuture();
                     }).then(this.arg("command", StringArgumentType.greedyString()).executes(context -> {
                        String name = (String)context.getArgument("name", String.class);
                        String bind = ((String)context.getArgument("bind", String.class)).toUpperCase();
                        String command = (String)context.getArgument("command", String.class);
                        if (SlikDlc.INSTANCE.macroStorage.getMacro(name) != null) {
                           ChatUtils.sendMessage("Макрос " + name + " уже существует!");
                           return 1;
                        } else {
                           try {
                              int key = "NONE".equals(bind) ? -1 : GLFW.class.getField("GLFW_KEY_" + bind).getInt(null);
                              SlikDlc.INSTANCE.macroStorage.add(new Macro(name, command, new BindSetting("bind", key)));
                              ChatUtils.sendMessage("Макрос " + name + " был добавлен!");
                           } catch (Exception var5) {
                              ChatUtils.sendMessage("Неверный бинд: " + bind);
                           }

                           return 1;
                        }
                     }))))
               ))
               .then(this.literal("remove").then(this.arg("name", StringArgumentType.word()).suggests((context, builder1) -> {
                  SlikDlc.INSTANCE.macroStorage.getNames().stream().filter(name -> name.startsWith(builder1.getRemaining())).forEach(builder1::suggest);
                  return builder1.buildFuture();
               }).executes(context -> {
                  String name = (String)context.getArgument("name", String.class);
                  if (SlikDlc.INSTANCE.macroStorage.isEmpty()) {
                     ChatUtils.sendMessage("Список макросов пуст!");
                     return 1;
                  } else {
                     Macro macro = SlikDlc.INSTANCE.macroStorage.getMacro(name);
                     if (macro == null) {
                        ChatUtils.sendMessage("Макрос " + name + " не найден!");
                        return 1;
                     } else {
                        SlikDlc.INSTANCE.macroStorage.remove(macro);
                        ChatUtils.sendMessage("Макрос " + name + " был удалён!");
                        return 1;
                     }
                  }
               }))))
            .then(this.literal("list").executes(context -> {
               StringBuilder builder1 = new StringBuilder();
               if (SlikDlc.INSTANCE.macroStorage.getNames().isEmpty()) {
                  ChatUtils.sendMessage("Список макросов пуст!");
               } else {
                  for (int i = 0; i < SlikDlc.INSTANCE.macroStorage.getNames().size(); i++) {
                     builder1.append(SlikDlc.INSTANCE.macroStorage.getNames().get(i));
                     if (i < SlikDlc.INSTANCE.macroStorage.getNames().size() - 1) {
                        builder1.append(", ");
                     }
                  }

                  builder1.append(".");
                  ChatUtils.sendMessage("Макросы: " + builder1);
               }

               return 1;
            })))
         .then(this.literal("clear").executes(context -> {
            if (!SlikDlc.INSTANCE.macroStorage.isEmpty()) {
               SlikDlc.INSTANCE.macroStorage.clear();
               ChatUtils.sendMessage("Все макросы были удалены!");
            } else {
               ChatUtils.sendMessage("Список макросов пуст!");
            }

            return 1;
         }));
   }
}
