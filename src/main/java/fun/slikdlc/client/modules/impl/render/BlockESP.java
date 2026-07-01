package fun.slikdlc.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_10142;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import net.minecraft.class_7923;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class BlockESP extends Module {
   public static BlockESP INSTANCE = new BlockESP();
   private static final float BOX_LINE_WIDTH = 2.0F;
   private static final float FILL_ALPHA = 0.18F;
   private static final float GREEN_R = 0.1F;
   private static final float GREEN_G = 1.0F;
   private static final float GREEN_B = 0.15F;
   private static final long SCAN_INTERVAL_MS = 50L;
   private static final int MAX_CHUNKS_PER_PASS = 2;
   private final FloatSetting distance = new FloatSetting("Дистанция", 60.0F, 10.0F, 120.0F, 1.0F);
   private final Set<String> trackedBlocks = ConcurrentHashMap.newKeySet();
   private final Map<class_2338, String> foundBlocks = new ConcurrentHashMap<>();
   private final Set<class_1923> scannedChunks = ConcurrentHashMap.newKeySet();
   private class_1923 lastPlayerChunk;
   private int lastScanRadius = -1;
   private long lastScanTime;

   public BlockESP() {
      super("BlockESP", "Показывает выбранные блоки через стену", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.distance});
   }

   @Override
   public void onEnable() {
      this.resetScanState();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetScanState();
      super.onDisable();
   }

   @EventLink(
      priority = 100
   )
   public void onRender3D(Event3DRender event) {
      if (mc.field_1687 != null && mc.field_1724 != null && !this.trackedBlocks.isEmpty()) {
         int scanRadius = this.getDistance();
         class_1923 currentChunk = new class_1923(mc.field_1724.method_24515());
         if (scanRadius != this.lastScanRadius) {
            this.resetScanState();
            this.lastScanRadius = scanRadius;
         }

         if (this.lastPlayerChunk == null || !this.lastPlayerChunk.equals(currentChunk)) {
            this.scannedChunks.clear();
            this.lastPlayerChunk = currentChunk;
         }

         long now = System.currentTimeMillis();
         if (now - this.lastScanTime >= 50L) {
            this.scanNearbyBlocks(scanRadius);
            this.lastScanTime = now;
         }

         this.cleanupInvalidAndDistantBlocks(mc.field_1724.method_19538(), scanRadius);
         this.renderFoundBlocks(event.getMatrices());
      }
   }

   private void scanNearbyBlocks(int scanRadius) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_2338 playerPos = mc.field_1724.method_24515();
         int playerChunkX = playerPos.method_10263() >> 4;
         int playerChunkZ = playerPos.method_10260() >> 4;
         int chunkRange = (scanRadius >> 4) + 2;
         List<class_1923> candidates = new ArrayList<>();

         for (int cx = -chunkRange; cx <= chunkRange; cx++) {
            for (int cz = -chunkRange; cz <= chunkRange; cz++) {
               class_1923 chunkPos = new class_1923(playerChunkX + cx, playerChunkZ + cz);
               if (!this.scannedChunks.contains(chunkPos)) {
                  candidates.add(chunkPos);
               }
            }
         }

         candidates.sort((a, b) -> {
            long da = this.chunkDistanceSq(a, playerChunkX, playerChunkZ);
            long db = this.chunkDistanceSq(b, playerChunkX, playerChunkZ);
            return Long.compare(da, db);
         });
         int scannedThisPass = 0;

         for (class_1923 chunkPos : candidates) {
            if (scannedThisPass >= 2) {
               break;
            }

            class_2818 chunk = mc.field_1687.method_8497(chunkPos.field_9181, chunkPos.field_9180);
            if (chunk != null) {
               this.scanChunk(chunk, playerPos, scanRadius);
               this.scannedChunks.add(chunkPos);
               scannedThisPass++;
            }
         }
      }
   }

   private void scanChunk(class_2818 chunk, class_2338 playerPos, int scanRadius) {
      int minX = chunk.method_12004().method_8326();
      int minZ = chunk.method_12004().method_8328();
      int maxX = minX + 15;
      int maxZ = minZ + 15;
      int minY = Math.max(mc.field_1687.method_31607(), playerPos.method_10264() - scanRadius);
      int maxY = Math.min(mc.field_1687.method_31600(), playerPos.method_10264() + scanRadius);
      int radiusSq = scanRadius * scanRadius;
      class_2339 mutable = new class_2339();

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               mutable.method_10103(x, y, z);
               if (!(mutable.method_10262(playerPos) > radiusSq)) {
                  class_2680 state = chunk.method_8320(mutable);
                  if (!state.method_26215()) {
                     String blockName = class_7923.field_41175.method_10221(state.method_26204()).method_12832().toLowerCase();
                     if (this.trackedBlocks.contains(blockName)) {
                        this.foundBlocks.put(mutable.method_10062(), blockName);
                     }
                  }
               }
            }
         }
      }
   }

   private void cleanupInvalidAndDistantBlocks(class_243 playerPos, int renderDistance) {
      if (mc.field_1687 == null) {
         this.foundBlocks.clear();
      } else {
         int renderDistanceSq = renderDistance * renderDistance;
         this.foundBlocks.entrySet().removeIf(entry -> {
            class_2338 pos = entry.getKey();
            class_2680 currentState = mc.field_1687.method_8320(pos);
            if (currentState.method_26215()) {
               return true;
            } else {
               String currentBlockName = class_7923.field_41175.method_10221(currentState.method_26204()).method_12832().toLowerCase();
               return !this.trackedBlocks.contains(currentBlockName) ? true : pos.method_19770(playerPos) > renderDistanceSq;
            }
         });
      }
   }

   private void renderFoundBlocks(class_4587 matrices) {
      if (!this.foundBlocks.isEmpty()) {
         class_243 camera = mc.field_1773.method_19418().method_19326();
         matrices.method_22903();
         matrices.method_22904(-camera.field_1352, -camera.field_1351, -camera.field_1350);
         Matrix4f matrix = matrices.method_23760().method_23761();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(class_10142.field_53876);
         class_289 tessellator = class_289.method_1348();
         class_287 fillBuffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1576);

         for (class_2338 pos : this.foundBlocks.keySet()) {
            this.addFilledBox(fillBuffer, matrix, pos, 0.1F, 1.0F, 0.15F, 0.18F);
         }

         class_286.method_43433(fillBuffer.method_60800());
         RenderSystem.lineWidth(2.0F);
         class_287 lineBuffer = tessellator.method_60827(class_5596.field_29344, class_290.field_1576);

         for (class_2338 pos : this.foundBlocks.keySet()) {
            this.addOutlinedBox(lineBuffer, matrix, pos, 0.1F, 1.0F, 0.15F, 1.0F);
         }

         class_286.method_43433(lineBuffer.method_60800());
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         matrices.method_22909();
      }
   }

   private void addFilledBox(class_287 buffer, Matrix4f matrix, class_2338 pos, float r, float g, float b, float a) {
      float minX = pos.method_10263();
      float minY = pos.method_10264();
      float minZ = pos.method_10260();
      float maxX = minX + 1.0F;
      float maxY = minY + 1.0F;
      float maxZ = minZ + 1.0F;
      buffer.method_22918(matrix, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, maxZ).method_22915(r, g, b, a);
   }

   private void addOutlinedBox(class_287 buffer, Matrix4f matrix, class_2338 pos, float r, float g, float b, float a) {
      float minX = pos.method_10263();
      float minY = pos.method_10264();
      float minZ = pos.method_10260();
      float maxX = minX + 1.0F;
      float maxY = minY + 1.0F;
      float maxZ = minZ + 1.0F;
      buffer.method_22918(matrix, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(matrix, minX, maxY, maxZ).method_22915(r, g, b, a);
   }

   public void addBlock(String blockName) {
      this.trackedBlocks.add(blockName.toLowerCase());
      this.scannedChunks.clear();
      this.foundBlocks.clear();
   }

   public void removeBlock(String blockName) {
      this.trackedBlocks.remove(blockName.toLowerCase());
      this.foundBlocks.entrySet().removeIf(entry -> entry.getValue().equalsIgnoreCase(blockName));
   }

   public void clearBlocks() {
      this.trackedBlocks.clear();
      this.resetScanState();
   }

   public Set<String> getTrackedBlocks() {
      return new HashSet<>(this.trackedBlocks);
   }

   public boolean isTracking(String blockName) {
      return this.trackedBlocks.contains(blockName.toLowerCase());
   }

   private int getDistance() {
      return Math.round(this.distance.get());
   }

   private long chunkDistanceSq(class_1923 chunkPos, int playerChunkX, int playerChunkZ) {
      long dx = chunkPos.field_9181 - playerChunkX;
      long dz = chunkPos.field_9180 - playerChunkZ;
      return dx * dx + dz * dz;
   }

   private void resetScanState() {
      this.foundBlocks.clear();
      this.scannedChunks.clear();
      this.lastPlayerChunk = null;
      this.lastScanTime = 0L;
      this.lastScanRadius = -1;
   }
}
