package fun.slikdlc.client.modules.impl.render.base.implement;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.client.UserInfo;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import java.awt.Color;
import java.util.Arrays;
import net.minecraft.class_4587;
import net.minecraft.class_640;
import net.minecraft.class_642;

public class WaterMark extends InterfaceProcessing {
   private boolean showFps = true;
   private boolean showMs = true;
   private boolean showServer = true;
   private boolean showTps = true;

   public static String getUsername() {
      UserInfo userInfo = SlikDlc.INSTANCE.getUserInfo();
      return userInfo == null ? "Developer" : userInfo.getUsername();
   }

   public static String getUID() {
      return "1";
   }

   public WaterMark(Draggable draggable) {
      super(draggable);
   }

   public boolean isShowFps() {
      return this.showFps;
   }

   public void setShowFps(boolean showFps) {
      this.showFps = showFps;
   }

   public boolean isShowMs() {
      return this.showMs;
   }

   public void setShowMs(boolean showMs) {
      this.showMs = showMs;
   }

   public boolean isShowServer() {
      return this.showServer;
   }

   public void setShowServer(boolean showServer) {
      this.showServer = showServer;
   }

   public boolean isShowTps() {
      return this.showTps;
   }

   public void setShowTps(boolean showTps) {
      this.showTps = showTps;
   }

