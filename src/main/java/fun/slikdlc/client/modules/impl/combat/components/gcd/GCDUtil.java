package fun.slikdlc.client.modules.impl.combat.components.gcd;

import fun.slikdlc.api.QClient;

public class GCDUtil implements QClient {
   public GCDUtil() {
   }

   public static float getFixedRotation(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getGCDValue() {
      return (float)(getGCD() * 0.15);
   }

   public static float getGCD() {
      double f = 0.5000000149011612;
      return (float)(f * f * f * 8.0);
   }

   public static float getDeltaMouse(float delta) {
      return Math.round(delta / getGCDValue());
   }
}
