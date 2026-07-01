package fun.slikdlc.api.events.implement;

import fun.slikdlc.api.events.Event;
import lombok.Generated;
import net.minecraft.class_1041;
import net.minecraft.class_332;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import org.joml.Matrix4f;

public class EventRender extends Event {
   public EventRender() {
   }

   public static class Default extends Event {
      private final class_332 context;
      private final float partialTicks;

      @Generated
      public class_332 getContext() {
         return this.context;
      }

      @Generated
      public float getPartialTicks() {
         return this.partialTicks;
      }

      @Generated
      public Default(class_332 context, float partialTicks) {
         this.context = context;
         this.partialTicks = partialTicks;
      }
   }

   public static class Game extends Event {
      private final class_761 context;
      private final class_4587 matrix;
      private final Matrix4f projectionMatrix;
      private final class_4184 camera;
      private final float partialTicks;
      private final long finishTimeNano;

      @Generated
      public class_761 getContext() {
         return this.context;
      }

      @Generated
      public class_4587 getMatrix() {
         return this.matrix;
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
      public float getPartialTicks() {
         return this.partialTicks;
      }

      @Generated
      public long getFinishTimeNano() {
         return this.finishTimeNano;
      }

      @Generated
      public Game(class_761 context, class_4587 matrix, Matrix4f projectionMatrix, class_4184 camera, float partialTicks, long finishTimeNano) {
         this.context = context;
         this.matrix = matrix;
         this.projectionMatrix = projectionMatrix;
         this.camera = camera;
         this.partialTicks = partialTicks;
         this.finishTimeNano = finishTimeNano;
      }
   }

   public static class World extends Event {
      private final class_1041 scaledResolution;
      private final float partialTicks;
      private final Matrix4f matrix;
      private final class_4587 matrixStack;

      @Generated
      public class_1041 getScaledResolution() {
         return this.scaledResolution;
      }

      @Generated
      public float getPartialTicks() {
         return this.partialTicks;
      }

      @Generated
      public Matrix4f getMatrix() {
         return this.matrix;
      }

      @Generated
      public class_4587 getMatrixStack() {
         return this.matrixStack;
      }

      @Generated
      public World(class_1041 scaledResolution, float partialTicks, Matrix4f matrix, class_4587 matrixStack) {
         this.scaledResolution = scaledResolution;
         this.partialTicks = partialTicks;
         this.matrix = matrix;
         this.matrixStack = matrixStack;
      }
   }
}
