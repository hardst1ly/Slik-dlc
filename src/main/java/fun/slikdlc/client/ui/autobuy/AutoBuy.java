package fun.slikdlc.client.ui.autobuy;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class AutoBuy extends class_437 implements QClient {
   private final float WIDTH = 170.0F;
   private final float HEIGHT = 240.0F;

   public AutoBuy() {
      super(class_2561.method_30163("AutoBuy"));
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      float X = mw.method_4486() / 2.0F - 170.0F;
      float Y = mw.method_4502() / 2.0F - 240.0F;
      RenderUtils.drawGradientRect(
         context.method_51448(), X, Y, 170.0F, 240.0F, 5.0F, ColorUtils.getThemeColor(), ColorUtils.darken(ColorUtils.getThemeColor(), 0.5F), true
      );
      super.method_25394(context, mouseX, mouseY, delta);
   }
}
