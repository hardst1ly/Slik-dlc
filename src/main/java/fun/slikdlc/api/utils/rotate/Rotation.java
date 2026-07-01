package fun.slikdlc.api.utils.rotate;

import fun.slikdlc.api.QClient;
import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_5611;

public class Rotation implements QClient {
   private float yaw;
   private float pitch;

   public Rotation(class_1297 entity) {
      this.yaw = entity.method_36454();
      this.pitch = entity.method_36455();
   }

   public float getDelta(Rotation target) {
      float yawDelta = class_3532.method_15393(target.getYaw() - this.yaw);
      float pitchDelta = target.getPitch() - this.pitch;
      return (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
   }

   public double getDeltaDouble(Rotation target) {
      double yawDelta = class_3532.method_15393(target.getYaw() - this.yaw);
      double pitchDelta = class_3532.method_15393(target.getPitch() - this.pitch);
      return Math.hypot(yawDelta, pitchDelta);
   }

   public static class_5611 camera() {
      return new class_5611(cameraYaw(), cameraPitch());
   }

   public static float cameraYaw() {
      return class_3532.method_15393(mc.field_1773.method_19418().method_19330() + (mc.field_1773.method_19418().method_19333() ? 180 : 0));
   }

   public static float cameraPitch() {
      return (mc.field_1773.method_19418().method_19333() ? -1 : 1) * mc.field_1773.method_19418().method_19329();
   }

   public static Rotation from(class_1657 player, class_1297 target) {
      class_243 playerPos = player.method_5836(0.0F);
      class_243 targetPos = target.method_19538().method_1031(0.0, target.method_17682() * 0.5, 0.0);
      double dx = targetPos.field_1352 - playerPos.field_1352;
      double dy = targetPos.field_1351 - playerPos.field_1351;
      double dz = targetPos.field_1350 - playerPos.field_1350;
      double distanceXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distanceXZ)));
      return new Rotation(yaw, pitch);
   }

   public final class_243 toVector() {
      float f = this.pitch * (float) (Math.PI / 180.0);
      float g = -this.yaw * (float) (Math.PI / 180.0);
      float h = class_3532.method_15362(g);
      float i = class_3532.method_15374(g);
      float j = class_3532.method_15362(f);
      float k = class_3532.method_15374(f);
      return new class_243(i * j, -k, h * j);
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Rotation other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         return Float.compare(this.getYaw(), other.getYaw()) != 0 ? false : Float.compare(this.getPitch(), other.getPitch()) == 0;
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Rotation;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getYaw());
      return result * 59 + Float.floatToIntBits(this.getPitch());
   }

   @Generated
   @Override
   public String toString() {
      return "Rotation(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
   }

   @Generated
   public Rotation() {
   }

   @Generated
   public Rotation(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
}
