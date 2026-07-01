package fun.slikdlc.client.modules.impl.movement;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.Event3DRender;
import fun.slikdlc.api.events.implement.EventMove;
import fun.slikdlc.api.events.implement.EventMoveInput;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.client.modules.Module;
import net.minecraft.class_10142;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_2828;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class FreeCam extends Module {
   public static FreeCam INSTANCE = new FreeCam();
   public class_243 pos;

   public FreeCam() {
      super("FreeCam", "Обзор местности за фейк игрока", Module.ModuleCategory.MOVEMENT);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (mc.field_1724 != null) {
         this.pos = mc.field_1724.method_19538();
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.field_1724 != null && this.pos != null) {
         mc.field_1724.method_33574(this.pos);
      }
   }

   @EventLink
   public void onEvent(EventPacket event) {
      class_2596<?> packet = event.getPacket();
      if (packet instanceof class_2828) {
         event.cancel();
      } else if (packet instanceof class_2724 || packet instanceof class_2678) {
         this.toggle();
      }
   }

   @EventLink
   public void onEvent(Event3DRender event) {
      if (this.pos != null && mc.field_1724 != null) {
         float width = mc.field_1724.method_17681() / 2.0F;
         float height = mc.field_1724.method_17682();
         class_238 box = new class_238(
            this.pos.field_1352 - width,
            this.pos.field_1351,
            this.pos.field_1350 - width,
            this.pos.field_1352 + width,
            this.pos.field_1351 + height,
            this.pos.field_1350 + width
         );
         this.drawHitbox(event.getMatrices(), box, event.getCamera().method_19326());
      }
   }

   private void drawHitbox(class_4587 matrices, class_238 box, class_243 camera) {
      double x1 = box.field_1323 - camera.field_1352;
      double y1 = box.field_1322 - camera.field_1351;
      double z1 = box.field_1321 - camera.field_1350;
      double x2 = box.field_1320 - camera.field_1352;
      double y2 = box.field_1325 - camera.field_1351;
      double z2 = box.field_1324 - camera.field_1350;
      Matrix4f matrix = matrices.method_23760().method_23761();
      class_289 tessellator = class_289.method_1348();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(class_10142.field_53876);
      RenderSystem.lineWidth(1.5F);
      class_287 buffer = tessellator.method_60827(class_5596.field_29344, class_290.field_1576);
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      float a = 1.0F;
      buffer.method_22918(matrix, (float)x1, (float)y1, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y1, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y1, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y1, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y1, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y1, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y1, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y1, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y2, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y2, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y2, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y2, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y2, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y2, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y2, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y2, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y1, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y2, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y1, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y2, (float)z1).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y1, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y2, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y1, (float)z2).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x1, (float)y2, (float)z2).method_22915(r, g, b, a);
      class_286.method_43433(buffer.method_60800());
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   @EventLink
   public void onEvent(EventMove event) {
      if (mc.field_1724 != null) {
         mc.field_1724.field_5960 = true;
         double speed = 1.0;
         double forward = mc.field_1724.field_3913.field_3905;
         double strafe = mc.field_1724.field_3913.field_3907;
         double yaw = Math.toRadians(mc.field_1724.method_36454());
         double motionX = 0.0;
         double motionZ = 0.0;
         if (forward != 0.0 || strafe != 0.0) {
            double angle = yaw + Math.atan2(-strafe, forward);
            motionX = -Math.sin(angle) * speed;
            motionZ = Math.cos(angle) * speed;
         }

         double motionY = 0.0;
         if (mc.field_1690.field_1903.method_1434()) {
            motionY = speed;
         } else if (mc.field_1690.field_1832.method_1434()) {
            motionY = -speed;
         }

         event.setMovePos(new class_243(motionX, motionY, motionZ));
      }
   }

   @EventLink
   public void onEvent(EventMoveInput event) {
      if (mc.field_1724 != null) {
         if (mc.field_1724.method_18376() == class_4050.field_18081 || mc.field_1724.method_18376() == class_4050.field_18079) {
            event.setStrafe(event.getStrafe() * 5.0F);
         }
      }
   }
}
