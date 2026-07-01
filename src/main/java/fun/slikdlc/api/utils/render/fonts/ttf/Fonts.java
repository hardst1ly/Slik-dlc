package fun.slikdlc.api.utils.render.fonts.ttf;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import net.minecraft.class_2960;

public class Fonts {
   private static final String MOD_ID = "slikdlc";
   private static final Map<String, Map<Float, MCFontRenderer>> regularFonts = new HashMap<>();
   private static final Map<String, Map<Float, GradientFontRenderer>> gradientFonts = new HashMap<>();
   public static MCFontRenderer comfortaa16;
   public static MCFontRenderer comfortaa18;
   public static MCFontRenderer comfortaa20;
   public static GradientFontRenderer comfortaaGradient18;
   public static MCFontRenderer roboto16;
   public static MCFontRenderer roboto18;
   public static MCFontRenderer roboto20;
   public static GradientFontRenderer robotoGradient18;
   public static MCFontRenderer montserrat16;
   public static MCFontRenderer montserrat18;
   public static MCFontRenderer montserrat20;
   public static GradientFontRenderer montserratGradient18;
   private static boolean initialized = false;

   public Fonts() {
   }

   public static void init() {
      if (!initialized) {
         comfortaa16 = getFont("comfortaa.ttf", 16.0F);
         comfortaa18 = getFont("comfortaa.ttf", 18.0F);
         comfortaa20 = getFont("comfortaa.ttf", 20.0F);
         comfortaaGradient18 = getGradientFont("comfortaa.ttf", 18.0F);
         roboto16 = getFont("roboto.ttf", 16.0F);
         roboto18 = getFont("roboto.ttf", 18.0F);
         roboto20 = getFont("roboto.ttf", 20.0F);
         robotoGradient18 = getGradientFont("roboto.ttf", 18.0F);
         montserrat16 = getFont("montserrat.ttf", 16.0F);
         montserrat18 = getFont("montserrat.ttf", 18.0F);
         montserrat20 = getFont("montserrat.ttf", 20.0F);
         montserratGradient18 = getGradientFont("montserrat.ttf", 18.0F);
         initialized = true;
      }
   }

   public static MCFontRenderer getFont(String fontName, float size) {
      regularFonts.computeIfAbsent(fontName, k -> new HashMap<>());
      Map<Float, MCFontRenderer> fontSizes = regularFonts.get(fontName);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = FontUtil.getFontFromTTF(class_2960.method_60655("slikdlc", "fonts/ttf/" + fontName), size, 0);
         if (font == null) {
            font = new Font("Arial", 0, (int)size);
         }

         MCFontRenderer renderer = new MCFontRenderer(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static GradientFontRenderer getGradientFont(String fontName, float size) {
      gradientFonts.computeIfAbsent(fontName, k -> new HashMap<>());
      Map<Float, GradientFontRenderer> fontSizes = gradientFonts.get(fontName);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = FontUtil.getFontFromTTF(class_2960.method_60655("slikdlc", "fonts/" + fontName), size, 0);
         if (font == null) {
            font = new Font("Arial", 0, (int)size);
         }

         GradientFontRenderer renderer = new GradientFontRenderer(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static void drawStringWithFade(MCFontRenderer font, String text, float x, float y, float maxWidth, int color) {
      if (text != null && !text.isEmpty() && !(maxWidth <= 0.0F)) {
         float currentX = x;
         float fadeZoneWidth = Math.min(22.0F, Math.max(8.0F, maxWidth * 0.35F));
         float fadeStartX = x + maxWidth - fadeZoneWidth;
         int originalAlpha = color >> 24 & 0xFF;

         for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            float charWidth = font.getStringWidth(ch);
            if (currentX > x + maxWidth && i > 0) {
               break;
            }

            int finalColor = color;
            if (currentX > fadeStartX) {
               float progress = (currentX - fadeStartX) / fadeZoneWidth;
               progress = Math.max(0.0F, Math.min(1.0F, progress));
               float fadeFactor = (float)Math.cos(progress * Math.PI / 2.0);
               int newAlpha = (int)(originalAlpha * fadeFactor);
               finalColor = color & 16777215 | newAlpha << 24;
            }

            if ((finalColor >> 24 & 0xFF) > 4) {
               font.drawString(ch, currentX, y, finalColor);
            }

            currentX += charWidth;
         }
      }
   }

   public static MCFontRenderer getSystemFont(String fontName, float size) {
      String key = "system_" + fontName;
      regularFonts.computeIfAbsent(key, k -> new HashMap<>());
      Map<Float, MCFontRenderer> fontSizes = regularFonts.get(key);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = new Font(fontName, 0, (int)size);
         MCFontRenderer renderer = new MCFontRenderer(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static MCFontRenderer getSystemFont(String fontName, float size, int style) {
      String key = "system_" + fontName + "_" + style;
      regularFonts.computeIfAbsent(key, k -> new HashMap<>());
      Map<Float, MCFontRenderer> fontSizes = regularFonts.get(key);
      if (fontSizes.containsKey(size)) {
         return fontSizes.get(size);
      } else {
         Font font = new Font(fontName, style, (int)size);
         MCFontRenderer renderer = new MCFontRenderer(font, true, true);
         fontSizes.put(size, renderer);
         return renderer;
      }
   }

   public static void clearCache() {
      regularFonts.clear();
      gradientFonts.clear();
      initialized = false;
   }

   public static void clearCache(String fontName) {
      regularFonts.remove(fontName);
      gradientFonts.remove(fontName);
   }

   @Generated
   public static boolean isInitialized() {
      return initialized;
   }
}
