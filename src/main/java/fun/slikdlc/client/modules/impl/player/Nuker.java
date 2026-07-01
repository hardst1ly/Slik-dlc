package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_7923;

public class Nuker extends Module {
   public static Nuker INSTANCE = new Nuker();
   private final FloatSetting radius = new FloatSetting("Дистанция", 3.0F, 1.0F, 5.0F, 1.0F);
   private final BooleanSetting breakAll = new BooleanSetting("Ломать все блоки", false);
   private final BooleanSetting swing = new BooleanSetting("Анимация руки", true);
   private final Set<String> targetBlocks = new HashSet<>();
   private class_2338 currentTargetBlock;

   public Nuker() {
      super("Nuker", "Автоматически ломает блоки в радиусе", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.radius, this.breakAll, this.swing});
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1761 == null) {
         this.resetBreaking();
      } else if (!this.breakAll.isState() && this.targetBlocks.isEmpty()) {
         this.resetBreaking();
      } else {
         if (!this.isCurrentTargetValid()) {
            this.currentTargetBlock = this.findNewTarget();
         }

         if (this.currentTargetBlock != null) {
            this.breakCurrentTarget();
         }
      }
   }

   private boolean isCurrentTargetValid() {
      return this.currentTargetBlock != null && this.isInRange(this.currentTargetBlock) && this.shouldBreak(this.currentTargetBlock);
   }

   private class_2338 findNewTarget() {
      int range = Math.round(this.radius.get());
      class_2338 playerPos = mc.field_1724.method_24515();
      return class_2338.method_20437(playerPos.method_10069(-range, 0, -range), playerPos.method_10069(range, range, range))
         .<class_2338>map(class_2338::method_10062)
         .filter(this::isInRange)
         .filter(this::shouldBreak)
         .min(Comparator.comparingDouble(pos -> mc.field_1724.method_5707(class_243.method_24953(pos))))
         .orElse(null);
   }

   private boolean isInRange(class_2338 pos) {
      double maxDistance = this.radius.get();
      return mc.field_1724.method_5707(class_243.method_24953(pos)) <= maxDistance * maxDistance;
   }

   private boolean shouldBreak(class_2338 pos) {
      class_2680 state = mc.field_1687.method_8320(pos);
      if (state == null || state.method_26215() || state.method_26214(mc.field_1687, pos) < 0.0F) {
         return false;
      } else if (this.breakAll.isState()) {
         return true;
      } else {
         String blockName = class_7923.field_41175.method_10221(state.method_26204()).method_12832().toLowerCase();
         return this.targetBlocks.contains(blockName);
      }
   }

   private void breakCurrentTarget() {
      if (this.currentTargetBlock != null && mc.field_1724 != null && mc.field_1761 != null) {
         mc.field_1761.method_2910(this.currentTargetBlock, class_2350.field_11036);
         mc.field_1761.method_2902(this.currentTargetBlock, class_2350.field_11036);
         if (this.swing.isState()) {
            mc.field_1724.method_6104(class_1268.field_5808);
         }

         if (mc.field_1687.method_8320(this.currentTargetBlock).method_26215()) {
            this.resetBreaking();
         }
      }
   }

   private void resetBreaking() {
      this.currentTargetBlock = null;
      if (mc.field_1761 != null) {
         mc.field_1761.method_2925();
      }
   }

   public void addBlock(String blockName) {
      this.targetBlocks.add(normalizeBlockName(blockName));
   }

   public void removeBlock(String blockName) {
      this.targetBlocks.remove(normalizeBlockName(blockName));
   }

   public void clearBlocks() {
      this.targetBlocks.clear();
      this.resetBreaking();
   }

   public boolean isTargetBlock(String blockName) {
      return this.targetBlocks.contains(normalizeBlockName(blockName));
   }

   public Set<String> getTargetBlocks() {
      return new HashSet<>(this.targetBlocks);
   }

   public static String normalizeBlockName(String blockName) {
      if (blockName == null) {
         return "";
      } else {
         String normalized = blockName.toLowerCase().trim();
         int namespaceSeparator = normalized.indexOf(58);
         return namespaceSeparator >= 0 ? normalized.substring(namespaceSeparator + 1) : normalized;
      }
   }

   @Override
   public void onDisable() {
      this.resetBreaking();
      super.onDisable();
   }
}
