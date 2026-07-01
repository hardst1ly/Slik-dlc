package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_10142;
import net.minecraft.class_243;
import net.minecraft.class_2663;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class TotemAngel extends Module {
   public static TotemAngel INSTANCE = new TotemAngel();
   private final ModeSetting mode = new ModeSetting("Режим", "Angel", "Angel");
   private final BooleanSetting visuals = new BooleanSetting("Визуал", true);
   private final BooleanSetting chatInfo = new BooleanSetting("Чат инфо", true);
   private final FloatSetting riseHeight = new FloatSetting("Высота", 4.0F, 0.2F, 10.0F, 0.1F);
   private final FloatSetting duration = new FloatSetting("Длительность", 3.0F, 0.2F, 6.0F, 0.1F);
   private final ListSetting renderModes = new ListSetting("Режим", new BooleanSetting("Ангел", true));
   private static final float WING_SCALE = 1.0F;
   private static final float FLAP_SPEED = 1.6F;
   private static final float FLAP_AMPLITUDE = 25.0F;
   private static final float GLOW_INTENSITY = 0.1F;
   private static final float HALO_SIZE = 0.4F;
   private static final class_2960 SPARKLE_TEXTURE = class_2960.method_60655("slikdlc", "textures/particle/sparkle.png");
   private static final int GREEN_COLOR = -13238485;
   private static final int YELLOW_COLOR = -3797;
   private final List<TotemAngel.TotemGhost> ghosts = new CopyOnWriteArrayList<>();
   private final List<TotemAngel.TotemSphereEffect> sphereEffects = new CopyOnWriteArrayList<>();
   private final Map<Integer, Long> recentSphereSpawns = new ConcurrentHashMap<>();

   public TotemAngel() {
      super("TotemPop", "Отображает эффект и пишет в чат при срабатывании тотема", Module.ModuleCategory.RENDER);
      this.addSettings(
         new Setting[]{this.renderModes, this.mode.visible(() -> false), this.visuals.visible(() -> false), this.chatInfo, this.riseHeight, this.duration}
      );
   }

   @Override
   public void onDisable() {
      this.ghosts.clear();
      super.onDisable();
   }

   private class_2960 getGlowTexture() {
      return class_2960.method_60655("slikdlc", "textures/targetesp/bloom.png");
   }

   private class_2960 getSkinTexture() {
      return class_2960.method_60655("slikdlc", "textures/skin/skin.png");
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (mc.field_1687 != null && mc.field_1724 != null && event.getType() == EventPacket.Type.RECEIVE) {
         if (event.getPacket() instanceof class_2663 packet && packet.method_11470() == 35) {
            mc.execute(() -> this.handleTotemPopPacket(packet));
         }
      }
   }

   private void handleTotemPopPacket(class_2663 packet) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (packet.method_11469(mc.field_1687) instanceof class_742 player) {
            if (this.renderModes.is("Ангел")) {
               this.addGhost(player);
            }

            if (this.chatInfo.isState() && player != mc.field_1724) {
               String name = player.method_5477().getString();
               ChatUtils.sendMessage(name + " §7снёс тотем!");
            }
         }
      }
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (this.renderModes.is("Ангел") && !this.ghosts.isEmpty()) {
            this.renderGhosts(event.getMatrices(), event.getTickDelta());
         }
      }
   }

   private void addGhost(class_742 player) {
      float partialTicks = mc.method_61966().method_60637(true);
      double x = class_3532.method_16436(partialTicks, player.field_6038, player.method_23317());
      double y = class_3532.method_16436(partialTicks, player.field_5971, player.method_23318());
      double z = class_3532.method_16436(partialTicks, player.field_5989, player.method_23321());
      float bodyYaw = class_3532.method_16439(partialTicks, player.field_6220, player.field_6283);
      float headYaw = class_3532.method_16439(partialTicks, player.field_6259, player.field_6241);
      float headPitch = class_3532.method_16439(partialTicks, player.field_6004, player.method_36455());
      float limbSwing = player.field_42108.method_48572(partialTicks);
      float limbSwingAmount = player.field_42108.method_48570(partialTicks);
      boolean sneaking = player.method_5715();
      float height = player.method_17682();
      this.ghosts
         .add(
            new TotemAngel.TotemGhost(
               new class_243(x, y, z), bodyYaw, headYaw - bodyYaw, headPitch, limbSwing, limbSwingAmount, sneaking, height, System.currentTimeMillis()
            )
         );
   }

   private void addSphereEffect(class_742 player) {
      if (player != null && player != mc.field_1724) {
         long now = System.currentTimeMillis();
         this.recentSphereSpawns.entrySet().removeIf(entry -> now - entry.getValue() > 1000L);
         Long lastSpawn = this.recentSphereSpawns.get(player.method_5628());
         if (lastSpawn == null || now - lastSpawn >= 120L) {
            this.recentSphereSpawns.put(player.method_5628(), now);
            double centerY = player.method_23318() + player.method_17682() * 0.62;
            List<TotemAngel.SphereParticle> particles = new ArrayList<>(64);
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < 64; i++) {
               double yaw = random.nextDouble(0.0, Math.PI * 2);
               double pitch = random.nextDouble(-0.8, 0.8);
               class_243 direction = new class_243(
                     Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch) * 0.62 + random.nextDouble(-0.12, 0.24), Math.sin(yaw) * Math.cos(pitch)
                  )
                  .method_1029();
               particles.add(
                  new TotemAngel.SphereParticle(
                     direction,
                     random.nextFloat(0.28F, 1.08F),
                     random.nextFloat(0.85F, 1.55F),
                     random.nextFloat(1.05F, 1.85F),
                     random.nextFloat(0.95F, 1.45F),
                     random.nextFloat(0.0F, 1.0F),
                     random.nextBoolean() ? -13238485 : -3797
                  )
               );
            }

            this.sphereEffects
               .add(
                  new TotemAngel.TotemSphereEffect(
                     new class_243(player.method_23317(), centerY, player.method_23321()),
                     now,
                     random.nextFloat(0.0F, 360.0F),
                     particles,
                     this.createSphereOrbitLines()
                  )
               );
         }
      }
   }

   private void renderGhosts(class_4587 matrices, float tickDelta) {
      class_243 cameraPos = mc.field_1773.method_19418().method_19326();
      long now = System.currentTimeMillis();
      List<TotemAngel.TotemGhost> toRemove = new ArrayList<>();
      int themeColor = ColorUtils.getThemeColor();
      float r = ColorUtils.redf(themeColor);
      float g = ColorUtils.greenf(themeColor);
      float b = ColorUtils.bluef(themeColor);

      for (TotemAngel.TotemGhost ghost : this.ghosts) {
         float progress = (float)(now - ghost.startTime) / (this.duration.get() * 1000.0F);
         if (progress >= 1.0F) {
            toRemove.add(ghost);
         } else {
            double motionY = this.riseHeight.get() * this.easeOutCubic(progress);
            float alpha = (1.0F - this.easeInCubic(progress)) * 0.85F;
            double renderX = ghost.position.field_1352 - cameraPos.field_1352;
            double renderY = ghost.position.field_1351 - cameraPos.field_1351 + motionY;
            double renderZ = ghost.position.field_1350 - cameraPos.field_1350;
            matrices.method_22903();
            matrices.method_22904(renderX, renderY, renderZ);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-ghost.bodyYaw));
            this.renderGlowingPlayerModel(matrices, r, g, b, alpha, ghost);
            this.renderWings(matrices, ghost, progress, tickDelta, themeColor, alpha);
            this.renderHalo(matrices, ghost, themeColor, alpha);
            matrices.method_22909();
         }
      }

      if (!toRemove.isEmpty()) {
         this.ghosts.removeAll(toRemove);
      }
   }

   private void renderSphereEffects(class_4587 matrices, class_243 cameraPos) {
      long now = System.currentTimeMillis();
      float sphereDurationMs = this.duration.get() * 1000.0F;
      this.sphereEffects.removeIf(effect -> (float)(now - effect.startTime) >= sphereDurationMs);
      if (!this.sphereEffects.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         this.renderSphereParticles(matrices, cameraPos, now, sphereDurationMs);
         this.renderSphereArcs(matrices, cameraPos, now, sphereDurationMs);
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private void renderSphereParticles(class_4587 matrices, class_243 cameraPos, long now, float durationMs) {
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShaderTexture(0, SPARKLE_TEXTURE);
      RenderSystem.setShader(class_10142.field_53880);
      float cameraYaw = mc.field_1773.method_19418().method_19330();
      float cameraPitch = mc.field_1773.method_19418().method_19329();
      float baseRadius = 1.18F;
      float baseSize = 0.28F;

      for (TotemAngel.TotemSphereEffect effect : this.sphereEffects) {
         float age = (float)(now - effect.startTime) / durationMs;
         float appear = class_3532.method_15363(1.0F - age, 0.0F, 1.0F);
         float burstProgress = this.easeOutQuad(Math.min(1.0F, age * 1.12F));

         for (TotemAngel.SphereParticle particle : effect.particles) {
            float localProgress = class_3532.method_15363(age * particle.timeScale + particle.progressOffset * 0.1F, 0.0F, 1.0F);
            float launchProgress = this.easeOutQuad(localProgress);
            float radial = (0.34F + launchProgress * (1.2F + particle.spread * 1.05F) + burstProgress * 0.32F) * baseRadius;
            float orbit = (float)now * 0.0012F * particle.rotationScale + particle.progressOffset * 5.4F;
            double swirlScale = (1.0F - localProgress) * 0.18F;
            double swirlX = Math.cos(orbit) * swirlScale * particle.swirlAmount;
            double swirlY = Math.sin(orbit * 1.3F) * swirlScale * 0.75 * particle.swirlAmount + localProgress * 0.08F;
            double swirlZ = Math.sin(orbit) * swirlScale * particle.swirlAmount;
            double dragY = localProgress * localProgress * 0.14F;
            class_243 worldPos = effect.origin.method_1019(particle.direction.method_1021(radial)).method_1031(swirlX, swirlY - dragY, swirlZ);
            double x = worldPos.field_1352 - cameraPos.field_1352;
            double y = worldPos.field_1351 - cameraPos.field_1351;
            double z = worldPos.field_1350 - cameraPos.field_1350;
            int color = this.setAlpha(particle.color, (int)(255.0F * appear * (0.5F + 0.5F * (1.0F - localProgress))));
            float drawSize = baseSize * (0.68F + particle.spread * 0.34F) * (0.7F + appear * 0.52F);
            matrices.method_22903();
            matrices.method_22904(x, y, z);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-cameraYaw));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(cameraPitch));
            this.drawSphereBillboard(matrices.method_23760().method_23761(), drawSize, color);
            matrices.method_22909();
         }
      }
   }

   private void renderSphereArcs(class_4587 matrices, class_243 cameraPos, long now, float durationMs) {
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(class_10142.field_53876);
      RenderSystem.lineWidth(1.05F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);

      for (TotemAngel.TotemSphereEffect effect : this.sphereEffects) {
         float age = (float)(now - effect.startTime) / durationMs;
         float appear = class_3532.method_15363(1.0F - age, 0.0F, 1.0F);
         float grow = this.easeOutQuad(Math.min(1.0F, age * 1.25F));
         float elapsedSec = (float)(now - effect.startTime) / 1000.0F;
         float scale = 1.18F * (0.78F + grow * 0.1F);
         double x = effect.origin.field_1352 - cameraPos.field_1352;
         double y = effect.origin.field_1351 - cameraPos.field_1351;
         double z = effect.origin.field_1350 - cameraPos.field_1350;

         for (TotemAngel.OrbitLine line : effect.orbitLines) {
            matrices.method_22903();
            matrices.method_22904(x, y, z);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(effect.baseRotation + line.baseYaw + elapsedSec * line.speedDeg));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(line.tiltX));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(line.tiltZ));
            this.drawSphereOrbitArc(
               matrices,
               line.radiusX * scale,
               line.radiusZ * scale,
               line.yOffset,
               line.startDeg,
               line.arcDeg,
               appear * line.alphaMul,
               line.startColor,
               line.endColor
            );
            matrices.method_22909();
         }
      }

      GL11.glDisable(2848);
   }

   private void drawSphereOrbitArc(
      class_4587 matrices, float radiusX, float radiusZ, float y, float startDeg, float arcDeg, float alphaMul, int startColor, int endColor
   ) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      int segments = 28;
      float from = (float)Math.toRadians(startDeg);
      float to = (float)Math.toRadians(startDeg + arcDeg);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29345, class_290.field_1576);

      for (int i = 0; i <= segments; i++) {
         float progress = (float)i / segments;
         float angle = class_3532.method_16439(progress, from, to);
         float px = class_3532.method_15362(angle) * radiusX;
         float pz = class_3532.method_15374(angle) * radiusZ;
         float localY = y + class_3532.method_15374(angle * 1.35F) * 0.01F;
         float edgeFade = class_3532.method_15363(1.0F - Math.abs(progress - 0.5F) * 2.0F, 0.0F, 1.0F);
         int color = this.fadeLerp(startColor, endColor, progress, alphaMul * (0.22F + 0.78F * edgeFade));
         buffer.method_22918(matrix, px, localY, pz).method_39415(color);
      }

      class_286.method_43433(buffer.method_60800());
      class_287 echo = class_289.method_1348().method_60827(class_5596.field_29345, class_290.field_1576);

      for (int i = 0; i <= segments; i++) {
         float progress = (float)i / segments;
         float angle = class_3532.method_16439(progress, from + 0.015F, to - 0.012F);
         float px = class_3532.method_15362(angle) * (radiusX + 0.012F);
         float pz = class_3532.method_15374(angle) * (radiusZ + 0.012F);
         float localY = y + 0.004F + class_3532.method_15374(angle * 1.35F + 0.9F) * 0.008F;
         float edgeFade = class_3532.method_15363(1.0F - Math.abs(progress - 0.5F) * 2.0F, 0.0F, 1.0F);
         int color = this.fadeLerp(startColor, endColor, progress, alphaMul * 0.14F * edgeFade);
         echo.method_22918(matrix, px, localY, pz).method_39415(color);
      }

      class_286.method_43433(echo.method_60800());
   }

   private List<TotemAngel.OrbitLine> createSphereOrbitLines() {
      List<TotemAngel.OrbitLine> lines = new ArrayList<>(5);
      lines.add(new TotemAngel.OrbitLine(1.02F, 0.66F, 0.2F, 196.0F, 156.0F, 14.0F, -12.0F, 54.0F, 0.46F, -13238485, -13238485));
      lines.add(new TotemAngel.OrbitLine(0.92F, 0.6F, 0.16F, 188.0F, 148.0F, 14.0F, -12.0F, 54.0F, 0.22F, -13238485, -13238485));
      lines.add(new TotemAngel.OrbitLine(0.86F, 0.54F, -0.12F, 122.0F, 112.0F, 78.0F, 4.0F, -68.0F, 0.65F, -3797, -3797));
      lines.add(new TotemAngel.OrbitLine(0.74F, 0.46F, -0.02F, 314.0F, 88.0F, 62.0F, -18.0F, 76.0F, 0.58F, -13238485, -3797));
      lines.add(new TotemAngel.OrbitLine(0.68F, 0.34F, 0.0F, 202.0F, 44.0F, 8.0F, 52.0F, -44.0F, 0.18F, -13238485, -13238485));
      return lines;
   }

   private void drawSphereBillboard(Matrix4f matrix, float size, int color) {
      float half = size * 0.5F;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      buffer.method_22918(matrix, -half, -half, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, -half, half, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, half, half, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, half, -half, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
      class_286.method_43433(buffer.method_60800());
   }

   private int fadeLerp(int start, int end, float progress, float alphaMul) {
      int sr = start >> 16 & 0xFF;
      int sg = start >> 8 & 0xFF;
      int sb = start & 0xFF;
      int er = end >> 16 & 0xFF;
      int eg = end >> 8 & 0xFF;
      int eb = end & 0xFF;
      int r = class_3532.method_48781(progress, sr, er);
      int g = class_3532.method_48781(progress, sg, eg);
      int b = class_3532.method_48781(progress, sb, eb);
      int a = class_3532.method_15340((int)(255.0F * alphaMul), 0, 255);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private int setAlpha(int color, int alpha) {
      return class_3532.method_15340(alpha, 0, 255) << 24 | color & 16777215;
   }

   private float easeOutQuad(float value) {
      float inv = 1.0F - value;
      return 1.0F - inv * inv;
   }

   private void renderSkinPlayerModel(class_4587 matrices, float alpha, TotemAngel.TotemGhost ghost) {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 771);
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.setShaderTexture(0, this.getSkinTexture());
      RenderSystem.setShader(class_10142.field_53880);
      float unit = 0.0625F;
      float sneakOffset = ghost.sneaking ? 0.25F : 0.0F;
      float limbSwing = ghost.limbSwing;
      float limbSwingAmount = Math.min(1.0F, ghost.limbSwingAmount);
      float legSwing = class_3532.method_15362(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      float armSwing = class_3532.method_15362(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount;
      int alphaInt = (int)(alpha * 255.0F);
      matrices.method_22903();
      matrices.method_46416(0.0F, 24.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(ghost.netHeadYaw));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(ghost.headPitch));
      this.renderSkinBox(matrices, -4.0F * unit, -8.0F * unit, -4.0F * unit, 8.0F * unit, 8.0F * unit, 8.0F * unit, 8, 8, 16, 16, 64, 64, alphaInt);
      matrices.method_22909();
      matrices.method_22903();
      if (ghost.sneaking) {
         matrices.method_46416(0.0F, 12.0F * unit, 0.0F);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(28.0F));
         matrices.method_46416(0.0F, -12.0F * unit, 0.0F);
      }

      this.renderSkinBox(
         matrices, -4.0F * unit, 12.0F * unit - sneakOffset, -2.0F * unit, 8.0F * unit, 12.0F * unit, 4.0F * unit, 20, 20, 28, 32, 64, 64, alphaInt
      );
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(-5.0F * unit, 22.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(armSwing * (180.0F / (float)Math.PI)));
      this.renderSkinBox(matrices, -2.0F * unit, -12.0F * unit, -2.0F * unit, 4.0F * unit, 12.0F * unit, 4.0F * unit, 44, 20, 48, 32, 64, 64, alphaInt);
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(5.0F * unit, 22.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(-armSwing * (180.0F / (float)Math.PI)));
      this.renderSkinBox(matrices, -2.0F * unit, -12.0F * unit, -2.0F * unit, 4.0F * unit, 12.0F * unit, 4.0F * unit, 36, 52, 40, 64, 64, 64, alphaInt);
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(-2.0F * unit, 12.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(legSwing * (180.0F / (float)Math.PI)));
      this.renderSkinBox(matrices, -2.0F * unit, -12.0F * unit, -2.0F * unit, 4.0F * unit, 12.0F * unit, 4.0F * unit, 4, 20, 8, 32, 64, 64, alphaInt);
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(2.0F * unit, 12.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(-legSwing * (180.0F / (float)Math.PI)));
      this.renderSkinBox(matrices, -2.0F * unit, -12.0F * unit, -2.0F * unit, 4.0F * unit, 12.0F * unit, 4.0F * unit, 20, 52, 24, 64, 64, 64, alphaInt);
      matrices.method_22909();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }

   private void renderSkinBox(
      class_4587 matrices,
      float x,
      float y,
      float z,
      float width,
      float height,
      float depth,
      int u,
      int v,
      int u2,
      int v2,
      int texWidth,
      int texHeight,
      int alpha
   ) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      float x2 = x + width;
      float y2 = y + height;
      float z2 = z + depth;
      float w = width * 16.0F;
      float h = height * 16.0F;
      float d = depth * 16.0F;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      float uMin = (float)u / texWidth;
      float vMin = (float)v / texHeight;
      float uMax = (float)u2 / texWidth;
      float vMax = (float)v2 / texHeight;
      float frontU1 = (u + d) / texWidth;
      float frontU2 = (u + d + w) / texWidth;
      float frontV1 = (v + d) / texHeight;
      float frontV2 = (v + d + h) / texHeight;
      buffer.method_22918(matrix, x, y, z2).method_22913(frontU1, frontV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y, z2).method_22913(frontU2, frontV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y2, z2).method_22913(frontU2, frontV1).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y2, z2).method_22913(frontU1, frontV1).method_1336(255, 255, 255, alpha);
      float backU1 = (u + d + w + d) / texWidth;
      float backU2 = (u + d + w + d + w) / texWidth;
      float backV1 = (v + d) / texHeight;
      float backV2 = (v + d + h) / texHeight;
      buffer.method_22918(matrix, x2, y, z).method_22913(backU1, backV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y, z).method_22913(backU2, backV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y2, z).method_22913(backU2, backV1).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y2, z).method_22913(backU1, backV1).method_1336(255, 255, 255, alpha);
      float topU1 = (u + d) / texWidth;
      float topU2 = (u + d + w) / texWidth;
      float topV1 = (float)v / texHeight;
      float topV2 = (v + d) / texHeight;
      buffer.method_22918(matrix, x, y2, z).method_22913(topU1, topV1).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y2, z2).method_22913(topU1, topV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y2, z2).method_22913(topU2, topV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y2, z).method_22913(topU2, topV1).method_1336(255, 255, 255, alpha);
      float bottomU1 = (u + d + w) / texWidth;
      float bottomU2 = (u + d + w + w) / texWidth;
      float bottomV1 = (float)v / texHeight;
      float bottomV2 = (v + d) / texHeight;
      buffer.method_22918(matrix, x, y, z2).method_22913(bottomU1, bottomV1).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y, z).method_22913(bottomU1, bottomV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y, z).method_22913(bottomU2, bottomV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y, z2).method_22913(bottomU2, bottomV1).method_1336(255, 255, 255, alpha);
      float rightU1 = (float)u / texWidth;
      float rightU2 = (u + d) / texWidth;
      float rightV1 = (v + d) / texHeight;
      float rightV2 = (v + d + h) / texHeight;
      buffer.method_22918(matrix, x, y, z).method_22913(rightU1, rightV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y, z2).method_22913(rightU2, rightV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y2, z2).method_22913(rightU2, rightV1).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x, y2, z).method_22913(rightU1, rightV1).method_1336(255, 255, 255, alpha);
      float leftU1 = (u + d + w) / texWidth;
      float leftU2 = (u + d + w + d) / texWidth;
      float leftV1 = (v + d) / texHeight;
      float leftV2 = (v + d + h) / texHeight;
      buffer.method_22918(matrix, x2, y, z2).method_22913(leftU1, leftV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y, z).method_22913(leftU2, leftV2).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y2, z).method_22913(leftU2, leftV1).method_1336(255, 255, 255, alpha);
      buffer.method_22918(matrix, x2, y2, z2).method_22913(leftU1, leftV1).method_1336(255, 255, 255, alpha);
      class_286.method_43433(buffer.method_60800());
   }

   private void renderWings(class_4587 matrices, TotemAngel.TotemGhost ghost, float progress, float tickDelta, int themeColor, float alpha) {
      float anim = (float)System.currentTimeMillis() / 50.0F * 0.22F * 1.6F + progress * 2.0F;
      float sin = class_3532.method_15374(anim);
      float cos = class_3532.method_15362(anim);
      float spreadAngle = 18.0F + progress * 15.0F;
      float pitchAngle = 13.0F + cos * 4.0F;
      float rollAngle = sin * 25.0F;
      if (ghost.sneaking) {
         spreadAngle -= 3.0F;
         pitchAngle += 8.0F;
      }

      int topColor = ColorUtils.setAlphaColor(themeColor, (int)(132.0F * alpha));
      int bottomColor = ColorUtils.setAlphaColor(ColorUtils.darken(themeColor, 0.85F), (int)(102.0F * alpha));
      int edgeColor = ColorUtils.setAlphaColor(ColorUtils.darken(themeColor, 0.7F), (int)(190.0F * alpha));
      int boneColorA = ColorUtils.setAlphaColor(ColorUtils.darken(themeColor, 0.52F), (int)(175.0F * alpha));
      int boneColorB = ColorUtils.setAlphaColor(ColorUtils.darken(themeColor, 0.58F), (int)(150.0F * alpha));
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.blendFunc(770, 771);
      RenderSystem.setShader(class_10142.field_53876);
      matrices.method_22903();
      float sneakOffset = ghost.sneaking ? 0.25F : 0.0F;
      matrices.method_46416(0.0F, 1.3F - sneakOffset, -0.08F);
      if (ghost.sneaking) {
         matrices.method_22907(class_7833.field_40714.rotationDegrees(24.0F));
      }

      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      this.renderButterflyWing(buffer, matrices, 1.0F, spreadAngle, pitchAngle, rollAngle, 1.0F, topColor, bottomColor, edgeColor, boneColorA, boneColorB);
      this.renderButterflyWing(buffer, matrices, -1.0F, spreadAngle, pitchAngle, rollAngle, 1.0F, topColor, bottomColor, edgeColor, boneColorA, boneColorB);
      class_286.method_43433(buffer.method_60800());
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShader(class_10142.field_53876);
      int glowA = ColorUtils.setAlphaColor(themeColor, (int)(72.0F * alpha));
      int glowB = ColorUtils.setAlphaColor(ColorUtils.darken(themeColor, 0.82F), (int)(66.0F * alpha));
      class_287 glowBuffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      this.renderButterflyGlow(glowBuffer, matrices, 1.0F, spreadAngle, pitchAngle, rollAngle, 1.0F, glowA, glowB);
      this.renderButterflyGlow(glowBuffer, matrices, -1.0F, spreadAngle, pitchAngle, rollAngle, 1.0F, glowA, glowB);
      class_286.method_43433(glowBuffer.method_60800());
      matrices.method_22909();
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
   }

   private void renderButterflyWing(
      class_287 buffer,
      class_4587 matrices,
      float side,
      float spread,
      float pitch,
      float roll,
      float scale,
      int topColor,
      int bottomColor,
      int edgeColor,
      int boneColorA,
      int boneColorB
   ) {
      float root = 0.12F * scale;
      float topW = 1.5F * scale;
      float topH = 0.61F * scale;
      float lowW = 1.1F * scale;
      float lowH = 0.35F * scale;
      matrices.method_22903();
      matrices.method_46416(0.15F * side, 0.0F, -0.17F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * spread));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(side * roll));
      Matrix4f matrix = matrices.method_23760().method_23761();
      this.addDoubleSidedGradientQuad(
         buffer,
         matrix,
         side * root,
         0.02F,
         0.0F,
         side * (root + topW * 0.18F),
         topH * 0.95F,
         -0.06F,
         side * (root + topW),
         topH * 0.3F,
         -0.13F,
         side * (root + topW * 0.2F),
         0.06F,
         -0.03F,
         topColor,
         bottomColor
      );
      this.addDoubleSidedGradientQuad(
         buffer,
         matrix,
         side * root,
         -0.01F,
         -0.02F,
         side * (root + lowW * 0.18F),
         -lowH * 0.94F,
         -0.1F,
         side * (root + lowW),
         -lowH * 0.36F,
         -0.17F,
         side * (root + lowW * 0.6F),
         -0.1F,
         -0.07F,
         bottomColor,
         topColor
      );
      this.addDoubleSidedQuad(
         buffer,
         matrix,
         side * root,
         0.012F,
         0.01F,
         side * root,
         -0.032F,
         -0.01F,
         side * (root + topW * 0.56F),
         -0.008F,
         -0.08F,
         side * (root + topW * 0.56F),
         0.008F,
         -0.04F,
         edgeColor >> 16 & 0xFF,
         edgeColor >> 8 & 0xFF,
         edgeColor & 0xFF,
         edgeColor >> 24 & 0xFF
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.0F,
         -0.02F,
         side * (root + topW * 0.22F),
         topH * 0.82F,
         -0.07F,
         side * (root + topW),
         topH * 0.3F,
         -0.13F,
         0.016F * scale,
         boneColorB,
         boneColorB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.012F,
         -0.008F,
         side * (root + topW * 0.36F),
         topH * 0.56F,
         -0.065F,
         side * (root + topW * 0.86F),
         topH * 0.26F,
         -0.115F,
         0.012F * scale,
         boneColorA,
         boneColorB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         -0.02F,
         -0.04F,
         side * (root + lowW * 0.22F),
         -lowH * 0.84F,
         -0.11F,
         side * (root + lowW),
         -lowH * 0.34F,
         -0.18F,
         0.009F * scale,
         boneColorB,
         boneColorB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         -0.004F,
         -0.018F,
         side * (root + lowW * 0.34F),
         -lowH * 0.52F,
         -0.085F,
         side * (root + lowW * 0.88F),
         -lowH * 0.3F,
         -0.145F,
         0.01F * scale,
         boneColorB,
         boneColorA
      );
      matrices.method_22909();
   }

   private void renderButterflyGlow(class_287 buffer, class_4587 matrices, float side, float spread, float pitch, float roll, float scale, int glowA, int glowB) {
      float root = 0.12F * scale;
      float topW = 1.5F * scale;
      float topH = 0.61F * scale;
      float lowW = 1.1F * scale;
      float lowH = 0.35F * scale;
      matrices.method_22903();
      matrices.method_46416(0.15F * side, 0.0F, -0.17F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * spread));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(side * roll));
      Matrix4f matrix = matrices.method_23760().method_23761();
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.0F,
         -0.02F,
         side * (root + topW * 0.2F),
         topH * 0.86F,
         -0.08F,
         side * (root + topW),
         topH * 0.3F,
         -0.16F,
         0.02F * scale,
         glowA,
         glowB
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         -0.02F,
         -0.05F,
         side * (root + lowW * 0.2F),
         -lowH * 0.86F,
         -0.13F,
         side * (root + lowW),
         -lowH * 0.32F,
         -0.2F,
         0.018F * scale,
         glowB,
         glowA
      );
      this.renderWingBoneLine(
         buffer,
         matrix,
         side * root,
         0.012F,
         -0.008F,
         side * (root + topW * 0.36F),
         topH * 0.56F,
         -0.07F,
         side * (root + topW * 0.84F),
         topH * 0.25F,
         -0.125F,
         0.016F * scale,
         glowA,
         glowB
      );
      matrices.method_22909();
   }

   private void renderHalo(class_4587 matrices, TotemAngel.TotemGhost ghost, int themeColor, float alpha) {
      float sneakOffset = ghost.sneaking ? 0.25F : 0.0F;
      float rotation = (float)System.currentTimeMillis() / 30.0F % 360.0F;
      matrices.method_22903();
      matrices.method_46416(0.0F, 1.9F - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(15.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(rotation));
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShader(class_10142.field_53876);
      int haloColor = ColorUtils.setAlphaColor(themeColor, (int)(200.0F * alpha));
      int haloGlow = ColorUtils.setAlphaColor(themeColor, (int)(100.0F * alpha));
      Matrix4f matrix = matrices.method_23760().method_23761();
      this.renderHaloRing(matrix, 0.4F, 0.03F, haloColor);
      this.renderHaloRing(matrix, 0.42000002F, 0.05F, haloGlow);
      this.renderHaloRing(matrix, 0.38F, 0.02F, haloGlow);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      matrices.method_22909();
   }

   private void renderHaloRing(Matrix4f matrix, float radius, float thickness, int color) {
      int segments = 36;
      float angleStep = (float)((Math.PI * 2) / segments);
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);

      for (int i = 0; i < segments; i++) {
         float angle1 = i * angleStep;
         float angle2 = (i + 1) * angleStep;
         float x1Inner = class_3532.method_15362(angle1) * (radius - thickness / 2.0F);
         float z1Inner = class_3532.method_15374(angle1) * (radius - thickness / 2.0F);
         float x1Outer = class_3532.method_15362(angle1) * (radius + thickness / 2.0F);
         float z1Outer = class_3532.method_15374(angle1) * (radius + thickness / 2.0F);
         float x2Inner = class_3532.method_15362(angle2) * (radius - thickness / 2.0F);
         float z2Inner = class_3532.method_15374(angle2) * (radius - thickness / 2.0F);
         float x2Outer = class_3532.method_15362(angle2) * (radius + thickness / 2.0F);
         float z2Outer = class_3532.method_15374(angle2) * (radius + thickness / 2.0F);
         buffer.method_22918(matrix, x1Inner, 0.01F, z1Inner).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x1Outer, 0.01F, z1Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Outer, 0.01F, z2Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Inner, 0.01F, z2Inner).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x1Inner, -0.01F, z1Inner).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Inner, -0.01F, z2Inner).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Outer, -0.01F, z2Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x1Outer, -0.01F, z1Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x1Outer, -0.01F, z1Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Outer, -0.01F, z2Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Outer, 0.01F, z2Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x1Outer, 0.01F, z1Outer).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x1Inner, 0.01F, z1Inner).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Inner, 0.01F, z2Inner).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x2Inner, -0.01F, z2Inner).method_1336(r, g, b, a);
         buffer.method_22918(matrix, x1Inner, -0.01F, z1Inner).method_1336(r, g, b, a);
      }

      class_286.method_43433(buffer.method_60800());
   }

   private void addDoubleSidedQuad(
      class_287 buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int r,
      int g,
      int b,
      int a
   ) {
      this.addQuad(buffer, matrix, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, r, g, b, a);
      this.addQuad(buffer, matrix, x4, y4, z4, x3, y3, z3, x2, y2, z2, x1, y1, z1, r, g, b, a);
   }

   private void addDoubleSidedGradientQuad(
      class_287 buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int nearColor,
      int farColor
   ) {
      int nr = nearColor >> 16 & 0xFF;
      int ng = nearColor >> 8 & 0xFF;
      int nb = nearColor & 0xFF;
      int na = nearColor >> 24 & 0xFF;
      int fr = farColor >> 16 & 0xFF;
      int fg = farColor >> 8 & 0xFF;
      int fb = farColor & 0xFF;
      int fa = farColor >> 24 & 0xFF;
      buffer.method_22918(matrix, x1, y1, z1).method_1336(nr, ng, nb, na);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x4, y4, z4).method_1336(nr, ng, nb, na);
      buffer.method_22918(matrix, x4, y4, z4).method_1336(nr, ng, nb, na);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(fr, fg, fb, fa);
      buffer.method_22918(matrix, x1, y1, z1).method_1336(nr, ng, nb, na);
   }

   private void renderWingBoneLine(
      class_287 buffer,
      Matrix4f matrix,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float thickness,
      int colorA,
      int colorB
   ) {
      float vx1 = x1 - x0;
      float vy1 = y1 - y0;
      float len1 = Math.max(1.0E-4F, (float)Math.sqrt(vx1 * vx1 + vy1 * vy1));
      float nx1 = -vy1 / len1 * thickness;
      float ny1 = vx1 / len1 * thickness;
      int aR = colorA >> 16 & 0xFF;
      int aG = colorA >> 8 & 0xFF;
      int aB = colorA & 0xFF;
      int aA = colorA >> 24 & 0xFF;
      int bR = colorB >> 16 & 0xFF;
      int bG = colorB >> 8 & 0xFF;
      int bB = colorB & 0xFF;
      int bA = colorB >> 24 & 0xFF;
      this.addDoubleSidedQuad(buffer, matrix, x0 + nx1, y0 + ny1, z0, x0 - nx1, y0 - ny1, z0, x1 - nx1, y1 - ny1, z1, x1 + nx1, y1 + ny1, z1, aR, aG, aB, aA);
      float vx2 = x2 - x1;
      float vy2 = y2 - y1;
      float len2 = Math.max(1.0E-4F, (float)Math.sqrt(vx2 * vx2 + vy2 * vy2));
      float nx2 = -vy2 / len2 * thickness;
      float ny2 = vx2 / len2 * thickness;
      this.addDoubleSidedQuad(buffer, matrix, x1 + nx2, y1 + ny2, z1, x1 - nx2, y1 - ny2, z1, x2 - nx2, y2 - ny2, z2, x2 + nx2, y2 + ny2, z2, bR, bG, bB, bA);
   }

   private void addQuad(
      class_287 buffer,
      Matrix4f matrix,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int r,
      int g,
      int b,
      int a
   ) {
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x3, y3, z3).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x4, y4, z4).method_1336(r, g, b, a);
   }

   private void renderGlowingPlayerModel(class_4587 matrices, float r, float g, float b, float alpha, TotemAngel.TotemGhost ghost) {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.getGlowTexture());
      RenderSystem.setShader(class_10142.field_53880);
      float unit = 0.0625F;
      float sneakOffset = ghost.sneaking ? 0.25F : 0.0F;
      float limbSwing = ghost.limbSwing;
      float limbSwingAmount = Math.min(1.0F, ghost.limbSwingAmount);
      float legSwing = class_3532.method_15362(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      float armSwing = class_3532.method_15362(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
      matrices.method_22903();
      matrices.method_46416(0.0F, 24.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(ghost.netHeadYaw));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(ghost.headPitch));
      this.renderGlowBox(matrices, -4.0F * unit, -8.0F * unit, -4.0F * unit, 8.0F * unit, 8.0F * unit, 8.0F * unit, r, g, b, alpha * 0.1F);
      matrices.method_22909();
      matrices.method_22903();
      if (ghost.sneaking) {
         matrices.method_46416(0.0F, 12.0F * unit, 0.0F);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(25.0F));
         matrices.method_46416(0.0F, -12.0F * unit, 0.0F);
      }

      this.renderGlowBox(matrices, -4.0F * unit, 12.0F * unit - sneakOffset, -2.0F * unit, 8.0F * unit, 12.0F * unit, 4.0F * unit, r, g, b, alpha * 0.1F);
      matrices.method_22909();
      float armWidth = 3.0F * unit;
      matrices.method_22903();
      matrices.method_46416(-4.0F * unit - armWidth / 2.0F, 22.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(armSwing * (180.0F / (float)Math.PI)));
      this.renderGlowBox(matrices, -armWidth / 2.0F, -10.0F * unit, -2.0F * unit, armWidth, 12.0F * unit, 4.0F * unit, r, g, b, alpha * 0.1F);
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(4.0F * unit + armWidth / 2.0F, 22.0F * unit - sneakOffset, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(-armSwing * (180.0F / (float)Math.PI)));
      this.renderGlowBox(matrices, -armWidth / 2.0F, -10.0F * unit, -2.0F * unit, armWidth, 12.0F * unit, 4.0F * unit, r, g, b, alpha * 0.1F);
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(-2.0F * unit, 12.0F * unit, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(legSwing * (180.0F / (float)Math.PI)));
      this.renderGlowBox(matrices, -2.0F * unit, -12.0F * unit, -2.0F * unit, 4.0F * unit, 12.0F * unit, 4.0F * unit, r, g, b, alpha * 0.1F);
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(2.0F * unit, 12.0F * unit, 0.0F);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(-legSwing * (180.0F / (float)Math.PI)));
      this.renderGlowBox(matrices, -2.0F * unit, -12.0F * unit, -2.0F * unit, 4.0F * unit, 12.0F * unit, 4.0F * unit, r, g, b, alpha * 0.1F);
      matrices.method_22909();
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }

   private void renderGlowBox(class_4587 matrices, float x, float y, float z, float width, float height, float depth, float r, float g, float b, float alpha) {
      float centerX = x + width / 2.0F;
      float centerY = y + height / 2.0F;
      float centerZ = z + depth / 2.0F;
      float glowSize = Math.max(width, Math.max(height, depth)) * 1.8F;
      this.renderGlowSprite(matrices, centerX, centerY, centerZ + depth / 2.0F + 0.01F, glowSize, width, height, r, g, b, alpha);
      this.renderGlowSprite(matrices, centerX, centerY, centerZ - depth / 2.0F - 0.01F, glowSize, width, height, r, g, b, alpha);
      matrices.method_22903();
      matrices.method_46416(centerX, centerY, centerZ);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(90.0F));
      this.renderGlowSpriteRotated(matrices, 0.0F, 0.0F, depth / 2.0F + 0.01F, glowSize, depth, height, r, g, b, alpha);
      this.renderGlowSpriteRotated(matrices, 0.0F, 0.0F, -depth / 2.0F - 0.01F, glowSize, depth, height, r, g, b, alpha);
      matrices.method_22909();
      matrices.method_22903();
      matrices.method_46416(centerX, centerY, centerZ);
      matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
      this.renderGlowSpriteRotated(matrices, 0.0F, 0.0F, height / 2.0F + 0.01F, glowSize, width, depth, r, g, b, alpha);
      this.renderGlowSpriteRotated(matrices, 0.0F, 0.0F, -height / 2.0F - 0.01F, glowSize, width, depth, r, g, b, alpha);
      matrices.method_22909();
      float innerAlpha = alpha * 0.4F;
      RenderSystem.setShader(class_10142.field_53876);
      this.renderSolidBox(matrices, x, y, z, width, height, depth, r, g, b, innerAlpha);
      RenderSystem.setShaderTexture(0, this.getGlowTexture());
      RenderSystem.setShader(class_10142.field_53880);
   }

   private void renderGlowSprite(
      class_4587 matrices, float x, float y, float z, float glowSize, float boxWidth, float boxHeight, float r, float g, float b, float alpha
   ) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      int rInt = (int)(r * 255.0F);
      int gInt = (int)(g * 255.0F);
      int bInt = (int)(b * 255.0F);
      int aInt = (int)(class_3532.method_15363(alpha, 0.0F, 1.0F) * 255.0F);
      float halfW = glowSize / 2.0F;
      float halfH = glowSize / 2.0F;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      buffer.method_22918(matrix, x - halfW, y - halfH, z).method_22913(0.0F, 0.0F).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x - halfW, y + halfH, z).method_22913(0.0F, 1.0F).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x + halfW, y + halfH, z).method_22913(1.0F, 1.0F).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x + halfW, y - halfH, z).method_22913(1.0F, 0.0F).method_1336(rInt, gInt, bInt, aInt);
      class_286.method_43433(buffer.method_60800());
   }

   private void renderGlowSpriteRotated(
      class_4587 matrices, float x, float y, float z, float glowSize, float boxWidth, float boxHeight, float r, float g, float b, float alpha
   ) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      int rInt = (int)(r * 255.0F);
      int gInt = (int)(g * 255.0F);
      int bInt = (int)(b * 255.0F);
      int aInt = (int)(class_3532.method_15363(alpha, 0.0F, 1.0F) * 255.0F);
      float halfW = glowSize / 2.0F;
      float halfH = glowSize / 2.0F;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      buffer.method_22918(matrix, x - halfW, y - halfH, z).method_22913(0.0F, 0.0F).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x - halfW, y + halfH, z).method_22913(0.0F, 1.0F).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x + halfW, y + halfH, z).method_22913(1.0F, 1.0F).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x + halfW, y - halfH, z).method_22913(1.0F, 0.0F).method_1336(rInt, gInt, bInt, aInt);
      class_286.method_43433(buffer.method_60800());
   }

   private void renderSolidBox(class_4587 matrices, float x, float y, float z, float width, float height, float depth, float r, float g, float b, float alpha) {
      Matrix4f matrix = matrices.method_23760().method_23761();
      float x2 = x + width;
      float y2 = y + height;
      float z2 = z + depth;
      int rInt = (int)(r * 255.0F);
      int gInt = (int)(g * 255.0F);
      int bInt = (int)(b * 255.0F);
      int aInt = (int)(class_3532.method_15363(alpha, 0.0F, 1.0F) * 255.0F);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      buffer.method_22918(matrix, x, y, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y2, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y2, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y2, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y2, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y2, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y2, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y2, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y2, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y2, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y, z2).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x, y, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y, z).method_1336(rInt, gInt, bInt, aInt);
      buffer.method_22918(matrix, x2, y, z2).method_1336(rInt, gInt, bInt, aInt);
      class_286.method_43433(buffer.method_60800());
   }

   private float easeOutCubic(float t) {
      return 1.0F - (float)Math.pow(1.0 - t, 3.0);
   }

   private float easeInCubic(float t) {
      return t * t * t;
   }

   private static class OrbitLine {
      private final float radiusX;
      private final float radiusZ;
      private final float yOffset;
      private final float startDeg;
      private final float arcDeg;
      private final float tiltX;
      private final float tiltZ;
      private final float speedDeg;
      private final float alphaMul;
      private final int startColor;
      private final int endColor;
      private final float baseYaw;

      private OrbitLine(
         float radiusX,
         float radiusZ,
         float yOffset,
         float startDeg,
         float arcDeg,
         float tiltX,
         float tiltZ,
         float speedDeg,
         float alphaMul,
         int startColor,
         int endColor
      ) {
         this.radiusX = radiusX;
         this.radiusZ = radiusZ;
         this.yOffset = yOffset;
         this.startDeg = startDeg;
         this.arcDeg = arcDeg;
         this.tiltX = tiltX;
         this.tiltZ = tiltZ;
         this.speedDeg = speedDeg;
         this.alphaMul = alphaMul;
         this.startColor = startColor;
         this.endColor = endColor;
         this.baseYaw = startDeg * 0.35F;
      }
   }

   private static class SphereParticle {
      private final class_243 direction;
      private final float spread;
      private final float swirlAmount;
      private final float rotationScale;
      private final float timeScale;
      private final float progressOffset;
      private final int color;

      private SphereParticle(class_243 direction, float spread, float swirlAmount, float rotationScale, float timeScale, float progressOffset, int color) {
         this.direction = direction;
         this.spread = spread;
         this.swirlAmount = swirlAmount;
         this.rotationScale = rotationScale;
         this.timeScale = timeScale;
         this.progressOffset = progressOffset;
         this.color = color;
      }
   }

   private static class TotemGhost {
      final class_243 position;
      final float bodyYaw;
      final float netHeadYaw;
      final float headPitch;
      final float limbSwing;
      final float limbSwingAmount;
      final boolean sneaking;
      final float height;
      final long startTime;

      TotemGhost(
         class_243 position,
         float bodyYaw,
         float netHeadYaw,
         float headPitch,
         float limbSwing,
         float limbSwingAmount,
         boolean sneaking,
         float height,
         long startTime
      ) {
         this.position = position;
         this.bodyYaw = bodyYaw;
         this.netHeadYaw = netHeadYaw;
         this.headPitch = headPitch;
         this.limbSwing = limbSwing;
         this.limbSwingAmount = limbSwingAmount;
         this.sneaking = sneaking;
         this.height = height;
         this.startTime = startTime;
      }
   }

   private static class TotemSphereEffect {
      private final class_243 origin;
      private final long startTime;
      private final float baseRotation;
      private final List<TotemAngel.SphereParticle> particles;
      private final List<TotemAngel.OrbitLine> orbitLines;

      private TotemSphereEffect(
         class_243 origin, long startTime, float baseRotation, List<TotemAngel.SphereParticle> particles, List<TotemAngel.OrbitLine> orbitLines
      ) {
         this.origin = origin;
         this.startTime = startTime;
         this.baseRotation = baseRotation;
         this.particles = particles;
         this.orbitLines = orbitLines;
      }
   }
}
