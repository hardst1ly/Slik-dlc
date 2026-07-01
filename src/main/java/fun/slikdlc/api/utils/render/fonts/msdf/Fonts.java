package fun.slikdlc.api.utils.render.fonts.msdf;

import java.util.HashMap;
import net.minecraft.class_4587;

public class Fonts {
   private static final HashMap<String, MsdfFont> loadedFonts = new HashMap<>();
   private static final HashMap<String, Font[]> fontCache = new HashMap<>();
   private static boolean initialized = false;

   public Fonts() {
   }

   public static void init() {
      if (!initialized) {
         initialized = true;
         loadFont("sf_regular");
         loadFont("wave");
         loadFont("icon");
         loadFont("icon1");
         loadFont("iconnew");
         loadFont("suisse");
      }
   }

   private static void loadFont(String name) {
      try {
         MsdfFont msdfFont = MsdfFont.builder().atlas(name).data(name).build();
         loadedFonts.put(name, msdfFont);
         Font[] fonts = new Font[100];

         for (int i = 8; i < 100; i++) {
            fonts[i] = new Font(msdfFont, (float)i);
         }

         fontCache.put(name, fonts);
      } catch (Exception var4) {
         System.err.println("[Fonts] Failed to load " + name + ": " + var4.getMessage());
      }
   }

   public static Font getFont(String name, int size) {
      if (!initialized) {
         init();
      }

      String cleanName = name.replace(".ttf", "");
      if (size < 8) {
         size = 8;
      }

      if (size >= 100) {
         size = 99;
      }

      Font[] fonts = fontCache.get(cleanName);
      if (fonts != null && fonts[size] != null) {
         return fonts[size];
      } else {
         if (!loadedFonts.containsKey(cleanName)) {
            loadFont(cleanName);
         }

         fonts = fontCache.get(cleanName);
         return fonts != null && fonts[size] != null ? fonts[size] : null;
      }
   }

   public static void drawStringWithFade(Font font, String text, float x, float y, float maxWidth, int color) {
      if (font != null) {
         class_4587 stack = new class_4587();
         font.drawStringWithFade(stack, text, x, y, maxWidth, color);
      }
   }
}
