package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.bot.BotSessionManager;
import fun.slikdlc.api.utils.chat.ChatUtils;
import java.util.List;
import net.minecraft.class_2172;
import net.minecraft.class_310;

public class BotCommand extends Command {
   public BotCommand() {
      super("bot");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                              this.literal("connect")
                                 .then(this.arg("name", StringArgumentType.string()).then(this.arg("ip", StringArgumentType.string()).executes(context -> {
                                    class_310 mc = class_310.method_1551();
                                    if (mc.field_1724 == null) {
                                       return 0;
                                    } else {
                                       String name = StringArgumentType.getString(context, "name");
                                       String ip = StringArgumentType.getString(context, "ip");
                                       BotSessionManager.connect(name, ip);
                                       ChatUtils.sendMessage("§7[Bot] §fПодключение выполнено: " + name + " -> " + ip);
                                       return 1;
                                    }
                                 })))
                           ))
                           .then(this.literal("remove").then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                              BotSessionManager.getSessionNames(false).forEach(suggestions::suggest);
                              return suggestions.buildFuture();
                           }).executes(context -> {
                              class_310 mc = class_310.method_1551();
                              if (mc.field_1724 == null) {
                                 return 0;
                              } else {
                                 String name = StringArgumentType.getString(context, "name");
                                 if (BotSessionManager.remove(name)) {
                                    ChatUtils.sendMessage("§7[Bot] §fСессия отключена и удалена: " + name);
                                 } else {
                                    ChatUtils.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                                 }

                                 return 1;
                              }
                           }))))
                        .then(this.literal("control").then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                           BotSessionManager.getSessionNames(false).forEach(suggestions::suggest);
                           return suggestions.buildFuture();
                        }).executes(context -> {
                           class_310 mc = class_310.method_1551();
                           if (mc.field_1724 == null) {
                              return 0;
                           } else {
                              String name = StringArgumentType.getString(context, "name");
                              if (name.equalsIgnoreCase(BotSessionManager.getCurrentSessionName())) {
                                 ChatUtils.sendMessage("§7[Bot] §fТы уже управляешь этой сессией: " + name);
                                 return 1;
                              } else {
                                 if (BotSessionManager.control(name)) {
                                    ChatUtils.sendMessage("§7[Bot] §fПереключаю на сессию: " + name);
                                 } else {
                                    ChatUtils.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                                 }

                                 return 1;
                              }
                           }
                        }))))
                     .then(this.literal("say").then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                        BotSessionManager.getSessionNames(false).forEach(suggestions::suggest);
                        return suggestions.buildFuture();
                     }).then(this.arg("message", StringArgumentType.greedyString()).executes(context -> {
                        class_310 mc = class_310.method_1551();
                        if (mc.field_1724 == null) {
                           return 0;
                        } else {
                           String name = StringArgumentType.getString(context, "name");
                           String message = StringArgumentType.getString(context, "message");
                           if (BotSessionManager.say(name, message)) {
                              ChatUtils.sendMessage("§7[Bot] §fСообщение отправлено от сессии " + name);
                           } else {
                              ChatUtils.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                           }

                           return 1;
                        }
                     })))))
                  .then(this.literal("sayall").then(this.arg("message", StringArgumentType.greedyString()).executes(context -> {
                     class_310 mc = class_310.method_1551();
                     if (mc.field_1724 == null) {
                        return 0;
                     } else {
                        String message = StringArgumentType.getString(context, "message");
                        BotSessionManager.sayAll(message);
                        ChatUtils.sendMessage("§7[Bot] §fСообщение отправлено от всех ботов.");
                        return 1;
                     }
                  }))))
               .then(((LiteralArgumentBuilder)this.literal("return").executes(context -> {
                  class_310 mc = class_310.method_1551();
                  if (mc.field_1724 == null) {
                     return 0;
                  } else {
                     if (BotSessionManager.restore()) {
                        ChatUtils.sendMessage("§7[Bot] §fВозвращаю предыдущую сессию");
                     } else {
                        ChatUtils.sendMessage("§7[Bot] §fНет сохранённой сессии для возврата");
                     }

                     return 1;
                  }
               })).then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                  BotSessionManager.getSessionNames(true).forEach(suggestions::suggest);
                  return suggestions.buildFuture();
               }).executes(context -> {
                  class_310 mc = class_310.method_1551();
                  if (mc.field_1724 == null) {
                     return 0;
                  } else {
                     String name = StringArgumentType.getString(context, "name");
                     if (name.equalsIgnoreCase(BotSessionManager.getCurrentSessionName())) {
                        ChatUtils.sendMessage("§7[Bot] §fТы уже управляешь этой сессией: " + name);
                        return 1;
                     } else {
                        if (BotSessionManager.restore(name)) {
                           ChatUtils.sendMessage("§7[Bot] §fПереключаю на сессию: " + name);
                        } else {
                           ChatUtils.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                        }

                        return 1;
                     }
                  }
               }))))
            .then(this.literal("ignore").executes(context -> {
               class_310 mc = class_310.method_1551();
               if (mc.field_1724 == null) {
                  return 0;
               } else {
                  boolean enabled = BotSessionManager.toggleIgnoreBotMessages();
                  ChatUtils.sendMessage("§7[Bot] §fИгнор сообщений ботов: " + (enabled ? "§aвключен" : "§cвыключен"));
                  return 1;
               }
            })))
         .then(this.literal("list").executes(context -> {
            class_310 mc = class_310.method_1551();
            if (mc.field_1724 == null) {
               return 0;
            } else {
               List<BotSessionManager.BotConnection> connections = BotSessionManager.getConnections();
               ChatUtils.sendMessage("§7[Bot] §fТекущая сессия: " + BotSessionManager.getCurrentSessionName());
               if (connections.isEmpty()) {
                  ChatUtils.sendMessage("§7[Bot] §fСписок сохранённых сессий пуст");
               } else {
                  ChatUtils.sendMessage("§7[Bot] §fСохранённые сессии:");

                  for (BotSessionManager.BotConnection bot : connections) {
                     ChatUtils.sendMessage("§7- §f" + bot.name() + " (§7" + bot.address() + "§f)");
                  }
               }

               return 1;
            }
         }));
   }
}
