package fun.slikdlc.client.modules.impl.render.base.implement;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_276;
import net.minecraft.class_3532;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import net.minecraft.class_6367;

public class TargetHud extends InterfaceProcessing {
   private final AnimationUtils alphaAnimation = new AnimationUtils(0.0F, 9.0F, Easings.QUAD_OUT);
   private final AnimationUtils unusualRevealAnimation = new AnimationUtils(0.0F, 4.6F, Easings.QUAD_OUT);
   private final AnimationUtils hpAnimation = new AnimationUtils(1.0F, 9.2F, Easings.QUAD_OUT);
   private final AnimationUtils hpTrailAnimation = new AnimationUtils(1.0F, 7.4F, Easings.QUAD_OUT);
   private final AnimationUtils hpValueAnimation = new AnimationUtils(20.0F, 7.0F, Easings.QUAD_OUT);
   private final AnimationUtils ABValueAnimation = new AnimationUtils(0.0F, 7.0F, Easings.QUAD_OUT);
   private final AnimationUtils goldenHpAnimation = new AnimationUtils(0.0F, 9.2F, Easings.QUAD_OUT);
   private final AnimationUtils goldenHpTrailAnimation = new AnimationUtils(0.0F, 7.4F, Easings.QUAD_OUT);
   private final AnimationUtils goldenAlphaAnimation = new AnimationUtils(0.0F, 9.0F, Easings.QUAD_OUT);
   private final List<TargetHud.HeadParticle> headParticles = new ObjectArrayList();
   private class_1309 lastTarget;
   private float maxAbsorption = 20.0F;
   private boolean headParticlesEnabled = true;
   private boolean healthBarStyleEnabled = false;
   private long lastParticleUpdateNs = System.nanoTime();
   private class_1309 particleTarget;
   private int lastTargetHurtTime = 0;
   private int cachedBarThemeColor = ColorUtils.rgba(124, 91, 242, 255);
   private int cachedBarThemeColor2 = ColorUtils.rgba(93, 67, 175, 255);
   private class_276 burnBuffer;
   private int burnBufferWidth = -1;
   private int burnBufferHeight = -1;
   private final class_1799[] displayItems = new class_1799[6];
   private final class_1799[] armorScratch = new class_1799[4];
   private final AnimationUtils scaleAnimation = new AnimationUtils(0.0F, 12.0F, Easings.BACK_OUT);
   private final AnimationUtils hideScaleAnimation = new AnimationUtils(1.0F, 8.0F, Easings.BACK_IN);
   private boolean wasVisible = false;

   public TargetHud(Draggable draggable) {
      super(draggable);
   }

   private Font issue(int size) {
      return Fonts.getFont("suisse", size);
   }

   public boolean isHeadParticlesEnabled() {
      return this.headParticlesEnabled;
   }

   public void setHeadParticlesEnabled(boolean headParticlesEnabled) {
      this.headParticlesEnabled = headParticlesEnabled;
      if (!headParticlesEnabled) {
         this.headParticles.clear();
      }
   }

   public boolean isHealthBarStyleEnabled() {
      return this.healthBarStyleEnabled;
   }

   public void setHealthBarStyleEnabled(boolean healthBarStyleEnabled) {
      this.healthBarStyleEnabled = healthBarStyleEnabled;
   }

