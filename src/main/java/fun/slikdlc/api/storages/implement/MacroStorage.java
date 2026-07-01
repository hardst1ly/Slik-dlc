package fun.slikdlc.api.storages.implement;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventBinding;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.api.utils.cmd.macro.Macro;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.class_124;

public class MacroStorage implements QClient {
   private final List<Macro> macros = new ArrayList<>();
   private final List<String> names = new ArrayList<>();

   public MacroStorage() {
      EventInvoker.register(this);
   }

   public void add(Macro macro) {
      if (macro != null && macro.getName() != null && !macro.getName().isBlank() && this.getMacro(macro.getName()) == null) {
         this.macros.add(macro);
         this.names.add(macro.getName());
      }
   }

   public void remove(Macro macro) {
      if (macro != null) {
         this.macros.remove(macro);
         this.names.remove(macro.getName());
      }
   }

   public void clear() {
      if (!this.macros.isEmpty()) {
         this.macros.clear();
      }

      if (!this.names.isEmpty()) {
         this.names.clear();
      }
   }

   public boolean isEmpty() {
      return this.macros.isEmpty();
   }

   public Macro getMacro(String name) {
      for (Macro macro : this.macros) {
         if (macro.getName().equalsIgnoreCase(name)) {
            return macro;
         }
      }

      return null;
   }

   @EventLink
   public void onKey(EventBinding e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null && mc.field_1724.field_3944 != null && !this.macros.isEmpty()) {
         for (Macro macro : this.macros) {
            if (macro != null && macro.getBind() != null && macro.getBind().getKey() == e.getKey()) {
               this.executeMacro(macro);
            }
         }
      }
   }

   private void executeMacro(Macro macro) {
      String command = macro.getCommand();
      if (command != null && !command.isBlank()) {
         if (command.startsWith("/")) {
            mc.field_1724.field_3944.method_45730(command.substring(1));
         } else {
            String prefix = SlikDlc.INSTANCE.commandStorage.getPrefix();
            if (prefix != null && !prefix.isEmpty() && command.startsWith(prefix)) {
               try {
                  SlikDlc.INSTANCE.commandStorage.getDispatcher().execute(command.substring(prefix.length()), SlikDlc.INSTANCE.commandStorage.getSource());
               } catch (CommandSyntaxException var5) {
                  ChatUtils.sendMessage(class_124.field_1061 + "Ошибка в использовании макроса " + macro.getName() + "!");
               }
            } else {
               mc.field_1724.field_3944.method_45729(command);
            }
         }
      }
   }

   @Generated
   public List<Macro> getMacros() {
      return this.macros;
   }

   @Generated
   public List<String> getNames() {
      return this.names;
   }
}
