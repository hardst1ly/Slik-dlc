package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.api.events.EventLink;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.utils.color.ColorUtils;
import fun.slikdlc.client.modules.Module;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1686;
import net.minecraft.class_1844;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_9334;

public class PotionTracker extends Module {
   public static PotionTracker INSTANCE = new PotionTracker();
   private static final double TRACK_RADIUS = 50.0;
   private static final double SPLASH_RADIUS = 4.0;
   private static final double SPLASH_HEIGHT = 2.0;
   private static final int MAX_MESSAGES = 4;
   private static final int GRAY = new Color(200, 200, 200).getRGB();
   private static final int PLAYER = new Color(235, 235, 235).getRGB();
   private final Map<Integer, PotionTracker.PotionData> trackedPotions = new HashMap<>();
   private class_638 lastWorld;

   public PotionTracker() {
      super("PotionTracker", "Показывает попадание выкинутых зелий по игрокам", Module.ModuleCategory.MISC);
   }

   @Override
   public void onDisable() {
      this.trackedPotions.clear();
      this.lastWorld = null;
      super.onDisable();
   }

   @EventLink
   public void onUpdate(EventUpdate event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.lastWorld != mc.field_1687) {
            this.trackedPotions.clear();
            this.lastWorld = mc.field_1687;
         }

         Set<Integer> currentPotions = new HashSet<>();
         double trackRadiusSq = 2500.0;
         class_238 searchBox = mc.field_1724.method_5829().method_1014(50.0);

         for (class_1686 potionEntity : mc.field_1687.method_8390(class_1686.class, searchBox, class_1297::method_5805)) {
            if (!(mc.field_1724.method_5858(potionEntity) > trackRadiusSq)) {
               PotionTracker.PotionInfo potionInfo = this.getPotionInfo(potionEntity);
               if (potionInfo != null) {
                  int entityId = potionEntity.method_5628();
                  currentPotions.add(entityId);
                  PotionTracker.PotionData data = this.trackedPotions.get(entityId);
                  if (data == null) {
                     this.trackedPotions
                        .put(
                           entityId,
                           new PotionTracker.PotionData(potionInfo, potionEntity.method_23317(), potionEntity.method_23318(), potionEntity.method_23321())
                        );
                  } else {
                     data.lastX = potionEntity.method_23317();
                     data.lastY = potionEntity.method_23318();
                     data.lastZ = potionEntity.method_23321();
                     data.potionInfo = potionInfo;
                  }
               }
            }
         }

         Set<Integer> removedPotions = new HashSet<>(this.trackedPotions.keySet());
         removedPotions.removeAll(currentPotions);

