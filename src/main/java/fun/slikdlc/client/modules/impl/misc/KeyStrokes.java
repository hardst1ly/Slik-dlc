package fun.slikdlc.client.modules.impl.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;

public class KeyStrokes extends Module {
   public static KeyStrokes INSTANCE = new KeyStrokes();
   private final InterfaceProcessing keyStrokes;

   public KeyStrokes() {
      super("KeyStrokes", "Shows key presses and CPS", Module.ModuleCategory.MISC);
      this.keyStrokes = new fun.slikdlc.client.modules.impl.render.base.implement.KeyStrokes(
         SlikDlc.draggable(this, "MiscKeyStrokes", 150.0F, 120.0F)
      );
   }

   @EventLink(
      priority = -200
   )
   public void onRender(EventRender.Default event) {
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      this.keyStrokes.draggable.beginRenderTilt(event.getContext().method_51448());

      try {
         this.keyStrokes.onRender(event);
      } finally {
         this.keyStrokes.draggable.endRenderTilt(event.getContext().method_51448());
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
      }
   }
}
