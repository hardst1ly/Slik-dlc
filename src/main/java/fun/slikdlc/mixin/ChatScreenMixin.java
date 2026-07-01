package fun.slikdlc.mixin;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.storages.implement.DragStorage;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.draggable.Draggable;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_408.class})
public class ChatScreenMixin {
   @Unique
   private boolean slikdlc$leftPressed;

   public ChatScreenMixin() {
   }

   @Inject(
      method = {"method_25402"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
      if (ModuleClass.interfaceModule.handleHudContextClick(mouseX, mouseY, button)) {
         cir.setReturnValue(true);
      } else {
         for (Draggable draggable : DragStorage.draggables.values()) {
            if (draggable.getModule().isEnable() && draggable.onClick(mouseX, mouseY, button)) {
               cir.setReturnValue(true);
               return;
            }
         }
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")}
   )
   private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      class_1041 window = mc.method_22683();
      boolean leftPressed = GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 0) == 1;
      if (this.slikdlc$leftPressed && !leftPressed) {
         for (Draggable draggable : DragStorage.draggables.values()) {
            draggable.onRelease(0);
         }
      }

      this.slikdlc$leftPressed = leftPressed;

      for (Draggable draggable : DragStorage.draggables.values()) {
         if (draggable.getModule().isEnable()) {
            draggable.onDraw(mouseX, mouseY, window, context.method_51448());
         }
      }

      ModuleClass.interfaceModule.renderHudContextMenu(context, mouseX, mouseY);
   }

   @Inject(
      method = {"method_25432"},
      at = {@At("HEAD")}
   )
   private void onRemoved(CallbackInfo ci) {
      this.slikdlc$leftPressed = false;

      for (Draggable draggable : DragStorage.draggables.values()) {
         draggable.onRelease(0);
      }

      try {
         SlikDlc.INSTANCE.configStorage.saveConfig(SlikDlc.INSTANCE.configStorage.currentConfig);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }
}
