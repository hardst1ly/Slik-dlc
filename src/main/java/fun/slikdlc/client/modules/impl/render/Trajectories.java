package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.player.InventoryUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.Optional;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1835;
import net.minecraft.class_1893;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_239.class_240;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.joml.Matrix4f;

public class Trajectories extends Module {
   public static Trajectories INSTANCE = new Trajectories();
   private static final int MAX_STEPS = 440;
   private static final double SIMULATION_STEP = 0.5;
   private static final double SPLASH_RADIUS = 4.0;
   private static final class_2960 GLOW_TEXTURE = class_2960.method_60655("slikdlc", "textures/trajectories/glow.png");
   private final FloatSetting lineWidth = new FloatSetting("Ширина линии", 2.2F, 0.5F, 5.0F, 0.1F);

   public Trajectories() {
      super("Trajectories", "Показывает траекторию предмета в руке", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.lineWidth});
   }

   @EventLink
   public void onRender3D(Event3DRender event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_1799 stack = this.getHeldProjectileStack();
         if (!stack.method_7960()) {
            Trajectories.ProjectileParams params = this.getParams(stack);
            if (params != null) {
               float tickDelta = event.getTickDelta();
               class_243 startPos = mc.field_1724.method_5836(tickDelta);
               class_243[] directions = this.getShotDirections(stack, tickDelta);
               Trajectories.PredictionResult[] results = new Trajectories.PredictionResult[directions.length];
               int resultCount = 0;

               for (class_243 direction : directions) {
                  Trajectories.PredictionResult result = this.predict(mc.field_1724, params, startPos, direction);
                  if (result != null && result.points.length >= 2) {
                     results[resultCount++] = result;
                  }
               }

               if (resultCount != 0) {
                  class_4587 matrices = event.getMatrices();
                  class_4184 camera = event.getCamera();
                  class_243 cameraPos = camera.method_19326();
                  int themeColor = ColorUtils.getThemeColor();
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableCull();
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthMask(false);
                  RenderSystem.setShader(class_10142.field_53876);
                  RenderSystem.lineWidth(this.lineWidth.getValue().floatValue());
                  matrices.method_22903();
                  matrices.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);
                  Matrix4f matrix = matrices.method_23760().method_23761();

                  for (int i = 0; i < resultCount; i++) {
                     Trajectories.PredictionResult result = results[i];
                     this.drawTrajectoryLine(matrix, result.points, ColorUtils.setAlphaColor(themeColor, 190));
                     if (result.entityHit != null && result.entityHit.method_5805()) {
                        this.drawEntityBox(matrix, result.entityHit, ColorUtils.rgba(255, 70, 70, 210));
                     } else if (result.blockHit != null) {
                        this.drawImpactMarker(matrix, result.hitPos, result.blockHit.method_17780(), ColorUtils.setAlphaColor(themeColor, 230));
                     }

                     if (stack.method_31574(class_1802.field_8436) && result.hitPos != null) {
                        this.drawPotionRadiusGlow(matrices, result.hitPos, themeColor);
                     }
                  }

                  matrices.method_22909();
                  RenderSystem.depthMask(true);
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  RenderSystem.defaultBlendFunc();
               }
            }
         }
      }
   }

   private class_1799 getHeldProjectileStack() {
      class_1799 main = mc.field_1724.method_6047();
      if (!main.method_7960() && this.getParams(main) != null) {
         return main;
      } else {
         class_1799 off = mc.field_1724.method_6079();
         return !off.method_7960() && this.getParams(off) != null ? off : class_1799.field_8037;
      }
   }

   private Trajectories.ProjectileParams getParams(class_1799 stack) {
      class_1792 item = stack.method_7909();
      if (item == class_1802.field_8634 || item == class_1802.field_8543 || item == class_1802.field_8803) {
         return new Trajectories.ProjectileParams(1.5, 0.03, 0.99);
      } else if (item == class_1802.field_8436 || item == class_1802.field_8150) {
         return new Trajectories.ProjectileParams(0.5, 0.05, 0.99);
      } else if (item instanceof class_1753) {
         float power = 1.0F;
         if (mc.field_1724.method_6115() && mc.field_1724.method_6030() == stack) {
            float use = mc.field_1724.method_6048();
            float f = use / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            power = Math.min(f, 1.0F);
         }

         double velocity = 3.0 * power;
         return velocity <= 0.01 ? null : new Trajectories.ProjectileParams(velocity, 0.05, 0.99);
      } else if (item instanceof class_1764) {
         return !class_1764.method_7781(stack) ? null : new Trajectories.ProjectileParams(3.15, 0.05, 0.99);
      } else {
         return item instanceof class_1835 ? new Trajectories.ProjectileParams(2.5, 0.05, 0.99) : null;
      }
   }

   private class_243[] getShotDirections(class_1799 stack, float tickDelta) {
      class_243 baseDir = mc.field_1724.method_5828(tickDelta).method_1029();
      if (stack.method_7909() instanceof class_1764 && InventoryUtils.getEnchantmentLevel(stack, class_1893.field_9108) > 0) {
         float baseYaw = (float)(class_3532.method_15349(baseDir.field_1350, baseDir.field_1352) * (180.0 / Math.PI)) - 90.0F;
         float basePitch = (float)(
            -(
               class_3532.method_15349(
                     baseDir.field_1351, class_3532.method_15355((float)(baseDir.field_1352 * baseDir.field_1352 + baseDir.field_1350 * baseDir.field_1350))
                  )
                  * (180.0 / Math.PI)
            )
         );
         return new class_243[]{this.getDirectionFromYawPitch(baseYaw - 10.0F, basePitch), baseDir, this.getDirectionFromYawPitch(baseYaw + 10.0F, basePitch)};
      } else {
         return new class_243[]{baseDir};
      }
   }

   private class_243 getDirectionFromYawPitch(float yawDeg, float pitchDeg) {
      float yaw = yawDeg * (float) (Math.PI / 180.0);
      float pitch = pitchDeg * (float) (Math.PI / 180.0);
      float x = class_3532.method_15374(-yaw - (float) Math.PI) * -class_3532.method_15362(-pitch);
      float y = class_3532.method_15374(-pitch);
      float z = class_3532.method_15362(-yaw - (float) Math.PI) * -class_3532.method_15362(-pitch);
      return new class_243(x, y, z).method_1029();
   }

   private Trajectories.PredictionResult predict(class_1657 player, Trajectories.ProjectileParams params, class_243 startPos, class_243 direction) {
      class_243 pos = startPos;
      class_243 motion = direction.method_1029().method_1021(params.velocity);
      class_243[] points = new class_243[441];
      int count = 0;
      points[count++] = startPos;
      class_1297 entityHit = null;
      class_243 entityHitPos = null;

      for (int i = 0; i < 440; i++) {
         class_243 next = pos.method_1019(motion.method_1021(0.5));
         if (entityHit == null) {
            Trajectories.EntityHit hit = this.rayTraceEntities(pos, next, player);
            if (hit != null) {
               entityHit = hit.entity;
               entityHitPos = hit.hitPos;
            }
         }

         class_3965 blockHit = mc.field_1687.method_17742(new class_3959(pos, next, class_3960.field_17558, class_242.field_1348, player));
         if (blockHit.method_17783() == class_240.field_1332) {
            points[count++] = blockHit.method_17784();
            return new Trajectories.PredictionResult(this.copyPoints(points, count), blockHit, blockHit.method_17784(), entityHit, entityHitPos);
         }

         points[count++] = next;
         pos = next;
         boolean inWater = mc.field_1687.method_8320(class_2338.method_49638(next)).method_27852(class_2246.field_10382);
         double drag = Math.pow(inWater ? 0.8 : params.drag, 0.5);
         motion = motion.method_1021(drag).method_1023(0.0, params.gravity * 0.5, 0.0);
         if (next.field_1351 <= mc.field_1687.method_31607()) {
            break;
         }
      }

      class_243 hitPos = entityHitPos != null ? entityHitPos : points[count - 1];
      return new Trajectories.PredictionResult(this.copyPoints(points, count), null, hitPos, entityHit, entityHitPos);
   }

   private class_243[] copyPoints(class_243[] points, int count) {
      class_243[] out = new class_243[count];
      System.arraycopy(points, 0, out, 0, count);
      return out;
   }

   private Trajectories.EntityHit rayTraceEntities(class_243 from, class_243 to, class_1297 owner) {
      class_238 search = new class_238(from, to).method_1014(1.0);
      class_1297 closest = null;
      class_243 closestHit = null;
      double closestDistance = Double.MAX_VALUE;

      for (class_1297 entity : mc.field_1687.method_8333(owner, search, entityx -> entityx != null && entityx.method_5805() && entityx.method_5863())) {
         Optional<class_243> hit = entity.method_5829().method_1014(0.3).method_992(from, to);
         if (!hit.isEmpty()) {
            double distance = from.method_1025(hit.get());
            if (distance < closestDistance) {
               closestDistance = distance;
               closest = entity;
               closestHit = hit.get();
            }
         }
      }

      return closest == null ? null : new Trajectories.EntityHit(closest, closestHit);
   }

   private void drawTrajectoryLine(Matrix4f matrix, class_243[] points, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);

      for (int i = 0; i < points.length - 1; i++) {
         class_243 start = points[i];
         class_243 end = points[i + 1];
         buffer.method_22918(matrix, (float)start.field_1352, (float)start.field_1351, (float)start.field_1350).method_1336(r, g, b, a);
         buffer.method_22918(matrix, (float)end.field_1352, (float)end.field_1351, (float)end.field_1350).method_1336(r, g, b, a);
      }

      class_286.method_43433(buffer.method_60800());
   }

   private void drawImpactMarker(Matrix4f matrix, class_243 pos, class_2350 side, int color) {
      class_243 normal = class_243.method_24954(side.method_62675()).method_1029();
      class_243 u = side != class_2350.field_11036 && side != class_2350.field_11033
         ? normal.method_1036(new class_243(0.0, 1.0, 0.0)).method_1029()
         : new class_243(1.0, 0.0, 0.0);
      class_243 v = normal.method_1036(u).method_1029();
      class_243 center = pos.method_1019(normal.method_1021(0.004));
      double radius = 0.35;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);
      int segments = 48;
      class_243 previous = null;

      for (int i = 0; i <= segments; i++) {
         double angle = (Math.PI * 2) * i / segments;
         class_243 point = center.method_1019(u.method_1021(Math.cos(angle) * radius)).method_1019(v.method_1021(Math.sin(angle) * radius));
         if (previous != null) {
            buffer.method_22918(matrix, (float)previous.field_1352, (float)previous.field_1351, (float)previous.field_1350).method_1336(r, g, b, a);
            buffer.method_22918(matrix, (float)point.field_1352, (float)point.field_1351, (float)point.field_1350).method_1336(r, g, b, a);
         }

         previous = point;
      }

      class_243 left = center.method_1019(u.method_1021(-radius));
      class_243 right = center.method_1019(u.method_1021(radius));
      class_243 down = center.method_1019(v.method_1021(-radius));
      class_243 up = center.method_1019(v.method_1021(radius));
      buffer.method_22918(matrix, (float)left.field_1352, (float)left.field_1351, (float)left.field_1350).method_1336(r, g, b, a);
      buffer.method_22918(matrix, (float)right.field_1352, (float)right.field_1351, (float)right.field_1350).method_1336(r, g, b, a);
      buffer.method_22918(matrix, (float)down.field_1352, (float)down.field_1351, (float)down.field_1350).method_1336(r, g, b, a);
      buffer.method_22918(matrix, (float)up.field_1352, (float)up.field_1351, (float)up.field_1350).method_1336(r, g, b, a);
      class_286.method_43433(buffer.method_60800());
   }

   private void drawEntityBox(Matrix4f matrix, class_1297 entity, int color) {
      class_238 box = entity.method_5829();
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);
      this.vertexBox(buffer, matrix, box, r, g, b, a);
      class_286.method_43433(buffer.method_60800());
   }

   private void vertexBox(class_287 buffer, Matrix4f matrix, class_238 box, int r, int g, int b, int a) {
      float minX = (float)box.field_1323;
      float minY = (float)box.field_1322;
      float minZ = (float)box.field_1321;
      float maxX = (float)box.field_1320;
      float maxY = (float)box.field_1325;
      float maxZ = (float)box.field_1324;
      this.line(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
      this.line(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
      this.line(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
      this.line(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
      this.line(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
      this.line(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
      this.line(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
   }

   private void line(class_287 buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x2, y2, z2).method_1336(r, g, b, a);
   }

   private void drawPotionRadiusGlow(class_4587 matrices, class_243 pos, int themeColor) {
      int color = ColorUtils.setAlphaColor(themeColor, 82);
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      float radius = 4.0F;
      RenderSystem.setShader(class_10142.field_53880);
      RenderSystem.setShaderTexture(0, GLOW_TEXTURE);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrices.method_22903();
      matrices.method_22904(pos.field_1352, pos.field_1351 + 0.012, pos.field_1350);
      Matrix4f matrix = matrices.method_23760().method_23761();
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      buffer.method_22918(matrix, -radius, 0.0F, -radius).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, -radius, 0.0F, radius).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, radius, 0.0F, radius).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, radius, 0.0F, -radius).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
      class_286.method_43433(buffer.method_60800());
      matrices.method_22909();
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.setShader(class_10142.field_53876);
   }

   private record EntityHit(class_1297 entity, class_243 hitPos) {
   }

   private record PredictionResult(class_243[] points, class_3965 blockHit, class_243 hitPos, class_1297 entityHit, class_243 entityHitPos) {
   }

   private record ProjectileParams(double velocity, double gravity, double drag) {
   }
}
