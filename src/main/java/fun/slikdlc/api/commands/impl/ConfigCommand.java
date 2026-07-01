package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.chat.ChatUtils;
import java.io.File;
import java.util.Arrays;
import net.minecraft.class_2172;

public class ConfigCommand extends Command {
   public ConfigCommand() {
      super("config");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("save").then(this.arg("config", StringArgumentType.word()).suggests((context, builder1) -> {
                     if (SlikDlc.INSTANCE.configsDir.exists() && SlikDlc.INSTANCE.configsDir.isDirectory()) {
                        File[] files = SlikDlc.INSTANCE.configsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wonder"));
                        if (files != null) {
                           Arrays.stream(files).map(File::getName).map(name -> name.replace(".wonder", "")).forEach(builder1::suggest);
                        }
                     }

                     return builder1.buildFuture();
                  }).executes(context -> {
                     String config = (String)context.getArgument("config", String.class);

                     try {
                        SlikDlc.INSTANCE.configStorage.saveConfig(config);
                        ChatUtils.sendMessage("Конфиг " + config + " успешно сохранён!");
                     } catch (Exception var3) {
                        ChatUtils.sendMessage("Ошибка при сохранении конфига " + config + "!");
                        var3.printStackTrace();
                     }

                     return 1;
                  }))
               ))
               .then(this.literal("load").then(this.arg("config", StringArgumentType.word()).suggests((context, builder1) -> {
                  if (SlikDlc.INSTANCE.configsDir.exists() && SlikDlc.INSTANCE.configsDir.isDirectory()) {
                     File[] files = SlikDlc.INSTANCE.configsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wonder"));
                     if (files != null) {
                        Arrays.stream(files).map(File::getName).map(name -> name.replace(".wonder", "")).forEach(builder1::suggest);
                     }
                  }

                  return builder1.buildFuture();
               }).executes(context -> {
                  String config = (String)context.getArgument("config", String.class);

                  try {
                     SlikDlc.INSTANCE.configStorage.loadConfig(config);
                     ChatUtils.sendMessage("Конфиг " + config + " успешно загружен!");
                  } catch (Exception var3) {
                     ChatUtils.sendMessage("Ошибка при загрузке конфига " + config + "!");
                     var3.printStackTrace();
                  }

                  return 1;
               }))))
            .then(this.literal("list").executes(context -> {
               File[] files = SlikDlc.INSTANCE.configsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wonder"));
               if (files != null && files.length != 0) {
                  StringBuilder builder1 = new StringBuilder();

                  for (int i = 0; i < files.length; i++) {
                     String fileName = files[i].getName().replace(".wonder", "");
                     builder1.append(fileName);
                     if (i < files.length - 1) {
                        builder1.append(", ");
                     }
                  }

                  ChatUtils.sendMessage("Конфиги: " + builder1);
               } else {
                  ChatUtils.sendMessage("Список конфигов пуст!");
               }

               return 1;
            })))
         .then(this.literal("dir").executes(context -> {
            try {
               File configsDir = new File(SlikDlc.INSTANCE.globalsDir, "configs");
               if (!configsDir.exists()) {
                  configsDir.mkdirs();
               }

               new ProcessBuilder("explorer.exe", configsDir.getAbsolutePath()).start();
               ChatUtils.sendMessage("Папка с конфигами открыта!");
            } catch (Exception var2) {
               ChatUtils.sendMessage("Ошибка при открытии папки с конфигами!");
               var2.printStackTrace();
            }

            return 1;
         }));
   }
}
