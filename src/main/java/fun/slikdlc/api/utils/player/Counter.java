package fun.slikdlc.api.utils.player;

import fun.slikdlc.api.QClient;
import lombok.Generated;
import net.minecraft.class_3532;

public final class Counter implements QClient {
   private static int currentFPS;

   public static void updateFPS() {
      int prevFPS = mc.method_47599();
      currentFPS = class_3532.method_48781(0.5F, prevFPS, currentFPS);
   }

   @Generated
   private Counter() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static int getCurrentFPS() {
      return currentFPS;
   }
}
