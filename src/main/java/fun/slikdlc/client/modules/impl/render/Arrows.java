package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.FreeLookStorage;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.api.utils.math.MathUtils;
import fun.slikdlc.api.utils.render.RenderUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import net.minecraft.class_742;
import net.minecraft.class_7833;

public class Arrows extends Module {
   public static Arrows INSTANCE = new Arrows();
   private static final class_2960 FIRST_ARROW_TEXTURE = class_2960.method_60655("slikdlc", "textures/arrows/arrow.png");
   private static final class_2960 SECOND_ARROW_TEXTURE = class_2960.method_60655("slikdlc", "textures/arrows/arr.png");
   private static final class_2960 MAMA_ARROW_TEXTURE = class_2960.method_60655("slikdlc", "textures/arrows/arrowsnurik.png");
   private final ModeSetting type = new ModeSetting("Вид", "Первый", "Первый", "Второй", "Третий");
   private final FloatSetting radius = new FloatSetting("Радиус", 58.0F, 30.0F, 120.0F, 1.0F);
   private final FloatSetting size = new FloatSetting("Размер", 13.0F, 8.0F, 28.0F, 0.5F);
   private final FloatSetting glowRadius = new FloatSetting("Свечение", 7.5F, 0.0F, 20.0F, 0.5F);
   private final Map<UUID, Arrows.ArrowState> states = new HashMap<>();
   private final Set<UUID> seenPlayers = new HashSet<>();

   public Arrows() {
      super("Arrows", "Красивые стрелочки на энтити", Module.ModuleCategory.RENDER);
      this.addSettings(new Setting[]{this.type, this.radius, this.size, this.glowRadius});
   }

   @EventLink
   public void onRender(EventRender.Default event) {
      if (mc.field_1724 != null && mc.field_1687 != null && !mc.field_1690.field_1842) {
         if (mc.field_1690.method_31044() != class_5498.field_26664) {
            this.fadeAllStates();
         } else {
            float partialTicks = event.getPartialTicks();
            float centerX = mc.method_22683().method_4486() * 0.5F;
            float centerY = mc.method_22683().method_4502() * 0.5F;
            float arrowSize = this.size.get();
            float y = centerY - this.radius.get();
            float playerYaw = this.getReferenceYaw(partialTicks);
            class_243 selfPos = this.getReferencePos(partialTicks);
            this.seenPlayers.clear();

            for (class_742 player : mc.field_1687.method_18456()) {
               if (player != mc.field_1724 && player.method_5805() && !player.method_7325() && !this.isGhostPlayer(player)) {
                  UUID uuid = player.method_5667();
                  Arrows.ArrowState state = this.states.computeIfAbsent(uuid, id -> new Arrows.ArrowState());
                  this.seenPlayers.add(uuid);
                  int color = this.getPlayerColor(player);
                  float targetYaw = this.getRelativeYaw(player, partialTicks, playerYaw, selfPos);
                  state.rotation = this.interpolateAngle(state.rotation, targetYaw, 0.18F);
                  state.alpha = this.approach(state.alpha, 1.0F, 0.12F);
                  float alpha = class_3532.method_15363(state.alpha, 0.0F, 1.0F);
                  if (!(alpha <= 0.01F)) {
                     int drawColor = ColorUtils.applyAlpha(color, alpha);
                     int shadowColor = ColorUtils.applyAlpha(ColorUtils.darken(color, 0.55F), alpha * 0.65F);
                     this.renderArrow(event.getContext().method_51448(), centerX, centerY, y, arrowSize, state.rotation, drawColor, shadowColor);
                  }
               }
            }

            this.states.entrySet().removeIf(entry -> {
               if (this.seenPlayers.contains(entry.getKey())) {
                  return false;
               } else {
                  Arrows.ArrowState statex = entry.getValue();
                  statex.alpha = this.approach(statex.alpha, 0.0F, 0.1F);
                  return statex.alpha <= 0.02F;
               }
            });
         }
      } else {
         this.states.clear();
      }
   }

   private void renderArrow(class_4587 matrices, float centerX, float centerY, float y, float size, float rotation, int color, int shadowColor) {
      class_2960 ARROW;
      if (this.type.getIndex() == 0) {
         ARROW = FIRST_ARROW_TEXTURE;
      } else if (this.type.getIndex() == 1) {
         ARROW = SECOND_ARROW_TEXTURE;
      } else {
         ARROW = MAMA_ARROW_TEXTURE;
      }

      matrices.method_22903();
      matrices.method_46416(centerX, centerY, 0.0F);
      matrices.method_22907(class_7833.field_40718.rotationDegrees(rotation));
      matrices.method_46416(-centerX, -centerY, 0.0F);
      float x = centerX - size * 0.5F;
      RenderUtils.drawImage(matrices, ARROW, x, y + size * 0.08F, size, size, shadowColor);
      RenderUtils.drawImage(matrices, ARROW, x, y, size, size, color);
      matrices.method_22909();
   }

   private void fadeAllStates() {
      this.states.entrySet().removeIf(entry -> {
         Arrows.ArrowState state = entry.getValue();
         state.alpha = this.approach(state.alpha, 0.0F, 0.1F);
         return state.alpha <= 0.02F;
      });
   }

   private float approach(float current, float target, float factor) {
      factor = class_3532.method_15363(factor, 0.0F, 1.0F);
      return class_3532.method_16439(factor, current, target);
   }

   private int getPlayerColor(class_742 player) {
      String name = player.method_5477().getString();
      boolean isFriend = SlikDlc.INSTANCE.friendStorage != null && SlikDlc.INSTANCE.friendStorage.isFriend(name);
      return isFriend ? ColorUtils.rgb(80, 170, 255) : ColorUtils.getThemeColor();
   }

   private float getRelativeYaw(class_1297 entity, float partialTicks, float playerYaw, class_243 selfPos) {
      class_243 entityPos = MathUtils.interpolate(entity, partialTicks);
      double dx = entityPos.field_1352 - selfPos.field_1352;
      double dz = entityPos.field_1350 - selfPos.field_1350;
      float yaw = (float)(-Math.toDegrees(Math.atan2(dx, dz)));
      return class_3532.method_15393(yaw - playerYaw);
   }

   private float getReferenceYaw(float partialTicks) {
      return FreeLookStorage.isActive()
         ? FreeLookStorage.getFreeYaw()
         : class_3532.method_16439(partialTicks, mc.field_1724.field_5982, mc.field_1724.method_36454());
   }

   private class_243 getReferencePos(float partialTicks) {
      return FreeLookStorage.isActive() && mc.field_1773 != null && mc.field_1773.method_19418() != null
         ? mc.field_1773.method_19418().method_19326()
         : MathUtils.interpolate(mc.field_1724, partialTicks);
   }

   private float interpolateAngle(float current, float target, float factor) {
      float delta = class_3532.method_15393(target - current);
      return current + delta * factor;
   }

   private boolean isGhostPlayer(class_742 player) {
      if (player.method_5797() != null) {
         String name = player.method_5797().getString();
         if (name != null && name.startsWith("Ghost_")) {
            return true;
         }
      }

      return "OtherClientPlayerEntity".equals(player.getClass().getSimpleName()) && player.method_36455() == -30.0F;
   }

   private static final class ArrowState {
      private float alpha;
      private float rotation;

      private ArrowState() {
      }
   }
}