   @Override
   public void onRender(EventRender.Default eventRender) {
      if (ModuleClass.interfaceModule.style.is("Wave")) {
         this.WaveStyle(eventRender);
      } else {
         this.DefaultStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   public void DefaultStyle(EventRender.Default eventRender) {
      class_4587 matrices = eventRender.getContext().method_51448();
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      Font logoFont = Fonts.getFont("iconnew", 17);
      Font iconNew14 = Fonts.getFont("icon", 14);
      Font iconNew15 = Fonts.getFont("icon", 15);
      Font icon14 = Fonts.getFont("icon", 14);
      Font statsIconFont = Fonts.getFont("slikdlc", 14);
      if (statsIconFont == null) {
         statsIconFont = iconNew14 != null ? iconNew14 : icon14;
      }

      Font suisse13 = Fonts.getFont("suisse", 13);
      float slikdlcRectH = 16.0F;
      int iconSize = 17;
      String iconGlyph = "g";
      float iconW = logoFont.getStringWidth(iconGlyph);
      float iconX = x + (17.0F - iconW) / 2.0F;
      float iconY = y + 5.5F;
      int iconTop;
      if (!SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
         iconTop = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0];
      } else {
         iconTop = ColorUtils.getThemeColor();
      }

      boolean drawSquares = this.isUnusualRectType();
      float rect2Pad = 3.0F;
      String username = getUsername();
      String UID = getUID();
      int whiteColor = new Color(255, 255, 255, 255).getRGB();
      float textY = y + 6.8F;
      String brandText = "Slik dlc";
      float brandTextX = iconX + iconW + 2.5F;
      float brandTextW = suisse13.getStringWidth(brandText);
      float slikdlcRectW = brandTextX + brandTextW + 1.5F - x;
      RenderUtils.drawDefaultHudThemedPanelWithStroke(
         matrices, x, y, slikdlcRectW, slikdlcRectH, 2.8F, 3.3F, iconTop, ModuleClass.interfaceModule.strokeStyle.getCurrent()
      );
      if (drawSquares) {
      }

      int logoShadow = ColorUtils.applyAlpha(iconTop, 0.32F);
      RenderUtils.drawShadow(matrices, iconX + 0.3F, iconY - 1.25F, iconW - 1.0F, iconSize - 11, 3.0F, 5.0F, logoShadow);
      logoFont.drawGradientStringHorizontal(matrices, iconGlyph, iconX - 0.25F, iconY, iconTop, iconTop);
      suisse13.drawString(matrices, brandText, brandTextX, textY, whiteColor);
      float rect2X = x + slikdlcRectW + 2.5F;
      float rect2H = 15.85F;
      int icon2Size = 14;
      String iconGlyph2 = "e";
      float icon2Y = y + 7.45F;
      int icon3Size = 14;
      String fpsIconGlyph = "j";
      String pingIconGlyph = "f";
      float icon3Y = y + 7.25F;
      int fps = mc != null ? mc.method_47599() : 0;
      String fpsValue = String.valueOf(fps);
      String fpsSuffix = "fps";
      String fpsText = fpsValue + fpsSuffix;
      int ping = 0;
      if (mc != null && mc.field_1724 != null && mc.method_1562() != null) {
         class_640 entry = mc.method_1562().method_2871(mc.field_1724.method_5667());
         if (entry != null) {
            ping = entry.method_2959();
         }
      }

      String pingValue = String.valueOf(ping);
      String pingSuffix = "ms";
      String pingText = pingValue + pingSuffix;
      float contentW = rect2Pad + (iconNew14.getStringWidth(iconGlyph2) + 1.0F);
      if (!username.isEmpty()) {
         contentW += suisse13.getStringWidth(username) + 2.0F;
      }

      if (this.showFps) {
         contentW += statsIconFont != null ? statsIconFont.getStringWidth(fpsIconGlyph) + 2.0F : 0.0F;
         contentW += suisse13.getStringWidth(fpsText) + 2.0F;
      }

      if (this.showMs) {
         contentW += statsIconFont != null ? statsIconFont.getStringWidth(pingIconGlyph) + 2.0F : 0.0F;
         contentW += suisse13.getStringWidth(pingText) + 2.0F;
      }

      contentW += rect2Pad;
      float rect2W = contentW - 1.05F;
      RenderUtils.drawDefaultHudThemedPanelWithStroke(
         matrices, rect2X, y, rect2W, rect2H, 2.8F, 3.3F, iconTop, ModuleClass.interfaceModule.strokeStyle.getCurrent()
      );
      if (drawSquares) {
         RenderUtils.drawHudSquarePattern(matrices, rect2X, y, rect2W, rect2H, iconTop);
      }

      float drawX = rect2X + rect2Pad + 1.5F;
      iconNew14.drawGradientStringHorizontal(matrices, iconGlyph2, drawX - 1.0F, icon2Y, iconTop, iconTop);
      drawX += iconNew14.getStringWidth(iconGlyph2) + 1.0F;
      if (!username.isEmpty()) {
         suisse13.drawString(matrices, username, drawX, textY, whiteColor);
         drawX += suisse13.getStringWidth(username) + 2.0F;
      }

      if (this.showFps) {
         if (statsIconFont != null) {
            statsIconFont.drawGradientStringHorizontal(matrices, fpsIconGlyph, drawX, icon3Y, iconTop, iconTop);
            drawX += statsIconFont.getStringWidth(fpsIconGlyph) + 2.0F;
         }

         suisse13.drawString(matrices, fpsValue, drawX, textY, whiteColor);
         suisse13.drawString(matrices, fpsSuffix, drawX + suisse13.getStringWidth(fpsValue) - 1.0F, textY, iconTop);
         drawX += suisse13.getStringWidth(fpsText) + 2.0F;
      }

      if (this.showMs) {
         if (statsIconFont != null) {
            statsIconFont.drawGradientStringHorizontal(matrices, pingIconGlyph, drawX, icon3Y, iconTop, iconTop);
            drawX += statsIconFont.getStringWidth(pingIconGlyph) + 2.0F;
         }

         suisse13.drawString(matrices, pingValue, drawX, textY, whiteColor);
         suisse13.drawString(matrices, pingSuffix, drawX + suisse13.getStringWidth(pingValue) - 0.5, (double)textY, iconTop);
      }

      String serverName = "Singleplayer";
      if (mc != null) {
         class_642 info = mc.method_1558();
         if (info != null && info.field_3761 != null && !info.field_3761.isEmpty()) {
            serverName = info.field_3761;
         }
      }

      boolean showBottom = this.showServer || this.showTps;
      float rectBtmY = y + slikdlcRectH + 2.0F;
      float rectBtmH = 15.85F;
      int iconSmallSize = 15;
      float iconSmallW = iconNew15.getStringWidth(iconGlyph);
      float iconSmallY = rectBtmY + (rectBtmH - iconSmallSize) / 2.0F + 6.5F;
      float serverTextY = rectBtmY + (rectBtmH - 12.0F) / 2.0F + 4.8F;
      String serverDisplayName = this.formatServerNameForDisplay(serverName);
      float serverTextW = suisse13.getStringWidth(serverDisplayName);
      int extraIconSize = 15;
      String extraIconGlyph = "y";
      float extraIconW = iconNew15.getStringWidth(extraIconGlyph);
      float extraIconY = rectBtmY + (rectBtmH - extraIconSize) / 2.0F + 6.4F;
      String tpsValue = this.formatOneDecimal(this.getServerTps());
      String tpsSuffix = "tps";
      String tpsText = tpsValue + tpsSuffix;
      float tpsTextW = suisse13.getStringWidth(tpsText);
      float rectBtmW = 0.0F;
      if (showBottom) {
         float bottomX = x + rect2Pad + 8.5F;
         if (this.showServer) {
            bottomX += iconSmallW + 3.0F + serverTextW;
         }

         if (this.showTps) {
            if (this.showServer) {
               bottomX += 3.0F;
            }

            bottomX += extraIconW + 3.0F + tpsTextW;
         }

         rectBtmW = Math.max(40.0F, bottomX + rect2Pad - x);
         RenderUtils.drawDefaultHudThemedPanelWithStroke(
            matrices, x, rectBtmY, rectBtmW - 2.85F, rectBtmH, 2.8F, 3.3F, iconTop, ModuleClass.interfaceModule.strokeStyle.getCurrent()
         );
         if (drawSquares) {
            RenderUtils.drawHudSquarePattern(matrices, x, rectBtmY, rectBtmW, rectBtmH, iconTop);
         }

         float drawBottomX = x + rect2Pad + 7.0F;
         if (this.showServer) {
            iconNew15.drawGradientStringHorizontal(matrices, "n", drawBottomX - 6.5F, iconSmallY, iconTop, iconTop);
            drawBottomX += iconSmallW + 3.0F;
            this.drawServerNameWithThemeParts(matrices, serverDisplayName, drawBottomX, serverTextY, iconTop, whiteColor);
            drawBottomX += serverTextW;
         }

         if (this.showTps) {
            if (this.showServer) {
               drawBottomX += 3.0F;
            }

            iconNew15.drawGradientStringHorizontal(matrices, extraIconGlyph, drawBottomX - 1.5F, extraIconY, iconTop, iconTop);
            drawBottomX += extraIconW + 3.0F;
            suisse13.drawString(matrices, tpsValue, drawBottomX - 1.75F, serverTextY, whiteColor);
            suisse13.drawString(matrices, tpsSuffix, drawBottomX + suisse13.getStringWidth(tpsValue) - 2.5F, serverTextY, iconTop);
         }
      }

      float totalW = Math.max(slikdlcRectW + 2.0F + rect2W, rectBtmW);
      this.draggable.setWidth(totalW);
      this.draggable.setHeight(showBottom ? slikdlcRectH + 1.0F + rectBtmH : slikdlcRectH);
   }

   public void WaveStyle(EventRender.Default eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      class_4587 matrices = eventRender.getContext().method_51448();
      Font waveFont = Fonts.getFont("wave", 30);
      String watermarkText = "Slik dlc";
      int indexColor = ColorUtils.getThemeColor(90);
      int indexColor2 = ColorUtils.getThemeColor(180);
      int indexColor3 = ColorUtils.getThemeColor(270);
      int indexColor4 = ColorUtils.getColor(360);
      float glowWidth = 95.0F + waveFont.getStringWidth("ful");
      RenderUtils.drawShadow(matrices, x, y, glowWidth, 12.0F, 10.0F, 15.0F, indexColor4, indexColor2, indexColor, indexColor3);
      waveFont.drawGradientStringHorizontal(matrices, watermarkText, x, y, indexColor, indexColor2);
      this.draggable.setWidth(Math.max(glowWidth, waveFont.getStringWidth(watermarkText)));
      this.draggable.setHeight(12.0F);
   }

   private void drawServerNameWithThemeParts(class_4587 matrices, String serverName, float x, float y, int themeColor, int whiteColor) {
      Font font = Fonts.getFont("suisse", 13);
      String[] parts = serverName.split("\\.");
      if (parts.length < 2) {
         font.drawString(matrices, serverName, x, y, whiteColor);
      } else {
         String mainPart = String.join(".", Arrays.copyOf(parts, parts.length - 1));
         String suffixPart = "." + parts[parts.length - 1];
         font.drawString(matrices, mainPart, x, y, whiteColor);
         float suffixX = x + font.getStringWidth(mainPart) - 2.0F;
         font.drawString(matrices, suffixPart, suffixX, y, themeColor);
      }
   }

   private String formatServerNameForDisplay(String serverName) {
      if (serverName != null && !serverName.isEmpty()) {
         String host = serverName;
         int portIndex = serverName.indexOf(58);
         if (portIndex > 0) {
            host = serverName.substring(0, portIndex);
         }

         String[] parts = host.split("\\.");
         return parts.length >= 3 ? String.join(".", Arrays.copyOfRange(parts, 1, parts.length)) : host;
      } else {
         return "";
      }
   }

   private float getServerTps() {
      return SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.tpsCalc != null ? Math.max(0.0F, Math.min(20.0F, SlikDlc.INSTANCE.tpsCalc.getTPS())) : 20.0F;
   }

   private String formatOneDecimal(float value) {
      int scaled = Math.round(value * 10.0F);
      return scaled / 10 + "." + Math.abs(scaled % 10);
   }
}
