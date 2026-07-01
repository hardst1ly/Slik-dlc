package fun.slikdlc.mixin;

import fun.slikdlc.client.modules.impl.player.ItemScroller;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_465.class})
public abstract class HandledScreenMixin {
   public HandledScreenMixin() {
   }

   @Shadow
   @Nullable
   protected abstract class_1735 method_64240(double var1, double var3);

   @Shadow
   protected abstract void method_2383(@Nullable class_1735 var1, int var2, int var3, class_1713 var4);

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")}
   )
   private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      ItemScroller itemScroller = ItemScroller.INSTANCE;
      if (itemScroller.isEnable() && mc.field_1724 != null && mc.field_1761 != null) {
         long window = mc.method_22683().method_4490();
         boolean leftMousePressed = GLFW.glfwGetMouseButton(window, 0) == 1;
         boolean shiftPressed = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
         if (leftMousePressed && shiftPressed) {
            class_1735 slot = this.method_64240(mouseX, mouseY);
            if (slot != null && slot.method_7681()) {
               if (itemScroller.canQuickMove()) {
                  this.method_2383(slot, slot.field_7874, 0, class_1713.field_7794);
               }
            }
         } else {
            itemScroller.resetTimer();
         }
      }
   }
}
