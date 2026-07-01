package fun.slikdlc.api.utils.render.fonts.ttf;

import java.awt.Font;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

public class FontUtil {
   public FontUtil() {
   }

   public static Font getFontFromTTF(class_2960 loc, float fontSize, int fontType) {
      try {
         class_310 client = class_310.method_1551();
         if (client == null) {
            return null;
         } else if (client.method_1478() == null) {
            return null;
         } else {
            Optional<class_3298> resource = client.method_1478().method_14486(loc);
            if (resource.isPresent()) {
               InputStream inputStream = resource.get().method_14482();
               Font output = Font.createFont(fontType, inputStream);
               output = output.deriveFont(fontSize);
               inputStream.close();
               return output;
            } else {
               return null;
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         return null;
      }
   }
}
