package fun.slikdlc.client.modules.impl.combat;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1835;

public class ItemRelease extends Module {
   public static ItemRelease INSTANCE = new ItemRelease();
   private final ListSetting items = new ListSetting(
      "Предметы", new BooleanSetting("Лук", true), new BooleanSetting("Трезубец", false), new BooleanSetting("Арбалет", true)
   );
   private final FloatSetting tickBow = new FloatSetting("Задержка выстрела", 2.5F, 2.0F, 5.0F, 0.05F).visible(() -> this.items.is("Лук"));

   public ItemRelease() {
      super("ItemRelease", "Автоматически выпускает предмет когда он полностью натянут", Module.ModuleCategory.COMBAT);
      this.addSettings(new Setting[]{this.items, this.tickBow});
   }

   @EventLink
   public void onUpdate(EventUpdate e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.items.is("Лук")
            && mc.field_1724.method_6047().method_7909() instanceof class_1753
            && mc.field_1724.method_6115()
            && mc.field_1724.method_6048() >= this.tickBow.getValue().floatValue()) {
            mc.field_1761.method_2897(mc.field_1724);
         }

         if (this.items.is("Трезубец")
            && mc.field_1724.method_6047().method_7909() instanceof class_1835
            && mc.field_1724.method_6115()
            && mc.field_1724.method_6048() >= 10) {
            mc.field_1761.method_2897(mc.field_1724);
         }

         if (this.items.is("Арбалет")
            && mc.field_1724.method_6047().method_7909() instanceof class_1764
            && mc.field_1724.method_6115()
            && mc.field_1724.method_6048() >= class_1764.method_7775(mc.field_1724.method_6047(), mc.field_1724)) {
            mc.field_1761.method_2897(mc.field_1724);
         }
      }
   }
}
