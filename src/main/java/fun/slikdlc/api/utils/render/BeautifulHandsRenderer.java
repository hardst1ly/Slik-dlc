package fun.slikdlc.api.utils.render;

import fun.slikdlc.client.modules.impl.render.BeautifulHands;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1743;
import net.minecraft.class_1747;
import net.minecraft.class_1755;
import net.minecraft.class_1766;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1819;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_742;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import net.minecraft.class_918;

public class BeautifulHandsRenderer {
   private static final BeautifulHandsRenderer INSTANCE = new BeautifulHandsRenderer();
   private static final double ANIMATION_SPEED = 30.0;
   private static final double MAX_DELTA = 0.05;
   private double prevFrameTime = System.nanoTime() / 1.0E9;
   private double deltaTime;
   private double prevSwimRotation;
   private float swingAngleY;
   private float swingAngleX;
   private float swingVelY;
   private float swingVelX;
   private float swingVelZ;
   private float vertAngleY;
   private float vertVelYSlime;
   private float vertAngleYSlime;
   private float climbBlend;
   private float crawlCount;
   private float dirCrawlCount;
   private float climbCount;
   private float inWaterCounter;
   private boolean physicsUpdated;
   private boolean swingLeft;
   private float prevSwingProgress;
   private final class_310 mc = class_310.method_1551();

   public BeautifulHandsRenderer() {
   }

   public static BeautifulHandsRenderer getInstance() {
      return INSTANCE;
   }

   public void updateDelta() {
      double now = System.nanoTime() / 1.0E9;
      this.deltaTime = Math.min(0.05, Math.max(0.0, now - this.prevFrameTime));
      this.prevFrameTime = now;
      this.physicsUpdated = false;
   }

   public void onNewSwing(float swingProgress) {
      if (swingProgress > 0.0F && this.prevSwingProgress == 0.0F) {
         this.swingLeft = !this.swingLeft;
      }

      this.prevSwingProgress = swingProgress;
   }

   public void renderArm(class_4587 matrices, class_4597 consumers, int light, class_1306 side) {
      class_742 player = this.mc.field_1724;
      if (player != null) {
         matrices.method_22903();
         float f = side == class_1306.field_6183 ? 1.0F : -1.0F;
         matrices.method_22907(class_7833.field_40716.rotationDegrees(92.0F));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(45.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(f * -41.0F));
         matrices.method_46416(f * 0.3F, -1.1F, 0.45F);
         matrices.method_22909();
      }
   }

