package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_10142;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class JumpCircle extends Module {
   public static JumpCircle INSTANCE = new JumpCircle();
   private static final float MAX_LIFETIME_MS = 1850.0F;
   private static final float ROTATION_SPEED = 120.0F;
   private static final float PULSE_SPEED = 7.0F;
   private static final float PULSE_SCALE = 0.06F;
   private static final float PULSE_ALPHA = 0.12F;
   private static final int MAX_CIRCLES = 8;
   private final FloatSetting radius = new FloatSetting("Радиус", 1.85F, 0.5F, 4.0F, 0.1F);
   private final FloatSetting speed = new FloatSetting("Скорость", 1.2F, 1.0F, 5.0F, 0.1F);
   private final FloatSetting fadeSpeed = new FloatSetting("Скорость исчезновения", 1.5F, 1.0F, 5.0F, 0.5F);
   private final List<JumpCircle.CircleData> circles = new ArrayList<>();
   private final class_2960 circleTexture = class_2960.method_60655("slikdlc", "textures/jumpcircle/circle.png");
   private boolean wasOnGround = true;

   public JumpCircle() {
      super("JumpCircle", "Круг при прыжке", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.radius, this.speed, this.fadeSpeed});
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 != null) {
         this.wasOnGround = mc.field_1724.method_24828();
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.circles.clear();
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean isOnGround = mc.field_1724.method_24828();
         if (this.wasOnGround && !isOnGround) {
            class_243 pos = new class_243(mc.field_1724.method_23317(), Math.floor(mc.field_1724.method_23318()) + 0.001, mc.field_1724.method_23321());
            this.circles.add(new JumpCircle.CircleData(pos, System.currentTimeMillis()));

            while (this.circles.size() > 8) {
               this.circles.remove(0);
            }
         }

         this.wasOnGround = isOnGround;
         long now = System.currentTimeMillis();
         float lifeTimeMs = this.getLifeTimeMs();
         Iterator<JumpCircle.CircleData> iterator = this.circles.iterator();

         while (iterator.hasNext()) {
            JumpCircle.CircleData circle = iterator.next();
            if (now - circle.startTimeMs > (long)lifeTimeMs) {
               iterator.remove();
            }
         }
      }
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (!this.circles.isEmpty()) {
         long now = System.currentTimeMillis();
         class_243 cameraPos = event.getCamera().method_19326();
         class_4587 matrices = event.getMatrices();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, this.circleTexture);

         for (JumpCircle.CircleData circle : this.circles) {
            float progress = this.getProgress(now, circle);
            if (!(progress >= 1.0F)) {
               float alpha = this.getAlpha(progress);
               if (!(alpha <= 0.01F)) {
                  this.renderGlowCircle(matrices, cameraPos, circle, progress, alpha, now);
               }
            }
         }

         RenderSystem.enableCull();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private float getLifeTimeMs() {
      return 1850.0F / Math.max(0.25F, this.speed.get());
   }

   private float getProgress(long now, JumpCircle.CircleData circle) {
      return (float)(now - circle.startTimeMs) / this.getLifeTimeMs();
   }

   private float getAlpha(float progress) {
      float fade = class_3532.method_15363(progress * this.fadeSpeed.get(), 0.0F, 1.0F);
      return 1.0F - fade;
   }

   private void renderGlowCircle(class_4587 matrices, class_243 cameraPos, JumpCircle.CircleData circle, float progress, float alpha, long now) {
      float lifeTimeSec = (float)(now - circle.startTimeMs) / 1000.0F;
      float easedProgress = easeOutCubic(progress);
      float scale = Math.min(easedProgress * this.radius.get(), this.radius.get());
      float rotation = lifeTimeSec * 120.0F * this.speed.get();
      rotation += (float)Math.sin(progress * Math.PI * 2.0) * 30.0F;
      float pulse = (float)Math.sin(lifeTimeSec * 7.0F * this.speed.get());
      float pulseScale = 1.0F + pulse * 0.06F;
      float pulseAlpha = class_3532.method_15363(alpha * (1.0F + pulse * 0.12F), 0.0F, 1.0F);
      float alphaBoost = class_3532.method_15363(pulseAlpha * 1.25F, 0.0F, 1.0F);
      float finalScale = scale * pulseScale;
      int baseTheme = this.getStableThemeColor();
      int secondaryTheme = this.getStableThemeSecondaryColor();
      int colorA = ColorUtils.setAlphaColor(baseTheme, (int)(255.0F * alphaBoost));
      int colorB = ColorUtils.setAlphaColor(secondaryTheme, (int)(255.0F * alphaBoost));
      int darkA = ColorUtils.setAlphaColor(ColorUtils.darken(baseTheme, 0.65F), (int)(255.0F * class_3532.method_15363(alphaBoost * 0.9F, 0.0F, 1.0F)));
      int darkB = ColorUtils.setAlphaColor(ColorUtils.darken(secondaryTheme, 0.65F), (int)(255.0F * class_3532.method_15363(alphaBoost * 0.9F, 0.0F, 1.0F)));
      matrices.method_22903();
      matrices.method_22904(
         circle.pos.field_1352 - cameraPos.field_1352, circle.pos.field_1351 - cameraPos.field_1351, circle.pos.field_1350 - cameraPos.field_1350
      );
      matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(rotation));
      Matrix4f matrix = matrices.method_23760().method_23761();
      float half = finalScale * 0.5F;
      float thickScale = finalScale * 1.08F;
      float thickHalf = thickScale * 0.5F;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      this.addTexturedQuad(buffer, matrix, -half, -half, half, half, colorA, colorB);
      this.addTexturedQuad(buffer, matrix, -thickHalf, -thickHalf, thickHalf, thickHalf, darkA, darkB);
      class_286.method_43433(buffer.method_60800());
      matrices.method_22909();
   }

   private void addTexturedQuad(class_287 buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, int colorA, int colorB) {
      int aR = colorA >> 16 & 0xFF;
      int aG = colorA >> 8 & 0xFF;
      int aB = colorA & 0xFF;
      int aA = colorA >> 24 & 0xFF;
      int bR = colorB >> 16 & 0xFF;
      int bG = colorB >> 8 & 0xFF;
      int bB = colorB & 0xFF;
      int bA = colorB >> 24 & 0xFF;
      buffer.method_22918(matrix, x1, y1, 0.0F).method_22913(0.0F, 1.0F).method_1336(aR, aG, aB, aA);
      buffer.method_22918(matrix, x1, y2, 0.0F).method_22913(0.0F, 0.0F).method_1336(bR, bG, bB, bA);
      buffer.method_22918(matrix, x2, y2, 0.0F).method_22913(1.0F, 0.0F).method_1336(bR, bG, bB, bA);
      buffer.method_22918(matrix, x2, y1, 0.0F).method_22913(1.0F, 1.0F).method_1336(aR, aG, aB, aA);
   }

   private int getStableThemeColor() {
      return !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ColorUtils.getThemeColor();
   }

   private int getStableThemeSecondaryColor() {
      return !SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? SlikDlc.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ColorUtils.getThemeColor(180);
   }

   private static float easeOutCubic(float t) {
      float u = 1.0F - t;
      return 1.0F - u * u * u;
   }

   private record CircleData(class_243 pos, long startTimeMs) {
   }
}
