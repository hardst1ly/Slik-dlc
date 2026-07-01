package fun.slikdlc.api.utils.tps;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventPacket;
import lombok.Generated;
import net.minecraft.class_2761;
import net.minecraft.class_310;
import net.minecraft.class_3532;

public class TPSCalc {
   private float TPS = 20.0F;
   private float adjustTicks = 0.0F;
   private long timestamp;
   private long lastPacketTime;
   private static final int SAMPLE_SIZE = 20;
   private final float[] tpsSamples = new float[20];
   private int sampleIndex = 0;

   public TPSCalc() {
   }

   @EventLink
   public void onPacket(EventPacket e) {
      if (e.getType() == EventPacket.Type.RECEIVE && e.getPacket() instanceof class_2761) {
         this.updateTPS();
      }
   }

   public float getTPS() {
      if (this.lastPacketTime == 0L) {
         return this.TPS;
      } else {
         class_310 mc = class_310.method_1551();
         return mc != null && mc.method_1562() != null && System.currentTimeMillis() - this.lastPacketTime <= 3500L ? this.TPS : 20.0F;
      }
   }

   private void updateTPS() {
      long now = System.nanoTime();
      this.lastPacketTime = System.currentTimeMillis();
      if (this.timestamp == 0L) {
         this.timestamp = now;
      } else {
         long delay = now - this.timestamp;
         this.timestamp = now;
         if (delay > 0L) {
            float maxTPS = 20.0F;
            float rawTPS = maxTPS * (1.0E9F / (float)delay);
            float boundedTPS = class_3532.method_15363(rawTPS, 0.0F, maxTPS);
            this.tpsSamples[this.sampleIndex % 20] = boundedTPS;
            this.sampleIndex++;
            int sampleCount = Math.min(this.sampleIndex, 20);
            float sum = 0.0F;

            for (int i = 0; i < sampleCount; i++) {
               float sample = this.tpsSamples[i];
               sum += sample;
            }

            this.TPS = (float)this.round(sum / sampleCount);
            this.adjustTicks = this.TPS - maxTPS;
         }
      }
   }

   public double round(double input) {
      return Math.round(input * 10.0) / 10.0;
   }

   @Generated
   public float getAdjustTicks() {
      return this.adjustTicks;
   }

   @Generated
   public long getTimestamp() {
      return this.timestamp;
   }

   @Generated
   public long getLastPacketTime() {
      return this.lastPacketTime;
   }

   @Generated
   public float[] getTpsSamples() {
      return this.tpsSamples;
   }

   @Generated
   public int getSampleIndex() {
      return this.sampleIndex;
   }
}
