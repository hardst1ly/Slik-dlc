package fun.slikdlc.api.utils.color;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.utils.math.MathUtils;
import java.awt.Color;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import org.lwjgl.opengl.GL11;

public class ColorUtils {
   public static final Color green = new Color(36, 218, 118);
   public static final Color yellow = new Color(255, 196, 67);
   public static final Color orange = new Color(255, 134, 0);
   public static final Color red = new Color(239, 72, 54);
   public static final Color Blues = new Color(125, 217, 250);

   public ColorUtils() {
   }

   public static int red(int c) {
      return c >> 16 & 0xFF;
   }

   public static int green(int c) {
      return c >> 8 & 0xFF;
   }

   public static float redf(int c) {
      return red(c) / 255.0F;
   }

   public static float greenf(int c) {
      return green(c) / 255.0F;
   }

   public static float bluef(int c) {
      return blue(c) / 255.0F;
   }

   public static float alphaf(int c) {
      return alpha(c) / 255.0F;
   }

   public static int getColor(int brightness, int alpha) {
      return getColor(brightness, brightness, brightness, alpha);
   }

   public static int gradient(int color1, int color2, float amount) {
      amount = class_3532.method_15363(amount, 0.0F, 1.0F);
      int r = class_3532.method_48781(amount, red(color1), red(color2));
      int g = class_3532.method_48781(amount, green(color1), green(color2));
      int b = class_3532.method_48781(amount, blue(color1), blue(color2));
      int a = class_3532.method_48781(amount, alpha(color1), alpha(color2));
      return rgba(r, g, b, a);
   }

   public static int toColor(String hexColor) {
      if (hexColor != null && hexColor.length() == 7 && hexColor.startsWith("#")) {
         try {
            int rgb = Integer.parseInt(hexColor.substring(1), 16);
            return 0xFF000000 | rgb;
         } catch (NumberFormatException var2) {
            return -16777216;
         }
      } else {
         return -16777216;
      }
   }

   public static int applyAlpha(int color, float alphaMul) {
      int a = color >> 24 & 0xFF;
      int na = (int)(a * Math.max(0.0F, Math.min(1.0F, alphaMul)));
      return color & 16777215 | na << 24;
   }

   public static int r(int color) {
      return color >> 16 & 0xFF;
   }

   public static int g(int color) {
      return color >> 8 & 0xFF;
   }

   public static int b(int color) {
      return color & 0xFF;
   }

   public static int a(int color) {
      return color >> 24 & 0xFF;
   }

   public static int hexToRgb(String hex) {
      if (hex.startsWith("#")) {
         hex = hex.substring(1);
      }

      if (hex.length() != 6) {
         throw new IllegalArgumentException("Недопустимый формат HEX: " + hex);
      } else {
         int r = Integer.parseInt(hex.substring(0, 2), 16);
         int g = Integer.parseInt(hex.substring(2, 4), 16);
         int b = Integer.parseInt(hex.substring(4, 6), 16);
         return rgb(r, g, b);
      }
   }

   public static int getThemeColor() {
      return !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : getThemeColor(0);
   }

   public static int getThemeColor(int index) {
      return SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getColor(index);
   }

