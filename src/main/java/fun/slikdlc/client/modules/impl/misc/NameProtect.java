package fun.slikdlc.client.modules.impl.misc;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.utils.replace.ReplaceUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.TextSetting;
import fun.slikdlc.mixin.ChatScreenAccessor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2561;
import net.minecraft.class_342;
import net.minecraft.class_408;

public class NameProtect extends Module {
   public static final NameProtect INSTANCE = new NameProtect();
   private final BooleanSetting friends = new BooleanSetting("Скрывать друзей", true);
   private final BooleanSetting grief = new BooleanSetting("Скрывать гриф", false);
   private final TextSetting nickname = new TextSetting("Никнейм", "slikdlc", 32);
   private static final int PATCH_CACHE_LIMIT = 512;
   private final Map<String, String> patchCache = new LinkedHashMap<String, String>(512, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, String> eldest) {
         return this.size() > 512;
      }
   };

   private NameProtect() {
      super("NameProtect", "Скрывает никнеймы", Module.ModuleCategory.MISC);
      this.addSettings(new Setting[]{this.friends, this.grief, this.nickname});
   }

   public String patch(String text) {
      if (text == null) {
         return null;
      } else if (!this.shouldPatch()) {
         return text;
      } else {
         String cacheKey = this.getPatchCacheKey(text);
         String cached = this.patchCache.get(cacheKey);
         if (cached != null) {
            return cached;
         } else {
            String replacement = this.getReplacementName();
            String out = this.replaceIgnoreCase(text, mc.method_1548().method_1676(), replacement);
            if (this.friends.isState() && SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.friendStorage != null) {
               for (String friend : SlikDlc.INSTANCE.friendStorage.getFriends()) {
                  out = this.replaceIgnoreCase(out, friend, replacement);
               }
            }

            out = this.patchGrief(out);
            this.patchCache.put(cacheKey, out);
            return out;
         }
      }
   }

   public String patchIncomingText(String text) {
      return this.patch(text);
   }

   public class_2561 patchText(class_2561 text) {
      if (text == null) {
         return null;
      } else if (!this.shouldPatch()) {
         return text;
      } else {
         String replacement = this.getReplacementName();
         class_2561 output = ReplaceUtils.replace(text, mc.method_1548().method_1676(), replacement);
         if (this.friends.isState() && SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.friendStorage != null) {
            for (String friend : SlikDlc.INSTANCE.friendStorage.getFriends()) {
               output = ReplaceUtils.replace(output, friend, replacement);
            }
         }

         return output;
      }
   }

   public String getReplacementName() {
      String value = this.nickname.get();
      return value != null && !value.isBlank() ? value : "slikdlc";
   }

   public boolean shouldHideGrief() {
      return this.grief.isState();
   }

   private String replaceIgnoreCase(String text, String target, String replacement) {
      if (text != null && target != null && !target.isEmpty()) {
         int firstIndex = this.indexOfIgnoreCase(text, target, 0);
         if (firstIndex < 0) {
            return text;
         } else {
            StringBuilder out = new StringBuilder(text.length() + replacement.length());
            int from = 0;

            for (int index = firstIndex; index >= 0; index = this.indexOfIgnoreCase(text, target, from)) {
               out.append(text, from, index).append(replacement);
               from = index + target.length();
            }

            out.append(text, from, text.length());
            return out.toString();
         }
      } else {
         return text;
      }
   }

   private int indexOfIgnoreCase(String text, String target, int from) {
      int max = text.length() - target.length();

      for (int i = Math.max(0, from); i <= max; i++) {
         if (text.regionMatches(true, i, target, 0, target.length())) {
            return i;
         }
      }

      return -1;
   }

   private String patchGrief(String text) {
      if (text != null && this.grief.isState()) {
         String out = text.replaceAll("Анархия-\\d+", "slikdlcclient.fun");
         return out.replaceAll("ГРИФ #\\d+", "slikdlcclient.fun");
      } else {
         return text;
      }
   }

   private String getPatchCacheKey(String text) {
      String username = mc != null && mc.method_1548() != null ? mc.method_1548().method_1676() : "";
      int friendsHash = 0;
      if (this.friends.isState() && SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.friendStorage != null) {
         List<String> friendList = SlikDlc.INSTANCE.friendStorage.getFriends();
         friendsHash = friendList.hashCode();
      }

      return username + this.getReplacementName() + this.friends.isState() + this.grief.isState() + friendsHash + text;
   }

   private boolean shouldPatch() {
      return this.isEnable() && mc != null && mc.field_1724 != null && mc.field_1687 != null && !this.isFriendRemoveInputActive();
   }

   private boolean isFriendRemoveInputActive() {
      if (!(mc.field_1755 instanceof class_408 chatScreen)) {
         return false;
      } else {
         class_342 chatField = ((ChatScreenAccessor)chatScreen).slikdlc$getChatField();
         if (chatField == null) {
            return false;
         } else {
            String input = chatField.method_1882();
            if (input == null) {
               return false;
            } else {
               String normalized = input.trim().toLowerCase();
               String prefix = SlikDlc.INSTANCE != null && SlikDlc.INSTANCE.commandStorage != null
                  ? SlikDlc.INSTANCE.commandStorage.getPrefix().toLowerCase()
                  : ".";
               return normalized.startsWith(prefix + "friend remove");
            }
         }
      }
   }
}
