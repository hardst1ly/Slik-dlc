package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.Theme;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.api.utils.render.fonts.msdf.Font;
import fun.slikdlc.api.utils.render.fonts.msdf.Fonts;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1694;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2636;
import net.minecraft.class_2680;
import net.minecraft.class_2745;
import net.minecraft.class_2818;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LootTracker extends Module {
   public static final LootTracker INSTANCE = new LootTracker();
   private static final float TAG_BOX_HEIGHT = 12.5F;
   private static final float TAG_PADDING = 5.0F;
   private static final float TAG_HUD_RADIUS = 1.1F;
   private static final int TAG_HUD_ALPHA = 204;
   private final BooleanSetting showSpawners = new BooleanSetting("Спавнеры", true);
   private final BooleanSetting showMinecarts = new BooleanSetting("Вагонетки", true);
   private final FloatSetting maxDistance = new FloatSetting("Макс. дистанция", 64.0F, 16.0F, 128.0F, 1.0F);
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   private class_243 lastCameraPos = class_243.field_1353;
   private float lastTickDelta;
   private boolean hasProjection;
   private final List<LootTracker.LootSource> cachedSources = new ArrayList<>();
   private long lastCacheUpdate = 0L;
   private static final long CACHE_UPDATE_INTERVAL = 500L;

   public LootTracker() {
      super("LootTracker", "Показывает залутанные спавнеры и вагонетки", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.showSpawners, this.showMinecarts, this.maxDistance});
   }

   @Override
   public void onDisable() {
      this.hasProjection = false;
      this.cachedSources.clear();
      super.onDisable();
   }

   @EventLink(
      priority = 100
   )
   public void onRender3D(Event3DRender event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.hasProjection = true;
         this.lastProjectionMatrix.set(event.getProjectionMatrix());
         this.lastCameraPos = event.getCamera().method_19326();
         this.lastCameraRotation.set(event.getCamera().method_23767());
         this.lastTickDelta = event.getTickDelta();
         long now = System.currentTimeMillis();
         if (now - this.lastCacheUpdate > 500L) {
            this.updateCache();
            this.lastCacheUpdate = now;
         }
      }
   }

   @EventLink(
      priority = 100
   )
   public void onRender2D(EventRender.Default event) {
      if (this.hasProjection && mc.field_1687 != null && mc.field_1724 != null) {
         class_4587 matrices = event.getContext().method_51448();
         Font font = Fonts.getFont("sf_regular", 14);
         if (font != null) {
            for (LootTracker.LootSource source : this.cachedSources) {
               if (!(
                  mc.field_1724.method_5649(source.pos.method_10263(), source.pos.method_10264(), source.pos.method_10260())
                     > this.maxDistance.getValue().floatValue() * this.maxDistance.getValue().floatValue()
               )) {
                  class_243 screenPos = this.worldToScreen(
                     new class_243(source.pos.method_10263() + 0.5, source.pos.method_10264() + 1.5, source.pos.method_10260() + 0.5)
                  );
                  if (screenPos != null) {
                     this.drawLootTag(matrices, font, (float)screenPos.field_1352, (float)screenPos.field_1351, source);
                  }
               }
            }
         }
      }
   }

   private void updateCache() {
      this.cachedSources.clear();
      if (this.showSpawners.isState()) {
         int renderDistance = (Integer)mc.field_1690.method_42503().method_41753();
         class_1923 playerChunk = mc.field_1724.method_31476();

         for (int cx = -renderDistance; cx <= renderDistance; cx++) {
            for (int cz = -renderDistance; cz <= renderDistance; cz++) {
               class_2818 chunk = mc.field_1687.method_8497(playerChunk.field_9181 + cx, playerChunk.field_9180 + cz);
               if (chunk != null) {
                  for (class_2586 blockEntity : chunk.method_12214().values()) {
                     if (blockEntity instanceof class_2636 spawner && this.hasSingleChestNearby(spawner.method_11016())) {
                        int delay = this.getSpawnerDelay(spawner);
                        boolean isLooted = delay > 0 && delay != 20 || this.isAreaExplored(spawner.method_11016());
                        this.cachedSources.add(new LootTracker.LootSource(spawner.method_11016(), LootTracker.LootType.SPAWNER, isLooted));
                     }
                  }
               }
            }
         }
      }

      if (this.showMinecarts.isState()) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1694 minecart) {
               boolean isLooted = this.isAreaExplored(minecart.method_24515());
               this.cachedSources.add(new LootTracker.LootSource(minecart.method_24515(), LootTracker.LootType.MINECART, isLooted));
            }
         }
      }
   }

   private void drawLootTag(class_4587 matrices, Font font, float x, float y, LootTracker.LootSource source) {
      String typeText = source.type == LootTracker.LootType.SPAWNER ? "Спавнер" : "Вагонетка";
      String statusText = source.isLooted ? " [Залутано]" : " [Не залутано]";
      float typeWidth = font.getStringWidth(typeText);
      float statusWidth = font.getStringWidth(statusText);
      float totalWidth = typeWidth + statusWidth;
      float boxWidth = totalWidth + 10.0F;
      float boxHeight = 12.5F;
      float tagX = x - boxWidth / 2.0F;
      float tagY = y - boxHeight / 2.0F;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      this.drawDefaultTagPanel(matrices, tagX, tagY, boxWidth, boxHeight);
      float textX = tagX + 5.0F;
      float textY = tagY + boxHeight / 2.0F - font.getHeight() * 0.1F;
      font.drawString(matrices, typeText, textX, textY, -1);
      textX += typeWidth;
      int statusColor = source.isLooted ? -43691 : -11141291;
      font.drawString(matrices, statusText, textX, textY, statusColor);
      RenderSystem.disableBlend();
   }

   private void drawDefaultTagPanel(class_4587 matrices, float x, float y, float width, float height) {
      int themeColor = this.getStableThemeColor();
      RenderUtils.drawDefaultHudPanel(
         matrices,
         x,
         y,
         width,
         height,
         1.1F,
         1.1F,
         ColorUtils.rgba(50, 50, 50, 204),
         ColorUtils.setAlphaColor(ColorUtils.darken(themeColor, 0.15F), 204),
         ColorUtils.setAlphaColor(ColorUtils.darken(themeColor, 0.05F), 204)
      );
   }

   private int getStableThemeColor() {
      if (SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.themeStorage != null && SlikDlc.INSTANCE.themeStorage.getThemes() != null) {
         Theme theme = SlikDlc.INSTANCE.themeStorage.getThemes().getTheme();
         return theme != null && theme.color != null && theme.color.length != 0 ? theme.color[0] : ColorUtils.getThemeColor(0);
      } else {
         return ColorUtils.getThemeColor(0);
      }
   }

   private boolean isAreaExplored(class_2338 pos) {
      int radius = 20;
      int airCount = 0;
      int checkCount = 0;

      for (int x = -radius; x <= radius; x += 4) {
         for (int y = -radius; y <= radius; y += 4) {
            for (int z = -radius; z <= radius; z += 4) {
               class_2338 checkPos = pos.method_10069(x, y, z);
               class_2680 state = mc.field_1687.method_8320(checkPos);
               checkCount++;
               if (state.method_27852(class_2246.field_10124) || state.method_27852(class_2246.field_10543)) {
                  airCount++;
               }
            }
         }
      }

      return airCount > checkCount * 0.3;
   }

   private int getSpawnerDelay(class_2636 spawner) {
      try {
         class_2487 tag = spawner.method_38242(mc.field_1687.method_30349());
         return tag.method_10545("Delay") ? tag.method_10568("Delay") : 20;
      } catch (Exception var3) {
         return 20;
      }
   }

   private boolean hasSingleChestNearby(class_2338 spawnerPos) {
      int radius = 3;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               class_2338 checkPos = spawnerPos.method_10069(x, y, z);
               class_2680 state = mc.field_1687.method_8320(checkPos);
               if (state.method_26204() instanceof class_2281 && state.method_11654(class_2281.field_10770) == class_2745.field_12569) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private class_243 worldToScreen(class_243 worldPos) {
      Vector3f relative = new Vector3f(
         (float)(worldPos.field_1352 - this.lastCameraPos.field_1352),
         (float)(worldPos.field_1351 - this.lastCameraPos.field_1351),
         (float)(worldPos.field_1350 - this.lastCameraPos.field_1350)
      );
      Quaternionf invCameraRotation = new Quaternionf(this.lastCameraRotation).conjugate();
      relative.rotate(invCameraRotation);
      Vector4f clip = new Vector4f(relative.x, relative.y, relative.z, 1.0F);
      this.lastProjectionMatrix.transform(clip);
      float w = clip.w;
      if (w <= 1.0E-5F) {
         return null;
      } else {
         float ndcX = clip.x / w;
         float ndcY = clip.y / w;
         float screenX = (ndcX * 0.5F + 0.5F) * mc.method_22683().method_4486();
         float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * mc.method_22683().method_4502();
         if (Float.isNaN(screenX) || Float.isNaN(screenY)) {
            return null;
         } else {
            return !Float.isInfinite(screenX) && !Float.isInfinite(screenY) ? new class_243(screenX, screenY, 0.0) : null;
         }
      }
   }

   private record LootSource(class_2338 pos, LootTracker.LootType type, boolean isLooted) {
   }

   private static enum LootType {
      SPAWNER,
      MINECART;

      private LootType() {
      }
   }
}