   public void renderArmFirstPerson(class_4587 matrices, class_4597 consumers, int light, float equipProgress, float swingProgress, class_1306 side) {
      class_742 player = this.mc.field_1724;
      if (player != null) {
         boolean right = side != class_1306.field_6182;
         float f = right ? 1.0F : -1.0F;
         float f1 = class_3532.method_15355(swingProgress);
         float f2 = -0.3F * class_3532.method_15374(f1 * (float) Math.PI);
         float f3 = 0.4F * class_3532.method_15374(f1 * (float) (Math.PI * 2));
         float f4 = -0.4F * class_3532.method_15374(swingProgress * (float) Math.PI);
         matrices.method_46416(f * (f2 + 0.64F), f3 + -0.6F + equipProgress * -0.6F, f4 + -0.72F);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(f * 45.0F));
         float f5 = class_3532.method_15374(swingProgress * swingProgress * (float) Math.PI);
         float f6 = class_3532.method_15374(f1 * (float) Math.PI);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(f * f6 * 70.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(f * f5 * -20.0F));
         matrices.method_22904(f * -1.0F, 3.6F, 3.5);
         matrices.method_22907(class_7833.field_40718.rotationDegrees(f * 120.0F));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(200.0F));
         matrices.method_22907(class_7833.field_40716.rotationDegrees(f * -135.0F));
         matrices.method_46416(f * 5.6F, 0.0F, 0.0F);
         this.applyHandOffsets(matrices, side);
      }
   }

   private void applyHandOffsets(class_4587 matrices, class_1306 side) {
      BeautifulHands m = BeautifulHands.INSTANCE;
      if (m.isEnable()) {
         if (side == class_1306.field_6183) {
            matrices.method_46416(m.rightX.getValue().floatValue(), m.rightY.getValue().floatValue(), m.rightZ.getValue().floatValue());
         } else {
            matrices.method_46416(m.leftX.getValue().floatValue(), m.leftY.getValue().floatValue(), m.leftZ.getValue().floatValue());
         }
      }
   }

   public void renderItem(class_742 player, class_1799 stack, class_811 mode, boolean leftHanded, class_4587 matrices, class_4597 consumers, int light) {
      if (!stack.method_7960()) {
         class_918 itemRenderer = this.mc.method_1480();
         itemRenderer.method_23177(
            player, stack, mode, leftHanded, matrices, consumers, player.method_37908(), light, class_4608.field_21444, player.method_5628() + mode.ordinal()
         );
      }
   }

   private float ease(float v) {
      float c1 = 1.70158F;
      float c2 = c1 * 1.525F;
      if (v < 0.5F) {
         float d = 2.0F * v;
         return d * d * ((c2 + 1.0F) * d - c2) * 0.5F;
      } else {
         float s = 2.0F * v - 2.0F;
         return (s * s * ((c2 + 1.0F) * s + c2) + 2.0F) * 0.5F;
      }
   }

   private float swingRot(float p) {
      return p < 0.6F
         ? class_3532.method_15374(class_3532.method_15363(p, 0.0F, 0.12506F) * 12.56F)
         : class_3532.method_15374(class_3532.method_15363(p, 0.62532F, 0.75038F) * 12.56F);
   }

   private boolean isLantern(class_1799 s) {
      return s.method_31574(class_1802.field_16539) || s.method_31574(class_1802.field_22016);
   }

   private boolean isThinBlock(class_1799 s) {
      return !(s.method_7909() instanceof class_1747)
         ? false
         : s.method_31574(class_1802.field_8276)
            || s.method_31574(class_1802.field_8725)
            || s.method_31574(class_1802.field_8865)
            || s.method_31574(class_1802.field_8366);
   }

   private boolean isTorch(class_1799 s) {
      String n = s.method_7964().getString().toLowerCase();
      return n.contains("torch") || n.contains("факел");
   }

   private boolean isSmallItem(class_1799 s) {
      return !(s.method_7909() instanceof class_1747)
         && !(s.method_7909() instanceof class_1766)
         && !(s.method_7909() instanceof class_1829)
         && !(s.method_7909() instanceof class_1743)
         && !(s.method_7909() instanceof class_1787)
         && !(s.method_7909() instanceof class_1755)
         && !(s.method_7909() instanceof class_1819);
   }

   private boolean isWeapon(class_1799 s) {
      return s.method_7909() instanceof class_1829 || s.method_7909() instanceof class_1743;
   }

   private boolean isTool(class_1799 s) {
      return s.method_7909() instanceof class_1766;
   }

   private boolean isShovel(class_1799 s) {
      return s.method_7909() instanceof class_1821;
   }

   private void applySwing(class_4587 matrices, class_742 player, class_1268 handIn, class_1799 stack, float swingProgress) {
      boolean mainHand = handIn == class_1268.field_5808;
      if (player.method_6068() == class_1306.field_6182) {
         mainHand = !mainHand;
      }

      BeautifulHands m = BeautifulHands.INSTANCE;
      float ll = mainHand ? 1.0F : -1.0F;
      float handDir = handIn == class_1268.field_5808 ? 1.0F : -1.0F;
      float swingR = this.swingRot(swingProgress);
      float swing = this.ease(class_3532.method_15374(swingProgress * (float) Math.PI));
      boolean forward = m.useForwardAttack();
      boolean normal = m.useNormalAttack();
      if (stack.method_7909() instanceof class_1829 && forward) {
         matrices.method_22904(0.12 * ll * swingR, 0.04 * swingR, -0.95 * swing);
         matrices.method_22904(0.02 * ll * swing, 0.1 * swing, -0.1 * swingR);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(8.0F * swingR * ll));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(14.0F * swingR));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(-18.0F * swingR * ll));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-32.0F * swing));
      } else if (stack.method_7909() instanceof class_1829 && normal) {
         this.applyGenericSwing(matrices, ll, swingR, swing);
      } else if ((this.swingLeft || this.isWeapon(stack)) && !this.isShovel(stack)) {
         if (this.isWeapon(stack)) {
            matrices.method_22904(0.8 * ll * swingR, 0.3 * swingR, -0.5 * swing);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(15.0F * swingR * ll));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(20.0F * swingR));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-70.0F * swingR * ll));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-(stack.method_7909() instanceof class_1829 ? 40.0F : 30.0F) * swing));
         } else if (this.isTool(stack)) {
            matrices.method_22904(0.1 * ll * swingR, 0.1 * swingR, -0.5 * swing);
            matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F * swingR));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-20.0F * swingR * ll));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-40.0F * swing));
         } else {
            matrices.method_22904(0.1 * ll * swingR, 0.1 * swingR, -0.1 * swing);
            matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F * swingR));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-10.0F * swingR * ll));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-40.0F * swing));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(10.0F * swing * ll));
         }
      } else if (this.isShovel(stack)) {
         matrices.method_22904(0.0, 0.15 * swingR, -0.25 * swingR);
         matrices.method_22904(0.0, 0.0, -0.2 * swing);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(15.0F * swingR));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(35.0F * swingR));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-30.0F * swing));
      } else if (stack.method_7909() instanceof class_1829) {
         matrices.method_22904(-0.55 * ll * swingR, -0.8 * swingR, -0.77 * swing);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(5.0F * swingR * ll));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F * swingR));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(70.0F * swingR * ll));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-50.0F * swing));
      } else if (this.isTool(stack)) {
         matrices.method_22904(0.1 * ll * swingR, 0.1 * swingR, -0.5 * swing);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F * swingR));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(-20.0F * swingR * ll));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-40.0F * swing));
      } else {
         this.applyGenericSwing(matrices, ll, swingR, swing);
      }
   }

   private void applyGenericSwing(class_4587 m, float dir, float swingR, float swing) {
      m.method_22904(0.1 * dir * swingR, 0.1 * swingR, -0.1 * swing);
      m.method_22907(class_7833.field_40714.rotationDegrees(30.0F * swingR));
      m.method_22907(class_7833.field_40718.rotationDegrees(-10.0F * swingR * dir));
      m.method_22907(class_7833.field_40714.rotationDegrees(-40.0F * swing));
      m.method_22907(class_7833.field_40716.rotationDegrees(10.0F * swing * dir));
   }

   private void applyBaseHandPose(class_4587 matrices, class_1306 side, float equipProgress, float swingProgress) {
      int dir = side == class_1306.field_6183 ? 1 : -1;
      matrices.method_22904(dir, -equipProgress * 0.3, 0.3);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(45.0F * dir));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(-40.0F * dir));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(dir * 45.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(dir * -45.0F));
      matrices.method_22905(0.9F, 0.9F, 0.9F);
   }

   private void applyArmPrePose(class_4587 matrices, class_1799 stack, class_1306 side) {
      int dir = side == class_1306.field_6183 ? 1 : -1;
      if (this.isLantern(stack)) {
         matrices.method_22904(0.1 * dir, 0.0, -0.1);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(10.0F));
      }
   }

   private void applyItemPose(class_4587 matrices, class_742 player, class_1268 handIn, class_1306 side, class_1799 stack, float swingProgress) {
      int dir = side == class_1306.field_6183 ? 1 : -1;
      boolean mainHand = handIn == class_1268.field_5808;
      if (player.method_6068() == class_1306.field_6182) {
         mainHand = !mainHand;
      }

      matrices.method_22904(-0.3 * dir, 0.65, -0.1);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(-65.0F * dir));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(10.0F));
      if (stack.method_7909() instanceof class_1747 && !(stack.method_7909() instanceof class_1755)) {
         if (this.isTorch(stack)) {
            matrices.method_22905(1.5F, 1.5F, 1.5F);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-25.0F * dir));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(5.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * dir));
            matrices.method_22904(0.2 * dir, 0.2, 0.05);
         } else if (this.isThinBlock(stack)) {
            matrices.method_22904(0.0, 0.0, -0.1);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-5.0F * dir));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(15.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * dir));
         } else if (this.isLantern(stack)) {
            this.applyLanternPose(matrices, player, side, swingProgress);
         } else {
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-25.0F * dir));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(5.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * dir));
            matrices.method_22904(0.2 * dir, 0.2, 0.05);
         }
      } else if (this.isSmallItem(stack)) {
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-5.0F * dir));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(15.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * dir));
         matrices.method_22904(0.0, -0.05, -0.1);
         matrices.method_22905(0.7F, 0.7F, 0.7F);
      } else {
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-75.0F * dir));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(70.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(45.0F * dir));
         matrices.method_22905(1.2F, 1.2F, 1.2F);
      }
   }

   private void applyLanternPose(class_4587 matrices, class_742 player, class_1306 side, float swingProgress) {
      float dt = (float)(this.deltaTime * 30.0);
      int dir = side == class_1306.field_6183 ? 1 : -1;
      float yawDelta = player.field_6259 - player.field_6241;
      float pitchDelta = player.field_6004 - player.method_36455();
      this.swingVelY += yawDelta * 0.015F * dt;
      this.swingVelY += swingProgress * 2.0F * dt;
      this.swingVelX += pitchDelta * 0.015F * dt;
      this.swingVelY = this.swingVelY - 0.1F * this.swingAngleY * dt;
      this.swingVelX = this.swingVelX - 0.1F * this.swingAngleX * dt;
      this.swingVelY = this.swingVelY * (float)Math.pow(0.88, dt);
      this.swingVelX = this.swingVelX * (float)Math.pow(0.88, dt);
      this.swingAngleY = this.swingAngleY + this.swingVelY * dt;
      this.swingAngleX = this.swingAngleX + this.swingVelX * dt;
      double spd = player.method_18798().method_1033();
      this.swingVelZ = this.swingVelZ + (float)(dir > 0 ? (spd * -15.0 - this.swingVelZ) * 0.1 * dt : (spd * 15.0 - this.swingVelZ) * 0.1 * dt);
      if (spd > 0.09 && (player.method_24828() || player.method_5681() || player.method_6101()) && (Boolean)this.mc.field_1690.method_42448().method_41753()) {
         this.swingVelY = this.swingVelY + (float)((Math.random() < 0.5 ? -5.5 : 5.5) * spd * dt);
      }

      matrices.method_22904(0.0, 0.0, -0.1);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(35.0F * dir + this.swingAngleY));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(15.0F + this.swingAngleX));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * dir + this.swingVelZ));
      matrices.method_22904(0.3 * dir, -0.35, 0.0);
      matrices.method_22904(0.0, 0.0, 0.1);
      matrices.method_22905(1.5F, 1.5F, 1.5F);
   }

   public void render(
      class_742 player,
      float partialTicks,
      class_1268 handIn,
      float swingProgress,
      class_1799 stack,
      float equipProgress,
      class_4587 matrices,
      class_4597 consumers,
      int light,
      class_1306 side
   ) {
      boolean right = side == class_1306.field_6183;
      matrices.method_22903();
      if (stack.method_7960()) {
         this.applySwing(matrices, player, handIn, stack, swingProgress);
         this.applyBaseHandPose(matrices, side, equipProgress, swingProgress);
         this.renderArmFirstPerson(matrices, consumers, light, 0.0F, 0.0F, side);
         matrices.method_22909();
      } else {
         this.applySwing(matrices, player, handIn, stack, swingProgress);
         this.applyArmPrePose(matrices, stack, side);
         this.applyBaseHandPose(matrices, side, equipProgress, swingProgress);
         this.renderArmFirstPerson(matrices, consumers, light, 0.0F, 0.0F, side);
         this.applyItemPose(matrices, player, handIn, side, stack, swingProgress);
         this.renderItem(player, stack, right ? class_811.field_4322 : class_811.field_4321, !right, matrices, consumers, light);
         matrices.method_22909();
      }
   }
}
