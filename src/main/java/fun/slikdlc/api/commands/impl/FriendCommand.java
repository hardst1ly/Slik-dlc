package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.chat.ChatUtils;
import net.minecraft.class_2172;
import net.minecraft.class_640;

public class FriendCommand extends Command {
   public FriendCommand() {
      super("friend");
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
                     if (!SlikDlc.INSTANCE.friendStorage.isFriend(player)) {
                        SlikDlc.INSTANCE.friendStorage.add(player);
                        ChatUtils.sendMessage("Игрок " + player + " добавлен в друзья!");
                     } else {
                        ChatUtils.sendMessage("Игрок " + player + " уже в списке друзей!");
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
                                    .friendStorage
                                    .getFriends()
                                    .stream()
                                    .sorted(String::compareTo)
                                    .filter(name -> name.startsWith(builder1.getRemaining()))
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String player = (String)context.getArgument("player", String.class);
                              if (SlikDlc.INSTANCE.friendStorage.isFriend(player)) {
                                 SlikDlc.INSTANCE.friendStorage.remove(player);
                                 ChatUtils.sendMessage("Игрок " + player + " удалён из друзей!");
                              } else {
                                 ChatUtils.sendMessage("Игрок " + player + " не найден в списке друзей!");
                              }

                              return 1;
                           })
                     )
               ))
            .then(this.literal("list").executes(context -> {
               if (SlikDlc.INSTANCE.friendStorage.getFriends().isEmpty()) {
                  ChatUtils.sendMessage("Список друзей пуст!");
               } else {
                  StringBuilder builder1 = new StringBuilder();

                  for (int i = 0; i < SlikDlc.INSTANCE.friendStorage.getFriends().size(); i++) {
                     builder1.append(SlikDlc.INSTANCE.friendStorage.getFriends().get(i));
                     if (i < SlikDlc.INSTANCE.friendStorage.getFriends().size() - 1) {
                        builder1.append(", ");
                     }
                  }

                  ChatUtils.sendMessage("Друзья: " + builder1);
               }

               return 1;
            })))
         .then(this.literal("clear").executes(context -> {
            if (!SlikDlc.INSTANCE.friendStorage.isEmpty()) {
               SlikDlc.INSTANCE.friendStorage.clear();
               ChatUtils.sendMessage("Список друзей очищен!");
            } else {
               ChatUtils.sendMessage("Список друзей пуст!");
            }

            return 1;
         }));
   }
}
