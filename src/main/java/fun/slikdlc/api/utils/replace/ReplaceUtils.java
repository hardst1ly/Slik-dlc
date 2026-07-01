package fun.slikdlc.api.utils.replace;

import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import net.minecraft.class_7417;
import net.minecraft.class_8828.class_2585;

public class ReplaceUtils {
   public ReplaceUtils() {
   }

   public static class_2561 replace(class_2561 input, String target, String replacement) {
      if (input != null && target != null && replacement != null) {
         class_5250 result = class_2561.method_43473().method_10862(input.method_10866());
         appendReplaced(result, input, target, replacement);
         return result;
      } else {
         return input;
      }
   }

   private static void appendReplaced(class_5250 result, class_2561 current, String target, String replacement) {
      class_7417 content = current.method_10851();
      class_2583 style = current.method_10866();
      if (content instanceof class_2585 literal) {
         Pattern pattern = Pattern.compile(Pattern.quote(target), 2);
         String replaced = pattern.matcher(literal.comp_737()).replaceAll(replacement);
         result.method_10852(class_2561.method_43470(replaced).method_10862(style));
      }

      for (class_2561 sibling : current.method_10855()) {
         appendReplaced(result, sibling, target, replacement);
      }
   }

   public static String replaceSymbols(String string) {
      return string.replaceAll("ꔗ", class_124.field_1078 + "MODER")
         .replaceAll("ꔥ", class_124.field_1078 + "ST.MODER")
         .replaceAll("ꔡ", class_124.field_1076 + "MODER+")
         .replaceAll("ꔀ", class_124.field_1080 + "PLAYER")
         .replaceAll("ꔉ", class_124.field_1054 + "HELPER")
         .replaceAll("◆", "@")
         .replaceAll("┃", "|")
         .replaceAll("ꕆ", class_124.field_1054 + "PEGAS")
         .replaceAll("ꔸ", class_124.field_1054 + "GOD")
         .replaceAll("ꔳ", class_124.field_1075 + "Ml.admin")
         .replaceAll("ꔅ", class_124.field_1061 + "Y" + class_124.field_1068 + "T")
         .replaceAll("ꔂ", class_124.field_1078 + "D.MODER")
         .replaceAll("ꕠ", class_124.field_1054 + "D.HELPER")
         .replaceAll("ꕄ", class_124.field_1061 + "VAMPIRE")
         .replaceAll("ꔖ", class_124.field_1075 + "OVERLORD")
         .replaceAll("ꕈ", class_124.field_1060 + "COBRA")
         .replaceAll("ꔨ", class_124.field_1076 + "DRAGON")
         .replaceAll("ꔤ", class_124.field_1061 + "IMPERATOR")
         .replaceAll("ꔠ", class_124.field_1065 + "MAGISTER")
         .replaceAll("ꔄ", class_124.field_1078 + "HERO")
         .replaceAll("ꔒ", class_124.field_1060 + "AVENGER")
         .replaceAll("ꕒ", class_124.field_1068 + "RABBIT")
         .replaceAll("ꔈ", class_124.field_1054 + "TITAN")
         .replaceAll("ꕀ", class_124.field_1077 + "HYDRA")
         .replaceAll("ꔶ", class_124.field_1065 + "TIGER")
         .replaceAll("ꔲ", class_124.field_1064 + "BULL")
         .replaceAll("ꕖ", class_124.field_1074 + "BUNNY")
         .replaceAll("ꕗꕘ", class_124.field_1054 + "SPONSOR")
         .replaceAll("\ud83d\udd25", "@")
         .replaceAll("ᴀ", "A")
         .replaceAll("ʙ", "B")
         .replaceAll("ᴄ", "C")
         .replaceAll("ᴅ", "D")
         .replaceAll("ᴇ", "E")
         .replaceAll("ғ", "F")
         .replaceAll("ɢ", "G")
         .replaceAll("ʜ", "H")
         .replaceAll("ɪ", "I")
         .replaceAll("ᴊ", "J")
         .replaceAll("ᴋ", "K")
         .replaceAll("ʟ", "L")
         .replaceAll("ᴍ", "M")
         .replaceAll("ɴ", "N")
         .replaceAll("ꜱ", "S")
         .replaceAll("s", "S")
         .replaceAll("ᴏ", "O")
         .replaceAll("ᴘ", "P")
         .replaceAll("ǫ", "Q")
         .replaceAll("ʀ", "R")
         .replaceAll("ᴛ", "T")
         .replaceAll("ᴜ", "U")
         .replaceAll("ᴠ", "V")
         .replaceAll("ᴡ", "W")
         .replaceAll("ꜰ", "F")
         .replaceAll("x", "X")
         .replaceAll("ʏ", "Y")
         .replaceAll("ᴢ", "Z");
   }

