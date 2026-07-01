package fun.slikdlc.api.utils.player;

import fun.slikdlc.api.QClient;
import net.minecraft.class_1799;
import org.jetbrains.annotations.NotNull;

public record SlotSearchResult(int slot, boolean found, class_1799 stack) implements QClient {
   private static final SlotSearchResult NOT_FOUND_RESULT = new SlotSearchResult(-1, false, class_1799.field_8037);

   public static SlotSearchResult notFound() {
      return NOT_FOUND_RESULT;
   }

   @NotNull
   public static SlotSearchResult inOffhand(class_1799 stack) {
      return new SlotSearchResult(999, true, stack);
   }

   public boolean isHolding() {
      return mc.field_1724 == null ? false : this.isOffhand() || mc.field_1724.method_31548().field_7545 == this.slot;
   }

   public boolean isOffhand() {
      return this.slot == 999;
   }

   public boolean isInHotBar() {
      return this.slot >= 0 && this.slot < 9;
   }

   public void switchTo() {
      if (this.found && this.isInHotBar()) {
         HotbarUtil.switchTo(this.slot);
      }
   }

   public void switchToSilent() {
      if (this.found && this.isInHotBar()) {
         HotbarUtil.switchToSilent(this.slot);
      }
   }
}
