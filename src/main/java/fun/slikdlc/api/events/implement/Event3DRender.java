package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import org.joml.Matrix4f;

public class Event3DRender extends Event {
   private final class_4587 matrices;
   private final Matrix4f positionMatrix;
   private final Matrix4f projectionMatrix;
   private final class_4184 camera;
   private final float tickDelta;

   @Generated
   public class_4587 getMatrices() {
      return this.matrices;
   }

   @Generated
   public Matrix4f getPositionMatrix() {
      return this.positionMatrix;
   }

   @Generated
   public Matrix4f getProjectionMatrix() {
      return this.projectionMatrix;
   }

   @Generated
   public class_4184 getCamera() {
      return this.camera;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }

   @Generated
   public Event3DRender(class_4587 matrices, Matrix4f positionMatrix, Matrix4f projectionMatrix, class_4184 camera, float tickDelta) {
      this.matrices = matrices;
      this.positionMatrix = positionMatrix;
      this.projectionMatrix = projectionMatrix;
      this.camera = camera;
      this.tickDelta = tickDelta;
   }
}
