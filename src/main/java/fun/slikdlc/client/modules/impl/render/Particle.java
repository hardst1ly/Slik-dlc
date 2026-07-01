package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventAttackEntity;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2663;
import net.minecraft.class_2680;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_239.class_240;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.joml.Matrix4f;

public class Particle extends Module {
   public static Particle INSTANCE = new Particle();
   private static final class_2960 STAR_TEXTURE = class_2960.method_60655("slikdlc", "textures/particle/star.png");
   private static final class_2960 HEART_TEXTURE = class_2960.method_60655("slikdlc", "textures/particle/heart.png");
   private static final class_2960 DOLLAR_TEXTURE = class_2960.method_60655("slikdlc", "textures/particle/dollar.png");
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("slikdlc", "textures/particle/bloom.png");
   private static final class_2960 SPARKLE_TEXTURE = class_2960.method_60655("slikdlc", "textures/particle/sparkle.png");
   private final ModeSetting type = new ModeSetting("Тип частиц", "Звездочки", "Звездочки", "Сердечки", "Доллары", "Блум", "Сияние");
   private final ListSetting reason = new ListSetting(
      "Добавлять при",
      new BooleanSetting("Бездействии", false),
      new BooleanSetting("Беге", false),
      new BooleanSetting("Ударе", true),
      new BooleanSetting("Падении перла", false),
      new BooleanSetting("Падении трезубца", false),
      new BooleanSetting("Сносе тотема", true)
   );
   private final FloatSetting count = new FloatSetting("Количество", 10.0F, 2.0F, 40.0F, 1.0F);
   private final FloatSetting particleSize = new FloatSetting("Размер", 0.3F, 0.1F, 1.0F, 0.05F);
   private final FloatSetting fallSpeed = new FloatSetting("Скорость падения", 7.0E-4F, 0.0F, 0.005F, 1.0E-4F);
   private final BooleanSetting glow = new BooleanSetting("Светяшка", true);
   private final ArrayList<Particle.ParticleData> particles = new ArrayList<>();
   private final Random rnd = new Random();

