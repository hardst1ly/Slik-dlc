package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_243;
import net.minecraft.class_476;
import net.minecraft.class_640;
import net.minecraft.class_7439;

public class AutoDuel extends Module {
   public static AutoDuel INSTANCE = new AutoDuel();
   public ModeSetting mode = new ModeSetting("Режим", "Шары", "Щит", "Шипы", "Лук", "Тотемы", "Нодебафф", "Шары", "Классик", "Читер", "Незер");
   private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
   private final List<String> sent = new ArrayList<>();
   private final TimerUtils duelT = new TimerUtils();
   private final TimerUtils clrT = new TimerUtils();
   private final TimerUtils pickT = new TimerUtils();
   private final TimerUtils setT = new TimerUtils();
   private class_243 lastPos;
   private boolean inDuel;

   public AutoDuel() {
      super("AutoDuel", "Автоматически кидает дуель", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.mode});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.sent.clear();
      this.inDuel = false;
      if (mc.field_1724 != null) {
         this.lastPos = mc.field_1724.method_19538();
      }

      this.duelT.reset();
      this.clrT.reset();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.sent.clear();
      this.inDuel = false;
   }

   @EventLink
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.inDuel) {
         if (this.lastPos != null && mc.field_1724.method_19538().method_1022(this.lastPos) > 500.0) {
            this.toggle();
         } else {
            this.lastPos = mc.field_1724.method_19538();
            if (this.clrT.getElapsedTime() >= 30000L) {
               this.sent.clear();
               this.clrT.reset();
            }

            if (this.duelT.getElapsedTime() >= 1000L) {
               this.sendDuel();
               this.duelT.reset();
            }

            this.handleGui();
         }
      }
   }

   @EventLink
   public void onPacket(EventPacket e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (e.getType() == EventPacket.Type.RECEIVE && e.getPacket() instanceof class_7439 p) {
            String msg = p.comp_763().getString().toLowerCase();
            if (msg.contains("начало") && msg.contains("через") && msg.contains("секунд")
               || msg.contains("поединок начался")
               || msg.contains("во время поединка")) {
               this.inDuel = true;
               this.toggle();
            }
         }
      }
   }

   private void sendDuel() {
      for (String p : this.getPlayers()) {
         if (!this.sent.contains(p) && !p.equals(mc.field_1724.method_5477().getString())) {
            mc.method_1562().method_45730("duel " + p);
            this.sent.add(p);
            break;
         }
      }
   }

   private void handleGui() {
      if (mc.field_1755 instanceof class_476 s) {
         int var4 = ((class_1707)s.method_17577()).field_7763;
         String t = s.method_25440().getString();
         if (t.contains("Выбор набора") && this.pickT.getElapsedTime() >= 150L) {
            mc.field_1761.method_2906(var4, this.getModeSlot(), 0, class_1713.field_7794, mc.field_1724);
            this.pickT.reset();
         } else if (t.contains("Настройка поединка") && this.setT.getElapsedTime() >= 150L) {
            mc.field_1761.method_2906(var4, 0, 0, class_1713.field_7794, mc.field_1724);
            this.setT.reset();
         }
      }
   }

   private int getModeSlot() {
      if (this.mode.is("Щит")) {
         return 0;
      } else if (this.mode.is("Шипы")) {
         return 1;
      } else if (this.mode.is("Лук")) {
         return 2;
      } else if (this.mode.is("Тотемы")) {
         return 3;
      } else if (this.mode.is("Нодебафф")) {
         return 4;
      } else if (this.mode.is("Шары")) {
         return 5;
      } else if (this.mode.is("Классик")) {
         return 6;
      } else if (this.mode.is("Читер")) {
         return 7;
      } else {
         return this.mode.is("Незер") ? 8 : 5;
      }
   }

   private List<String> getPlayers() {
      List<String> list = new ArrayList<>();
      if (mc.method_1562() == null) {
         return list;
      } else {
         for (class_640 e : mc.method_1562().method_2880()) {
            String n = e.method_2966().getName();
            if (NAME_PATTERN.matcher(n).matches()) {
               list.add(n);
            }
         }

         return list;
      }
   }
}
