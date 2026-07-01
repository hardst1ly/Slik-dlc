package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_10142;
import net.minecraft.class_1309;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TargetESP extends Module {
   public static TargetESP INSTANCE = new TargetESP();
   private static final float GHOST_ALPHA_MULT = 0.6F;
   private static final float CELKA_SPEED_MULT = 1.2F;
   private static final float SCALE_FACTOR = 0.007F;
   static final long CUBE_ATTACH_LIFE_MS = 560L;
   static final long CUBE_FADE_LIFE_MS = 320L;
   static final int MAX_CUBE_PARTICLES = 72;
   static final byte[][] CUBE_EDGES = new byte[][]{
      {-1, -1, -1, 1, -1, -1},
      {1, -1, -1, 1, -1, 1},
      {1, -1, 1, -1, -1, 1},
      {-1, -1, 1, -1, -1, -1},
      {-1, 1, -1, 1, 1, -1},
      {1, 1, -1, 1, 1, 1},
      {1, 1, 1, -1, 1, 1},
      {-1, 1, 1, -1, 1, -1},
      {-1, -1, -1, -1, 1, -1},
      {1, -1, -1, 1, 1, -1},
      {1, -1, 1, 1, 1, 1},
      {-1, -1, 1, -1, 1, 1}
   };
   private final ModeSetting mode = new ModeSetting("Режим", "Картинка 1", "Картинка 1", "Картинка 2", "Кольцо", "Души", "Кубы", "Кристаллы");
   private final FloatSetting size = new FloatSetting("Размер", 1.15F, 0.6F, 2.5F, 0.05F);
   private final FloatSetting ringRadius = new FloatSetting("Радиус кольца", 0.5F, 0.3F, 1.5F, 0.05F);
   private final FloatSetting ringSpeed = new FloatSetting("Скорость кольца", 1.0F, 0.3F, 3.0F, 0.1F);
   private final FloatSetting rotateSpeed = new FloatSetting("Скорость вращения", 1.2F, 0.2F, 4.0F, 0.05F);
   private final BooleanSetting hurtColor = new BooleanSetting("Окрашивание при ударе", true);
   private final FloatSetting bmwGhostCount = new FloatSetting("Кол-во призраков", 3.0F, 2.0F, 5.0F, 1.0F);
   private final FloatSetting bmwGhostLife = new FloatSetting("Время жизни (мс)", 350.0F, 150.0F, 500.0F, 25.0F);
   private final FloatSetting bmwStrengthXZ = new FloatSetting("Цикл XZ", 2000.0F, 1000.0F, 5000.0F, 100.0F);
   private final FloatSetting bmwStrengthY = new FloatSetting("Цикл Y", 1700.0F, 1000.0F, 5000.0F, 100.0F);
   private float appearValue = 0.0F;
   private float scaleValue = 0.0F;
   private float rotProgress = 0.0F;
   private float rotFrom = -280.0F;
   private float rotTo = 280.0F;
   private long lastRotateUpdate = System.currentTimeMillis();
   private class_1309 lastTarget = null;
   private class_1309 lastHandledTarget = null;
   private class_243 lastTargetPos = null;
   private float lastTargetHeight = 1.8F;
   private float lastTargetWidth = 0.6F;
   private final CopyOnWriteArrayList<TargetESP.GlowPoint> bmwPoints = new CopyOnWriteArrayList<>();
   private float crystalRotationAngle = 0.0F;
   private float crystalAnimation = 0.0F;
   private float spawnAccumulator = 0.0F;
   private long lastCubeTime = 0L;
   private final ArrayList<CubeParticle> cubeParticles = new ArrayList<>();
   private final ArrayList<CubeParticle> renderCubeParticles = new ArrayList<>();
   private static final float SPAWN_INTERVAL = 0.022F;
   private static final int PARTICLES_PER_SPAWN = 1;

   public TargetESP() {
      super("TargetESP", "Отображения таргета", Module.ModuleCategory.RENDER);
      this.size.visible(this::isImageMode);
      this.rotateSpeed.visible(this::isImageMode);
      this.bmwGhostCount.visible(() -> this.mode.is("Райдер"));
      this.bmwGhostLife.visible(() -> this.mode.is("Райдер"));
      this.bmwStrengthXZ.visible(() -> this.mode.is("Райдер"));
      this.bmwStrengthY.visible(() -> this.mode.is("Райдер"));
      this.ringRadius.visible(() -> this.mode.is("Кольцо"));
      this.ringSpeed.visible(() -> this.mode.is("Кольцо"));
      this.addSettings(
         new Setting[]{
            this.mode,
            this.size,
            this.rotateSpeed,
            this.hurtColor,
            this.ringRadius,
            this.ringSpeed,
            this.bmwGhostCount,
            this.bmwGhostLife,
            this.bmwStrengthXZ,
            this.bmwStrengthY
         }
      );
   }

   @Override
   public void onDisable() {
      this.appearValue = 0.0F;
      this.scaleValue = 0.0F;
      this.lastTarget = null;
      this.lastHandledTarget = null;
      this.lastTargetPos = null;
      this.rotProgress = 0.0F;
      this.rotFrom = -280.0F;
      this.rotTo = 280.0F;
      this.bmwPoints.clear();
      this.crystalRotationAngle = 0.0F;
      this.crystalAnimation = 0.0F;
      this.spawnAccumulator = 0.0F;
      this.lastCubeTime = 0L;
      this.cubeParticles.clear();
      this.renderCubeParticles.clear();
      super.onDisable();
   }

   private boolean isImageMode() {
      return this.mode.is("Картинка 1") || this.mode.is("Картинка 2");
   }

   private class_2960 getCaptureTexture() {
      return this.mode.is("Картинка 2")
         ? class_2960.method_60655("slikdlc", "textures/targetesp/targetesp_3.png")
         : class_2960.method_60655("slikdlc", "textures/targetesp/targetesp_2.png");
   }

   private class_2960 getBloomTexture() {
      return class_2960.method_60655("slikdlc", "textures/targetesp/bloom.png");
   }

   private int getESPColor() {
      int color = ColorUtils.getThemeColor();
      if ((color >> 24 & 0xFF) == 0) {
         color |= -16777216;
      }

      return color;
   }

   private float animateTo(float current, float target, float delta) {
      if (current < target) {
         current = Math.min(current + delta, target);
      } else if (current > target) {
         current = Math.max(current - delta, target);
      }

      return current;
   }

   private float getDistanceScale(class_243 cameraPos, double worldX, double worldY, double worldZ) {
      double dx = worldX - cameraPos.field_1352;
      double dy = worldY - cameraPos.field_1351;
      double dz = worldZ - cameraPos.field_1350;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return (float)Math.max(0.1, distance * 0.007F);
   }

   @EventLink(
      priority = -100
   )
   public void onRender3D(Event3DRender event) {
      if (mc != null && mc.field_1724 != null && mc.field_1687 != null) {
         Aura aura = ModuleClass.aura;
         boolean auraEnabled = aura != null && aura.isEnable();
         class_1309 target = auraEnabled ? aura.getTarget() : null;
         boolean hasTarget = target != null && target.method_5805();
         float speed = 0.05F;
         this.appearValue = this.animateTo(this.appearValue, hasTarget ? 1.0F : 0.0F, speed);
         this.scaleValue = this.animateTo(this.scaleValue, hasTarget ? 1.0F : 0.5F, speed);
         if (hasTarget) {
            this.lastTarget = target;
            this.lastHandledTarget = target;
         }

         if (this.mode.is("Кристаллы")) {
            float crystalSpeed = hasTarget ? 0.07F : 0.045F;
            this.crystalAnimation = this.animateTo(this.crystalAnimation, hasTarget ? 1.0F : 0.0F, crystalSpeed);
            if (hasTarget) {
               this.crystalRotationAngle += 0.8F;
            }
         }

         if (!(this.appearValue <= 0.001F) || hasTarget || this.mode.is("Кристаллы") && !(this.crystalAnimation <= 0.001F)) {
            if (hasTarget && target != null) {
               float td = event.getTickDelta();
               this.lastTargetPos = new class_243(
                  class_3532.method_16436(td, target.field_6038, target.method_23317()),
                  class_3532.method_16436(td, target.field_5971, target.method_23318()),
                  class_3532.method_16436(td, target.field_5989, target.method_23321())
               );
               this.lastTargetHeight = target.method_17682();
               this.lastTargetWidth = target.method_17681();
            }

            if (this.lastTargetPos != null) {
               if (this.mode.is("Райдер")) {
                  if (hasTarget && target != null) {
                     this.addBMWGhosts(
                        target,
                        event.getTickDelta(),
                        Math.max(1, Math.round(this.bmwGhostCount.getValue().floatValue())),
                        Math.max(1, Math.round(this.bmwGhostLife.getValue().floatValue())),
                        this.getESPColor()
                     );
                  }

                  this.bmwPoints.removeIf(TargetESP.GlowPoint::shouldRemove);
                  this.drawBMW3D(event);
               } else if (this.mode.is("Кристаллы")) {
                  class_1309 crystalTarget = hasTarget ? target : this.lastTarget;
                  if ((crystalTarget != null || this.lastTargetPos != null) && this.crystalAnimation > 0.01F) {
                     this.renderCrystals3D(event.getMatrices(), crystalTarget, event.getTickDelta());
                  }
               } else {
                  if (this.isImageMode()) {
                     this.renderMarker3D(event);
                  }

                  if (this.mode.is("Души")) {
                     this.drawSouls3D(event);
                  }

                  if (this.mode.is("Призраки")) {
                     this.drawCelka3D(event);
                  }

                  if (this.mode.is("Кольцо")) {
                     this.drawRing3D(event);
                  }

                  if (this.mode.is("Кубы")) {
                     this.renderCubes(event, target, hasTarget);
                  }
               }
            }
         } else {
            this.lastTarget = null;
            this.lastTargetPos = null;
         }
      }
   }

   private void renderCubes(Event3DRender event, class_1309 target, boolean hasTarget) {
      long now = System.currentTimeMillis();
      if (this.lastCubeTime == 0L) {
         this.lastCubeTime = now;
      }

      float dt = Math.min((float)(now - this.lastCubeTime) / 1000.0F, 0.1F);
      this.lastCubeTime = now;
      if (Float.isFinite(dt) && mc.field_1773 != null && mc.field_1773.method_19418() != null) {
         if (hasTarget && target != null) {
            this.lastTarget = target;
            this.spawnAccumulator += dt;

            while (this.spawnAccumulator >= 0.022F) {
               this.spawnAccumulator -= 0.022F;
               if (this.cubeParticles.size() >= 72) {
                  break;
               }

               for (int i = 0; i < 1; i++) {
                  double rand = Math.random() * 360.0;
                  double px = Math.cos(Math.toRadians(rand)) * 0.7;
                  double py = 0.02 + Math.random() * 0.1;
                  double pz = Math.sin(Math.toRadians(rand)) * 0.7;
                  this.cubeParticles.add(new CubeParticle(target, px, py, pz));
               }
            }
         } else {
            this.spawnAccumulator = 0.0F;
         }

         this.renderCubeParticles.clear();

         for (int i = this.cubeParticles.size() - 1; i >= 0; i--) {
            CubeParticle particle = this.cubeParticles.get(i);

            try {
               particle.update(dt, now, hasTarget ? target : null);
               if (particle.shouldRemove(now)) {
                  this.cubeParticles.remove(i);
               } else {
                  this.renderCubeParticles.add(particle);
               }
            } catch (Throwable var29) {
               this.cubeParticles.remove(i);
            }
         }

         if (!this.renderCubeParticles.isEmpty()) {
            float partialTicks = event.getTickDelta();
            class_4587 matrices = event.getMatrices();
            class_243 camPos = mc.field_1773.method_19418().method_19326();
            class_1309 colorTarget = hasTarget ? target : this.lastTarget;
            float hurtPC = this.getHurtPC(colorTarget);
            int baseColor = this.getESPColor();
            int redColor = ColorUtils.rgb(255, 3, 3);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(class_10142.field_53876);
            class_287 faceBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
            boolean hasFaces = false;
            int i = 0;

            for (int size = this.renderCubeParticles.size(); i < size; i++) {
               CubeParticle particle = this.renderCubeParticles.get(i);

               try {
                  int particleColor = particle.getRenderColor(baseColor, redColor, hurtPC, now);
                  if ((particleColor >> 24 & 0xFF) > 0 && particle.appendCubeFaces(faceBuilder, matrices, camPos, partialTicks, particleColor)) {
                     hasFaces = true;
                  }
               } catch (Throwable var28) {
               }
            }

            if (hasFaces) {
               class_286.method_43433(faceBuilder.method_60800());
            }

            class_287 lineBuilder = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);
            boolean hasLines = false;
            int ix = 0;

            for (int size = this.renderCubeParticles.size(); ix < size; ix++) {
               CubeParticle particle = this.renderCubeParticles.get(ix);

               try {
                  int particleColor = particle.getRenderColor(baseColor, redColor, hurtPC, now);
                  if ((particleColor >> 24 & 0xFF) > 0 && particle.appendCubeLines(lineBuilder, matrices, camPos, partialTicks, particleColor)) {
                     hasLines = true;
                  }
               } catch (Throwable var27) {
               }
            }

            if (hasLines) {
               class_286.method_43433(lineBuilder.method_60800());
            }

            RenderSystem.setShader(class_10142.field_53880);
            RenderSystem.setShaderTexture(0, this.getBloomTexture());
            class_287 bloomBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
            boolean hasBloom = false;
            float camYaw = mc.field_1773.method_19418().method_19330();
            float camPitch = mc.field_1773.method_19418().method_19329();
            int ixx = 0;

            for (int size = this.renderCubeParticles.size(); ixx < size; ixx++) {
               CubeParticle particle = this.renderCubeParticles.get(ixx);

               try {
                  int particleColor = particle.getRenderColor(baseColor, redColor, hurtPC, now);
                  if (particle.appendBloom(bloomBuilder, matrices, camPos, camYaw, camPitch, partialTicks, particleColor, now)) {
                     hasBloom = true;
                  }
               } catch (Throwable var26) {
               }
            }

            if (hasBloom) {
               class_286.method_43433(bloomBuilder.method_60800());
            }

            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
         }
      }
   }

   private void drawRing3D(Event3DRender event) {
      if (!(this.appearValue <= 0.001F) && this.lastTargetPos != null) {
         float partialTicks = mc.method_61966().method_60637(true);
         class_1309 target = this.lastTarget;
         class_243 vec;
         float entityHeight;
         if (target != null && target.method_5805()) {
            vec = new class_243(
               class_3532.method_16436(partialTicks, target.field_6038, target.method_23317()),
               class_3532.method_16436(partialTicks, target.field_5971, target.method_23318()),
               class_3532.method_16436(partialTicks, target.field_5989, target.method_23321())
            );
            entityHeight = target.method_17682();
         } else {
            vec = this.lastTargetPos;
            entityHeight = this.lastTargetHeight;
         }

         class_243 cam = mc.field_1773.method_19418().method_19326();
         double x = vec.field_1352 - cam.field_1352;
         double y = vec.field_1351 - cam.field_1351;
         double z = vec.field_1350 - cam.field_1350;
         double duration = 2000.0 / this.ringSpeed.get();
         double elapsed = System.currentTimeMillis() % (long)duration;
         boolean side = elapsed > duration / 2.0;
         double progress = elapsed / (duration / 2.0);
         if (side) {
            progress--;
         } else {
            progress = 1.0 - progress;
         }

         progress = progress < 0.5 ? 2.0 * progress * progress : 1.0 - Math.pow(-2.0 * progress + 2.0, 2.0) / 2.0;
         double eased = entityHeight / 1.2 * (progress > 0.5 ? 1.0 - progress : progress) * (side ? -1 : 1);
         int baseCol = this.getESPColor();
         float hurtPC = this.getHurtPC(target);
         int redCol = ColorUtils.rgb(255, 3, 3);
         int mainColor = this.overCol(baseCol, redCol, hurtPC);
         int colorWithAlpha = this.setAlpha(mainColor, 0.88235295F * this.appearValue);
         int colorTransparent = this.setAlpha(mainColor, 0.003921569F * this.appearValue);
         int colorFull = this.setAlpha(mainColor, this.appearValue);
         double radius = this.ringRadius.get();
         class_4587 matrices = event.getMatrices();
         Matrix4f matrix = matrices.method_23760().method_23761();
         RenderSystem.depthMask(false);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableCull();
         RenderSystem.setShader(class_10142.field_53876);
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);

         for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians(i);
            float px = (float)(x + Math.cos(rad) * radius);
            float pz = (float)(z + Math.sin(rad) * radius);
            float py1 = (float)(y + entityHeight * progress);
            float py2 = (float)(y + entityHeight * progress + eased);
            buffer.method_22918(matrix, px, py1, pz).method_39415(colorWithAlpha);
            buffer.method_22918(matrix, px, py2, pz).method_39415(colorTransparent);
         }

         class_286.method_43433(buffer.method_60800());
         RenderSystem.lineWidth(1.5F);
         class_287 lineBuffer = class_289.method_1348().method_60827(class_5596.field_29345, class_290.field_1576);

         for (int i = 0; i <= 360; i++) {
            double rad = Math.toRadians(i);
            float px = (float)(x + Math.cos(rad) * radius);
            float pz = (float)(z + Math.sin(rad) * radius);
            float py = (float)(y + entityHeight * progress);
            lineBuffer.method_22918(matrix, px, py, pz).method_39415(colorFull);
         }

         class_286.method_43433(lineBuffer.method_60800());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
      }
   }

   private int setAlpha(int color, float alpha) {
      alpha = Math.max(0.0F, Math.min(1.0F, alpha));
      return color & 16777215 | (int)(alpha * 255.0F) << 24;
   }

   @EventLink(
      priority = -100
   )
   public void onRender2D(EventRender.Default event) {
      if (this.mode.is("Кристаллы") && !(this.crystalAnimation <= 0.001F) && this.lastTargetPos != null) {
         class_1309 crystalTarget = this.lastTarget != null && this.lastTarget.method_5805() ? this.lastTarget : null;
         this.drawCrystalGlow2D(event.getContext().method_51448(), crystalTarget);
      }
   }

   private int multAlpha(int color, float mult) {
      int a = (int)((color >> 24 & 0xFF) * mult);
      a = Math.max(0, Math.min(255, a));
      return a << 24 | color & 16777215;
   }

   private int replAlpha(int color, int alpha) {
      alpha = Math.max(0, Math.min(255, alpha));
      return alpha << 24 | color & 16777215;
   }

   int overCol(int color1, int color2, float factor) {
      factor = Math.max(0.0F, Math.min(1.0F, factor));
      int r1 = color1 >> 16 & 0xFF;
      int g1 = color1 >> 8 & 0xFF;
      int b1 = color1 & 0xFF;
      int a1 = color1 >> 24 & 0xFF;
      int r2 = color2 >> 16 & 0xFF;
      int g2 = color2 >> 8 & 0xFF;
      int b2 = color2 & 0xFF;
      int a2 = color2 >> 24 & 0xFF;
      int r = (int)(r1 + (r2 - r1) * factor);
      int g = (int)(g1 + (g2 - g1) * factor);
      int b = (int)(b1 + (b2 - b1) * factor);
      int a = (int)(a1 + (a2 - a1) * factor);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private float getHurtPC(class_1309 target) {
      if (this.hurtColor.isState() && target != null) {
         float partialTicks = mc != null ? mc.method_61966().method_60637(true) : 0.0F;
         float hurtTicks = class_3532.method_15363(target.field_6235 - partialTicks, 0.0F, 10.0F);
         float progress = hurtTicks / 10.0F;
         return progress * progress * (3.0F - 2.0F * progress);
      } else {
         return 0.0F;
      }
   }

   private void drawBillboard(
      class_4587 matrices, class_243 cameraPos, double worldX, double worldY, double worldZ, float baseScreenSize, int color, float rotation
   ) {
      float distScale = this.getDistanceScale(cameraPos, worldX, worldY, worldZ);
      float half = baseScreenSize * distScale * 0.5F;
      this.drawBillboardInternal(matrices, cameraPos, worldX, worldY, worldZ, half, color, rotation);
   }

   private void drawStaticBillboard(
      class_4587 matrices, class_243 cameraPos, double worldX, double worldY, double worldZ, float worldSize, int color, float rotation
   ) {
      float half = worldSize * 0.5F;
      this.drawBillboardInternal(matrices, cameraPos, worldX, worldY, worldZ, half, color, rotation);
   }

   private void drawBillboardInternal(
      class_4587 matrices, class_243 cameraPos, double worldX, double worldY, double worldZ, float half, int color, float rotation
   ) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      if (a > 0) {
         matrices.method_22903();
         matrices.method_22904(worldX - cameraPos.field_1352, worldY - cameraPos.field_1351, worldZ - cameraPos.field_1350);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
         if (rotation != 0.0F) {
            matrices.method_22907(class_7833.field_40718.rotationDegrees(rotation));
         }

         Matrix4f matrix = matrices.method_23760().method_23761();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
         buffer.method_22918(matrix, -half, -half, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, -half, half, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, half, half, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, half, -half, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
         class_286.method_43433(buffer.method_60800());
         matrices.method_22909();
      }
   }

   private void renderMarker3D(Event3DRender event) {
      if (this.lastTargetPos != null && !(this.appearValue <= 0.001F)) {
         class_243 cam = mc.field_1773.method_19418().method_19326();
         double worldX = this.lastTargetPos.field_1352;
         double worldY = this.lastTargetPos.field_1351 + (this.lastTargetHeight + 0.4F) * 0.5F;
         double worldZ = this.lastTargetPos.field_1350;
         float baseSize = this.size.getValue().floatValue() * 12.0F;
         float renderSize = baseSize * this.scaleValue;
         long now = System.currentTimeMillis();
         float dt = Math.max(0.001F, (float)(now - this.lastRotateUpdate) / 1000.0F);
         this.lastRotateUpdate = now;
         float cycleDuration = Math.max(0.35F, 2.2F / this.rotateSpeed.getValue().floatValue());

         for (this.rotProgress += dt / cycleDuration; this.rotProgress >= 1.0F; this.rotTo = this.rotTo > 0.0F ? -280.0F : 280.0F) {
            this.rotProgress--;
            this.rotFrom = this.rotTo;
         }

         float accel = (float)Easings.SINE_IN_OUT.ease(this.rotProgress);
         float rotation = class_3532.method_16439(accel, this.rotFrom, this.rotTo);
         float hurtPC = this.getHurtPC(this.lastTarget);
         int baseColor = this.multAlpha(this.getESPColor(), this.appearValue);
         int redColor = this.multAlpha(ColorUtils.rgb(255, 3, 3), this.appearValue);
         int color = this.overCol(baseColor, redColor, hurtPC);
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, this.getCaptureTexture());
         this.drawBillboard(event.getMatrices(), cam, worldX, worldY, worldZ, renderSize, color, rotation);
         RenderSystem.enableCull();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private void drawSouls3D(Event3DRender event) {
      if (!(this.appearValue <= 0.001F) && this.lastTargetPos != null) {
         float partialTicks = mc.method_61966().method_60637(true);
         class_1309 target = this.lastTarget;
         class_243 vec;
         float height;
         if (target != null && target.method_5805()) {
            vec = new class_243(
               class_3532.method_16436(partialTicks, target.field_6038, target.method_23317()),
               class_3532.method_16436(partialTicks, target.field_5971, target.method_23318()),
               class_3532.method_16436(partialTicks, target.field_5989, target.method_23321())
            );
            height = target.method_17682();
         } else {
            vec = this.lastTargetPos;
            height = this.lastTargetHeight;
         }

         class_243 cam = mc.field_1773.method_19418().method_19326();
         double baseX = vec.field_1352;
         double baseY = vec.field_1351 + height / 2.0F;
         double baseZ = vec.field_1350;
         double radius = 0.7;
         float fixedSize = 4.0F;
         long time = System.currentTimeMillis();
         float hurtPC = this.getHurtPC(target);
         int baseCol = this.getESPColor();
         int redCol = ColorUtils.rgb(255, 3, 3);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, this.getBloomTexture());
         class_4587 matrices = event.getMatrices();

         for (int i = 0; i < 20; i++) {
            float trailFactor = 1.0F - i / 20.0F * 0.7F;
            double angle = 0.15 * (time - i * 10.0) / 25.0;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            double worldX = baseX + s;
            double worldY = baseY + c;
            double worldZ = baseZ - c;
            float sz = fixedSize * trailFactor;
            float alphaTrail = this.appearValue * 0.6F;
            int col = this.multAlpha(baseCol, alphaTrail * this.appearValue);
            int red = this.multAlpha(redCol, alphaTrail * this.appearValue);
            int color = this.overCol(col, red, hurtPC);
            this.drawStaticBillboard(matrices, cam, worldX, worldY, worldZ, sz * 0.12F, color, 0.0F);
            int glowColor = this.multAlpha(color, 0.45F);
            this.drawStaticBillboard(matrices, cam, worldX, worldY, worldZ, sz * 0.21F, glowColor, 0.0F);
         }

         for (int i = 0; i < 20; i++) {
            float trailFactor = 1.0F - i / 20.0F * 0.7F;
            double angle = 0.15 * (time - i * 10.0) / 25.0;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            double worldX = baseX - s;
            double worldY = baseY + s;
            double worldZ = baseZ - c;
            float sz = fixedSize * trailFactor;
            float alphaTrail = this.appearValue * 0.6F;
            int col = this.multAlpha(baseCol, alphaTrail * this.appearValue);
            int red = this.multAlpha(ColorUtils.rgb(235, 7, 7), alphaTrail * this.appearValue);
            int color = this.overCol(col, red, hurtPC);
            this.drawStaticBillboard(matrices, cam, worldX, worldY, worldZ, sz * 0.12F, color, 0.0F);
            int glowColor = this.multAlpha(color, 0.45F);
            this.drawStaticBillboard(matrices, cam, worldX, worldY, worldZ, sz * 0.21F, glowColor, 0.0F);
         }

         for (int i = 0; i < 20; i++) {
            float trailFactor = 1.0F - i / 20.0F * 0.7F;
            double angle = 0.15 * (time - i * 10.0) / 25.0;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            double worldX = baseX - s;
            double worldY = baseY - s;
            double worldZ = baseZ + c;
            float sz = fixedSize * trailFactor;
            float alphaTrail = this.appearValue * 0.6F;
            int col = this.multAlpha(baseCol, alphaTrail * this.appearValue);
            int red = this.multAlpha(redCol, alphaTrail * this.appearValue);
            int color = this.overCol(col, red, hurtPC);
            this.drawStaticBillboard(matrices, cam, worldX, worldY, worldZ, sz * 0.12F, color, 0.0F);
            int glowColor = this.multAlpha(color, 0.45F);
            this.drawStaticBillboard(matrices, cam, worldX, worldY, worldZ, sz * 0.21F, glowColor, 0.0F);
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
      }
   }

   private void addBMWGhosts(class_1309 entity, float partialTicks, int cornersCount, int maxTime, int colorBase) {
      float xzRange = 0.7F;
      float yRange = entity.method_17682();
      int delayXZ = (int)this.bmwStrengthXZ.getValue().floatValue();
      int delayY = (int)this.bmwStrengthY.getValue().floatValue();
      long time = System.currentTimeMillis();
      float rotateProgress = (float)(time % delayXZ) / delayXZ;
      float xzRotate = rotateProgress * 360.0F;
      float yProgress = (float)(time % delayY) / delayY;
      float yLrpPC = 0.5F - 0.5F * class_3532.method_15362(yProgress * (float) (Math.PI * 2));

      for (int corner = 0; corner < cornersCount; corner++) {
         float cornersPC = (float)corner / cornersCount;
         double yawRad = Math.toRadians(class_3532.method_15393(cornersPC * 360.0F + xzRotate));
         float offsetX = -((float)Math.sin(yawRad)) * xzRange;
         float offsetY = yRange * yLrpPC;
         float offsetZ = (float)Math.cos(yawRad) * xzRange;
         this.bmwPoints.add(new TargetESP.GlowPoint(offsetX, offsetY, offsetZ, maxTime, colorBase));
      }
   }

   private void drawBMW3D(Event3DRender event) {
      if (!this.bmwPoints.isEmpty() && !(this.appearValue <= 0.001F)) {
         class_1309 renderTarget = this.lastTarget != null ? this.lastTarget : this.lastHandledTarget;
         if (renderTarget != null || this.lastTargetPos != null) {
            float partialTicks = mc.method_61966().method_60637(true);
            class_243 basePos;
            if (renderTarget != null && renderTarget.method_5805()) {
               basePos = new class_243(
                  class_3532.method_16436(partialTicks, renderTarget.field_6038, renderTarget.method_23317()),
                  class_3532.method_16436(partialTicks, renderTarget.field_5971, renderTarget.method_23318()),
                  class_3532.method_16436(partialTicks, renderTarget.field_5989, renderTarget.method_23321())
               );
            } else {
               basePos = this.lastTargetPos;
            }

            if (basePos != null) {
               class_243 cam = mc.field_1773.method_19418().method_19326();
               float hurtPC = this.getHurtPC(renderTarget);
               float fixedScreenSize = 6.0F;
               RenderSystem.disableDepthTest();
               RenderSystem.enableBlend();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.blendFunc(770, 1);
               RenderSystem.setShader(class_10142.field_53880);
               RenderSystem.setShaderTexture(0, this.getBloomTexture());
               class_4587 matrices = event.getMatrices();

               for (TargetESP.GlowPoint point : this.bmwPoints) {
                  float timePC = point.getTimeProgress();
                  float trailFactor = 1.0F - timePC * 0.6F;
                  double worldX = basePos.field_1352 + point.x;
                  double worldY = basePos.field_1351 + point.y;
                  double worldZ = basePos.field_1350 + point.z;
                  float sz = fixedScreenSize * trailFactor;
                  int alpha = (int)(255.0F * this.appearValue * trailFactor * 0.8F);
                  alpha = Math.max(0, Math.min(255, alpha));
                  int col = this.replAlpha(point.baseColor, alpha);
                  int red = this.replAlpha(ColorUtils.rgb(255, 3, 3), alpha);
                  int finalColor = this.overCol(col, red, hurtPC);
                  this.drawBillboard(matrices, cam, worldX, worldY, worldZ, sz, finalColor, 0.0F);
               }

               RenderSystem.enableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableBlend();
               RenderSystem.depthMask(true);
            }
         }
      }
   }

   private void drawCelka3D(Event3DRender event) {
      if (!(this.appearValue <= 0.001F) && this.lastTargetPos != null) {
         float partialTicks = mc.method_61966().method_60637(true);
         class_1309 target = this.lastTarget;
         class_243 vec;
         if (target != null && target.method_5805()) {
            vec = new class_243(
               class_3532.method_16436(partialTicks, target.field_6038, target.method_23317()),
               class_3532.method_16436(partialTicks, target.field_5971, target.method_23318()),
               class_3532.method_16436(partialTicks, target.field_5989, target.method_23321())
            );
            float entityHeight = target.method_17682();
         } else {
            vec = this.lastTargetPos;
            float entityHeight = this.lastTargetHeight;
         }

         class_243 cam = mc.field_1773.method_19418().method_19326();
         double bx = vec.field_1352;
         double by = vec.field_1351;
         double bz = vec.field_1350;
         double t = System.currentTimeMillis() / 384.61539872299335 * 1.2F;
         double tv = System.currentTimeMillis() / 666.6666666666666 * 1.2F;
         int baseCol = this.getESPColor();
         float fixedSize = 4.0F;
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, this.getBloomTexture());
         class_4587 matrices = event.getMatrices();
         float radius = 0.65F;

         for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 20; j++) {
               float kf = j / 20.0F;
               float sizeFactor = 1.0F - kf * 0.55F;
               double tj = t - j * 0.05;
               double tvj = tv - j * 0.05;
               double cyc = (Math.sin(tvj) + 1.0) * 0.5;
               double baseAngle = Math.toRadians(k * 90.0 + tj * 50.0 % 360.0);
               double offX = Math.cos(baseAngle) * radius;
               double offZ = Math.sin(baseAngle) * radius;
               double offY = k % 2 == 0 ? 0.1 + 1.7 * cyc : 1.8 - 1.7 * cyc;
               double worldX = bx + offX;
               double worldY = by + offY;
               double worldZ = bz + offZ;
               float sz = fixedSize * sizeFactor;
               int finalAlpha = (int)(255.0F * this.appearValue * 0.6F);
               int color = this.replAlpha(baseCol, finalAlpha);
               this.drawBillboard(matrices, cam, worldX, worldY, worldZ, sz, color, 0.0F);
               int glowColor = this.multAlpha(color, 0.45F);
               this.drawBillboard(matrices, cam, worldX, worldY, worldZ, sz * 1.75F, glowColor, 0.0F);
            }

            radius *= -1.0F;
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
      }
   }

   private void renderCrystals3D(class_4587 ms, class_1309 target, float partialTicks) {
      if (this.lastTargetPos != null && !(this.crystalAnimation <= 0.01F)) {
         class_243 cameraPos = mc.field_1773.method_19418().method_19326();
         int baseColor = ColorUtils.getThemeColor();
         int color = this.multAlpha(baseColor, this.crystalAnimation);
         int glowColor = this.multAlpha(baseColor, this.crystalAnimation * 0.28F);
         float hurtPC = this.getHurtPC(target);
         if (hurtPC > 0.0F) {
            int hurtColor = this.multAlpha(ColorUtils.rgb(255, 3, 3), this.crystalAnimation);
            color = this.overCol(color, hurtColor, hurtPC);
            glowColor = this.overCol(glowColor, this.multAlpha(hurtColor, 0.65F), hurtPC);
         }

         float entityWidth = target != null ? target.method_17681() : this.lastTargetWidth;
         float entityHeight = target != null ? target.method_17682() : this.lastTargetHeight;
         float width = entityWidth * 1.5F;
         class_243 renderPos;
         if (target != null && target.method_5805()) {
            renderPos = new class_243(
               class_3532.method_16436(partialTicks, target.field_6038, target.method_23317()),
               class_3532.method_16436(partialTicks, target.field_5971, target.method_23318()),
               class_3532.method_16436(partialTicks, target.field_5989, target.method_23321())
            );
         } else {
            renderPos = this.lastTargetPos;
         }

         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         float orbitScale = 1.2F - 0.5F * this.crystalAnimation;
         ms.method_22903();
         ms.method_22904(renderPos.field_1352 - cameraPos.field_1352, renderPos.field_1351 - cameraPos.field_1351, renderPos.field_1350 - cameraPos.field_1350);
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(class_10142.field_53876);
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27379, class_290.field_1576);

         for (int i = 0; i < 360; i += 20) {
            float angleRad = (float)Math.toRadians(i + this.crystalRotationAngle);
            float sin = (float)(Math.sin(angleRad) * width * orbitScale);
            float cos = (float)(Math.cos(angleRad) * width * orbitScale);
            float crystalSize = 0.1F;
            float yOffset = 0.1F + entityHeight * Math.abs(class_3532.method_15374(i));
            float targetCenterY = entityHeight / 2.0F;
            float dirX = -sin;
            float dirY = targetCenterY - yOffset;
            float dirZ = -cos;
            float length = (float)Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (!(length < 0.001F)) {
               dirX /= length;
               dirY /= length;
               dirZ /= length;
               ms.method_22903();
               ms.method_46416(sin, yOffset, cos);
               Vector3f initial = new Vector3f(0.0F, 1.0F, 0.0F);
               Vector3f dir = new Vector3f(dirX, dirY, dirZ);
               Vector3f axis = new Vector3f();
               initial.cross(dir, axis);
               float axisLen = axis.length();
               if (axisLen >= 0.001F) {
                  axis.div(axisLen);
                  float dot = Math.max(-1.0F, Math.min(1.0F, initial.dot(dir)));
                  float angle = (float)Math.acos(dot);
                  ms.method_22907(new Quaternionf().setAngleAxis(angle, axis.x, axis.y, axis.z));
               }

               this.renderCrystalShape(buffer, ms.method_23760().method_23761(), crystalSize, color);
               ms.method_22909();
            }
         }

         class_286.method_43433(buffer.method_60800());
         ms.method_22909();
         float glowBaseSize = 4.5F + entityWidth * 3.0F;
         float outerGlowSize = glowBaseSize * 1.28F;
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, this.getBloomTexture());

         for (int ix = 0; ix < 360; ix += 20) {
            float angleRad = (float)Math.toRadians(ix + this.crystalRotationAngle);
            float sin = (float)(Math.sin(angleRad) * width * orbitScale);
            float cos = (float)(Math.cos(angleRad) * width * orbitScale);
            float yOffset = 0.1F + entityHeight * Math.abs(class_3532.method_15374(ix));
            double worldX = renderPos.field_1352 + sin;
            double worldY = renderPos.field_1351 + yOffset;
            double worldZ = renderPos.field_1350 + cos;
            this.drawBillboard(ms, cameraPos, worldX, worldY, worldZ, outerGlowSize, this.multAlpha(glowColor, 0.24F), this.crystalRotationAngle + ix);
            this.drawBillboard(ms, cameraPos, worldX, worldY, worldZ, glowBaseSize, glowColor, -(this.crystalRotationAngle + ix * 0.5F));
         }

         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.depthMask(true);
      }
   }

   private void renderCrystalShape(class_287 buffer, Matrix4f matrix, float size, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      float w = 0.34F * size / 0.1F;
      float h = 1.15F * size / 0.1F;
      w = 0.06F;
      h = 0.2F;
      this.tri(buffer, matrix, 0.0F, h, 0.0F, w, 0.0F, 0.0F, 0.0F, 0.0F, w, r, g, b, a);
      this.tri(buffer, matrix, 0.0F, h, 0.0F, 0.0F, 0.0F, w, -w, 0.0F, 0.0F, r, g, b, a);
      this.tri(buffer, matrix, 0.0F, h, 0.0F, -w, 0.0F, 0.0F, 0.0F, 0.0F, -w, r, g, b, a);
      this.tri(buffer, matrix, 0.0F, h, 0.0F, 0.0F, 0.0F, -w, w, 0.0F, 0.0F, r, g, b, a);
      this.tri(buffer, matrix, 0.0F, -h, 0.0F, w, 0.0F, 0.0F, 0.0F, 0.0F, w, r, g, b, a);
      this.tri(buffer, matrix, 0.0F, -h, 0.0F, 0.0F, 0.0F, w, -w, 0.0F, 0.0F, r, g, b, a);
      this.tri(buffer, matrix, 0.0F, -h, 0.0F, -w, 0.0F, 0.0F, 0.0F, 0.0F, -w, r, g, b, a);
      this.tri(buffer, matrix, 0.0F, -h, 0.0F, 0.0F, 0.0F, -w, w, 0.0F, 0.0F, r, g, b, a);
   }

   private float[] project2D(double worldX, double worldY, double worldZ) {
      return null;
   }

   private double getScale(double worldX, double worldY, double worldZ) {
      class_243 cam = mc.field_1773.method_19418().method_19326();
      double dx = worldX - cam.field_1352;
      double dy = worldY - cam.field_1351;
      double dz = worldZ - cam.field_1350;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return Math.max(0.5, 8.0 / Math.max(0.1, distance));
   }

   private void drawTexturedRect2D(class_4587 matrix, float x, float y, float width, float height, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      if (a > 0) {
         Matrix4f mat = matrix.method_23760().method_23761();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
         buffer.method_22918(mat, x, y, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x, y + height, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x + width, y + height, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x + width, y, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
         class_286.method_43433(buffer.method_60800());
      }
   }

   private void drawCrystalGlow2D(class_4587 matrix, class_1309 target) {
   }

   private void tri(
      class_287 buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a
   ) {
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(r, g, b, a);
   }

   private static class GlowPoint {
      final float x;
      final float y;
      final float z;
      final long startTime;
      final int maxLife;
      final int baseColor;

      GlowPoint(float x, float y, float z, int maxLife, int baseColor) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.startTime = System.currentTimeMillis();
         this.maxLife = maxLife;
         this.baseColor = baseColor;
      }

      boolean shouldRemove() {
         return System.currentTimeMillis() - this.startTime >= this.maxLife;
      }

      float getTimeProgress() {
         return class_3532.method_15363((float)(System.currentTimeMillis() - this.startTime) / this.maxLife, 0.0F, 1.0F);
      }

      int getColor(float timePC) {
         int a = (int)((this.baseColor >> 24 & 0xFF) * (1.0F - timePC));
         a = Math.max(0, Math.min(255, a));
         return a << 24 | this.baseColor & 16777215;
      }
   }
}