   public Particle() {
      super("Particles", "Красивые партиклы при разных действиях", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.type, this.reason, this.count, this.particleSize, this.fallSpeed, this.glow});
   }

   @Override
   public void onDisable() {
      this.particles.clear();
      super.onDisable();
   }

   private class_2960 getTexture() {
      return switch (this.type.getIndex()) {
         case 1 -> HEART_TEXTURE;
         case 2 -> DOLLAR_TEXTURE;
         case 3 -> BLOOM_TEXTURE;
         case 4 -> SPARKLE_TEXTURE;
         default -> STAR_TEXTURE;
      };
   }

   private boolean isPositionInBlock(class_243 position) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_2338 blockPos = class_2338.method_49638(position);
         if (mc.field_1687.method_8320(blockPos).method_26212(mc.field_1687, blockPos)) {
            return true;
         } else {
            class_3959 context = new class_3959(
               new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318() + mc.field_1724.method_5751(), mc.field_1724.method_23321()),
               position,
               class_3960.field_17558,
               class_242.field_1348,
               mc.field_1724
            );
            class_3965 result = mc.field_1687.method_17742(context);
            return result.method_17783() == class_240.field_1332;
         }
      } else {
         return true;
      }
   }

   private float random(float min, float max) {
      return min + this.rnd.nextFloat() * (max - min);
   }

   private boolean isMoving() {
      return mc.field_1724 != null && (mc.field_1724.field_3913.field_3905 != 0.0F || mc.field_1724.field_3913.field_3907 != 0.0F);
   }

   @EventLink
   public void onAttack(EventAttackEntity event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.reason.is("Ударе")) {
            class_1297 target = event.getTarget();
            if (target != null) {
               for (int i = 0; i < 35; i++) {
                  double targetX = target.method_23317() + this.random(-0.4F, 0.4F);
                  double targetY = target.method_23318() + this.random(-0.4F, target.method_17682() + 0.4F);
                  double targetZ = target.method_23321() + this.random(-0.4F, 0.4F);
                  if (!this.isPositionInBlock(new class_243(targetX, targetY, targetZ))) {
                     float baseMx = this.random(-0.8F, 0.8F) * 2.0F;
                     float baseMy = this.random(-0.25F, 1.4F);
                     float baseMz = this.random(-0.8F, 0.8F) * 2.0F;
                     class_243 velocity = new class_243(baseMx * 0.075F, baseMy * 0.075F, baseMz * 0.075F);
                     long life = (long)this.random(1000.0F, 1200.0F);
                     this.addParticle(
                        targetX, targetY, targetZ, velocity, ColorUtils.getThemeColor(), this.particleSize.get(), life, 0.5F, this.fallSpeed.get()
                     );
                  }
               }
            }
         }
      }
   }

   @EventLink
   public void onPacket(EventPacket e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (this.reason.is("Сносе тотема")) {
            if (e.getPacket() instanceof class_2663 packet && packet.method_11470() == 35) {
               class_1297 entity = packet.method_11469(mc.field_1687);
               if (entity != null) {
                  double centerX = entity.method_23317();
                  double centerY = entity.method_23318() + entity.method_17682() / 2.0;
                  double centerZ = entity.method_23321();

                  for (int i = 0; i < 50; i++) {
                     double theta = this.rnd.nextDouble() * 2.0 * Math.PI;
                     double phi = this.rnd.nextDouble() * Math.PI;
                     double speed = (this.rnd.nextDouble() * 0.5 + 0.5) * 0.1;
                     double vx = Math.sin(phi) * Math.cos(theta) * speed;
                     double vy = Math.sin(phi) * Math.sin(theta) * speed;
                     double vz = Math.cos(phi) * speed;
                     double spawnX = centerX + this.random(-0.3F, 0.3F);
                     double spawnY = centerY + this.random(-0.3F, 0.3F);
                     double spawnZ = centerZ + this.random(-0.3F, 0.3F);
                     if (!this.isPositionInBlock(new class_243(spawnX, spawnY, spawnZ))) {
                        int color = this.rnd.nextDouble() < 0.7 ? -16711936 : -256;
                        long life = (long)this.random(1500.0F, 2000.0F);
                        this.addParticle(spawnX, spawnY, spawnZ, new class_243(vx, vy, vz), color, this.particleSize.get(), life, 2.0F, this.fallSpeed.get());
                     }
                  }
               }
            }
         }
      }
   }

   @EventLink
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int particleCount = (int)this.count.get();
         if (this.reason.is("Бездействии")) {
            class_243 base = new class_243(
               mc.field_1724.method_23317(), mc.field_1724.method_23318() + mc.field_1724.method_17682() / 2.0, mc.field_1724.method_23321()
            );

            for (int i = 0; i < particleCount; i++) {
               double distance = this.random(7.0F, 35.0F);
               double angle = Math.toRadians(this.random(0.0F, 360.0F));
               double height = this.random(-7.0F, 25.0F);
               double spawnX = base.field_1352 + Math.cos(angle) * distance;
               double spawnY = base.field_1351 + height;
               double spawnZ = base.field_1350 + Math.sin(angle) * distance;
               class_243 spawnPos = new class_243(spawnX, spawnY, spawnZ);
               if (!this.isPositionInBlock(spawnPos)) {
                  long life = (long)this.random(1500.0F, 2000.0F);
                  double speed = this.rnd.nextDouble() < 0.8 ? this.random(0.015F, 0.03F) : 0.125;
                  double phi = Math.toRadians(this.random(0.0F, 360.0F));
                  class_243 velocity = new class_243(Math.cos(phi) * speed, this.random((float)(-speed * 0.1F), (float)(speed * 0.1F)), Math.sin(phi) * speed);
                  this.addParticle(spawnX, spawnY, spawnZ, velocity, ColorUtils.getThemeColor(), this.particleSize.get(), life, 3.0F, this.fallSpeed.get());
               }
            }
         }

         if (this.reason.is("Беге") && this.isMoving()) {
            class_243 motion = mc.field_1724.method_18798();
            double speed = Math.sqrt(motion.field_1352 * motion.field_1352 + motion.field_1350 * motion.field_1350);
            class_243 direction;
            if (speed < 0.01) {
               direction = mc.field_1724.method_5720().method_1021(-1.0);
            } else if (mc.field_1724.method_6128()) {
               direction = motion.method_1029().method_1021(-1.0);
            } else {
               direction = new class_243(-motion.field_1352 / speed, 0.0, -motion.field_1350 / speed);
            }

            double distanceBehind = (mc.field_1724.method_6128() ? 1.2 : 0.5) + (speed > 0.1 ? speed * 1.5 : 0.0);
            double offsetX = this.random(-0.35F, 0.35F);
            double offsetZ = this.random(-0.35F, 0.35F);
            double posX = mc.field_1724.method_23317() + direction.field_1352 * distanceBehind + offsetX;
            double posY = mc.field_1724.method_6128()
               ? mc.field_1724.method_23318() + mc.field_1724.method_17682() / 2.0 + direction.field_1351 * distanceBehind + this.random(-0.35F, 0.35F)
               : mc.field_1724.method_23318() + this.random(0.2F, mc.field_1724.method_17682() + 0.1F);
            double posZ = mc.field_1724.method_23321() + direction.field_1350 * distanceBehind + offsetZ;
            if (!this.isPositionInBlock(new class_243(posX, posY, posZ))) {
               double baseSpeed = 0.075;
               class_243 velocity = direction.method_1021(baseSpeed)
                  .method_1031(this.random(-0.01F, 0.01F), this.random(-0.05F, 0.01F), this.random(-0.01F, 0.01F))
                  .method_1021(0.1);
               long life = (long)this.random(1500.0F, 2000.0F);
               this.addParticle(posX, posY, posZ, velocity, ColorUtils.getThemeColor(), this.particleSize.get(), life, 3.0F, this.fallSpeed.get());
            }
         }

         boolean trackPearls = this.reason.is("Падении перла");
         boolean trackTridents = this.reason.is("Падении трезубца");
         if (trackPearls || trackTridents) {
            class_238 searchBox = mc.field_1724.method_5829().method_1014(100.0);

            for (class_1297 entity : mc.field_1687.method_8333(null, searchBox, e2 -> true)) {
               if (trackPearls && entity instanceof class_1684 pearl && !pearl.method_24828()) {
                  this.createProjectileParticles(pearl.method_19538(), 1);
               }

               if (trackTridents && entity instanceof class_1685 trident && trident.method_18798().method_1027() > 0.01) {
                  this.createProjectileParticles(trident.method_19538(), 1);
               }
            }
         }
      }
   }

   private void createProjectileParticles(class_243 position, int cnt) {
      int particleColor = ColorUtils.getThemeColor();

      for (int i = 0; i < cnt * 2.5; i++) {
         double dy = this.random(0.1F, 0.35F);
         class_243 particlePos = new class_243(position.field_1352, position.field_1351 + dy, position.field_1350);
         if (!this.isPositionInBlock(particlePos)) {
            float speedMin = this.random(0.015F, 0.0375F);
            float speedMax = this.random(0.05F, 0.075F);
            double speedFinal = this.random(speedMin, speedMax);
            double speedFinalY = speedFinal * 0.4;
            double angleVel = Math.toRadians(this.random(0.0F, 360.0F));
            class_243 velocity = new class_243(
               Math.cos(angleVel) * speedFinal, this.random((float)(-speedFinalY), (float)speedFinalY), Math.sin(angleVel) * speedFinal
            );
            long life = (long)this.random(2400.0F, 2800.0F);
            this.addParticle(
               particlePos.field_1352,
               particlePos.field_1351,
               particlePos.field_1350,
               velocity,
               particleColor,
               this.particleSize.get(),
               life,
               2.0F,
               this.fallSpeed.get()
            );
         }
      }
   }

   private void addParticle(double x, double y, double z, class_243 velocity, int color, float size, long lifeTime, float smooth, double gravity) {
      if (Particle.ParticleData.checkCollision(x, y, z, size, mc)) {
         synchronized (this.particles) {
            this.particles.add(new Particle.ParticleData(new class_243(x, y, z), velocity, color, size, lifeTime, smooth, gravity));
         }
      }
   }

   @EventLink
   public void onRender3D(Event3DRender e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         synchronized (this.particles) {
            this.particles.removeIf(Particle.ParticleData::isDead);
         }

         if (!this.particles.isEmpty()) {
            class_4587 matrices = e.getMatrices();
            class_243 camera = mc.field_1773.method_19418().method_19326();
            class_2960 texture = this.getTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            if (this.glow.isState()) {
               RenderSystem.blendFunc(770, 1);
            } else {
               RenderSystem.defaultBlendFunc();
            }

            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(class_10142.field_53880);
            ArrayList<Particle.ParticleData> renderList;
            synchronized (this.particles) {
               renderList = new ArrayList<>(this.particles);
            }

            for (Particle.ParticleData particle : renderList) {
               particle.update(mc);
               double x = particle.position.field_1352 - camera.field_1352;
               double y = particle.position.field_1351 - camera.field_1351;
               double z = particle.position.field_1350 - camera.field_1350;
               matrices.method_22903();
               matrices.method_46416((float)x, (float)y, (float)z);
               matrices.method_22907(class_7833.field_40716.rotationDegrees(-mc.field_1773.method_19418().method_19330()));
               matrices.method_22907(class_7833.field_40714.rotationDegrees(mc.field_1773.method_19418().method_19329()));
               Matrix4f matrix = matrices.method_23760().method_23761();
               float half = particle.size / 2.0F;
               int alpha = (int)(particle.alpha * 255.0F);
               int r = particle.color >> 16 & 0xFF;
               int g = particle.color >> 8 & 0xFF;
               int b = particle.color & 0xFF;
               class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
               buffer.method_22918(matrix, -half, -half, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, alpha);
               buffer.method_22918(matrix, -half, half, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, alpha);
               buffer.method_22918(matrix, half, half, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, alpha);
               buffer.method_22918(matrix, half, -half, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, alpha);
               class_286.method_43433(buffer.method_60800());
               matrices.method_22909();
            }

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }

   static class ParticleData {
      class_243 position;
      class_243 velocity;
      int color;
      float size;
      long lifeTime;
      long birthTime;
      float alpha = 1.0F;
      float smoothFactor;
      long lastUpdateNs;
      double gravity;

      ParticleData(class_243 position, class_243 velocity, int color, float size, long lifeTime, float smooth, double gravity) {
         this.position = position;
         this.velocity = velocity;
         this.color = color;
         this.size = size;
         this.lifeTime = lifeTime;
         this.birthTime = System.currentTimeMillis();
         this.lastUpdateNs = System.nanoTime();
         this.smoothFactor = smooth;
         this.gravity = gravity;
      }

      boolean isDead() {
         return System.currentTimeMillis() - this.birthTime >= this.lifeTime;
      }

      void update(class_310 mc) {
         long nowNs = System.nanoTime();
         double deltaSec = (nowNs - this.lastUpdateNs) / 1.0E9;
         this.lastUpdateNs = nowNs;
         float progress = Math.min(1.0F, (float)(System.currentTimeMillis() - this.birthTime) / (float)this.lifeTime);
         double factor = Math.pow(1.0 - progress, this.smoothFactor);
         double vx = this.velocity.field_1352;
         double vy = this.velocity.field_1351;
         double vz = this.velocity.field_1350;
         double newX = this.position.field_1352;
         double newY = this.position.field_1351;
         double newZ = this.position.field_1350;
         newX += vx * factor * (deltaSec * 60.0);
         if (!checkCollision(newX, this.position.field_1351, this.position.field_1350, this.size, mc)) {
            vx = -vx * 0.8;
            newX = this.position.field_1352;
         }

         newY += vy * factor * (deltaSec * 60.0);
         if (!checkCollision(newX, newY, this.position.field_1350, this.size, mc)) {
            vy = -vy * 1.5;
            newY = this.position.field_1351;
         }

         newZ += vz * factor * (deltaSec * 60.0);
         if (!checkCollision(newX, newY, newZ, this.size, mc)) {
            vz = -vz * 0.8;
            newZ = this.position.field_1350;
         }

         this.position = new class_243(newX, newY, newZ);
         this.velocity = new class_243(vx * 0.9999, vy * 0.9999 - this.gravity, vz * 0.9999);
         this.alpha = 1.0F - progress;
      }

      static boolean checkCollision(double x, double y, double z, float size, class_310 mc) {
         if (mc.field_1687 == null) {
            return false;
         } else {
            double half = size * 0.5;
            int minX = class_3532.method_15357(x - half);
            int maxX = class_3532.method_15357(x + half);
            int minY = class_3532.method_15357(y - half);
            int maxY = class_3532.method_15357(y + half);
            int minZ = class_3532.method_15357(z - half);
            int maxZ = class_3532.method_15357(z + half);
            class_2339 pos = new class_2339();

            for (int bx = minX; bx <= maxX; bx++) {
               for (int by = minY; by <= maxY; by++) {
                  for (int bz = minZ; bz <= maxZ; bz++) {
                     pos.method_10103(bx, by, bz);
                     class_2680 state = mc.field_1687.method_8320(pos);
                     if (!state.method_26215() && state.method_26212(mc.field_1687, pos)) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      }
   }
}
