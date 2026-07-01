package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.impl.combat.Aura;
import java.util.List;
import net.minecraft.class_2172;

public class DataCommand extends Command {
   public DataCommand() {
      super("data");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.executes(
                              context -> {
                                 this.sendStatus();
                                 return 1;
                              }
                           ))
                           .then(this.literal("record").executes(context -> {
                              Aura aura = this.getAura();
                              if (aura == null) {
                                 return 1;
                              } else {
                                 aura.getDataSystem().startRecording();
                                 ChatUtils.sendMessage("Data: запись начата, старые паттерны в памяти очищены");
                                 return 1;
                              }
                           })))
                        .then(((LiteralArgumentBuilder)this.literal("stop").executes(context -> {
                           this.stopRecording("data_" + System.currentTimeMillis());
                           return 1;
                        })).then(this.arg("name", StringArgumentType.greedyString()).executes(context -> {
                           this.stopRecording((String)context.getArgument("name", String.class));
                           return 1;
                        }))))
                     .then(
                        this.literal("play")
                           .then(
                              this.arg("name", StringArgumentType.word())
                                 .suggests(
                                    (context, suggestions) -> {
                                       this.getAuraPatterns()
                                          .stream()
                                          .filter(name -> name.toLowerCase().startsWith(suggestions.getRemaining().toLowerCase()))
                                          .forEach(suggestions::suggest);
                                       return suggestions.buildFuture();
                                    }
                                 )
                                 .executes(context -> {
                                    this.playProfile((String)context.getArgument("name", String.class));
                                    return 1;
                                 })
                           )
                     ))
                  .then(
                     this.literal("delete")
                        .then(
                           this.arg("name", StringArgumentType.word())
                              .suggests(
                                 (context, suggestions) -> {
                                    this.getAuraPatterns()
                                       .stream()
                                       .filter(name -> name.toLowerCase().startsWith(suggestions.getRemaining().toLowerCase()))
                                       .forEach(suggestions::suggest);
                                    return suggestions.buildFuture();
                                 }
                              )
                              .executes(context -> {
                                 this.deleteProfile((String)context.getArgument("name", String.class));
                                 return 1;
                              })
                        )
                  ))
               .then(this.literal("list").executes(context -> {
                  this.listProfiles();
                  return 1;
               })))
            .then(this.literal("clear").executes(context -> {
               Aura aura = this.getAura();
               if (aura == null) {
                  return 1;
               } else {
                  aura.getDataSystem().clearPatterns();
                  ChatUtils.sendMessage("Data: паттерны очищены");
                  return 1;
               }
            })))
         .then(this.literal("status").executes(context -> {
            this.sendStatus();
            return 1;
         }));
   }

   private void stopRecording(String name) {
      Aura aura = this.getAura();
      if (aura != null) {
         if (!aura.getDataSystem().isRecording()) {
            ChatUtils.sendMessage("Data: запись не запущена");
         } else if (!aura.getDataSystem().savePatterns(name)) {
            ChatUtils.sendMessage("Data: нечего сохранять");
         } else {
            aura.getDataSystem().stopRecording();
            ChatUtils.sendMessage("Data: запись остановлена и сохранена как " + name);
         }
      }
   }

   private void playProfile(String name) {
      Aura aura = this.getAura();
      if (aura != null) {
         if (!aura.getDataSystem().loadPatterns(name)) {
            ChatUtils.sendMessage("Data: профиль " + name + " не найден или поврежден");
         } else {
            aura.getDataSystem().setRecording(false);
            aura.getDataSystem().setUsingNeuro(true);
            aura.getDataSystem().resetState();
            ChatUtils.sendMessage("Data: загружен профиль " + name + " (" + aura.getDataSystem().getPatternCount() + " паттернов)");
            ChatUtils.sendMessage("Data: выбери режим ротации Data в Aura");
         }
      }
   }

   private void deleteProfile(String name) {
      Aura aura = this.getAura();
      if (aura != null) {
         if (aura.getDataSystem().deletePatterns(name)) {
            ChatUtils.sendMessage("Data: профиль " + name + " удален");
         } else {
            ChatUtils.sendMessage("Data: профиль " + name + " не найден");
         }
      }
   }

   private void listProfiles() {
      List<String> patterns = this.getAuraPatterns();
      if (patterns.isEmpty()) {
         ChatUtils.sendMessage("Data: нет сохраненных профилей");
      } else {
         ChatUtils.sendMessage("Data: сохраненные профили (" + patterns.size() + "):");

         for (String name : patterns) {
            ChatUtils.sendMessage("  - " + name);
         }
      }
   }

   private void sendStatus() {
      Aura aura = this.getAura();
      if (aura != null) {
         ChatUtils.sendMessage(aura.getDataSystem().getStatusString());
         if (aura.getDataSystem().getPatternCount() > 0) {
            ChatUtils.sendMessage("Новых в сессии: " + aura.getDataSystem().getRecordedThisSession());
         }
      }
   }

   private Aura getAura() {
      Aura aura = ModuleClass.INSTANCE == null ? null : ModuleClass.aura;
      if (aura == null) {
         ChatUtils.sendMessage("Data: модуль Aura не найден");
      }

      return aura;
   }

   private List<String> getAuraPatterns() {
      Aura aura = ModuleClass.INSTANCE == null ? null : ModuleClass.aura;
      return aura == null ? List.of() : aura.getDataSystem().getPatternNames();
   }
}
