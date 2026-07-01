package fun.slikdlc.api.utils.network;

import fun.slikdlc.api.QClient;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.class_2596;

public final class NetworkUtils implements QClient {
   private static final List<class_2596<?>> silentPackets = new ArrayList<>();

   public static void sendSilentPacket(class_2596<?> packet) {
      silentPackets.add(packet);
      mc.method_1562().method_52787(packet);
   }

   public static void sendPacket(class_2596<?> packet) {
      mc.method_1562().method_52787(packet);
   }

   @Generated
   private NetworkUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static List<class_2596<?>> getSilentPackets() {
      return silentPackets;
   }
}
