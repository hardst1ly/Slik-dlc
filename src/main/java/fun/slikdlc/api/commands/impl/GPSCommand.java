package fun.slikdlc.api.commands.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.commands.Command;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.api.utils.cmd.waypoint.Waypoint;
import net.minecraft.class_1074;
import net.minecraft.class_2172;

public class GPSCommand extends Command {
   public GPSCommand() {
      super("gps");
   }

   @Override
   public void execute(LiteralArgumentBuilder<class_2172> builder) {
      ((LiteralArgumentBuilder)builder.then(
            this.arg("X", IntegerArgumentType.integer()).then(this.arg("Z", IntegerArgumentType.integer()).executes(context -> {
               int x = (Integer)context.getArgument("X", Integer.class);
               int z = (Integer)context.getArgument("Z", Integer.class);
               Waypoint waypoint = new Waypoint(x, z);
               SlikDlc.INSTANCE.waypointStorage.set(waypoint);
               ChatUtils.sendMessage(class_1074.method_4662("Метка поставлена: ", new Object[]{x, z}));
               return 1;
            }))
         ))
         .then(this.literal("remove").executes(context -> {
            if (!SlikDlc.INSTANCE.waypointStorage.isEmpty()) {
               SlikDlc.INSTANCE.waypointStorage.clear();
               ChatUtils.sendMessage(class_1074.method_4662("Метка удалена!", new Object[0]));
            } else {
               ChatUtils.sendMessage(class_1074.method_4662("Метки не было", new Object[0]));
            }

            return 1;
         }));
   }
}
