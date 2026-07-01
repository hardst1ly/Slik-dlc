package fun.slikdlc.client.modules.impl.player;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.utils.rotate.Rotation;
import fun.slikdlc.api.utils.rotate.RotationUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1802;
import net.minecraft.class_241;
import net.minecraft.class_243;

public class ItemAim extends Module {
   public static ItemAim INSTANCE = new ItemAim();
   public ListSetting element = new ListSetting("Лутать", new BooleanSetting("Шары", true), new BooleanSetting("Элитры", true));

   public ItemAim() {
      super("ItemAim", "Автоматически наводиться на предмет", Module.ModuleCategory.PLAYER);
      this.addSettings(new Setting[]{this.element});
   }

   @EventLink
   public void onEvent(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_1542 targetItem = this.findTargetItem();
         if (targetItem != null) {
            class_241 rotations = this.getItemRotations(targetItem);
            RotationStorage.update(new Rotation(rotations.field_1343, rotations.field_1342), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, false);
         }
      }
   }

   private class_1542 findTargetItem() {
      class_1542 bestItem = null;
      double bestDistance = Double.MAX_VALUE;

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1542 itemEntity && this.isWantedItem(itemEntity)) {
            double distance = mc.field_1724.method_5858(itemEntity);
            if (distance < bestDistance) {
               bestDistance = distance;
               bestItem = itemEntity;
            }
         }
      }

      return bestItem;
   }

   private boolean isWantedItem(class_1542 itemEntity) {
      return this.element.is("Шары") && itemEntity.method_6983().method_31574(class_1802.field_8575)
         || this.element.is("Элитры") && itemEntity.method_6983().method_31574(class_1802.field_8833);
   }

   private class_241 getItemRotations(class_1542 itemEntity) {
      class_243 targetPos = itemEntity.method_5829().method_1005();
      return RotationUtils.getRotations(targetPos);
   }
}
