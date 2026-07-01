package fun.slikdlc.client.modules.impl.movement;

import fun.slikdlc.api.utils.chat.ChatUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.combat.Aura;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import lombok.Generated;
import net.minecraft.class_241;
import net.minecraft.class_3532;

public class ElytraBoost extends Module {
   private static final String[] RANGE_LABELS = new String[]{"0 - 5", "5 - 10", "10 - 15", "15 - 20", "20 - 25", "25 - 30", "30 - 35", "35 - 40", "40 - 45"};
   private static final long DEBUG_MESSAGE_INTERVAL_MS = 800L;
   public static ElytraBoost INSTANCE = new ElytraBoost();
   private final FloatSetting[] yawSpeeds = new FloatSetting[9];
   private final FloatSetting[] pitchSpeeds = new FloatSetting[9];
   private final ModeSetting mode = new ModeSetting("Сервер", "Custom", "Custom", "LonyGrief", "BravoHVH", "ReallyWorld", "SlimeWorld");
   private final BooleanSetting debug = new BooleanSetting("Дебаг", false).visible(this::isCustomMode);
   private long lastDebugMessageAt;

   public ElytraBoost() {
      super("ElytraBoost", "Ускоряет на элитрах", Module.ModuleCategory.MOVEMENT);

      for (int i = 0; i < this.yawSpeeds.length; i++) {
         this.yawSpeeds[i] = new FloatSetting("yaw " + RANGE_LABELS[i], 1.5F, 1.5F, 2.5F, 0.01F).visible(this::isCustomMode);
      }

      for (int i = 0; i < this.pitchSpeeds.length; i++) {
         this.pitchSpeeds[i] = new FloatSetting("pitch " + RANGE_LABELS[i], 1.5F, 1.5F, 2.5F, 0.01F).visible(this::isCustomMode);
      }

      this.addSettings(new Setting[]{this.mode, this.debug});
      this.addSettings(this.yawSpeeds);
      this.addSettings(this.pitchSpeeds);
   }

   public boolean isCustomMode() {
      return this.mode.is("Custom");
   }

   public class_241 getBoostV2() {
      float yaw = mc.field_1724 != null ? mc.field_1724.method_36454() : 0.0F;
      float pitch = mc.field_1724 != null ? mc.field_1724.method_36455() : 0.0F;
      Aura aura = Aura.INSTANCE;
      if (aura != null && aura.isEnable() && aura.getTarget() != null) {
         class_241 rotations = aura.getTargetRotations();
         if (rotations != null) {
            yaw = rotations.field_1343;
            pitch = rotations.field_1342;
         }
      }

      float normalizedYaw = this.convertValToRange(class_3532.method_15393(yaw));
      float normalizedPitch = this.convertValToRange(Math.abs(pitch));
      int yawIndex = this.getRangeIndex(normalizedYaw, this.yawSpeeds.length);
      int pitchIndex = this.getRangeIndex(normalizedPitch, this.pitchSpeeds.length);
      float yawSpeed = this.yawSpeeds[yawIndex].getValue().floatValue();
      float pitchSpeed = this.pitchSpeeds[pitchIndex].getValue().floatValue();
      if (pitchSpeed > yawSpeed) {
         yawSpeed = pitchSpeed;
      }

      this.logDebug(yawIndex, yawSpeed, pitchIndex, pitchSpeed);
      return new class_241(yawSpeed, pitchSpeed);
   }

   private void logDebug(int yawIndex, float yawSpeed, int pitchIndex, float pitchSpeed) {
      if (this.debug.isState()) {
         long now = System.currentTimeMillis();
         if (now - this.lastDebugMessageAt >= 800L) {
            this.lastDebugMessageAt = now;
            ChatUtils.sendMessage("yaw " + RANGE_LABELS[yawIndex] + ": " + yawSpeed + " | pitch " + RANGE_LABELS[pitchIndex] + ": " + pitchSpeed);
         }
      }
   }

   private int getRangeIndex(float value, int length) {
      return Math.min((int)(value / 5.0F), length - 1);
   }

   private float convertValToRange(float value) {
      float result = Math.abs(value);
      if (result > 90.0F) {
         result = 180.0F - result;
      }

      if (result > 45.0F) {
         result = 90.0F - result;
      }

      return result;
   }

   @Generated
   public FloatSetting[] getYawSpeeds() {
      return this.yawSpeeds;
   }

   @Generated
   public FloatSetting[] getPitchSpeeds() {
      return this.pitchSpeeds;
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public BooleanSetting getDebug() {
      return this.debug;
   }

   @Generated
   public long getLastDebugMessageAt() {
      return this.lastDebugMessageAt;
   }

   @Generated
   public void setLastDebugMessageAt(long lastDebugMessageAt) {
      this.lastDebugMessageAt = lastDebugMessageAt;
   }
}
