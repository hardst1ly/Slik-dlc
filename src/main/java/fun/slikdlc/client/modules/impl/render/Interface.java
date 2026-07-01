package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.math.HoveringUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import fun.slikdlc.client.modules.impl.render.base.implement.ArrayListHud;
import fun.slikdlc.client.modules.impl.render.base.implement.HelperBinds;
import fun.slikdlc.client.modules.impl.render.base.implement.Information;
import fun.slikdlc.client.modules.impl.render.base.implement.KeyBinds;
import fun.slikdlc.client.modules.impl.render.base.implement.KeyStrokes;
import fun.slikdlc.client.modules.impl.render.base.implement.Notifications;
import fun.slikdlc.client.modules.impl.render.base.implement.Potions;
import fun.slikdlc.client.modules.impl.render.base.implement.Session;
import fun.slikdlc.client.modules.impl.render.base.implement.StaffList;
import fun.slikdlc.client.modules.impl.render.base.implement.TargetHud;
import fun.slikdlc.client.modules.impl.render.base.implement.WaterMark;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_408;
import net.minecraft.class_4587;

public class Interface extends Module {
   public static Interface INSTANCE = new Interface();
   private static final ConcurrentHashMap<String, Long> PERF_WARNINGS = new ConcurrentHashMap<>();
   private static final boolean PERF_DEBUG = Boolean.parseBoolean(System.getProperty("slikdlc.perf.debug", "false"));
   private static final long SLOW_HUD_ELEMENT_NANOS = Long.getLong("slikdlc.perf.hudMs", 5L) * 1000000L;
   private static final long PERF_WARN_COOLDOWN_NANOS = Long.getLong("slikdlc.perf.cooldownMs", 1000L) * 1000000L;
   private final WaterMark waterMark;
   private final ArrayListHud arrayListHud;
   private final KeyBinds keyBinds;
   private final HelperBinds helperBinds;
   private final Potions potions;
   private final KeyStrokes keyStrokes;
   private final Notifications notifications;
   private final TargetHud targetHud;
   private final Session session;
   private final Information information;
   private final StaffList staffList;
   private boolean targetHudMenuOpen;
   private float targetHudMenuX;
   private float targetHudMenuY;
   private InterfaceProcessing hudContextElement;
   private InterfaceProcessing pendingHudContextElement;
   private float pendingTargetHudMenuX;
   private float pendingTargetHudMenuY;
   private final AnimationUtils targetHudMenuAnimation = new AnimationUtils(0.0F, 12.5F, Easings.CUBIC_OUT);
   private final AnimationUtils targetHudParticlesBgAnimation = new AnimationUtils(1.0F, 15.0F, Easings.CUBIC_OUT);
   private final AnimationUtils targetHudParticlesCircleAnimation = new AnimationUtils(1.0F, 8.2F, Easings.BACK_OUT);
   private final AnimationUtils targetHudBarSwitchAnimation = new AnimationUtils(0.0F, 7.0F, Easings.CUBIC_OUT);
   private final AnimationUtils waterMarkFpsBgAnimation = new AnimationUtils(1.0F, 15.0F, Easings.CUBIC_OUT);
   private final AnimationUtils waterMarkFpsCircleAnimation = new AnimationUtils(1.0F, 8.2F, Easings.BACK_OUT);
   private final AnimationUtils waterMarkMsBgAnimation = new AnimationUtils(1.0F, 15.0F, Easings.CUBIC_OUT);
   private final AnimationUtils waterMarkMsCircleAnimation = new AnimationUtils(1.0F, 8.2F, Easings.BACK_OUT);
   private final AnimationUtils waterMarkServerBgAnimation = new AnimationUtils(1.0F, 15.0F, Easings.CUBIC_OUT);
   private final AnimationUtils waterMarkServerCircleAnimation = new AnimationUtils(1.0F, 8.2F, Easings.BACK_OUT);
   private final AnimationUtils waterMarkTpsBgAnimation = new AnimationUtils(1.0F, 15.0F, Easings.CUBIC_OUT);
   private final AnimationUtils waterMarkTpsCircleAnimation = new AnimationUtils(1.0F, 8.2F, Easings.BACK_OUT);
   private final AnimationUtils hudRectTypeSwitchAnimation = new AnimationUtils(1.0F, 7.0F, Easings.CUBIC_OUT);
   private static final String HUD_HINT_TEXT = "ПКМ - по элементу для открытия настроек";
   public ModeSetting style = new ModeSetting("Стиль", "Wave", "Wave", "Обычный");
   public ModeSetting strokeStyle = new ModeSetting("Обводка", "Обычная", "Обычная", "Светящаяся");
   public ModeSetting rectStyle = new ModeSetting("Рект", "Обычный", "Обычный");
   private final ListSetting hudModules = new ListSetting(
      "Элементы",
      new BooleanSetting("Ватермарка", true),
      new BooleanSetting("Аррай лист", true),
      new BooleanSetting("Горячие клавиши", true),
      new BooleanSetting("Серверные бинды", true),
      new BooleanSetting("Зелья", true),
      new BooleanSetting("Таргет худ", true),
      new BooleanSetting("Уведомления", true),
      new BooleanSetting("Стафф", true),
      new BooleanSetting("Сессия", true).visible(() -> this.style.is("Wave")),
      new BooleanSetting("КейСтроки", true).visible(() -> this.style.is("Wave")),
      new BooleanSetting("Информация", true)
   );

