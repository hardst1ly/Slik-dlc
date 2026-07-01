package fun.slikdlc.mixin;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.client.modules.impl.render.Removals;
import fun.slikdlc.client.modules.impl.render.ShaderEsp;
import fun.slikdlc.client.modules.impl.render.Sonar;
import net.minecraft.class_10209;
import net.minecraft.class_243;
import net.minecraft.class_4063;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9909;
import net.minecraft.class_9922;
import net.minecraft.class_9958;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_761.class})
public class WorldRendererMixin implements QClient {
   public WorldRendererMixin() {
   }

   @Inject(
      method = {"method_62201"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$renderParticles(class_9909 frameGraphBuilder, class_4184 camera, float tickDelta, class_9958 fog, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Частицы")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_62203"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$renderWeather(class_9909 frameGraphBuilder, class_243 pos, float tickDelta, class_9958 fog, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Погода")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_62209"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$addWeatherParticlesAndSound(class_4184 camera, CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Погода")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_62204"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$renderClouds(
      class_9909 frameGraphBuilder,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix,
      class_4063 renderMode,
      class_243 cameraPos,
      float ticks,
      int color,
      float cloudHeight,
      CallbackInfo ci
   ) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Облака")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_62208"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$renderBlockEntities(
      class_4587 matrices, class_4598 mainConsumers, class_4598 translucentConsumers, class_4184 camera, float tickDelta, CallbackInfo ci
   ) {
      if (ModuleClass.INSTANCE != null) {
         Removals removals = ModuleClass.removals;
         if (removals != null && removals.isEnabled("Блок-сущности")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_22710"},
      at = {@At("RETURN")}
   )
   private void render(
      class_9922 allocator,
      class_9779 tickCounter,
      boolean renderBlockOutline,
      class_4184 camera,
      class_757 gameRenderer,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix,
      CallbackInfo ci
   ) {
      Sonar sonar = ModuleClass.INSTANCE != null ? ModuleClass.sonar : null;
      boolean has3DListeners = EventInvoker.hasListeners(Event3DRender.class);
      boolean renderSonar = sonar != null && sonar.isEnable();
      if (has3DListeners || renderSonar) {
         class_10209.method_64146().method_15405("slikdlc_renderWorld");
         class_4587 matrices = new class_4587();
         matrices.method_34425(positionMatrix);
         if (has3DListeners) {
            new Event3DRender(matrices, positionMatrix, projectionMatrix, camera, tickCounter.method_60637(false)).call();
         }

         if (renderSonar) {
            sonar.renderFromMixin(positionMatrix, projectionMatrix, camera.method_19326());
         }
      }
   }

   @Inject(
      method = {"method_3254"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$drawEntityOutlinesFramebuffer(CallbackInfo ci) {
      if (ModuleClass.INSTANCE != null) {
         ShaderEsp shaderEsp = ModuleClass.shaderEsp;
         if (shaderEsp != null && shaderEsp.isEnable()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_22712"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onDrawBlockOutline(CallbackInfo ci) {
      if (ModuleClass.blockOverlay.isEnable()) {
         ci.cancel();
      }
   }
}