   public static int getThemeStaticColor() {
      return SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0];
   }

   public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      float hue = angle / 360.0F;
      int color = Color.HSBtoRGB(hue, saturation, brightness);
      return getColor(red(color), green(color), blue(color), Math.max(0, Math.min(255, (int)(opacity * 255.0F))));
   }

   public static int interpolate(int color1, int color2, double amount) {
      amount = (float)MathUtils.clamp(0.0, 1.0, amount);
      return getColor(
         MathUtils.ler1p(red(color1), red(color2), amount),
         MathUtils.ler1p(green(color1), green(color2), amount),
         MathUtils.ler1p(blue(color1), blue(color2), amount),
         MathUtils.ler1p(alpha(color1), alpha(color2), amount)
      );
   }

   public static int[] genGradientForText(int color1, int color2, int length) {
      int[] gradient = new int[length];

      for (int i = 0; i < length; i++) {
         double pc = (double)i / (length - 1);
         gradient[i] = interpolate(color1, color2, pc);
      }

      return gradient;
   }

   public static int blue(int c) {
      return c & 0xFF;
   }

   public static int overCol(int c1, int c2, float pc01) {
      return getColor(
         red(c1) * (1.0F - pc01) + red(c2) * pc01,
         green(c1) * (1.0F - pc01) + green(c2) * pc01,
         blue(c1) * (1.0F - pc01) + blue(c2) * pc01,
         alpha(c1) * (1.0F - pc01) + alpha(c2) * pc01
      );
   }

   public static int darken(int color, float factor) {
      float[] rgb = getColorT(color);
      float[] hsb = Color.RGBtoHSB((int)(rgb[0] * 255.0F), (int)(rgb[1] * 255.0F), (int)(rgb[2] * 255.0F), null);
      hsb[2] *= factor;
      hsb[2] = Math.max(0.0F, Math.min(1.0F, hsb[2]));
      int darkenedRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
      return applyOpacity(darkenedRGB, (int)(rgb[3] * 255.0F));
   }

   public static int multDark(int c, float brpc) {
      return getColor(red(c) * brpc, green(c) * brpc, blue(c) * brpc, (float)alpha(c));
   }

   public static int overCol(int c1, int c2) {
      return overCol(c1, c2, 0.5F);
   }

   public static int alpha(int c) {
      return c >> 24 & 0xFF;
   }

   public static int multAlpha(int c, float apc) {
      return getColor((float)red(c), (float)green(c), (float)blue(c), alpha(c) * apc);
   }

   public static int replAlpha(int color, int alpha) {
      alpha = Math.max(0, Math.min(255, alpha));
      return alpha << 24 | color & 16777215;
   }

   public static Color random() {
      return new Color(Color.HSBtoRGB((float)Math.random(), (float)(0.75 + Math.random() / 4.0), (float)(0.75 + Math.random() / 4.0)));
   }

   public static int getColor(float r, float g, float b, float a) {
      return new Color((int)r, (int)g, (int)b, (int)a).getRGB();
   }

   public static float[] getRGBAf(int c) {
      return new float[]{redf(c), greenf(c), bluef(c), alphaf(c)};
   }

   public static float[] getRGBAf1(int c) {
      return new float[]{red(c) / 255.0F, green(c) / 255.0F, blue(c) / 255.0F, alpha(c) / 255.0F};
   }

   public static Color interpolateTwoColors(int speed, int index, Color start, Color end, boolean trueColor) {
      int angle = 0;
      if (speed == 0) {
         angle = index % 360;
      } else {
         angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      }

      angle = (angle >= 180 ? 360 - angle : angle) * 2;
      return trueColor ? interpolateColorHue(start, end, angle / 360.0F) : interpolateColorC(start, end, angle / 360.0F);
   }

   public static Color interpolateTwoColors(int speed, int index, Color start, Color end) {
      return interpolateTwoColors(speed, index, start, end, false);
   }

   public static Color astolfo(float yDist, float yTotal, float saturation, float speedt) {
      float speed = 1800.0F;
      float hue = (float)(System.currentTimeMillis() % (int)speed) + (yTotal - yDist) * speedt;

      while (hue > speed) {
         hue -= speed;
      }

      hue /= speed;
      if (hue > 1.0F) {
         hue = 1.0F - (hue - 1.0F);
      }

      return Color.getHSBColor(++hue, saturation, 1.0F);
   }

   private static int calculateHueDegrees(int divisor, int offset) {
      long currentTime = System.currentTimeMillis();
      long calculatedValue = (currentTime / divisor + offset) % 360L;
      return (int)calculatedValue;
   }

   public static void setColor(Color color, float alpha) {
      float red = color.getRed() / 255.0F;
      float green = color.getGreen() / 255.0F;
      float blue = color.getBlue() / 255.0F;
      RenderSystem.setShaderColor(red, green, blue, alpha);
   }

   public static int rgb(int r, int g, int b) {
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static int rgba(int r, int g, int b, int a) {
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static float[] rgba(int color) {
      return new float[]{(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >> 24 & 0xFF) / 255.0F};
   }

   public static int rgba(double r, double g, double b, double a) {
      return rgba((int)r, (int)g, (int)b, (int)a);
   }

   public static int getRed(int hex) {
      return hex >> 16 & 0xFF;
   }

   public static int getGreen(int hex) {
      return hex >> 8 & 0xFF;
   }

   public static int interpolate(int start, int end, float value) {
      float[] startColor = rgba(start);
      float[] endColor = rgba(end);
      return rgba(
         (int)MathUtils.interpolate(startColor[0] * 255.0F, endColor[0] * 255.0F, value),
         (int)MathUtils.interpolate(startColor[1] * 255.0F, endColor[1] * 255.0F, value),
         (int)MathUtils.interpolate(startColor[2] * 255.0F, endColor[2] * 255.0F, value),
         (int)MathUtils.interpolate(startColor[3] * 255.0F, endColor[3] * 255.0F, value)
      );
   }

   public static int interpolateColor(int color1, int color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      int red1 = getRed(color1);
      int green1 = getGreen(color1);
      int blue1 = getBlue(color1);
      int alpha1 = getAlpha(color1);
      int red2 = getRed(color2);
      int green2 = getGreen(color2);
      int blue2 = getBlue(color2);
      int alpha2 = getAlpha(color2);
      int interpolatedRed = interpolateInt(red1, red2, amount);
      int interpolatedGreen = interpolateInt(green1, green2, amount);
      int interpolatedBlue = interpolateInt(blue1, blue2, amount);
      int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);
      return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
   }

   public static class_5250 gradient(String message, int first, int end) {
      class_5250 text = class_2561.method_43473();

      for (int i = 0; i < message.length(); i++) {
         int color = interpolateColor(first, end, (float)i / message.length());
         class_5250 charText = class_2561.method_43470(String.valueOf(message.charAt(i)))
            .method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(color)));
         text.method_10852(charText);
      }

      return text;
   }

   public static class_2561 replace(class_2561 original, String find, String replaceWith) {
      if (original != null && find != null && replaceWith != null) {
         String originalText = original.getString();
         String replacedText = originalText.replace(find, replaceWith);
         return class_2561.method_43470(replacedText);
      } else {
         return original;
      }
   }

   public static int gradient(int speed, int index, int... colors) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      int colorIndex = (int)(angle / 360.0F * colors.length);
      if (colorIndex == colors.length) {
         colorIndex--;
      }

      int color1 = colors[colorIndex];
      int color2 = colors[colorIndex == colors.length - 1 ? 0 : colorIndex + 1];
      return interpolateColor(color1, color2, angle / 360.0F * colors.length - colorIndex);
   }

   public static int themeGradient(int speed, int index, float darkenFactor) {
      int theme = getThemeColor();
      return gradient(speed, index, theme, darken(theme, darkenFactor));
   }

   public static int getBlue(int hex) {
      return hex & 0xFF;
   }

   public static int getAlpha(int hex) {
      return hex >> 24 & 0xFF;
   }

   public static int getColor(int red, int green, int blue, int alpha) {
      int color = 0;
      color |= alpha << 24;
      color |= red << 16;
      color |= green << 8;
      return color | blue;
   }

   public static int getColor(int bright) {
      return getColor(bright, bright, bright, 255);
   }

   public static float[] getColorA(int color) {
      return new float[]{red(color) / 255.0F, green(color) / 255.0F, blue(color) / 255.0F, alphaf(color)};
   }

   public static float[] getColorT(int color) {
      return new float[]{red(color) / 255.0F, green(color) / 255.0F, blue(color) / 255.0F, alphaf(color)};
   }

   public static void setColor(double red, double green, double blue, double alpha) {
      GL11.glColor4d(red, green, blue, alpha);
   }

   public static int setAlphaColor(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }

   public static float lerp(float a, float b, float f) {
      return a + f * (b - a);
   }

   public static Color interpolateColorC(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      return new Color(
         interpolateInt(color1.getRed(), color2.getRed(), amount),
         interpolateInt(color1.getGreen(), color2.getGreen(), amount),
         interpolateInt(color1.getBlue(), color2.getBlue(), amount),
         interpolateInt(color1.getAlpha(), color2.getAlpha(), amount)
      );
   }

   public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
      return interpolate((double)oldValue, (double)newValue, (double)((float)interpolationValue)).floatValue();
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return interpolate(oldValue, newValue, (float)interpolationValue);
   }

   public static Color interpolateColorHue(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
      float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
      Color resultColor = Color.getHSBColor(
         interpolateFloat(color1HSB[0], color2HSB[0], amount),
         interpolateFloat(color1HSB[1], color2HSB[1], amount),
         interpolateFloat(color1HSB[2], color2HSB[2], amount)
      );
      return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
   }

   public static void setColor(Color color) {
      if (color == null) {
         color = Color.white;
      }

      setColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
   }

   public static void setColor(int color) {
      setColor(color, (color >> 24 & 0xFF) / 255.0F);
   }

   public static void setColor(int color, float alpha) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      RenderSystem.setShaderColor(r, g, b, alpha);
   }

   public static int applyOpacity(int color, float alpha) {
      return rgba((double)getRed(color), (double)getGreen(color), (double)getBlue(color), (double)(getAlpha(color) * alpha / 255.0F));
   }

   public static int reFactorColor(int color, float factor) {
      return rgba(
         (double)(extractRedf(color) * factor), (double)(extractGreenf(color) * factor), (double)(extractBluef(color) * factor), (double)extractAlphaf(color)
      );
   }

   public static float extractRedf(int color) {
      return (color >> 16 & 0xFF) / 255.0F;
   }

   public static int extractRed(int color) {
      return color >> 16 & 0xFF;
   }

   public static float extractBluef(int color) {
      return (color & 0xFF) / 255.0F;
   }

   public static int extractBlue(int color) {
      return color & 0xFF;
   }

   public static float extractGreenf(int color) {
      return (color >> 8 & 0xFF) / 255.0F;
   }

   public static int extractGreen(int color) {
      return color >> 8 & 0xFF;
   }

   public static float extractAlphaf(int color) {
      return (color >> 24 & 0xFF) / 255.0F;
   }

   public static int extractAlpha(int color) {
      return color >> 24 & 0xFF;
   }
}