         for (int entityId : removedPotions) {
            PotionTracker.PotionData data = this.trackedPotions.remove(entityId);
            if (data != null) {
               this.printSplash(data);
            }
         }
      } else {
         this.trackedPotions.clear();
         this.lastWorld = null;
      }
   }

   private void printSplash(PotionTracker.PotionData data) {
      class_238 potionBox = new class_238(data.lastX - 4.0, data.lastY - 2.0, data.lastZ - 4.0, data.lastX + 4.0, data.lastY + 2.0, data.lastZ + 4.0);
      List<PotionTracker.PlayerHit> hits = new ArrayList<>();

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != null && player.method_5805() && potionBox.method_1006(player.method_19538())) {
            double dx = player.method_23317() - data.lastX;
            double dz = player.method_23321() - data.lastZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (!(distance > 4.0)) {
               double proximity = Math.max(0.0, 1.0 - distance / 4.0);
               int percent = Math.max(1, Math.min(100, (int)Math.round(proximity * 100.0)));
               hits.add(new PotionTracker.PlayerHit(player.method_5477().getString(), percent, distance));
            }
         }
      }

      hits.sort(Comparator.comparingDouble(PotionTracker.PlayerHit::distance));

      for (int i = 0; i < Math.min(4, hits.size()); i++) {
         PotionTracker.PlayerHit hit = hits.get(i);
         this.sendPotionMessage(hit.playerName(), data.potionInfo, hit.percent());
      }
   }

   private PotionTracker.PotionInfo getPotionInfo(class_1686 potionEntity) {
      class_1844 contents = (class_1844)potionEntity.method_7495().method_57824(class_9334.field_49651);
      PotionTracker.PotionInfo byEffects = this.getPotionInfo(contents);
      return byEffects != null ? byEffects : this.getPotionInfo(potionEntity.method_7495().method_7964().getString());
   }

   private PotionTracker.PotionInfo getPotionInfo(class_1844 contents) {
      if (contents != null && contents.method_57405()) {
         boolean regenerationTwo = this.hasEffect(contents, class_1294.field_5924, 1);
         boolean strengthFive = this.hasEffect(contents, class_1294.field_5910, 4);
         boolean healthBoostThree = this.hasEffect(contents, class_1294.field_5914, 2);
         boolean strengthFour = this.hasEffect(contents, class_1294.field_5910, 3);
         boolean speedThree = this.hasEffect(contents, class_1294.field_5904, 2);
         if (regenerationTwo) {
            return PotionTracker.PotionInfo.HOLY_WATER;
         } else if (strengthFive) {
            return PotionTracker.PotionInfo.WRATH;
         } else if (healthBoostThree) {
            return PotionTracker.PotionInfo.PALADIN;
         } else if (strengthFour && speedThree) {
            return PotionTracker.PotionInfo.ASSASSIN;
         } else {
            return strengthFour ? PotionTracker.PotionInfo.ASSASSIN : null;
         }
      } else {
         return null;
      }
   }

   private boolean hasEffect(class_1844 contents, class_6880<class_1291> effect, int amplifier) {
      for (class_1293 instance : contents.method_57397()) {
         if (instance.method_5579().equals(effect) && instance.method_5578() == amplifier) {
            return true;
         }
      }

      return false;
   }

   private PotionTracker.PotionInfo getPotionInfo(String itemName) {
      String normalizedName = this.normalize(itemName);

      for (PotionTracker.PotionInfo potionInfo : PotionTracker.PotionInfo.values()) {
         if (normalizedName.contains(this.normalize(potionInfo.plainName()))) {
            return potionInfo;
         }
      }

      return null;
   }

   private String normalize(String text) {
      return text.replaceAll("§.", "").replace("[", "").replace("]", "").replace("✦", "").toLowerCase(Locale.ROOT).trim();
   }

   private void sendPotionMessage(String playerName, PotionTracker.PotionInfo potionInfo, int percent) {
      if (mc.field_1724 != null) {
         class_5250 text = class_2561.method_43470("");
         text.method_10852(this.gradientText("SlikDlc", ColorUtils.getThemeColor(0), ColorUtils.getThemeColor(90), true));
         text.method_10852(class_2561.method_43470(" ⇒ ").method_10862(this.grayStyle()));
         text.method_10852(class_2561.method_43470(playerName).method_10862(class_2583.field_24360.method_27703(class_5251.method_27717(PLAYER))));
         text.method_10852(class_2561.method_43470(" получил ").method_10862(this.grayStyle()));
         text.method_10852(this.gradientText(potionInfo.displayName, potionInfo.startColor, potionInfo.endColor, true));
         text.method_10852(class_2561.method_43470(" " + percent + "%").method_10862(this.grayStyle()));
         mc.field_1724.method_7353(text, false);
      }
   }

   private class_5250 gradientText(String text, int startColor, int endColor, boolean bold) {
      class_5250 result = class_2561.method_43470("");

      for (int i = 0; i < text.length(); i++) {
         float progress = text.length() <= 1 ? 0.0F : (float)i / (text.length() - 1);
         int color = ColorUtils.gradient(startColor, endColor, progress);
         result.method_10852(
            class_2561.method_43470(String.valueOf(text.charAt(i)))
               .method_10862(class_2583.field_24360.method_10982(bold).method_27703(class_5251.method_27717(color)))
         );
      }

      return result;
   }

   private class_2583 grayStyle() {
      return class_2583.field_24360.method_27703(class_5251.method_27717(GRAY));
   }

   private record PlayerHit(String playerName, int percent, double distance) {
   }

   private static class PotionData {
      private PotionTracker.PotionInfo potionInfo;
      private double lastX;
      private double lastY;
      private double lastZ;

      private PotionData(PotionTracker.PotionInfo potionInfo, double lastX, double lastY, double lastZ) {
         this.potionInfo = potionInfo;
         this.lastX = lastX;
         this.lastY = lastY;
         this.lastZ = lastZ;
      }
   }

   private static enum PotionInfo {
      HOLY_WATER("[✦] Святая вода", 16774507, 12123970),
      WRATH("[✦] Зелье Гнева", 12849682, 16757051),
      PALADIN("[✦] Зелье Паладина", 12123970, 16773280),
      ASSASSIN("[✦] Зелье Ассасина", 5592405, 11545130);

      private final String displayName;
      private final int startColor;
      private final int endColor;

      private PotionInfo(String displayName, int startColor, int endColor) {
         this.displayName = displayName;
         this.startColor = startColor;
         this.endColor = endColor;
      }

      private String plainName() {
         int index = this.displayName.indexOf("] ");
         return index >= 0 ? this.displayName.substring(index + 2) : this.displayName;
      }
   }
}
