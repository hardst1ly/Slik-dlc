package fun.slikdlc.client.ui;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.client.ClientSoundPlayer;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.ui.clickgui.ClickGuiInputHandler;
import fun.slikdlc.client.ui.clickgui.ClickGuiRenderer;
import fun.slikdlc.client.ui.clickgui.ClickGuiSettingRenderer;
import fun.slikdlc.client.ui.clickgui.ClickGuiState;
import fun.slikdlc.client.ui.clickgui.ClickGuiThemeSelector;
import net.minecraft.class_1041;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;

public class MenuPanel extends class_437 implements QClient {
   private static final ClickGuiState SHARED_STATE = new ClickGuiState();
   private final int categoryCount = Module.ModuleCategory.values().length;
   private final ClickGuiState state = SHARED_STATE;
   private final ClickGuiThemeSelector themeSelector = new ClickGuiThemeSelector();
   private final ClickGuiRenderer renderer = new ClickGuiRenderer(this.state, new ClickGuiSettingRenderer(), this.themeSelector);
   private final ClickGuiInputHandler inputHandler = new ClickGuiInputHandler(this.state, this.themeSelector);
   private final AnimationUtils openAnimation = new AnimationUtils(0.0F, 7.5F, Easings.CUBIC_OUT);
   private boolean closing;
   private boolean closeSoundPlayed;

   public MenuPanel() {
      super(class_2561.method_30163("ClickGui"));
      this.state.refreshModules();
   }

   private class_1041 getWindow() {
      return mc == null ? null : mc.method_22683();
   }

   private void syncLayout() {
      class_1041 window = this.getWindow();
      if (window != null) {
         this.state.updatePosition(window, this.categoryCount);
      }
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      class_1041 window = this.getWindow();
      if (window != null) {
         this.updateAnimation();
         float progress = this.getAnimationProgress();
         if (this.closing && progress <= 0.001F) {
            if (mc != null) {
               mc.method_1507(null);
            }
         } else {
            this.state.updatePosition(window, this.categoryCount);
            this.state.setRenderOffsetY(this.getPanelOffsetY(progress));
            this.renderer.render(context, mouseX, mouseY, window, progress);
            super.method_25394(context, mouseX, mouseY, delta);
         }
      }
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         this.state.setRenderOffsetY(this.getPanelOffsetY(this.getAnimationProgress()));
         return this.inputHandler.mouseClicked(mouseX, mouseY, button, this.getWindow()) || super.method_25402(mouseX, mouseY, button);
      }
   }

   public boolean method_25406(double mouseX, double mouseY, int button) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         return this.inputHandler.mouseReleased(button) || super.method_25406(mouseX, mouseY, button);
      }
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         this.state.setRenderOffsetY(this.getPanelOffsetY(this.getAnimationProgress()));
         return this.inputHandler.mouseDragged(mouseX, mouseY, button) || super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.closing) {
         return true;
      } else {
         this.syncLayout();
         this.state.setRenderOffsetY(this.getPanelOffsetY(this.getAnimationProgress()));
         return this.inputHandler.mouseScrolled(mouseX, mouseY, verticalAmount) || super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (this.closing) {
         return true;
      } else if (this.inputHandler.keyPressed(keyCode, modifiers)) {
         return true;
      } else if (keyCode == 256) {
         this.startClosing();
         return true;
      } else {
         return super.method_25404(keyCode, scanCode, modifiers);
      }
   }

   public boolean method_25400(char chr, int modifiers) {
      return this.closing ? true : this.inputHandler.charTyped(chr) || super.method_25400(chr, modifiers);
   }

   public void method_25419() {
      this.startClosing();
   }

   public void method_25432() {
      if (!this.closeSoundPlayed) {
         this.closeSoundPlayed = true;
         ClientSoundPlayer.playSound("closegui.wav", 0.6, 1.0F);
      }

      super.method_25432();
   }

   private void startClosing() {
      if (!this.closing) {
         this.closing = true;
         this.openAnimation.setEasing(Easings.CUBIC_IN);
         if (!this.closeSoundPlayed) {
            this.closeSoundPlayed = true;
            ClientSoundPlayer.playSound("closegui.wav", 0.6, 1.0F);
         }
      }
   }

   private void updateAnimation() {
      if (this.closing) {
         this.openAnimation.update(0.0F);
      } else {
         this.openAnimation.setEasing(Easings.CUBIC_OUT);
         this.openAnimation.update(1.0F);
      }
   }

   private float getAnimationProgress() {
      return class_3532.method_15363(this.openAnimation.getValue(), 0.0F, 1.0F);
   }

   private float getPanelOffsetY(float progress) {
      return (1.0F - progress) * 22.0F;
   }
}
