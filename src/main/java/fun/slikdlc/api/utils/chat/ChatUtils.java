package fun.slikdlc.api.utils.chat;

import fun.slikdlc.api.utils.color.ColorUtils;
import java.awt.Color;
import lombok.Generated;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_5251;

public final class ChatUtils {
   public static void sendMessage(Object message) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null) {
         System.out.println("[SlikDlc] " + message);
      } else {
         class_5250 text = class_2561.method_43470("");
         String prefix = "SlikDlc";

         for (int i = 0; i < prefix.length(); i++) {
            text.method_10852(
               class_2561.method_43470(String.valueOf(prefix.charAt(i)))
                  .method_10862(
                     class_2583.field_24360
                        .method_10982(true)
                        .method_27703(
                           class_5251.method_27717(ColorUtils.gradient(ColorUtils.getThemeColor(0), ColorUtils.getThemeColor(90), (float)i / prefix.length()))
                        )
                  )
            );
         }

         text.method_10852(
            class_2561.method_43470(" ⇨ ")
               .method_10862(class_2583.field_24360.method_10982(false).method_27703(class_5251.method_27717(new Color(200, 200, 200).getRGB())))
         );
         text.method_10852(
            class_2561.method_43470(String.valueOf(message))
               .method_10862(class_2583.field_24360.method_10982(false).method_27703(class_5251.method_27717(new Color(200, 200, 200).getRGB())))
         );
         mc.field_1724.method_7353(text, false);
      }
   }

   @Generated
   private ChatUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
