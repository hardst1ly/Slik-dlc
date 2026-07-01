package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventAttackEntity;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_10142;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class HitBubbles extends Module {
   public static HitBubbles INSTANCE = new HitBubbles();
   private static final long LIFE_MS = 1600L;
   private final CopyOnWriteArrayList<HitBubbles.HitBubble> bubbles = new CopyOnWriteArrayList<>();
   private final class_2960 bubbleTexture = class_2960.method_60655("slikdlc", "textures/hitbubble/bubble.png");

   public HitBubbles() {
      super("HitBubbles", "Круг при ударе игрока", Module.ModuleCategory.RENDER);
   }

   @Override
   public void onDisable() {
      this.bubbles.clear();
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      long now = System.currentTimeMillis();
      this.bubbles.removeIf(b -> now - b.spawnTime() >= 1600L);
   }

   @EventLink
   public void onAttack(EventAttackEntity event) {
      if (event != null && event.getTarget() != null) {
         if (event.getTarget() instanceof class_1309 living) {
            if (event.getPlayer() != null) {
               class_243 sideDir = this.getHitSideDirection(living, event.getPlayer().method_19538());
               class_243 pos = this.getHitPosition(living, sideDir);
               float sideYaw = (float)Math.toDegrees(Math.atan2(sideDir.field_1352, sideDir.field_1350));
               this.bubbles.add(new HitBubbles.HitBubble(pos, System.currentTimeMillis(), (float)(Math.random() * 360.0), sideYaw));
            }
         }
      }
   }

   @EventLink
   public void onWorldRender(Event3DRender event) {
      if (!this.bubbles.isEmpty() && mc.field_1724 != null) {
         class_4587 stack = event.getMatrices();
         class_243 cameraPos = event.getCamera().method_19326();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, this.bubbleTexture);
         long now = System.currentTimeMillis();

         for (HitBubbles.HitBubble bubble : this.bubbles) {
            this.renderSingleBubble(stack, cameraPos, bubble, now);
         }

         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private void renderSingleBubble(class_4587 stack, class_243 cameraPos, HitBubbles.HitBubble bubble, long now) {
      float progress = (float)(now - bubble.spawnTime()) / 1600.0F;
      if (!(progress >= 1.0F)) {
         float inPhase = Math.max(0.0F, Math.min(1.0F, progress / 0.22F));
         float outPhase = Math.max(0.0F, Math.min(1.0F, (progress - 0.225F) / 0.4F));
         float scaleIn = inPhase * inPhase * (3.0F - 2.0F * inPhase);
         float scaleOut = 1.0F - outPhase * outPhase;
         float scale = 0.02F + 1.55F * scaleIn * scaleOut;
         float alpha = 1.0F - outPhase * outPhase * outPhase;
         float rotation = (float)(now - bubble.spawnTime()) / 1.5F + bubble.spinSeed();
         class_243 rel = bubble.pos().method_1020(cameraPos);
         int color = ColorUtils.multAlpha(ColorUtils.getThemeColor(), alpha);
         stack.method_22903();
         stack.method_22904(rel.field_1352, rel.field_1351, rel.field_1350);
         stack.method_22907(class_7833.field_40716.rotationDegrees(bubble.sideYaw()));
         stack.method_22907(class_7833.field_40714.rotationDegrees(-210.0F));
         stack.method_22907(class_7833.field_40718.rotationDegrees(rotation));
         this.drawTexturedQuad(stack, -scale * 0.5F, -scale * 0.5F, scale, scale, color);
         stack.method_22909();
      }
   }

   private void drawTexturedQuad(class_4587 stack, float x, float y, float width, float height, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      if (a > 0) {
         Matrix4f mat = stack.method_23760().method_23761();
         class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
         buffer.method_22918(mat, x, y, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x, y + height, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x + width, y + height, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x + width, y, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
         class_286.method_43433(buffer.method_60800());
      }
   }

   private class_243 getHitSideDirection(class_1309 target, class_243 attackerPos) {
      class_243 dir = attackerPos.method_1020(target.method_19538());
      dir = new class_243(dir.field_1352, 0.0, dir.field_1350);
      if (dir.method_1027() < 1.0E-4) {
         class_243 fallback = target.method_5720();
         dir = new class_243(fallback.field_1352, 0.0, fallback.field_1350);
      }

      if (dir.method_1027() < 1.0E-4) {
         dir = new class_243(0.0, 0.0, 1.0);
      }

      return dir.method_1029();
   }

   private class_243 getHitPosition(class_1309 target, class_243 sideDir) {
      class_243 head = new class_243(target.method_23317(), target.method_23318() + target.method_17682() + 0.18, target.method_23321());
      return head.method_1019(sideDir.method_1021(0.1));
   }

   private record HitBubble(class_243 pos, long spawnTime, float spinSeed, float sideYaw) {
   }
}
