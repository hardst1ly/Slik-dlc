package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.chat.ChatUtils;
import net.minecraft.class_2172;
import net.minecraft.class_640;

public class StaffCommand extends Command {
   public StaffCommand() {
      super("staff");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add").then(this.arg("player", StringArgumentType.word()).suggests((context, builder1) -> {
                     for (class_640 entry : mc.method_1562().method_2880()) {
                        String name = entry.method_2966().getName();
                        if (name.toLowerCase().startsWith(builder1.getRemaining().toLowerCase())) {
                           builder1.suggest(name);
                        }
                     }

                     return builder1.buildFuture();
                  }).executes(context -> {
                     String player = (String)context.getArgument("player", String.class);
                     if (!SlikDlc.INSTANCE.staffStorage.isStaff(player)) {
                        SlikDlc.INSTANCE.staffStorage.add(player);
                        ChatUtils.sendMessage("Игрок " + player + " добавлен в список стаффов!");
                     } else {
                        ChatUtils.sendMessage("Игрок " + player + " уже в списке стаффов!");
                     }

                     return 1;
                  }))
               ))
               .then(
                  this.literal("remove")
                     .then(
                        this.arg("player", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 SlikDlc.INSTANCE
                                    .staffStorage
                                    .getStaffs()
                                    .stream()
                                    .sorted(String::compareTo)
                                    .filter(name -> name.startsWith(builder1.getRemaining()))
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String player = (String)context.getArgument("player", String.class);
                              if (SlikDlc.INSTANCE.staffStorage.isStaff(player)) {
                                 SlikDlc.INSTANCE.staffStorage.remove(player);
                                 ChatUtils.sendMessage("Игрок " + player + " удалён из списка стаффов!");
                              } else {
                                 ChatUtils.sendMessage("Игрок " + player + " не найден в списке стаффов!");
                              }

                              return 1;
                           })
                     )
               ))
            .then(this.literal("list").executes(context -> {
               StringBuilder builder1 = new StringBuilder();
               if (SlikDlc.INSTANCE.staffStorage.getStaffs().isEmpty()) {
                  ChatUtils.sendMessage("Список стаффов пуст!");
               } else {
                  for (int i = 0; i < SlikDlc.INSTANCE.staffStorage.getStaffs().size(); i++) {
                     builder1.append(SlikDlc.INSTANCE.staffStorage.getStaffs().get(i));
                     if (i < SlikDlc.INSTANCE.staffStorage.getStaffs().size() - 1) {
                        builder1.append(", ");
                     }
                  }

                  builder1.append(".");
                  ChatUtils.sendMessage("Стаффы: " + builder1);
               }

               return 1;
            })))
         .then(this.literal("clear").executes(context -> {
            if (!SlikDlc.INSTANCE.staffStorage.isEmpty()) {
               SlikDlc.INSTANCE.staffStorage.clear();
               ChatUtils.sendMessage("Список стаффов очищен!");
            } else {
               ChatUtils.sendMessage("Список стаффов пуст!");
            }

            return 1;
         }));
   }
}
