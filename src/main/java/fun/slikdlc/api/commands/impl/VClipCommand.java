package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.api.commands.Command;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2350.class_2351;

public class VClipCommand extends Command {
   public VClipCommand() {
      super("vclip");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      builder.then(this.arg("Y", IntegerArgumentType.integer()).executes(context -> {
         int y = (Integer)context.getArgument("Y", Integer.class);
         mc.field_1724.method_5814(mc.field_1724.method_23317(), mc.field_1724.method_23318() + y, mc.field_1724.method_23321());
         return 1;
      }));
      builder.then(this.literal("up").executes(context -> {
         this.clipToSafeBlock(true);
         return 1;
      }));
      builder.then(this.literal("down").executes(context -> {
         this.clipToSafeBlock(false);
         return 1;
      }));
   }

   private void clipToSafeBlock(boolean up) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int startY = mc.field_1724.method_31478();
         int minY = mc.field_1687.method_31607();
         int maxY = mc.field_1687.method_31600() - 2;
         int step = up ? 1 : -1;
         int from = up ? startY + 1 : startY - 1;
         int to = up ? maxY : minY;

         for (int y = from; up ? y <= to : y >= to; y += step) {
            if (this.isSafeStandPosition(y)) {
               class_265 shape = mc.field_1687
                  .method_8320(new class_2338(mc.field_1724.method_31477(), y - 1, mc.field_1724.method_31479()))
                  .method_26220(mc.field_1687, new class_2338(mc.field_1724.method_31477(), y - 1, mc.field_1724.method_31479()));
               double offsetY = shape.method_1110() ? 0.0 : shape.method_1105(class_2351.field_11052);
               mc.field_1724.method_5814(mc.field_1724.method_23317(), y + offsetY, mc.field_1724.method_23321());
               return;
            }
         }
      }
   }

   private boolean isSafeStandPosition(int y) {
      class_2338 floorPos = new class_2338(mc.field_1724.method_31477(), y - 1, mc.field_1724.method_31479());
      class_2338 feetPos = floorPos.method_10084();
      class_2338 headPos = feetPos.method_10084();
      class_2680 floorState = mc.field_1687.method_8320(floorPos);
      return floorState.method_26220(mc.field_1687, floorPos).method_1110()
         ? false
         : mc.field_1687.method_8320(feetPos).method_26220(mc.field_1687, feetPos).method_1110()
            && mc.field_1687.method_8320(headPos).method_26220(mc.field_1687, headPos).method_1110();
   }
}
