package fun.slikdlc.api.utils.notification;

import fun.slikdlc.api.utils.client.ClientSoundPlayer;
import fun.slikdlc.client.modules.impl.misc.ClientSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationManager {
   public static final long DURATION_MS = 2500L;
   private static final long MODULE_SOUND_STARTUP_MUTE_MS = 4000L;
   private static final long INIT_TIME_MS = System.currentTimeMillis();
   private static final List<NotificationManager.Entry> entries = new ArrayList<>();

   public NotificationManager() {
   }

   public static void push(String moduleName, String categoryIcon, boolean enabled) {
      if (moduleName != null && !moduleName.isEmpty()) {
         entries.add(new NotificationManager.Entry(moduleName, categoryIcon, enabled, null, System.currentTimeMillis()));
         playModuleSound(enabled);
      }
   }

   public static void pushCustom(String text, String categoryIcon) {
      if (text != null && !text.isEmpty()) {
         entries.add(new NotificationManager.Entry(text, categoryIcon, false, text, System.currentTimeMillis()));
      }
   }

   public static List<NotificationManager.Entry> getActive() {
      long now = System.currentTimeMillis();
      Iterator<NotificationManager.Entry> it = entries.iterator();

      while (it.hasNext()) {
         NotificationManager.Entry e = it.next();
         if (now - e.startTime > 2500L) {
            it.remove();
         }
      }

      return entries;
   }

   private static void playModuleSound(boolean enabled) {
      if (System.currentTimeMillis() - INIT_TIME_MS >= 4000L) {
         ClientSounds clientSounds = ClientSounds.INSTANCE;
         if (clientSounds != null && clientSounds.isEnable()) {
            String soundName = clientSounds.stateSounds.getCurrent();
            if (!"Нет".equals(soundName)) {
               float pitch = enabled ? 1.0F : 0.95F;
               ClientSoundPlayer.playSound(soundName + ".wav", clientSounds.volume.get() / clientSounds.volume.getMax(), pitch);
            }
         }
      }
   }

   public static class Entry {
      public final String moduleName;
      public final String categoryIcon;
      public final boolean enabled;
      public final String customText;
      public final long startTime;

      public Entry(String moduleName, String categoryIcon, boolean enabled, String customText, long startTime) {
         this.moduleName = moduleName;
         this.categoryIcon = categoryIcon;
         this.enabled = enabled;
         this.customText = customText;
         this.startTime = startTime;
      }

      public boolean isCustom() {
         return this.customText != null && !this.customText.isEmpty();
      }
   }
}
