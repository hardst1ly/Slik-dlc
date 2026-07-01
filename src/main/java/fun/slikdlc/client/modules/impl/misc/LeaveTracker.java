package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.Module;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_638;

public class LeaveTracker extends Module {
   public static LeaveTracker INSTANCE = new LeaveTracker();
   private final Map<UUID, LeaveTracker.TrackedPlayer> trackedPlayers = new HashMap<>();
   private class_638 lastWorld;
   private boolean initialized;

   public LeaveTracker() {
      super("LeaveTracker", "Пишет координаты ливнутых игроков из прогрузки", Module.ModuleCategory.MISC);
   }

   @Override
   public void onDisable() {
      this.trackedPlayers.clear();
      this.initialized = false;
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1687 != this.lastWorld) {
            this.lastWorld = mc.field_1687;
            this.trackedPlayers.clear();
            this.initialized = false;
         }

         if (!this.initialized) {
            this.snapshotPlayers();
            this.initialized = true;
         } else {
            Set<UUID> seenPlayers = new HashSet<>();

            for (class_1657 player : mc.field_1687.method_18456()) {
               if (player != mc.field_1724 && player.method_5805()) {
                  UUID uuid = player.method_5667();
                  seenPlayers.add(uuid);
                  this.trackedPlayers.put(uuid, new LeaveTracker.TrackedPlayer(player.method_5477().getString(), player.method_24515()));
               }
            }

            Iterator<Entry<UUID, LeaveTracker.TrackedPlayer>> iterator = this.trackedPlayers.entrySet().iterator();

            while (iterator.hasNext()) {
               Entry<UUID, LeaveTracker.TrackedPlayer> entry = iterator.next();
               if (!seenPlayers.contains(entry.getKey())) {
                  LeaveTracker.TrackedPlayer tracked = entry.getValue();
                  double distSq = mc.field_1724.method_5649(tracked.pos.method_10263(), tracked.pos.method_10264(), tracked.pos.method_10260());
                  if (distSq < 65536.0) {
                     ChatUtils.sendMessage(
                        class_124.field_1080
                           + tracked.name
                           + class_124.field_1068
                           + " ливнул на "
                           + class_124.field_1080
                           + tracked.pos.method_10263()
                           + " "
                           + tracked.pos.method_10264()
                           + " "
                           + tracked.pos.method_10260()
                     );
                  }

                  iterator.remove();
               }
            }
         }
      }
   }

   private void snapshotPlayers() {
      this.trackedPlayers.clear();

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != mc.field_1724 && player.method_5805()) {
            this.trackedPlayers.put(player.method_5667(), new LeaveTracker.TrackedPlayer(player.method_5477().getString(), player.method_24515()));
         }
      }
   }

   private record TrackedPlayer(String name, class_2338 pos) {
   }
}
