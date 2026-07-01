package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.impl.player.Nuker;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.class_2172;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class NukerCommand extends Command {
   public NukerCommand() {
      super("nuker");
   }

   public NukerCommand(String command) {
      super(command);
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add")
                     .then(
                        this.arg("block", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 String input = Nuker.normalizeBlockName(builder1.getRemaining());
                                 class_7923.field_41175
                                    .method_10220()
                                    .map(class_7923.field_41175::method_10221)
                                    .<String>map(class_2960::method_12832)
                                    .filter(name -> name.startsWith(input))
                                    .limit(20L)
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String blockName = Nuker.normalizeBlockName((String)context.getArgument("block", String.class));
                              if (Nuker.INSTANCE.isTargetBlock(blockName)) {
                                 ChatUtils.sendMessage("§cБлок §e" + blockName + "§c уже в списке Nuker!");
                                 return 1;
                              } else if (!this.blockExists(blockName)) {
                                 ChatUtils.sendMessage("§cБлок §e" + blockName + "§c не найден!");
                                 return 1;
                              } else {
                                 Nuker.INSTANCE.addBlock(blockName);
                                 ChatUtils.sendMessage("§aБлок §e" + blockName + "§a добавлен в Nuker!");
                                 return 1;
                              }
                           })
                     )
               ))
               .then(this.literal("remove").then(this.arg("block", StringArgumentType.word()).suggests((context, builder1) -> {
                  String input = Nuker.normalizeBlockName(builder1.getRemaining());
                  Nuker.INSTANCE.getTargetBlocks().stream().sorted(String::compareTo).filter(name -> name.startsWith(input)).forEach(builder1::suggest);
                  return builder1.buildFuture();
               }).executes(context -> {
                  String blockName = Nuker.normalizeBlockName((String)context.getArgument("block", String.class));
                  if (!Nuker.INSTANCE.isTargetBlock(blockName)) {
                     ChatUtils.sendMessage("§cБлока §e" + blockName + "§c нет в списке Nuker!");
                     return 1;
                  } else {
                     Nuker.INSTANCE.removeBlock(blockName);
                     ChatUtils.sendMessage("§aБлок §e" + blockName + "§a удален из Nuker!");
                     return 1;
                  }
               }))))
            .then(this.literal("list").executes(context -> {
               Set<String> blocks = Nuker.INSTANCE.getTargetBlocks();
               if (blocks.isEmpty()) {
                  ChatUtils.sendMessage("§cСписок Nuker пуст!");
                  return 1;
               } else {
                  String blockList = blocks.stream().sorted().collect(Collectors.joining("§7, §e"));
                  ChatUtils.sendMessage("§aБлоки Nuker §7(§e" + blocks.size() + "§7)§a: §e" + blockList);
                  return 1;
               }
            })))
         .then(this.literal("clear").executes(context -> {
            if (Nuker.INSTANCE.getTargetBlocks().isEmpty()) {
               ChatUtils.sendMessage("§cСписок Nuker уже пуст!");
               return 1;
            } else {
               Nuker.INSTANCE.clearBlocks();
               ChatUtils.sendMessage("§aСписок Nuker очищен!");
               return 1;
            }
         }));
   }

   private boolean blockExists(String blockName) {
      return class_7923.field_41175.method_10220().anyMatch(block -> class_7923.field_41175.method_10221(block).method_12832().equalsIgnoreCase(blockName));
   }
}
