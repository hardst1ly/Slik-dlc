package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.impl.render.BlockESP;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.class_2172;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class BlockESPCommand extends Command {
   public BlockESPCommand() {
      super("blockesp");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add")
                     .then(
                        this.arg("block", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 String input = builder1.getRemaining().toLowerCase();
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
                              String blockName = (String)context.getArgument("block", String.class);
                              if (BlockESP.INSTANCE.isTracking(blockName)) {
                                 ChatUtils.sendMessage("§cБлок §e" + blockName + "§c уже отслеживается!");
                                 return 1;
                              } else {
                                 boolean exists = class_7923.field_41175.method_10220().anyMatch(block -> {
                                    String name = class_7923.field_41175.method_10221(block).method_12832();
                                    return name.equalsIgnoreCase(blockName);
                                 });
                                 if (!exists) {
                                    ChatUtils.sendMessage("§cБлок §e" + blockName + "§c не найден!");
                                    return 1;
                                 } else {
                                    BlockESP.INSTANCE.addBlock(blockName);
                                    ChatUtils.sendMessage("§aБлок §e" + blockName + "§a добавлен в отслеживание!");
                                    return 1;
                                 }
                              }
                           })
                     )
               ))
               .then(
                  this.literal("remove")
                     .then(
                        this.arg("block", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 BlockESP.INSTANCE
                                    .getTrackedBlocks()
                                    .stream()
                                    .sorted(String::compareTo)
                                    .filter(name -> name.startsWith(builder1.getRemaining().toLowerCase()))
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String blockName = (String)context.getArgument("block", String.class);
                              if (!BlockESP.INSTANCE.isTracking(blockName)) {
                                 ChatUtils.sendMessage("§cБлок §e" + blockName + "§c не отслеживается!");
                                 return 1;
                              } else {
                                 BlockESP.INSTANCE.removeBlock(blockName);
                                 ChatUtils.sendMessage("§aБлок §e" + blockName + "§a удалён из отслеживания!");
                                 return 1;
                              }
                           })
                     )
               ))
            .then(this.literal("list").executes(context -> {
               Set<String> blocks = BlockESP.INSTANCE.getTrackedBlocks();
               if (blocks.isEmpty()) {
                  ChatUtils.sendMessage("§cСписок отслеживаемых блоков пуст!");
                  return 1;
               } else {
                  String blockList = blocks.stream().sorted().collect(Collectors.joining("§7, §e"));
                  ChatUtils.sendMessage("§aОтслеживаемые блоки §7(§e" + blocks.size() + "§7)§a: §e" + blockList);
                  return 1;
               }
            })))
         .then(this.literal("clear").executes(context -> {
            if (BlockESP.INSTANCE.getTrackedBlocks().isEmpty()) {
               ChatUtils.sendMessage("§cСписок отслеживаемых блоков уже пуст!");
               return 1;
            } else {
               BlockESP.INSTANCE.clearBlocks();
               ChatUtils.sendMessage("§aСписок отслеживаемых блоков очищен!");
               return 1;
            }
         }));
   }
}