   public Interface() {
      super("Interface", "Интерфейс клиента", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.hudModules, this.style, this.strokeStyle, this.rectStyle});
      this.waterMark = new WaterMark(SlikDlc.draggable(this, "WaterMark", 10.0F, 10.0F));
      this.arrayListHud = new ArrayListHud(SlikDlc.draggable(this, "ArrayList", 5.0F, 24.0F));
      this.keyBinds = new KeyBinds(SlikDlc.draggable(this, "KeyBinds", 30.0F, 30.0F));
      this.helperBinds = new HelperBinds(SlikDlc.draggable(this, "HelperBinds", 90.0F, 30.0F));
      this.potions = new Potions(SlikDlc.draggable(this, "Potions", 30.0F, 60.0F));
      this.staffList = new StaffList(SlikDlc.draggable(this, "StaffList", 60.0F, 100.0F));
      this.session = new Session(SlikDlc.draggable(this, "Session", 70.0F, 30.0F));
      this.keyStrokes = new KeyStrokes(SlikDlc.draggable(this, "KeyStrokes", 150.0F, 120.0F));
      this.information = new Information(SlikDlc.draggable(this, "Information", 50.0F, 100.0F));
      this.notifications = new Notifications(SlikDlc.draggable(this, "Notifications", 0.0F, 0.0F));
      this.targetHud = new TargetHud(SlikDlc.draggable(this, "TargetHud", 30.0F, 90.0F));
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }

   private int fadeColorSafe(int color, float progress, int minAlpha) {
      int faded = ColorUtils.applyAlpha(color, progress);
      int a = ColorUtils.getAlpha(faded);
      return a == 0 && progress > 0.001F ? ColorUtils.setAlphaColor(faded, minAlpha) : faded;
   }

   private int fadeTextAlphaSafe(float progress, int maxAlpha, int minAlpha) {
      int alpha = class_3532.method_15340((int)(maxAlpha * progress), 0, maxAlpha);
      return alpha == 0 && progress > 0.001F ? minAlpha : alpha;
   }

   private int getThemeColor() {
      return !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ColorUtils.getThemeColor();
   }

   private boolean isHudElementHovered(InterfaceProcessing element, double mouseX, double mouseY) {
      float width = element.draggable.getWidth();
      float height = element.draggable.getHeight();
      return !(width <= 1.0F) && !(height <= 1.0F)
         ? HoveringUtils.isHovered(mouseX, mouseY, element.draggable.getX(), element.draggable.getY(), width, height)
         : false;
   }

   private boolean isHudElementEnabled(InterfaceProcessing element) {
      return element != null && element.draggable.getWidth() > 1.0F && element.draggable.getHeight() > 1.0F;
   }

   private InterfaceProcessing getHoveredHudElement(double mouseX, double mouseY) {
      if (this.isHudElementEnabled(this.targetHud) && this.isHudElementHovered(this.targetHud, mouseX, mouseY)) {
         return this.targetHud;
      } else if (this.isHudElementEnabled(this.waterMark) && this.isHudElementHovered(this.waterMark, mouseX, mouseY)) {
         return this.waterMark;
      } else if (this.isHudElementEnabled(this.arrayListHud) && this.isHudElementHovered(this.arrayListHud, mouseX, mouseY)) {
         return this.arrayListHud;
      } else if (this.isHudElementEnabled(this.keyBinds) && this.isHudElementHovered(this.keyBinds, mouseX, mouseY)) {
         return this.keyBinds;
      } else if (this.isHudElementEnabled(this.helperBinds) && this.isHudElementHovered(this.helperBinds, mouseX, mouseY)) {
         return this.helperBinds;
      } else if (this.isHudElementEnabled(this.potions) && this.isHudElementHovered(this.potions, mouseX, mouseY)) {
         return this.potions;
      } else if (this.isHudElementEnabled(this.keyStrokes) && this.isHudElementHovered(this.keyStrokes, mouseX, mouseY)) {
         return this.keyStrokes;
      } else if (this.isHudElementEnabled(this.information) && this.isHudElementHovered(this.information, mouseX, mouseY)) {
         return this.information;
      } else if (this.isHudElementEnabled(this.staffList) && this.isHudElementHovered(this.staffList, mouseX, mouseY)) {
         return this.staffList;
      } else if (this.isHudElementEnabled(this.session) && this.isHudElementHovered(this.session, mouseX, mouseY)) {
         return this.session;
      } else {
         return this.isHudElementEnabled(this.notifications) && this.isHudElementHovered(this.notifications, mouseX, mouseY) ? this.notifications : null;
      }
   }

   private float getTargetHudMenuWidth() {
      return 100.0F;
   }

   private float getMenuHeightForElement(InterfaceProcessing element) {
      if (element == this.targetHud) {
         return 59.0F;
      } else {
         return element == this.waterMark ? 69.0F : 29.0F;
      }
   }

   private float getTargetHudMenuHeight() {
      return this.getMenuHeightForElement(this.hudContextElement);
   }

   private void clampTargetHudMenuToWindow(float menuWidth, float menuHeight) {
      if (mc != null && mc.method_22683() != null) {
         float maxX = Math.max(2.0F, mc.method_22683().method_4486() - menuWidth - 2.0F);
         float maxY = Math.max(2.0F, mc.method_22683().method_4502() - menuHeight - 2.0F);
         this.targetHudMenuX = class_3532.method_15363(this.targetHudMenuX, 2.0F, maxX);
         this.targetHudMenuY = class_3532.method_15363(this.targetHudMenuY, 2.0F, maxY);
      }
   }

   public boolean handleHudContextClick(double mouseX, double mouseY, int button) {
      InterfaceProcessing hoveredElement = this.getHoveredHudElement(mouseX, mouseY);
      if (button == 1 && hoveredElement != null) {
         if (this.targetHudMenuOpen && this.hudContextElement == hoveredElement) {
            this.targetHudMenuOpen = false;
            this.pendingHudContextElement = null;
         } else if (this.targetHudMenuOpen && this.hudContextElement != null && this.hudContextElement != hoveredElement) {
            this.pendingHudContextElement = hoveredElement;
            float menuWidth = this.getTargetHudMenuWidth();
            float menuHeight = this.getMenuHeightForElement(hoveredElement);
            this.pendingTargetHudMenuX = hoveredElement.draggable.getX() + hoveredElement.draggable.getWidth() + 4.0F;
            this.pendingTargetHudMenuY = hoveredElement.draggable.getY() + 1.5F;
            float saveX = this.targetHudMenuX;
            float saveY = this.targetHudMenuY;
            this.targetHudMenuX = this.pendingTargetHudMenuX;
            this.targetHudMenuY = this.pendingTargetHudMenuY;
            this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
            this.pendingTargetHudMenuX = this.targetHudMenuX;
            this.pendingTargetHudMenuY = this.targetHudMenuY;
            this.targetHudMenuX = saveX;
            this.targetHudMenuY = saveY;
            this.targetHudMenuOpen = false;
         } else {
            this.hudContextElement = hoveredElement;
            this.pendingHudContextElement = null;
            this.targetHudMenuOpen = true;
            float menuWidth = this.getTargetHudMenuWidth();
            float menuHeight = this.getTargetHudMenuHeight();
            this.targetHudMenuX = hoveredElement.draggable.getX() + hoveredElement.draggable.getWidth() + 4.0F;
            this.targetHudMenuY = hoveredElement.draggable.getY() + 1.5F;
            this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
         }

         return true;
      } else if (this.targetHudMenuOpen && this.hudContextElement != null) {
         float menuWidth = this.getTargetHudMenuWidth();
         float menuHeight = this.getTargetHudMenuHeight();
         this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
         float buttonGap = 3.0F;
         float buttonX = this.targetHudMenuX + 5.0F;
         float buttonW = (menuWidth - 10.0F - buttonGap) / 2.0F;
         float buttonH = 10.0F;
         float unusualButtonX = buttonX + buttonW + buttonGap;
         float rectButtonY = this.hudContextElement == this.targetHud
            ? this.targetHudMenuY + 46.0F
            : (this.hudContextElement == this.waterMark ? this.targetHudMenuY + 56.0F : this.targetHudMenuY + 14.0F);
         boolean menuHovered = HoveringUtils.isHovered(mouseX, mouseY, this.targetHudMenuX, this.targetHudMenuY, menuWidth, menuHeight);
         boolean rectNormalHovered = HoveringUtils.isHovered(mouseX, mouseY, buttonX, rectButtonY, buttonW, buttonH);
         boolean rectUnusualHovered = HoveringUtils.isHovered(mouseX, mouseY, unusualButtonX, rectButtonY, buttonW, buttonH);
         if (button == 0 && !menuHovered && hoveredElement == this.hudContextElement) {
            this.targetHudMenuOpen = false;
            this.pendingHudContextElement = null;
            return false;
         } else if (button == 0 && rectNormalHovered) {
            this.hudContextElement.setUnusualRectType(false);
            return true;
         } else if (button == 0 && rectUnusualHovered) {
            this.hudContextElement.setUnusualRectType(true);
            return true;
         } else {
            if (this.hudContextElement == this.targetHud) {
               float buttonY = this.targetHudMenuY + 25.0F;
               float particlesToggleX = this.targetHudMenuX + menuWidth - 21.0F;
               float particlesToggleY = this.targetHudMenuY + 4.0F;
               boolean normalHovered = HoveringUtils.isHovered(mouseX, mouseY, buttonX, buttonY, buttonW, buttonH);
               boolean unusualHovered = HoveringUtils.isHovered(mouseX, mouseY, unusualButtonX, buttonY, buttonW, buttonH);
               boolean particlesHovered = HoveringUtils.isHovered(mouseX, mouseY, particlesToggleX, particlesToggleY, 16.0, 9.0);
               if (button == 0 && normalHovered) {
                  this.targetHud.setHealthBarStyleEnabled(false);
                  return true;
               }

               if (button == 0 && unusualHovered) {
                  this.targetHud.setHealthBarStyleEnabled(true);
                  return true;
               }

               if (button == 0 && particlesHovered) {
                  this.targetHud.setHeadParticlesEnabled(!this.targetHud.isHeadParticlesEnabled());
                  return true;
               }
            } else if (this.hudContextElement == this.waterMark) {
               float baseY = this.targetHudMenuY + 4.5F;
               float toggleX = this.targetHudMenuX + menuWidth - 21.0F;
               boolean fpsHovered = HoveringUtils.isHovered(mouseX, mouseY, toggleX, baseY, 16.0, 9.0);
               boolean msHovered = HoveringUtils.isHovered(mouseX, mouseY, toggleX, baseY + 10.0F, 16.0, 9.0);
               boolean serverHovered = HoveringUtils.isHovered(mouseX, mouseY, toggleX, baseY + 20.0F, 16.0, 9.0);
               boolean tpsHovered = HoveringUtils.isHovered(mouseX, mouseY, toggleX, baseY + 30.0F, 16.0, 9.0);
               if (button == 0 && fpsHovered) {
                  this.waterMark.setShowFps(!this.waterMark.isShowFps());
                  return true;
               }

               if (button == 0 && msHovered) {
                  this.waterMark.setShowMs(!this.waterMark.isShowMs());
                  return true;
               }

               if (button == 0 && serverHovered) {
                  this.waterMark.setShowServer(!this.waterMark.isShowServer());
                  return true;
               }

               if (button == 0 && tpsHovered) {
                  this.waterMark.setShowTps(!this.waterMark.isShowTps());
                  return true;
               }
            }

            if (button == 0 || button == 1) {
               if (menuHovered) {
                  return true;
               }

               if (hoveredElement != this.hudContextElement) {
                  this.targetHudMenuOpen = false;
                  this.pendingHudContextElement = null;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public void renderHudContextMenu(class_332 context, int mouseX, int mouseY) {
      if (this.hudContextElement != null && !this.isHudElementEnabled(this.hudContextElement)) {
         this.targetHudMenuOpen = false;
         this.hudContextElement = null;
         this.pendingHudContextElement = null;
      }

      this.targetHudMenuAnimation.update(this.targetHudMenuOpen ? 1.0F : 0.0F);
      float targetMenuProgress = class_3532.method_15363(this.targetHudMenuAnimation.getValue(), 0.0F, 1.0F);
      if (!this.targetHudMenuOpen && targetMenuProgress <= 0.01F) {
         if (this.pendingHudContextElement != null) {
            this.hudContextElement = this.pendingHudContextElement;
            this.pendingHudContextElement = null;
            this.targetHudMenuX = this.pendingTargetHudMenuX;
            this.targetHudMenuY = this.pendingTargetHudMenuY;
            this.targetHudMenuOpen = true;
         } else {
            this.hudContextElement = null;
         }
      }

      if (!this.targetHudMenuOpen && targetMenuProgress <= 0.01F && this.hudContextElement == null) {
         this.hudContextElement = null;
      } else if (this.hudContextElement != null) {
         boolean targetContext = this.hudContextElement == this.targetHud;
         boolean waterMarkContext = this.hudContextElement == this.waterMark;
         boolean notificationsContext = this.hudContextElement == this.notifications;
         float menuWidth = this.getTargetHudMenuWidth();
         float menuHeight = this.getTargetHudMenuHeight();
         this.clampTargetHudMenuToWindow(menuWidth, menuHeight);
         float x = this.targetHudMenuX;
         float y = this.targetHudMenuY;
         int themeColor = this.getThemeColor();
         float contentProgress = class_3532.method_15363((targetMenuProgress - 0.06F) / 0.94F, 0.0F, 1.0F);
         int textAlpha = this.fadeTextAlphaSafe(contentProgress, 255, 2);
         class_4587 matrices = context.method_51448();
         matrices.method_22903();
         RenderUtils.drawDefaultHudPanel(
            matrices,
            x,
            y,
            menuWidth,
            menuHeight,
            3.0F,
            3.5F,
            ColorUtils.applyAlpha(ColorUtils.rgba(50, 50, 50, 255), targetMenuProgress),
            ColorUtils.applyAlpha(ColorUtils.darken(themeColor, 0.15F), targetMenuProgress),
            ColorUtils.applyAlpha(ColorUtils.darken(themeColor, 0.05F), targetMenuProgress)
         );
         if (contentProgress <= 0.02F) {
            matrices.method_22909();
         } else {
            float buttonGap = 3.0F;
            float buttonX = x + 5.0F;
            float buttonW = (menuWidth - 10.0F - buttonGap) / 2.0F;
            float buttonH = 10.0F;
            float unusualX = buttonX + buttonW + buttonGap;
            int inactiveColor = ColorUtils.applyAlpha(ColorUtils.rgba(70, 70, 70, 255), contentProgress);
            int activeLeftColor = this.fadeColorSafe(ColorUtils.darken(themeColor, 0.4F), contentProgress, 2);
            int activeRightColor = this.fadeColorSafe(themeColor, contentProgress, 2);
            if (targetContext) {
               this.issue(12)
                  .drawStringWithFade(matrices, "Партиклы с головы", x + 4.7F, y + 7.5F, menuWidth - 28.0F, ColorUtils.rgba(255, 255, 255, textAlpha));
               this.targetHudParticlesBgAnimation.update(this.targetHud.isHeadParticlesEnabled() ? 1.0F : 0.0F);
               this.targetHudParticlesCircleAnimation.update(this.targetHud.isHeadParticlesEnabled() ? 1.0F : 0.0F);
               float bgProgress = this.targetHudParticlesBgAnimation.getValue();
               float circleProgress = this.targetHudParticlesCircleAnimation.getValue();
               int particlesOffColor = ColorUtils.darken(themeColor, 0.05F);
               int particlesColor = ColorUtils.interpolateColor(particlesOffColor, themeColor, bgProgress);
               float particlesToggleX = x + menuWidth - 21.0F;
               float particlesToggleY = y + 4.5F;
               RenderUtils.drawGradientRect(
                  matrices,
                  particlesToggleX,
                  particlesToggleY,
                  16.0F,
                  9.0F,
                  3.0F,
                  this.fadeColorSafe(particlesColor, contentProgress, 2),
                  this.fadeColorSafe(ColorUtils.darken(particlesColor, 0.65F), contentProgress, 2)
               );
               float particlesCircleX = particlesToggleX + 4.5F + circleProgress * 6.2F;
               RenderUtils.drawRoundCircle(matrices, particlesCircleX + 0.5F, particlesToggleY + 4.5F, 6.85F, ColorUtils.rgba(255, 255, 255, textAlpha));
               this.issue(12)
                  .draw(matrices, "Вид полоски", x + 4.7F, y + 18.0F, ColorUtils.rgba(255, 255, 255, this.fadeTextAlphaSafe(contentProgress, 225, 2)));
               float buttonY = y + 25.0F;
               boolean healthBarStyle = this.targetHud.isHealthBarStyleEnabled();
               this.targetHudBarSwitchAnimation.update(healthBarStyle ? 1.0F : 0.0F);
               float typeSwitchProgress = class_3532.method_15363(this.targetHudBarSwitchAnimation.getValue(), 0.0F, 1.0F);
               RenderUtils.drawRoundedRect(matrices, buttonX, buttonY, buttonW, buttonH, 1.5F, inactiveColor);
               RenderUtils.drawRoundedRect(matrices, unusualX, buttonY, buttonW, buttonH, 1.5F, inactiveColor);
               float activeX = class_3532.method_16439(typeSwitchProgress, buttonX, unusualX);
               RenderUtils.drawGradientRect(matrices, activeX, buttonY, buttonW, buttonH, 1.5F, activeLeftColor, activeRightColor, true);
               String normalText = "Клиентский";
               String unusualText = "Здоровье";
               float normalTextX = buttonX + (buttonW - this.issue(12).getWidth(normalText)) * 0.5F;
               float unusualTextX = unusualX + (buttonW - this.issue(12).getWidth(unusualText)) * 0.55F;
               int normalTextAlpha = class_3532.method_15340((int)(textAlpha * (0.65F + 0.35F * (1.0F - typeSwitchProgress))), 0, 255);
               int unusualTextAlpha = class_3532.method_15340((int)(textAlpha * (0.65F + 0.35F * typeSwitchProgress)), 0, 255);
               this.issue(12).draw(matrices, normalText, normalTextX, buttonY + 3.8F, ColorUtils.rgba(255, 255, 255, normalTextAlpha));
               this.issue(12).draw(matrices, unusualText, unusualTextX, buttonY + 3.8F, ColorUtils.rgba(255, 255, 255, unusualTextAlpha));
            } else if (waterMarkContext) {
               float wmToggleX = x + menuWidth - 21.0F;
               float wmBaseY = y + 3.5F;
               float wmLabelX = x + 5.0F;
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать фпс",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY,
                  this.waterMark.isShowFps(),
                  this.waterMarkFpsBgAnimation,
                  this.waterMarkFpsCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать пинг",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY + 10.0F,
                  this.waterMark.isShowMs(),
                  this.waterMarkMsBgAnimation,
                  this.waterMarkMsCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать сервер",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY + 20.0F,
                  this.waterMark.isShowServer(),
                  this.waterMarkServerBgAnimation,
                  this.waterMarkServerCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
               this.drawWaterMarkToggle(
                  matrices,
                  "Отображать тпс",
                  wmLabelX,
                  wmToggleX,
                  wmBaseY + 30.0F,
                  this.waterMark.isShowTps(),
                  this.waterMarkTpsBgAnimation,
                  this.waterMarkTpsCircleAnimation,
                  themeColor,
                  contentProgress,
                  textAlpha
               );
            } else if (notificationsContext) {
               float ntToggleX = x + menuWidth - 21.0F;
               float ntBaseY = y + 3.5F;
               float var49 = x + 5.0F;
            }

            this.issue(12)
               .draw(
                  matrices,
                  "Тип ректа",
                  x + 4.7F,
                  targetContext ? y + 39.5F : (waterMarkContext ? y + 49.5F : y + 7.5F),
                  ColorUtils.rgba(255, 255, 255, this.fadeTextAlphaSafe(contentProgress, 225, 2))
               );
            float rectButtonY = targetContext ? y + 46.0F : (waterMarkContext ? y + 56.0F : y + 14.0F);
            boolean unusualRect = this.hudContextElement.isUnusualRectType();
            this.hudRectTypeSwitchAnimation.update(unusualRect ? 1.0F : 0.0F);
            float rectSwitchProgress = class_3532.method_15363(this.hudRectTypeSwitchAnimation.getValue(), 0.0F, 1.0F);
            RenderUtils.drawRoundedRect(matrices, buttonX, rectButtonY, buttonW, buttonH, 1.5F, inactiveColor);
            RenderUtils.drawRoundedRect(matrices, unusualX, rectButtonY, buttonW, buttonH, 1.5F, inactiveColor);
            float rectActiveX = class_3532.method_16439(rectSwitchProgress, buttonX, unusualX);
            RenderUtils.drawGradientRect(matrices, rectActiveX, rectButtonY, buttonW, buttonH, 1.5F, activeLeftColor, activeRightColor, true);
            String normalRectText = "Обычный";
            String unusualRectText = "Необычный";
            float normalRectTextX = buttonX + (buttonW - this.issue(12).getWidth(normalRectText)) * 0.5F;
            float unusualRectTextX = unusualX + (buttonW - this.issue(12).getWidth(unusualRectText)) * 0.5F;
            int normalRectAlpha = class_3532.method_15340((int)(textAlpha * (0.65F + 0.35F * (1.0F - rectSwitchProgress))), 0, 255);
            int unusualRectAlpha = class_3532.method_15340((int)(textAlpha * (0.65F + 0.35F * rectSwitchProgress)), 0, 255);
            this.issue(12).draw(matrices, normalRectText, normalRectTextX, rectButtonY + 3.8F, ColorUtils.rgba(255, 255, 255, normalRectAlpha));
            this.issue(12).draw(matrices, unusualRectText, unusualRectTextX, rectButtonY + 3.8F, ColorUtils.rgba(255, 255, 255, unusualRectAlpha));
            matrices.method_22909();
         }
      }
   }

   private void renderHudElement(InterfaceProcessing element, EventRender.Default event) {
      long start = PERF_DEBUG ? System.nanoTime() : 0L;
      element.draggable.beginRenderTilt(event.getContext().method_51448());

      try {
         element.onRender(event);
      } finally {
         element.draggable.endRenderTilt(event.getContext().method_51448());
         if (PERF_DEBUG) {
            long elapsed = System.nanoTime() - start;
            if (elapsed >= SLOW_HUD_ELEMENT_NANOS) {
               this.logSlowHudElement(element, elapsed);
            }
         }
      }
   }

   private void logSlowHudElement(InterfaceProcessing element, long elapsedNanos) {
      String name = element.getClass().getSimpleName();
      long now = System.nanoTime();
      Long lastWarn = PERF_WARNINGS.get(name);
      if (lastWarn == null || now - lastWarn >= PERF_WARN_COOLDOWN_NANOS) {
         PERF_WARNINGS.put(name, now);
         System.out.println(String.format(Locale.ROOT, "[PerfDebug] Slow HUD element: Interface -> %s took %.2f ms", name, elapsedNanos / 1000000.0));
      }
   }

   private void drawWaterMarkToggle(
      class_4587 matrices,
      String label,
      float labelX,
      float toggleX,
      float toggleY,
      boolean enabled,
      AnimationUtils bgAnimation,
      AnimationUtils circleAnimation,
      int themeColor,
      float contentProgress,
      int textAlpha
   ) {
      this.issue(12).draw(matrices, label, labelX, toggleY + 3.0F, ColorUtils.rgba(255, 255, 255, textAlpha));
      bgAnimation.update(enabled ? 1.0F : 0.0F);
      circleAnimation.update(enabled ? 1.0F : 0.0F);
      float bgProgress = bgAnimation.getValue();
      float circleProgress = circleAnimation.getValue();
      int offColor = ColorUtils.darken(themeColor, 0.05F);
      int toggleColor = ColorUtils.interpolateColor(offColor, themeColor, bgProgress);
      RenderUtils.drawGradientRect(
         matrices,
         toggleX,
         toggleY,
         16.0F,
         9.0F,
         3.0F,
         this.fadeColorSafe(toggleColor, contentProgress, 2),
         this.fadeColorSafe(ColorUtils.darken(toggleColor, 0.65F), contentProgress, 2)
      );
      float circleX = toggleX + 4.5F + circleProgress * 6.2F;
      RenderUtils.drawRoundCircle(matrices, circleX + 0.5F, toggleY + 4.5F, 6.85F, ColorUtils.rgba(255, 255, 255, textAlpha));
   }

   public Map<String, InterfaceProcessing> getConfigurableHudElements() {
      Map<String, InterfaceProcessing> elements = new LinkedHashMap<>();
      elements.put("waterMark", this.waterMark);
      elements.put("arrayList", this.arrayListHud);
      elements.put("keyBinds", this.keyBinds);
      elements.put("helperBinds", this.helperBinds);
      elements.put("potions", this.potions);
      elements.put("keyStrokes", this.keyStrokes);
      elements.put("notifications", this.notifications);
      elements.put("targetHud", this.targetHud);
      elements.put("session", this.session);
      elements.put("information", this.information);
      elements.put("staffList", this.staffList);
      return elements;
   }

   @EventLink(
      priority = -200
   )
   public void onEvent(EventRender.Default event) {
      boolean waveStyle = this.style.is("Wave");
      boolean showWaterMark = this.hudModules.is("Ватермарка");
      boolean showArrayList = this.hudModules.is("Аррай лист");
      boolean showKeyBinds = this.hudModules.is("Горячие клавиши");
      boolean showHelperBinds = this.hudModules.is("Серверные бинды");
      boolean showPotions = this.hudModules.is("Зелья");
      boolean showKeyStrokes = this.hudModules.is("КейСтроки");
      boolean showInformation = this.hudModules.is("Информация");
      boolean showStaff = this.hudModules.is("Стафф");
      boolean showSession = this.hudModules.is("Сессия");
      boolean showNotifications = this.hudModules.is("Уведомления");
      boolean showTargetHud = this.hudModules.is("Таргет худ");
      if (mc != null && mc.method_22683() != null && mc.field_1755 instanceof class_408) {
         Font hintFont = this.issue(18);
         float x = mc.method_22683().method_4486() * 0.5F - hintFont.getWidth("ПКМ - по элементу для открытия настроек") * 0.5F;
         hintFont.draw(event.getContext().method_51448(), "ПКМ - по элементу для открытия настроек", x, 40.0F, -1);
      }

      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);

      try {
         if (showWaterMark) {
            this.renderHudElement(this.waterMark, event);
         }

         if (showArrayList && waveStyle) {
            this.renderHudElement(this.arrayListHud, event);
         }

         if (showKeyBinds) {
            this.renderHudElement(this.keyBinds, event);
         }

         if (showHelperBinds) {
            this.renderHudElement(this.helperBinds, event);
         }

         if (showPotions) {
            this.renderHudElement(this.potions, event);
         }

         if (showKeyStrokes && waveStyle) {
            this.renderHudElement(this.keyStrokes, event);
         }

         if (showInformation) {
            this.renderHudElement(this.information, event);
         }

         if (showStaff) {
            this.renderHudElement(this.staffList, event);
         }

         if (showSession && waveStyle) {
            this.renderHudElement(this.session, event);
         }

         if (showNotifications) {
            this.renderHudElement(this.notifications, event);
         }

         if (showTargetHud) {
            this.renderHudElement(this.targetHud, event);
         }
      } finally {
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
      }

      if (!(mc.field_1755 instanceof class_408)) {
         this.targetHudMenuOpen = false;
         this.pendingHudContextElement = null;
      }
   }
}
