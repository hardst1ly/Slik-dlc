package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.math.TimerUtils;
import fun.slikdlc.api.utils.render.font.ReplaceSymbols;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_476;
import net.minecraft.class_640;
import net.minecraft.class_7439;

public class AutoPvp extends Module {
   public static AutoPvp INSTANCE = new AutoPvp();
   private static final Pattern SEARCH_PATTERN = Pattern.compile("Игрок\\s+(\\S+)\\s+ищет себе соперника!", 2);
   private static final long OPEN_DELAY_MS = 250L;
   private static final long RETRY_DELAY_MS = 1000L;
   private static final int SEARCH_MENU_SLOT = 20;
   private final ListSetting donateSettings = new ListSetting(
      "С кем идти...",
      new BooleanSetting("CUSTOM", false),
      new BooleanSetting("D.HELPER", false),
      new BooleanSetting("FROSTINE", false),
      new BooleanSetting("SPRING", false),
      new BooleanSetting("AUTUMN", false),
      new BooleanSetting("GLADIATOR", false),
      new BooleanSetting("PALADIN", false),
      new BooleanSetting("LUXE", false),
      new BooleanSetting("STAFF", false),
      new BooleanSetting("SUPPORT", false),
      new BooleanSetting("ETERNITY", false),
      new BooleanSetting("OVERLORD", false),
      new BooleanSetting("D.ADMIN", false),
      new BooleanSetting("POVELITEL", false),
      new BooleanSetting("IMPERATOR", false),
      new BooleanSetting("LEGENDA", false),
      new BooleanSetting("PRAVITEL", false),
      new BooleanSetting("PHOENIX", false),
      new BooleanSetting("PLAYER", false)
   );
   private final TimerUtils actionTimer = new TimerUtils();
   private boolean queuedJoin;
   private String queuedNickname;

   public AutoPvp() {
      super("AutoPvP", "Помощник в подборе пвп для сервера LonyGrief", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.donateSettings});
   }

   @Override
   public void onEnable() {
      this.queuedJoin = false;
      this.queuedNickname = null;
      this.actionTimer.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.queuedJoin = false;
      this.queuedNickname = null;
      super.onDisable();
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && mc.field_1687 != null && event.getType() == EventPacket.Type.RECEIVE) {
         if (event.getPacket() instanceof class_7439 packet) {
            String raw = packet.comp_763().getString();
            if (raw != null) {
               String plain = class_124.method_539(raw);
               if (plain == null) {
                  plain = raw;
               }

               Matcher matcher = SEARCH_PATTERN.matcher(plain);
               if (matcher.find()) {
                  String nickname = matcher.group(1);
                  if (nickname != null && !nickname.isBlank()) {
                     if (SlikDlc.INSTANCE == null || SlikDlc.INSTANCE.friendStorage == null || !SlikDlc.INSTANCE.friendStorage.isFriend(nickname)) {
                        AutoPvp.DonateRank rank = this.resolveDonateRank(nickname);
                        if (this.isAllowed(rank)) {
                           this.queuedJoin = true;
                           this.queuedNickname = nickname;
                           this.actionTimer.setMillis(0L);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (this.queuedJoin && mc.field_1724 != null && mc.field_1687 != null && mc.method_1562() != null && mc.field_1761 != null) {
         if (mc.field_1755 instanceof class_476 screen) {
            String title = screen.method_25440().getString().toLowerCase();
            if (title.contains("поиск поединка") && this.actionTimer.finished(250L)) {
               mc.field_1761.method_2906(((class_1707)screen.method_17577()).field_7763, 20, 0, class_1713.field_7790, mc.field_1724);
               this.queuedJoin = false;
               this.queuedNickname = null;
               this.actionTimer.reset();
            }
         } else if (mc.field_1755 == null) {
            if (this.actionTimer.finished(1000L)) {
               mc.method_1562().method_45730("pvp");
               this.actionTimer.reset();
            }
         }
      }
   }

   private AutoPvp.DonateRank resolveDonateRank(String nickname) {
      if (mc.method_1562() == null) {
         return null;
      } else {
         for (class_640 player : mc.method_1562().method_2880()) {
            String displayName = player.method_2971() != null ? player.method_2971().getString() : player.method_2966().getName();
            String cleanDisplayName = class_124.method_539(displayName);
            if (cleanDisplayName == null) {
               cleanDisplayName = displayName;
            }

            int nameIndex = this.indexOfIgnoreCase(cleanDisplayName, nickname);
            if (nameIndex >= 0) {
               String donatePrefix = cleanDisplayName.substring(0, nameIndex).trim();
               if (donatePrefix.isEmpty()) {
                  return null;
               }

               String cleanDonate = this.decodeDonatePrefix(donatePrefix).replace("+", "");
               return AutoPvp.DonateRank.fromString(cleanDonate);
            }
         }

         return null;
      }
   }

   private boolean isAllowed(AutoPvp.DonateRank rank) {
      return rank == null ? this.donateSettings.is("CUSTOM") : this.donateSettings.is(rank.getName());
   }

   private String safeStrip(String text) {
      String stripped = class_124.method_539(text);
      return stripped == null ? text : stripped;
   }

   private int indexOfIgnoreCase(String text, String target) {
      return text.toLowerCase().indexOf(target.toLowerCase());
   }

   private String decodeDonatePrefix(String prefix) {
      StringBuilder decoded = new StringBuilder(prefix.length());
      int offset = 0;

      while (offset < prefix.length()) {
         int codePoint = prefix.codePointAt(offset);
         offset += Character.charCount(codePoint);
         String replacement = ReplaceSymbols.replaceCodePoint(codePoint);
         if (replacement != null) {
            decoded.append(replacement);
         } else {
            decoded.appendCodePoint(codePoint);
         }
      }

      return this.convertStyledToNormal(decoded.toString()).trim();
   }

   private String convertStyledToNormal(String styledText) {
      String styled = "бґЂК™бґ„бґ…бґ‡књ°ЙўКњЙЄбґЉбґ‹КџбґЌЙґбґЏбґ\u0098КЂкњ±бґ›бґњбґ бґЎКЏбґўЙґ";
      String normal = "ABCDEFGHIJKLMNOPRSTUVWYZN";
      StringBuilder result = new StringBuilder();

      for (char c : styledText.toCharArray()) {
         int index = styled.indexOf(c);
         if (index != -1) {
            result.append(normal.charAt(index));
         } else {
            result.append(c);
         }
      }

      return result.toString();
   }

   public static enum DonateRank {
      CUSTOM("CUSTOM"),
      D_HELPER("D.HELPER"),
      FROSTINE("FROSTINE"),
      SPRING("SPRING"),
      AUTUMN("AUTUMN"),
      GLADIATOR("GLADIATOR"),
      PALADIN("PALADIN"),
      LUXE("LUXE"),
      STAFF("STAFF"),
      SUPPORT("SUPPORT"),
      ETERNITY("ETERNITY"),
      OVERLORD("OVERLORD"),
      D_ADMIN("D.ADMIN"),
      POVELITEL("POVELITEL"),
      IMPERATOR("IMPERATOR"),
      LEGENDA("LEGENDA"),
      PRAVITEL("PRAVITEL"),
      PHOENIX("PHOENIX"),
      PLAYER("PLAYER");

      private final String name;

      private DonateRank(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static AutoPvp.DonateRank fromString(String text) {
         for (AutoPvp.DonateRank rank : values()) {
            if (rank.name.equalsIgnoreCase(text)) {
               return rank;
            }
         }

         return null;
      }
   }
}
