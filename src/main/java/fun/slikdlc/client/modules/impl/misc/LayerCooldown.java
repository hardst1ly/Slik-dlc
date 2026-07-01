package fun.slikdlc.client.modules.impl.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2767;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_4587;
import net.minecraft.class_7923;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LayerCooldown extends Module {
   public static LayerCooldown INSTANCE = new LayerCooldown();
   private static final long DELAYED_SCAN_MS = 250L;
   private static final int SEARCH_RADIUS = 4;
   private static final int SEARCH_HEIGHT = 4;
   private static final int MAX_TIMERS = 100;
   private static final float TIMER_SECONDS = 19.5F;
   private static final float MAX_DISTANCE = 96.0F;
   private static final double TIMER_Y_OFFSET = 0.6;
   private static final class_1799 LAYER_ICON = new class_1799(class_1802.field_8551);
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   private class_243 lastCameraPos = class_243.field_1353;
   private boolean hasProjection;
   private final List<LayerCooldown.LayerTimer> timers = new ArrayList<>();
   private final List<LayerCooldown.PendingScan> pendingScans = new ArrayList<>();

   public LayerCooldown() {
      super("LayerCooldown", "Показывает таймер возле поставленного пласта", Module.ModuleCategory.MISC);
   }

   @Override
   public void onDisable() {
      this.timers.clear();
      this.pendingScans.clear();
      this.hasProjection = false;
      super.onDisable();
   }

   @EventLink
   public void onPacket(EventPacket event) {
      if (event.getType() == EventPacket.Type.RECEIVE && mc.field_1687 != null && mc.field_1724 != null) {
         if (event.getPacket() instanceof class_2767 packet) {
            String sound = this.getSoundPath(packet);
            if (sound != null) {
               class_243 soundPos = new class_243(packet.method_11890(), packet.method_11889(), packet.method_11893());
               class_2338 blockPos = class_2338.method_49638(soundPos);
               if ("block.piston.extend".equals(sound)) {
                  this.addTimer(blockPos, soundPos);
               } else {
                  if (this.isDelayedTrapSound(sound)) {
                     this.pendingScans.add(new LayerCooldown.PendingScan(blockPos, System.currentTimeMillis() + 250L));
                  }
               }
            }
         }
      }
   }

   @EventLink(
      priority = 100
   )
   public void onRender3D(Event3DRender event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.hasProjection = true;
         this.lastProjectionMatrix.set(event.getProjectionMatrix());
         this.lastCameraRotation.set(event.getCamera().method_23767());
         this.lastCameraPos = event.getCamera().method_19326();
         this.processPendingScans();
      }
   }

   @EventLink(
      priority = 100
   )
   public void onRender2D(EventRender.Default event) {
      if (this.hasProjection && mc.field_1687 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         this.timers.removeIf(timerx -> timerx.endTime <= now);

         while (this.timers.size() > 100) {
            this.timers.remove(0);
         }

         if (!this.timers.isEmpty()) {
            class_4587 matrices = event.getContext().method_51448();
            Font font = Fonts.getFont("sf_regular", 13);
            if (font != null) {
               float maxDistSq = 9216.0F;

               for (int i = 0; i < this.timers.size(); i++) {
                  LayerCooldown.LayerTimer timer = this.timers.get(i);
                  if (!(mc.field_1724.method_5707(timer.pos) > maxDistSq)) {
                     class_243 screen = this.worldToScreen(timer.pos);
                     if (screen != null) {
                        float seconds = Math.max(0.0F, (float)(timer.endTime - now) / 1000.0F);
                        this.drawTimer(event.getContext(), matrices, font, (float)screen.field_1352, (float)screen.field_1351, seconds);
                     }
                  }
               }
            }
         }
      }
   }

   private void processPendingScans() {
      if (!this.pendingScans.isEmpty() && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         Iterator<LayerCooldown.PendingScan> iterator = this.pendingScans.iterator();

         while (iterator.hasNext()) {
            LayerCooldown.PendingScan scan = iterator.next();
            if (scan.runAt <= now) {
               class_2338 found = this.findLayerLikeBlock(scan.center);
               class_243 pos = found == null
                  ? class_243.method_24953(scan.center)
                  : new class_243(found.method_10263() + 0.5, found.method_10264() + 0.65, found.method_10260() + 0.5);
               this.addTimer(found == null ? scan.center : found, pos);
               iterator.remove();
            }
         }
      }
   }

   private class_2338 findLayerLikeBlock(class_2338 center) {
      class_2338 best = null;
      double bestDistance = Double.MAX_VALUE;

      for (int x = -4; x <= 4; x++) {
         for (int y = -4; y <= 4; y++) {
            for (int z = -4; z <= 4; z++) {
               class_2338 pos = center.method_10069(x, y, z);
               class_2680 state = mc.field_1687.method_8320(pos);
               if (this.isLayerLikeBlock(state)) {
                  double distance = pos.method_10262(center);
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = pos;
                  }
               }
            }
         }
      }

      return best;
   }

   private boolean isLayerLikeBlock(class_2680 state) {
      if (state != null && !state.method_26215()) {
         class_2248 block = state.method_26204();
         return block == class_2246.field_10560
            || block == class_2246.field_10615
            || block == class_2246.field_10008
            || block == class_2246.field_10342
            || block == class_2246.field_10535
            || block == class_2246.field_10105
            || block == class_2246.field_10414;
      } else {
         return false;
      }
   }

   private void addTimer(class_2338 blockPos, class_243 renderPos) {
      long endTime = System.currentTimeMillis() + 19500L;

      for (int i = 0; i < this.timers.size(); i++) {
         LayerCooldown.LayerTimer timer = this.timers.get(i);
         if (timer.blockPos.method_10262(blockPos) <= 2.25) {
            this.timers.set(i, new LayerCooldown.LayerTimer(blockPos, renderPos.method_1031(0.0, 0.6, 0.0), endTime));
            return;
         }
      }

      this.timers.add(new LayerCooldown.LayerTimer(blockPos, renderPos.method_1031(0.0, 0.6, 0.0), endTime));
   }

   private boolean isDelayedTrapSound(String sound) {
      return "block.anvil.place".equals(sound) || "entity.zombie_horse.death".equals(sound) || "entity.ender_dragon.growl".equals(sound);
   }

   private String getSoundPath(class_2767 packet) {
      try {
         return class_7923.field_41172.method_10221((class_3414)packet.method_11894().comp_349()).method_12832();
      } catch (Exception var3) {
         return null;
      }
   }

   private void drawTimer(class_332 context, class_4587 matrices, Font font, float x, float y, float seconds) {
      String text = this.formatOneDecimal(seconds) + "с";
      float textWidth = font.getStringWidth(text);
      float iconSize = 10.0F;
      float iconScale = 0.62F;
      float gap = 3.0F;
      float boxWidth = iconSize + gap + textWidth + 8.0F;
      float boxHeight = 12.5F;
      float boxX = x - boxWidth * 0.5F;
      float boxY = y - boxHeight * 0.5F;
      int themeColor = ColorUtils.getThemeColor();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderUtils.drawDefaultHudThemedPanelWithStroke(
         matrices, boxX, boxY, boxWidth, boxHeight, 2.0F, 3.0F, themeColor, ModuleClass.interfaceModule.strokeStyle.getCurrent()
      );
      this.drawItemIcon(context, matrices, boxX + 4.0F, boxY + 1.25F, iconScale);
      font.drawString(matrices, text, boxX + 4.0F + iconSize + gap, boxY + 4.55F, -1);
      RenderSystem.disableBlend();
   }

   private String formatOneDecimal(float value) {
      int scaled = Math.round(value * 10.0F);
      return scaled / 10 + "." + Math.abs(scaled % 10);
   }

   private void drawItemIcon(class_332 context, class_4587 matrices, float x, float y, float scale) {
      if (context != null) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         matrices.method_22903();
         matrices.method_46416(x, y, 0.0F);
         matrices.method_22905(scale, scale, 1.0F);
         context.method_51427(LAYER_ICON, 0, 0);
         matrices.method_22909();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
      }
   }

   private class_243 worldToScreen(class_243 worldPos) {
      if (mc != null && mc.method_22683() != null) {
         Vector3f relative = new Vector3f(
            (float)(worldPos.field_1352 - this.lastCameraPos.field_1352),
            (float)(worldPos.field_1351 - this.lastCameraPos.field_1351),
            (float)(worldPos.field_1350 - this.lastCameraPos.field_1350)
         );
         Quaternionf invCameraRot = new Quaternionf(this.lastCameraRotation).conjugate();
         relative.rotate(invCameraRot);
         Vector4f clip = new Vector4f(relative.x, relative.y, relative.z, 1.0F);
         this.lastProjectionMatrix.transform(clip);
         float w = clip.w;
         if (w <= 1.0E-5F) {
            return null;
         } else {
            float ndcX = clip.x / w;
            float ndcY = clip.y / w;
            float ndcZ = clip.z / w;
            float screenX = (ndcX * 0.5F + 0.5F) * mc.method_22683().method_4486();
            float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * mc.method_22683().method_4502();
            if (!Float.isNaN(screenX) && !Float.isNaN(screenY) && !Float.isInfinite(screenX) && !Float.isInfinite(screenY)) {
               return !(screenX < -400.0F)
                     && !(screenY < -400.0F)
                     && !(screenX > mc.method_22683().method_4486() + 400)
                     && !(screenY > mc.method_22683().method_4502() + 400)
                  ? new class_243(screenX, screenY, ndcZ)
                  : null;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private record LayerTimer(class_2338 blockPos, class_243 pos, long endTime) {
   }

   private record PendingScan(class_2338 center, long runAt) {
   }
}