   public static class_2561 replaceSymbols(class_2561 text) {
      if (text.getString().contains("ꔗ")) {
         text = replace(text, "ꔗ", class_124.field_1078 + "MODER");
      }

      if (text.getString().contains("ꔥ")) {
         text = replace(text, "ꔥ", class_124.field_1078 + "ST.MODER");
      }

      if (text.getString().contains("ꔡ")) {
         text = replace(text, "ꔡ", class_124.field_1076 + "MODER+");
      }

      if (text.getString().contains("ꔀ")) {
         text = replace(text, "ꔀ", class_124.field_1080 + "PLAYER");
      }

      if (text.getString().contains("ꔉ")) {
         text = replace(text, "ꔉ", class_124.field_1054 + "HELPER");
      }

      if (text.getString().contains("◆")) {
         text = replace(text, "◆", "@");
      }

      if (text.getString().contains("┃")) {
         text = replace(text, "┃", "|");
      }

      if (text.getString().contains("ꔳ")) {
         text = replace(text, "ꔳ", class_124.field_1075 + "Ml.admin");
      }

      if (text.getString().contains("ꔅ")) {
         text = replace(text, "ꔅ", class_124.field_1061 + "Y" + class_124.field_1068 + "T");
      }

      if (text.getString().contains("ꔂ")) {
         text = replace(text, "ꔂ", class_124.field_1078 + "D.MODER");
      }

      if (text.getString().contains("ꕠ")) {
         text = replace(text, "ꕠ", class_124.field_1054 + "D.HELPER");
      }

      if (text.getString().contains("ꕄ")) {
         text = replace(text, "ꕄ", class_124.field_1061 + "DRACULA");
      }

      if (text.getString().contains("ꔖ")) {
         text = replace(text, "ꔖ", class_124.field_1075 + "OVERLORD");
      }

      if (text.getString().contains("ꕈ")) {
         text = replace(text, "ꕈ", class_124.field_1060 + "COBRA");
      }

      if (text.getString().contains("ꔨ")) {
         text = replace(text, "ꔨ", class_124.field_1076 + "DRAGON");
      }

      if (text.getString().contains("ꔤ")) {
         text = replace(text, "ꔤ", class_124.field_1061 + "IMPERATOR");
      }

      if (text.getString().contains("ꔠ")) {
         text = replace(text, "ꔠ", class_124.field_1065 + "MAGISTER");
      }

      if (text.getString().contains("ꔄ")) {
         text = replace(text, "ꔄ", class_124.field_1078 + "HERO");
      }

      if (text.getString().contains("ꔒ")) {
         text = replace(text, "ꔒ", class_124.field_1060 + "AVENGER");
      }

      if (text.getString().contains("ꕒ")) {
         text = replace(text, "ꕒ", class_124.field_1068 + "RABBIT");
      }

      if (text.getString().contains("ꔈ")) {
         text = replace(text, "ꔈ", class_124.field_1054 + "TITAN");
      }

      if (text.getString().contains("ꕀ")) {
         text = replace(text, "ꕀ", class_124.field_1077 + "HYDRA");
      }

      if (text.getString().contains("ꔶ")) {
         text = replace(text, "ꔶ", class_124.field_1065 + "TIGER");
      }

      if (text.getString().contains("ꔲ")) {
         text = replace(text, "ꔲ", class_124.field_1064 + "BULL");
      }

      if (text.getString().contains("ꕖ")) {
         text = replace(text, "ꕖ", class_124.field_1074 + "BUNNY");
      }

      if (text.getString().contains("ꕗꕘ")) {
         text = replace(text, "ꕗꕘ", class_124.field_1054 + "SPONSOR");
      }

      if (text.getString().contains("\ud83d\udd25")) {
         text = replace(text, "\ud83d\udd25", "@");
      }

      if (text.getString().contains("ᴀ")) {
         text = replace(text, "ᴀ", "A");
      }

      if (text.getString().contains("ʙ")) {
         text = replace(text, "ʙ", "B");
      }

      if (text.getString().contains("ᴄ")) {
         text = replace(text, "ᴄ", "C");
      }

      if (text.getString().contains("ᴅ")) {
         text = replace(text, "ᴅ", "D");
      }

      if (text.getString().contains("ᴇ")) {
         text = replace(text, "ᴇ", "E");
      }

      if (text.getString().contains("ғ")) {
         text = replace(text, "ғ", "F");
      }

      if (text.getString().contains("ɢ")) {
         text = replace(text, "ɢ", "G");
      }

      if (text.getString().contains("ʜ")) {
         text = replace(text, "ʜ", "H");
      }

      if (text.getString().contains("ɪ")) {
         text = replace(text, "ɪ", "I");
      }

      if (text.getString().contains("ᴊ")) {
         text = replace(text, "ᴊ", "J");
      }

      if (text.getString().contains("ᴋ")) {
         text = replace(text, "ᴋ", "K");
      }

      if (text.getString().contains("ʟ")) {
         text = replace(text, "ʟ", "L");
      }

      if (text.getString().contains("ᴍ")) {
         text = replace(text, "ᴍ", "M");
      }

      if (text.getString().contains("ɴ")) {
         text = replace(text, "ɴ", "N");
      }

      if (text.getString().contains("ꜱ")) {
         text = replace(text, "ꜱ", "S");
      }

      if (text.getString().contains("s")) {
         text = replace(text, "s", "S");
      }

      if (text.getString().contains("ᴏ")) {
         text = replace(text, "ᴏ", "O");
      }

      if (text.getString().contains("ᴘ")) {
         text = replace(text, "ᴘ", "P");
      }

      if (text.getString().contains("ǫ")) {
         text = replace(text, "ǫ", "Q");
      }

      if (text.getString().contains("ʀ")) {
         text = replace(text, "ʀ", "R");
      }

      if (text.getString().contains("ᴛ")) {
         text = replace(text, "ᴛ", "T");
      }

      if (text.getString().contains("ᴜ")) {
         text = replace(text, "ᴜ", "U");
      }

      if (text.getString().contains("ᴠ")) {
         text = replace(text, "ᴠ", "V");
      }

      if (text.getString().contains("ᴡ")) {
         text = replace(text, "ᴡ", "W");
      }

      if (text.getString().contains("ꜰ")) {
         text = replace(text, "ꜰ", "F");
      }

      if (text.getString().contains("x")) {
         text = replace(text, "x", "X");
      }

      if (text.getString().contains("ʏ")) {
         text = replace(text, "ʏ", "Y");
      }

      if (text.getString().contains("ᴢ")) {
         text = replace(text, "ᴢ", "Z");
      }

      return text;
   }
}
