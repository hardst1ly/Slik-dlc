package fun.slikdlc.api.utils.math;

import fun.slikdlc.api.QClient;
import lombok.Generated;

public class Timer implements QClient {
   private long startTime = System.currentTimeMillis();
   private long millis;

   public Timer() {
      this.reset();
   }

   public static Timer create() {
      return new Timer();
   }

   public boolean finished(long delay) {
      return System.currentTimeMillis() - delay >= this.millis;
   }

   public void reset() {
      this.millis = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      return System.currentTimeMillis() - this.millis;
   }

   public double deltaTime() {
      return mc.method_47599() > 0 ? 1.0 / mc.method_47599() : 1.0;
   }

   public boolean every(long ms) {
      boolean passed = this.getMillis(System.nanoTime() - this.millis) >= ms;
      if (passed) {
         this.reset();
      }

      return passed;
   }

   public boolean passed(long time) {
      return System.currentTimeMillis() - this.startTime > time;
   }

   public long getMillis(long time) {
      return time / 1000000L;
   }

   public long getTime() {
      return System.currentTimeMillis() - this.startTime;
   }

   public void setTime(long time) {
      this.startTime = time;
   }

   @Generated
   public long getStartTime() {
      return this.startTime;
   }

   @Generated
   public long getMillis() {
      return this.millis;
   }

   @Generated
   public void setStartTime(long startTime) {
      this.startTime = startTime;
   }

   @Generated
   public void setMillis(long millis) {
      this.millis = millis;
   }
}
