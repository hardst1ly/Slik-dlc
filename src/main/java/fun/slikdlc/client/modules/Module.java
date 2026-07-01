package fun.slikdlc.client.modules;

import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.utils.animation.AnimationUtils;
import fun.slikdlc.api.utils.animation.Easings;
import fun.slikdlc.api.utils.notification.NotificationManager;
import fun.slikdlc.client.modules.settings.Setting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Generated;

public abstract class Module implements QClient {
   private String name;
   private String description;
   private int key;
   private Module.ModuleCategory category;
   private boolean isOpen;
   private boolean enable;
   private final List<Setting> settings = new ArrayList<>();
   private final AnimationUtils animka = new AnimationUtils(60.0F, 11.0F, Easings.LINEAR);
   private final AnimationUtils arrayAnimka = new AnimationUtils(0.0F, 11.0F, Easings.LINEAR);

   public Module(String name, String description, Module.ModuleCategory category) {
      this.name = name;
      this.description = description;
      this.category = category;
      this.key = -1;
   }

   public Module(String name, Module.ModuleCategory category) {
      this.name = name;
      this.description = "NULLABLE";
      this.category = category;
      this.key = -1;
   }

   public void onEnable() {
      this.enable = true;
      EventInvoker.register(this);
      this.animka.update(1.0F);
      NotificationManager.push(this.name, this.category.getIcons(), true);
   }

   public void onDisable() {
      this.enable = false;
      EventInvoker.unregister(this);
      this.animka.update(0.0F);
      NotificationManager.push(this.name, this.category.getIcons(), false);
   }

   public void toggle() {
      this.enable = !this.enable;
      if (this.enable) {
         this.onEnable();
      } else {
         this.onDisable();
      }
   }

   public void setEnabled(boolean state) {
      boolean lastState = this.enable;
      this.enable = state;

      try {
         if (state) {
            this.onEnable();
         } else if (lastState) {
            this.onDisable();
         }
      } catch (Exception var4) {
         this.enable = false;
         this.onDisable();
      }
   }

   public void addSettings(Setting... settings) {
      if (settings != null && settings.length != 0) {
         Arrays.stream(settings).filter(Objects::nonNull).forEach(this.settings::add);
      }
   }

   public String getDisplayName() {
      return SlikDlc.INSTANCE.localizationStorage == null ? this.name : SlikDlc.INSTANCE.localizationStorage.translate(this.name);
   }

   public String getDisplayDescription() {
      return SlikDlc.INSTANCE.localizationStorage == null ? this.description : SlikDlc.INSTANCE.localizationStorage.translate(this.description);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public Module.ModuleCategory getCategory() {
      return this.category;
   }

   @Generated
   public boolean isOpen() {
      return this.isOpen;
   }

   @Generated
   public boolean isEnable() {
      return this.enable;
   }

   @Generated
   public List<Setting> getSettings() {
      return this.settings;
   }

   @Generated
   public AnimationUtils getAnimka() {
      return this.animka;
   }

   @Generated
   public AnimationUtils getArrayAnimka() {
      return this.arrayAnimka;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setDescription(String description) {
      this.description = description;
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }

   @Generated
   public void setCategory(Module.ModuleCategory category) {
      this.category = category;
   }

   @Generated
   public void setOpen(boolean isOpen) {
      this.isOpen = isOpen;
   }

   @Generated
   public void setEnable(boolean enable) {
      this.enable = enable;
   }

   public static enum ModuleCategory {
      COMBAT("Combat", "b"),
      MOVEMENT("Movement", "c"),
      RENDER("Render", "d"),
      MISC("Misc", "h"),
      PLAYER("Player", "e");

      private final String name;
      private final String icons;

      @Generated
      private ModuleCategory(final String name, final String icons) {
         this.name = name;
         this.icons = icons;
      }

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public String getIcons() {
         return this.icons;
      }
   }
}