   private int interpolateColor(int color1, int color2, float ratio) {
      ratio = class_3532.method_15363(ratio, 0.0F, 1.0F);
      int r1 = color1 >> 16 & 0xFF;
      int g1 = color1 >> 8 & 0xFF;
      int b1 = color1 & 0xFF;
      int a1 = color1 >> 24 & 0xFF;
      int r2 = color2 >> 16 & 0xFF;
      int g2 = color2 >> 8 & 0xFF;
      int b2 = color2 & 0xFF;
      int a2 = color2 >> 24 & 0xFF;
      int r = (int)(r1 + (r2 - r1) * ratio);
      int g = (int)(g1 + (g2 - g1) * ratio);
      int b = (int)(b1 + (b2 - b1) * ratio);
      int a = (int)(a1 + (a2 - a1) * ratio);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private void ensureBurnBuffer() {
      if (mc != null && mc.method_22683() != null) {
         int w = mc.method_22683().method_4489();
         int h = mc.method_22683().method_4506();
         if (this.burnBuffer == null || this.burnBufferWidth != w || this.burnBufferHeight != h) {
            if (this.burnBuffer != null) {
               this.burnBuffer.method_1238();
            }

            this.burnBuffer = new class_6367(w, h, true);
            this.burnBufferWidth = w;
            this.burnBufferHeight = h;
         }
      }
   }

   private int collectDisplayItems(class_1309 target) {
      int armorCount = 0;

      for (class_1799 stack : target.method_5661()) {
         if (armorCount < this.armorScratch.length) {
            this.armorScratch[armorCount++] = stack;
         }
      }

      int count = 0;

      for (int i = armorCount - 1; i >= 0; i--) {
         this.displayItems[count++] = this.armorScratch[i];
         this.armorScratch[i] = class_1799.field_8037;
      }

      class_1799 mainHand = target.method_6047();
      if (!mainHand.method_7960()) {
         this.displayItems[count++] = mainHand;
      }

      class_1799 offHand = target.method_6079();
      if (!offHand.method_7960()) {
         this.displayItems[count++] = offHand;
      }

      return count;
   }

   private boolean beginBurnPassIfNeeded(boolean unusualAnimation) {
      if (!unusualAnimation) {
         return false;
      } else {
         this.ensureBurnBuffer();
         if (this.burnBuffer == null) {
            return false;
         } else {
            this.burnBuffer.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
            this.burnBuffer.method_1230();
            this.burnBuffer.method_1235(false);
            return true;
         }
      }
   }

   private void applyHudTransform(class_4587 matrices, float x, float y, float width, float height, float scaleX, float scaleY) {
      float centerX = x + width * 0.5F;
      float centerY = y + height * 0.5F;
      matrices.method_46416(centerX, centerY, 0.0F);
      matrices.method_22905(scaleX, scaleY, 1.0F);
      matrices.method_46416(-centerX, -centerY, 0.0F);
   }

   private void updateAndRenderHeadParticles(class_4587 matrices, class_1309 target, float headX, float headY, float headSize, float alpha, int themeColor) {
      if (target != null && !(alpha <= 0.02F)) {
         long now = System.nanoTime();
         float deltaTicks = class_3532.method_15363((float)(now - this.lastParticleUpdateNs) / 1.0E9F * 60.0F, 0.2F, 3.0F);
         this.lastParticleUpdateNs = now;
         if (this.particleTarget != target) {
            this.headParticles.clear();
            this.particleTarget = target;
            this.lastTargetHurtTime = Math.max(0, target.field_6235);
         }

         ThreadLocalRandom random = ThreadLocalRandom.current();
         float centerX = headX + headSize * 0.5F;
         float centerY = headY + headSize * 0.5F;
         int hurtTime = Math.max(0, target.field_6235);
         boolean spawnBurst = hurtTime > 0 && (hurtTime > this.lastTargetHurtTime || hurtTime % 3 == 0);
         this.lastTargetHurtTime = hurtTime;
         if (spawnBurst) {
            int burstCount = 1 + random.nextInt(2);

            for (int n = 0; n < burstCount && this.headParticles.size() < 14; n++) {
               float angle = (float)(random.nextDouble() * Math.PI * 2.0);
               float radius = random.nextFloat() * headSize * 0.24F;
               float spreadAngle = (float)(random.nextDouble() * Math.PI * 2.0);
               float speed = 0.58F + random.nextFloat() * 0.9F;
               TargetHud.HeadParticle p = new TargetHud.HeadParticle();
               p.x = centerX + class_3532.method_15362(angle) * radius;
               p.y = centerY + class_3532.method_15374(angle) * radius;
               p.vx = class_3532.method_15362(spreadAngle) * speed + (p.x - centerX) * 0.025F;
               p.vy = class_3532.method_15374(spreadAngle) * speed + (p.y - centerY) * 0.025F;
               p.size = 3.8F + random.nextFloat() * 1.4F;
               p.age = 0.0F;
               p.maxAge = 74.0F + random.nextFloat() * 42.0F;
               this.headParticles.add(p);
            }
         }

         for (int i = this.headParticles.size() - 1; i >= 0; i--) {
            TargetHud.HeadParticle p = this.headParticles.get(i);
            p.age += deltaTicks;
            if (p.age >= p.maxAge) {
               this.headParticles.remove(i);
            } else {
               p.x = p.x + p.vx * deltaTicks;
               p.y = p.y + p.vy * deltaTicks;
               p.vx = p.vx * (float)Math.pow(0.975F, deltaTicks);
               p.vy = p.vy * (float)Math.pow(0.975F, deltaTicks);
               p.vy += 0.0012F * deltaTicks;
               float life = 1.0F - p.age / p.maxAge;
               float smoothLife = life * life * (3.0F - 2.0F * life);
               float particleAlpha = alpha * smoothLife;
               if (!(particleAlpha <= 0.02F)) {
                  float drawX = p.x - p.size * 0.5F;
                  float drawY = p.y - p.size * 0.5F;
                  int coreColor = ColorUtils.applyAlpha(themeColor, particleAlpha * 0.58F);
                  RenderUtils.drawRoundedRect(matrices, drawX, drawY, p.size, p.size, p.size * 0.45F, coreColor);
               }
            }
         }
      } else {
         this.headParticles.clear();
         this.particleTarget = target;
         this.lastTargetHurtTime = 0;
      }
   }

   private void drawTargetHudItem(EventRender.Default eventRender, class_4587 matrices, class_1799 stack, float slotX, float slotY, float itemScale) {
      if (!stack.method_7960()) {
         matrices.method_22903();
         matrices.method_46416(slotX + 4.0F, slotY + 4.0F, 0.0F);
         matrices.method_22905(itemScale, itemScale, 1.0F);
         matrices.method_46416(-4.0F, -4.0F, 0.0F);
         eventRender.getContext().method_51427(stack, 0, 0);
         matrices.method_22909();
      }
   }

   @Override
   public void onRender(EventRender.Default eventRender) {
      if (!ModuleClass.interfaceModule.style.is("Wave")) {
         this.DefaultStyle(eventRender);
      } else {
         this.WaveStyle(eventRender);
      }

      super.onRender(eventRender);
   }

   public void DefaultStyle(EventRender.Default eventRender) {
      if (mc.field_1724 == null) {
         this.headParticles.clear();
         this.lastTargetHurtTime = 0;
         this.draggable.setWidth(0.0F);
         this.draggable.setHeight(0.0F);
      } else {
         Aura aura = ModuleClass.aura;
         boolean chatOpen = mc.field_1755 instanceof class_408;
         class_1309 auraTarget = aura != null ? aura.getTarget() : null;
         boolean showTargetHud = chatOpen || auraTarget != null;
         this.alphaAnimation.setSpeed(showTargetHud ? 9.0F : 5.0F);
         this.alphaAnimation.update(showTargetHud ? 1.0F : 0.0F);
         float alpha = class_3532.method_15363(this.alphaAnimation.getValue(), 0.0F, 1.0F);
         if (showTargetHud) {
            this.lastTarget = (class_1309)(chatOpen ? mc.field_1724 : auraTarget);
         }

         class_1309 target = (class_1309)(showTargetHud ? (chatOpen ? mc.field_1724 : auraTarget) : this.lastTarget);
         if (target != null && !(alpha <= 0.01F)) {
            float currentAbsorption = target.method_6067();
            boolean hasAbsorption = currentAbsorption > 0.0F;
            this.goldenAlphaAnimation.setSpeed(hasAbsorption ? 9.0F : 5.0F);
            this.goldenAlphaAnimation.update(hasAbsorption ? 1.0F : 0.0F);
            float goldenAlpha = class_3532.method_15363(this.goldenAlphaAnimation.getValue(), 0.0F, 1.0F);
            float x = this.draggable.getX();
            float y = this.draggable.getY();
            int colorTheme;
            if (!SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
               colorTheme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0];
            } else {
               colorTheme = ColorUtils.getThemeColor();
            }

            int colorTheme2;
            if (!SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
               colorTheme2 = ColorUtils.darken(SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0], 0.4F);
            } else {
               colorTheme2 = ColorUtils.getThemeColor();
            }

            if (showTargetHud) {
               this.cachedBarThemeColor = colorTheme;
               this.cachedBarThemeColor2 = colorTheme2;
            }

            int barThemeColor = showTargetHud ? colorTheme : this.cachedBarThemeColor;
            int barThemeColor2 = showTargetHud ? colorTheme2 : this.cachedBarThemeColor2;
            float maxHealth = Math.max(1.0F, target.method_6063());
            float maxAB = Math.max(1.0F, this.maxAbsorption);
            float targetHealthForAnim = showTargetHud ? target.method_6032() : 0.0F;
            float targetABForAnim = showTargetHud ? currentAbsorption : 0.0F;
            this.hpValueAnimation.update(targetHealthForAnim);
            this.ABValueAnimation.update(targetABForAnim);
            float animatedHealthValue = class_3532.method_15363(this.hpValueAnimation.getValue(), 0.0F, maxHealth);
            float animatedABValue = class_3532.method_15363(this.ABValueAnimation.getValue(), 0.0F, maxAB);
            float healthProgress = class_3532.method_15363(targetHealthForAnim / maxHealth, 0.0F, 1.0F);
            this.hpAnimation.update(healthProgress);
            float hpProgressAnimated = class_3532.method_15363(this.hpAnimation.getValue(), 0.0F, 1.0F);
            if (hpProgressAnimated > this.hpTrailAnimation.getValue()) {
               this.hpTrailAnimation.setValue(class_3532.method_16439(0.78F, this.hpTrailAnimation.getValue(), hpProgressAnimated));
            } else {
               this.hpTrailAnimation.update(hpProgressAnimated);
            }

            float hpTrailProgressAnimated = class_3532.method_15363(this.hpTrailAnimation.getValue(), 0.0F, 1.0F);
            boolean hidingHud = !showTargetHud;
            if (hidingHud) {
               hpTrailProgressAnimated = hpProgressAnimated;
            }

            float absorptionProgress = class_3532.method_15363(currentAbsorption / maxAB, 0.0F, 1.0F);
            this.goldenHpAnimation.update(absorptionProgress);
            float goldenProgressAnimated = class_3532.method_15363(this.goldenHpAnimation.getValue(), 0.0F, 1.0F);
            if (goldenProgressAnimated > this.goldenHpTrailAnimation.getValue()) {
               this.goldenHpTrailAnimation.setValue(class_3532.method_16439(0.78F, this.goldenHpTrailAnimation.getValue(), goldenProgressAnimated));
            } else {
               this.goldenHpTrailAnimation.update(goldenProgressAnimated);
            }

            float goldenTrailProgressAnimated = class_3532.method_15363(this.goldenHpTrailAnimation.getValue(), 0.0F, 1.0F);
            if (hidingHud || !hasAbsorption) {
               goldenTrailProgressAnimated = goldenProgressAnimated;
            }

            String name = target.method_5477().getString();
            String hpText = "HP: " + String.format("%.1f", animatedHealthValue);
            String abText = " (" + String.format("%.1f", animatedABValue) + ")";
            float padding = 4.0F;
            float headSize = 27.5F;
            float gap = 5.0F;
            float rightPad = 6.0F;
            float textX = x + padding + headSize + gap - 5.0F;
            float textWidth = Math.max(this.issue(13).getWidth(name), this.issue(12).getWidth(hpText));
            float width = Math.max(92.5F, padding + headSize + gap + textWidth + rightPad) - 3.5F;
            float height = 32.0F;
            float headX = x + padding;
            float headY = y + 3.5F;
            float textMaxWidth = Math.max(10.0F, width - (textX - x) - rightPad) + 2.0F;
            class_4587 matrices = eventRender.getContext().method_51448();
            boolean drawSquares = this.isUnusualRectType();
            int drawAlphaInt = (int)(255.0F * alpha);
            matrices.method_22903();
            RenderUtils.drawDefaultHudPanel(
               matrices,
               x,
               y,
               width,
               height,
               4.0F,
               4.5F,
               ColorUtils.applyAlpha(ColorUtils.rgba(50, 50, 50, 255), alpha),
               ColorUtils.applyAlpha(ColorUtils.darken(colorTheme, 0.15F), alpha),
               ColorUtils.applyAlpha(ColorUtils.darken(colorTheme, 0.05F), alpha)
            );
            if (drawSquares && alpha > 0.06F) {
               RenderUtils.drawHudSquarePattern(matrices, x + 8.0F, y, width, height, ColorUtils.applyAlpha(barThemeColor, alpha));
            }

            if (this.headParticlesEnabled) {
               this.updateAndRenderHeadParticles(matrices, target, headX - 1.85F, headY - 1.0F, headSize, alpha, barThemeColor);
            } else {
               this.headParticles.clear();
            }

            if (target instanceof class_1657 playerEntity) {
               RenderUtils.drawPlayerHead(matrices, playerEntity.method_5667(), headX - 1.85F, headY - 1.0F, headSize, 3.5F, alpha, 0.0F);
            } else {
               RenderUtils.drawTargetHudDefaultPlaceholder(matrices, headX - 1.85F, headY - 1.0F, alpha);
               this.issue(26).drawCenteredString(matrices, "?", (double)(headX + headSize / 2.25F), headY + 7.5, ColorUtils.rgba(220, 220, 220, drawAlphaInt));
            }

            this.issue(14).drawStringWithFade(matrices, name, textX + 0.7F, y + 5.5F, textMaxWidth, ColorUtils.rgba(255, 255, 255, drawAlphaInt));
            this.issue(13).drawStringWithFade(matrices, hpText, textX + 1.0F, y + 14.5F, textMaxWidth, ColorUtils.rgba(232, 232, 232, drawAlphaInt));
            if (goldenAlpha > 0.01F) {
               float hpTextWidth = this.issue(13).getWidth(hpText);
               float abTextX = textX + 1.0F + hpTextWidth;
               int goldenAlphaInt = (int)(255.0F * goldenAlpha * alpha);
               this.issue(13)
                  .drawGradientStringHorizontal(
                     matrices, abText, abTextX, y + 14.5F, ColorUtils.rgba(236, 183, 39, goldenAlphaInt), ColorUtils.rgba(147, 108, 16, goldenAlphaInt)
                  );
            }

            float barY = y + height - 10.45F;
            float barW = Math.max(19.0F, width - rightPad - (textX - x));
            float barH = 3.85F;
            int barBgLeft;
            int barBgRight;
            int barTrailLeft;
            int barTrailRight;
            int barFillLeft;
            int barFillRight;
            if (this.healthBarStyleEnabled) {
               int redDarkBg = ColorUtils.rgba(70, 5, 10, (int)(115.0F * alpha));
               int greenDarkBg = ColorUtils.rgba(0, 70, 0, (int)(115.0F * alpha));
               int redBrightBg = ColorUtils.rgba(155, 5, 15, (int)(115.0F * alpha));
               int greenBrightBg = ColorUtils.rgba(55, 205, 15, (int)(115.0F * alpha));
               barBgLeft = this.interpolateColor(redDarkBg, greenDarkBg, healthProgress);
               barBgRight = this.interpolateColor(redBrightBg, greenBrightBg, healthProgress);
               int redDarkTrail = ColorUtils.rgba(70, 5, 10, (int)(200.0F * alpha));
               int greenDarkTrail = ColorUtils.rgba(0, 70, 0, (int)(200.0F * alpha));
               int redBrightTrail = ColorUtils.rgba(155, 5, 15, (int)(200.0F * alpha));
               int greenBrightTrail = ColorUtils.rgba(55, 205, 15, (int)(200.0F * alpha));
               barTrailLeft = this.interpolateColor(redDarkTrail, greenDarkTrail, healthProgress);
               barTrailRight = this.interpolateColor(redBrightTrail, greenBrightTrail, healthProgress);
               int redDarkFill = ColorUtils.rgba(70, 5, 10, (int)(250.0F * alpha));
               int greenDarkFill = ColorUtils.rgba(0, 70, 0, (int)(250.0F * alpha));
               int redBrightFill = ColorUtils.rgba(155, 5, 15, (int)(250.0F * alpha));
               int greenBrightFill = ColorUtils.rgba(55, 205, 15, (int)(250.0F * alpha));
               barFillLeft = this.interpolateColor(redDarkFill, greenDarkFill, healthProgress);
               barFillRight = this.interpolateColor(redBrightFill, greenBrightFill, healthProgress);
            } else {
               barBgLeft = ColorUtils.applyAlpha(ColorUtils.darken(barThemeColor2, 0.72F), alpha * 0.26F);
               barBgRight = ColorUtils.applyAlpha(ColorUtils.darken(barThemeColor, 0.72F), alpha * 0.26F);
               barTrailLeft = ColorUtils.applyAlpha(ColorUtils.darken(barThemeColor2, 0.9F), alpha * 0.42F);
               barTrailRight = ColorUtils.applyAlpha(ColorUtils.darken(barThemeColor, 0.9F), alpha * 0.42F);
               barFillLeft = ColorUtils.applyAlpha(barThemeColor2, alpha);
               barFillRight = ColorUtils.applyAlpha(barThemeColor, alpha);
            }

            if (alpha > 0.025F) {
               RenderUtils.drawGradientRect(matrices, textX, barY, barW + 3.0F, barH + 4.25F, 1.95F, barBgLeft, barBgRight, true);
               if (!hidingHud) {
                  float trailProgressDraw = class_3532.method_16439(0.58F, hpTrailProgressAnimated, hpProgressAnimated);
                  float trailW = barW * trailProgressDraw;
                  if (trailW > 1.15F) {
                     RenderUtils.drawGradientRect(matrices, textX, barY, trailW + 3.0F, barH + 4.25F, 1.95F, barTrailLeft, barTrailRight, true);
                  }
               }

               float filledW = barW * hpProgressAnimated;
               if (filledW > 1.15F) {
                  RenderUtils.drawGradientRect(matrices, textX, barY, filledW + 3.0F, barH + 4.25F, 1.95F, barFillLeft, barFillRight, true);
               }

               if (goldenAlpha > 0.01F) {
                  float goldenBarAlpha = goldenAlpha * alpha;
                  int goldenBaseLeft = ColorUtils.rgba(147, 108, 16, 255);
                  int goldenBaseRight = ColorUtils.rgba(236, 183, 39, 255);
                  int goldenTrailLeft = ColorUtils.applyAlpha(ColorUtils.darken(goldenBaseLeft, 0.7F), goldenBarAlpha * 0.5F);
                  int goldenTrailRight = ColorUtils.applyAlpha(ColorUtils.darken(goldenBaseRight, 0.7F), goldenBarAlpha * 0.5F);
                  int goldenFillLeft = ColorUtils.applyAlpha(ColorUtils.darken(goldenBaseLeft, 0.7F), goldenBarAlpha);
                  int goldenFillRight = ColorUtils.applyAlpha(goldenBaseRight, goldenBarAlpha);
                  if (!hidingHud && hasAbsorption) {
                     float goldenTrailProgressDraw = class_3532.method_16439(0.58F, goldenTrailProgressAnimated, goldenProgressAnimated);
                     float goldenTrailW = barW * goldenTrailProgressDraw;
                     if (goldenTrailW > 1.15F) {
                        RenderUtils.drawGradientRect(matrices, textX, barY, goldenTrailW + 3.0F, barH + 4.25F, 1.95F, goldenTrailLeft, goldenTrailRight, true);
                     }
                  }

                  float goldenFilledW = barW * goldenProgressAnimated;
                  if (goldenFilledW > 1.15F) {
                     RenderUtils.drawGradientRect(matrices, textX, barY, goldenFilledW + 3.0F, barH + 4.25F, 1.95F, goldenFillLeft, goldenFillRight, true);
                  }
               }
            }

            float itemY = y - 11.5F;
            float itemSpacing = 9.0F;
            float itemScale = 0.5F * alpha;
            int totalSlots = this.collectDisplayItems(target);
            float itemX = x + width - totalSlots * itemSpacing - 3.0F;

            for (int itemIndex = 0; itemIndex < totalSlots; itemIndex++) {
               class_1799 stack = this.displayItems[itemIndex];
               if (!stack.method_7960()) {
                  float slotX = itemX + itemIndex * itemSpacing;
                  this.drawTargetHudItem(eventRender, matrices, stack, slotX, itemY, itemScale);
               }

               this.displayItems[itemIndex] = class_1799.field_8037;
            }

            matrices.method_22909();
            this.draggable.setWidth(width);
            this.draggable.setHeight(height);
         } else {
            this.headParticles.clear();
            this.lastTargetHurtTime = 0;
            this.draggable.setWidth(0.0F);
            this.draggable.setHeight(0.0F);
            this.goldenAlphaAnimation.setValue(0.0F);
            this.ABValueAnimation.setValue(0.0F);
            this.goldenHpAnimation.setValue(0.0F);
            this.goldenHpTrailAnimation.setValue(0.0F);
         }
      }
   }

   public void WaveStyle(EventRender.Default eventRender) {
      if (mc.field_1724 == null) {
         this.draggable.setWidth(0.0F);
         this.draggable.setHeight(0.0F);
      } else {
         Aura aura = ModuleClass.aura;
         boolean chatOpen = mc.field_1755 instanceof class_408;
         class_1309 auraTarget = aura != null ? aura.getTarget() : null;
         boolean showTargetHud = chatOpen || auraTarget != null;
         this.alphaAnimation.update(showTargetHud ? 1.0F : 0.0F);
         if (showTargetHud && !this.wasVisible) {
            this.scaleAnimation.setValue(0.0F);
            this.hideScaleAnimation.setValue(1.0F);
            this.wasVisible = true;
         } else if (!showTargetHud && this.wasVisible) {
            this.hideScaleAnimation.setValue(1.0F);
            this.wasVisible = false;
         }

         if (showTargetHud) {
            this.scaleAnimation.update(1.0F);
            this.lastTarget = (class_1309)(chatOpen ? mc.field_1724 : auraTarget);
         } else {
            this.hideScaleAnimation.update(0.0F);
         }

         float scale;
         if (showTargetHud) {
            scale = class_3532.method_15363(this.scaleAnimation.getValue(), 0.0F, 1.0F);
         } else {
            scale = class_3532.method_15363(this.hideScaleAnimation.getValue(), 0.0F, 1.0F);
         }

         class_1309 target = (class_1309)(showTargetHud ? (chatOpen ? mc.field_1724 : auraTarget) : this.lastTarget);
         if (target != null && !(scale <= 0.01F)) {
            float x = this.draggable.getX();
            float y = this.draggable.getY();
            float padding = 3.0F;
            float width = 110.0F;
            float height = 46.0F;
            float health = target.method_6032();
            float maxHealth = Math.max(1.0F, target.method_6063());
            this.hpValueAnimation.update(health);
            float animatedHealthValue = class_3532.method_15363(this.hpValueAnimation.getValue(), 0.0F, maxHealth);
            float healthProgress = class_3532.method_15363(health / maxHealth, 0.0F, 1.0F);
            this.hpAnimation.update(healthProgress);
            float hpProgressAnimated = class_3532.method_15363(this.hpAnimation.getValue(), 0.0F, 1.0F);
            if (hpProgressAnimated > this.hpTrailAnimation.getValue()) {
               this.hpTrailAnimation.setValue(hpProgressAnimated);
            } else {
               this.hpTrailAnimation.update(hpProgressAnimated);
            }

            class_4587 matrices = eventRender.getContext().method_51448();
            matrices.method_22903();
            this.applyHudTransform(matrices, x, y, width, height, scale, scale);
            float visualAlpha = scale;
            int alphaInt = (int)(255.0F * scale);
            float entityBoxSize = height - padding * 2.0F - 4.0F;
            RenderUtils.drawTargetHudWaveFrame(matrices, x, y, width, height, padding, entityBoxSize, scale);
            matrices.method_22909();
            int entityX = (int)(x + padding + 3.0F);
            int entityY = (int)(y + padding + 3.0F);
            int entityX2 = (int)(x + padding + 3.0F + entityBoxSize - 2.0F);
            int entityY2 = (int)(y + padding + 3.0F + entityBoxSize - 2.0F);
            int entitySize = (int)(15.0F * scale);
            float entityCenterX = x + padding + 3.0F + (entityBoxSize - 2.0F) / 2.0F;
            float entityCenterY = y + padding + 3.0F + (entityBoxSize - 2.0F) / 3.0F;
            float yaw = target.field_6283;
            float pitch = target.method_36455();
            double yawRadians = Math.toRadians(yaw + 180.0F);
            float lookX = entityCenterX + (float)(Math.sin(yawRadians) * 50.0);
            float lookY = entityCenterY - pitch;
            if (scale > 0.5F) {
               class_490.method_2486(eventRender.getContext(), entityX, entityY, entityX2, entityY2, entitySize, 0.0F, lookX, lookY, target);
            }

            matrices.method_22903();
            this.applyHudTransform(matrices, x, y, width, height, scale, scale);
            String name = target.method_5477().getString();
            float textX = x + padding + entityBoxSize + 6.0F;
            float waveNameFadeMaxWidth = Math.max(8.0F, x + width - padding - 4.0F - textX);
            this.issue(14).drawStringWithFade(matrices, name, textX, y + padding + 5.0F, waveNameFadeMaxWidth, ColorUtils.rgba(255, 255, 255, alphaInt));
            this.issue(14)
               .draw(
                  matrices,
                  "HP: " + String.format("%.1f", animatedHealthValue) + " | Dist: " + (int)target.method_5739(mc.field_1724),
                  textX,
                  y + padding + 20.0F,
                  ColorUtils.rgba(255, 255, 255, alphaInt)
               );
            float heartsX = textX;
            float heartsY = y + padding + 15.0F;
            float heartSize = 5.0F;
            float heartSpacing = 0.5F;
            int totalHearts = 10;
            float healthPerHeart = maxHealth / totalHearts;
            float currentHealth = health;
            int heartColor;
            if (health <= maxHealth * 0.25F) {
               heartColor = ColorUtils.rgba(255, 50, 50, alphaInt);
            } else if (health <= maxHealth * 0.5F) {
               heartColor = ColorUtils.rgba(255, 220, 0, alphaInt);
            } else {
               heartColor = ColorUtils.rgba(0, 255, 0, alphaInt);
            }

            int shadowColor = ColorUtils.applyAlpha(heartColor, scale * 0.5F);

            for (int i = 0; i < totalHearts; i++) {
               float hx = heartsX + i * (heartSize + heartSpacing);
               RenderUtils.drawTargetHudHeartBase(matrices, hx, heartsY - 3.0F, visualAlpha);
               if (currentHealth > 0.0F) {
                  float fillAmount = class_3532.method_15363(currentHealth / healthPerHeart, 0.0F, 1.0F);
                  float filledWidth = heartSize * fillAmount;
                  if (filledWidth > 0.0F) {
                     RenderUtils.drawTargetHudHeartFill(matrices, hx, heartsY - 3.0F, filledWidth, heartColor, shadowColor);
                  }

                  currentHealth -= healthPerHeart;
               }
            }

            float itemX = textX - 1.0F;
            float itemY = y + padding + 28.0F;
            float itemSpacing = 10.0F;
            int waveThemeColor = !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
               ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
               : ColorUtils.getThemeColor();
            int waveSlotBorderColor = ColorUtils.applyAlpha(ColorUtils.rgba(50, 50, 50, 255), visualAlpha);
            int waveSlotTopColor = ColorUtils.applyAlpha(ColorUtils.darken(waveThemeColor, 0.15F), visualAlpha);
            int waveSlotBottomColor = ColorUtils.applyAlpha(ColorUtils.darken(waveThemeColor, 0.05F), visualAlpha);
            int totalSlots = this.collectDisplayItems(target);
            if (totalSlots > 0) {
               float containerX = itemX - 0.85F;
               float containerY = itemY - 0.85F;
               float containerW = (totalSlots - 1) * itemSpacing + 9.8F;
               float slotX = 9.8F;
            }

            float itemScale = 0.5F;

            for (int itemIndex = 0; itemIndex < totalSlots; itemIndex++) {
               class_1799 stack = this.displayItems[itemIndex];
               if (!stack.method_7960()) {
                  float slotX = itemX + itemIndex * itemSpacing;
                  this.drawTargetHudItem(eventRender, matrices, stack, slotX, itemY, itemScale);
               }

               this.displayItems[itemIndex] = class_1799.field_8037;
            }

            matrices.method_22909();
            this.draggable.setWidth(width);
            this.draggable.setHeight(height);
         } else {
            this.draggable.setWidth(0.0F);
            this.draggable.setHeight(0.0F);
         }
      }
   }

   private static final class HeadParticle {
      float x;
      float y;
      float vx;
      float vy;
      float size;
      float age;
      float maxAge;

      private HeadParticle() {
      }
   }
}
