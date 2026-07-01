package fun.slikdlc.api.storages.implement;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.cmd.macro.Macro;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.namespaced.FileUtils;
import fun.slikdlc.client.modules.Module;
import fun.slikdlc.client.modules.impl.render.Interface;
import fun.slikdlc.client.modules.impl.render.base.InterfaceProcessing;
import fun.slikdlc.client.modules.impl.render.base.implement.TargetHud;
import fun.slikdlc.client.modules.impl.render.base.implement.WaterMark;
import fun.slikdlc.client.modules.settings.Setting;
import fun.slikdlc.client.modules.settings.implement.BindSetting;
import fun.slikdlc.client.modules.settings.implement.BooleanSetting;
import fun.slikdlc.client.modules.settings.implement.FloatSetting;
import fun.slikdlc.client.modules.settings.implement.ListSetting;
import fun.slikdlc.client.modules.settings.implement.ModeSetting;
import fun.slikdlc.client.modules.settings.implement.TextSetting;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConfigStorage {
   public String currentConfig = "default";
   private final String extension = ".wonder";

   public ConfigStorage() {
      this.loadAll();
      Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
   }

   private void loadAll() {
      try {
         this.loadGlobals();
         this.loadConfig(this.currentConfig);
      } catch (Exception var2) {
         var2.printStackTrace(System.err);
      }
   }

   private void saveAll() {
      try {
         this.saveGlobals();
         this.saveConfig(this.currentConfig);
      } catch (Exception var2) {
         var2.printStackTrace(System.err);
      }
   }

   public void saveConfig(String config) throws Exception {
      File file = new File(SlikDlc.INSTANCE.configsDir, config + ".wonder");
      JsonObject object = new JsonObject();
      object.add("config", new JsonPrimitive(config));
      object.add("theme", new JsonPrimitive(SlikDlc.INSTANCE.themeStorage.getThemes().name()));
      object.add("language", new JsonPrimitive(SlikDlc.INSTANCE.localizationStorage.getLanguage().name()));
      object.add("modules", this.serializeModules());
      object.add("draggables", this.serializeDraggables());
      object.add("hud", this.serializeHudState());

      try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
         writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
      }

      this.currentConfig = config;
   }

   public void loadConfig(String config) throws Exception {
      if (FileUtils.exists(SlikDlc.INSTANCE.configsDir + "/" + config + ".wonder")) {
         JsonObject object;
         try (
            InputStream stream = Files.newInputStream(Paths.get(SlikDlc.INSTANCE.configsDir + "/" + config + ".wonder"));
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         ) {
            object = JsonParser.parseReader(reader).getAsJsonObject();
         }

         if (object.has("theme")) {
            String themeName = object.get("theme").getAsString();

            for (ThemeStorage.Themes theme : ThemeStorage.Themes.values()) {
               if (theme.name().equals(themeName)) {
                  SlikDlc.INSTANCE.themeStorage.setThemes(theme);
                  break;
               }
            }
         }

         if (object.has("language")) {
            try {
               SlikDlc.INSTANCE.localizationStorage.setLanguage(LocalizationStorage.Language.valueOf(object.get("language").getAsString()));
            } catch (Exception var10) {
            }
         }

         if (object.has("draggables")) {
            this.deserializeDraggables(object.get("draggables").getAsJsonObject());
         }

         if (object.has("modules")) {
            this.deserializeModules(object.get("modules").getAsJsonObject());
         }

         if (object.has("hud")) {
            this.deserializeHudState(object.get("hud").getAsJsonObject());
         }

         this.currentConfig = config;
      }
   }

   public void saveGlobals() throws Exception {
      File file = new File(SlikDlc.INSTANCE.globalsDir, "globals.wonder");
      JsonObject object = new JsonObject();
      object.add("config", new JsonPrimitive(this.currentConfig));
      object.add("theme", new JsonPrimitive(SlikDlc.INSTANCE.themeStorage.getThemes().name()));
      object.add("language", new JsonPrimitive(SlikDlc.INSTANCE.localizationStorage.getLanguage().name()));
      object.add("draggables", this.serializeDraggables());
      object.add("hud", this.serializeHudState());
      JsonArray friendsArray = new JsonArray();
      SlikDlc.INSTANCE.friendStorage.getFriends().forEach(friendsArray::add);
      object.add("friends", friendsArray);
      JsonArray staffsArray = new JsonArray();
      SlikDlc.INSTANCE.staffStorage.getStaffs().forEach(staffsArray::add);
      object.add("staffs", staffsArray);
      JsonArray macrosArray = new JsonArray();
      SlikDlc.INSTANCE.macroStorage.getMacros().forEach(macro -> {
         JsonObject macroObject = new JsonObject();
         macroObject.addProperty("name", macro.getName());
         macroObject.addProperty("command", macro.getCommand());
         macroObject.addProperty("key", macro.getBind().getKey());
         macrosArray.add(macroObject);
      });
      object.add("macros", macrosArray);

      try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
         writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
      }
   }

   public void loadGlobals() throws Exception {
      if (FileUtils.exists(SlikDlc.INSTANCE.globalsDir + "/globals.wonder")) {
         JsonObject object;
         try (
            InputStream stream = Files.newInputStream(Paths.get(SlikDlc.INSTANCE.globalsDir + "/globals.wonder"));
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         ) {
            object = JsonParser.parseReader(reader).getAsJsonObject();
         }

         if (object.has("config")) {
            this.currentConfig = object.get("config").getAsString();
         }

         if (object.has("theme")) {
            String themeName = object.get("theme").getAsString();

            for (ThemeStorage.Themes theme : ThemeStorage.Themes.values()) {
               if (theme.name().equals(themeName)) {
                  SlikDlc.INSTANCE.themeStorage.setThemes(theme);
                  break;
               }
            }
         }

         if (object.has("language")) {
            try {
               SlikDlc.INSTANCE.localizationStorage.setLanguage(LocalizationStorage.Language.valueOf(object.get("language").getAsString()));
            } catch (Exception var10) {
            }
         }

         if (object.has("draggables")) {
            this.deserializeDraggables(object.get("draggables").getAsJsonObject());
         }

         if (object.has("hud")) {
            this.deserializeHudState(object.get("hud").getAsJsonObject());
         }

         if (object.has("friends")) {
            for (JsonElement element : object.get("friends").getAsJsonArray()) {
               if (!SlikDlc.INSTANCE.friendStorage.isFriend(element.getAsString())) {
                  SlikDlc.INSTANCE.friendStorage.add(element.getAsString());
               }
            }
         }

         if (object.has("staffs")) {
            for (JsonElement elementx : object.get("staffs").getAsJsonArray()) {
               if (!SlikDlc.INSTANCE.staffStorage.isStaff(elementx.getAsString())) {
                  SlikDlc.INSTANCE.staffStorage.add(elementx.getAsString());
               }
            }
         }

         if (object.has("macros")) {
            for (JsonElement elementxx : object.get("macros").getAsJsonArray()) {
               try {
                  String name;
                  String command;
                  int key;
                  if (elementxx.isJsonObject()) {
                     JsonObject macroObject = elementxx.getAsJsonObject();
                     name = macroObject.has("name") ? macroObject.get("name").getAsString() : "";
                     command = macroObject.has("command") ? macroObject.get("command").getAsString() : "";
                     key = macroObject.has("key") ? macroObject.get("key").getAsInt() : -1;
                  } else {
                     String[] split = elementxx.getAsString().split(":", 3);
                     if (split.length < 3) {
                        continue;
                     }

                     name = split[0];
                     command = split[1];
                     key = Integer.parseInt(split[2]);
                  }

                  if (!name.isBlank() && SlikDlc.INSTANCE.macroStorage.getMacro(name) == null) {
                     SlikDlc.INSTANCE.macroStorage.add(new Macro(name, command, new BindSetting("bind", key)));
                  }
               } catch (Exception var13) {
               }
            }
         }
      }
   }

   private JsonObject serializeModules() {
      JsonObject modules = new JsonObject();
      ObjectListIterator var2 = ModuleClass.INSTANCE.getObject().iterator();

      while (var2.hasNext()) {
         Module module = (Module)var2.next();

         try {
            JsonObject object = new JsonObject();
            object.add("toggled", new JsonPrimitive(module.isEnable()));
            object.add("bind", new JsonPrimitive(module.getKey()));
            JsonObject settings = new JsonObject();

            for (Setting s : module.getSettings()) {
               try {
                  if (s instanceof BooleanSetting bool) {
                     settings.add(s.name(), new JsonPrimitive(bool.isState()));
                  } else if (s instanceof FloatSetting num) {
                     settings.add(s.name(), new JsonPrimitive(num.getValue().floatValue()));
                  } else if (s instanceof ModeSetting mode) {
                     settings.add(s.name(), new JsonPrimitive(mode.getCurrent()));
                  } else if (s instanceof TextSetting text) {
                     settings.add(s.name(), new JsonPrimitive(text.get()));
                  } else if (s instanceof BindSetting bind) {
                     settings.add(s.name(), new JsonPrimitive(bind.getKey()));
                  } else if (s instanceof ListSetting list) {
                     JsonObject listObj = new JsonObject();

                     for (BooleanSetting setting : list.getSettings()) {
                        listObj.add(setting.name(), new JsonPrimitive(setting.isState()));
                     }

                     settings.add(list.name(), listObj);
                  }
               } catch (Exception var17) {
               }
            }

            object.add("settings", settings);
            modules.add(module.getName(), object);
         } catch (Exception var18) {
         }
      }

      return modules;
   }

   private void deserializeModules(JsonObject modules) {
      Map<Module, Boolean> targetStates = new LinkedHashMap<>();
      ObjectListIterator var3 = ModuleClass.INSTANCE.getObject().iterator();

      while (var3.hasNext()) {
         Module module = (Module)var3.next();

         try {
            JsonObject object = modules.has(module.getName()) ? modules.get(module.getName()).getAsJsonObject() : null;
            boolean toggled = object != null && object.has("toggled") && object.get("toggled").getAsBoolean();
            targetStates.put(module, toggled);
            if (module.isEnable()) {
               module.setEnabled(false);
            }
         } catch (Exception var22) {
            targetStates.put(module, false);
         }
      }

      var3 = ModuleClass.INSTANCE.getObject().iterator();

      while (var3.hasNext()) {
         Module module = (Module)var3.next();

         try {
            if (modules.has(module.getName())) {
               JsonObject object = modules.get(module.getName()).getAsJsonObject();
               if (object.has("bind")) {
                  module.setKey(object.get("bind").getAsInt());
               }

               if (object.has("settings")) {
                  JsonObject settings = object.get("settings").getAsJsonObject();

                  for (Setting s : module.getSettings()) {
                     try {
                        if (settings.has(s.name())) {
                           JsonElement element = settings.get(s.name());
                           if (s instanceof BooleanSetting bool) {
                              bool.setState(element.getAsBoolean());
                           } else if (s instanceof FloatSetting num) {
                              num.setValue(element.getAsFloat());
                           } else if (s instanceof ModeSetting mode) {
                              mode.set(element.getAsString());
                           } else if (s instanceof TextSetting text) {
                              text.setText(element.getAsString());
                           } else if (s instanceof BindSetting bind) {
                              bind.setKey(element.getAsInt());
                           } else if (s instanceof ListSetting list) {
                              JsonObject listObj = element.getAsJsonObject();

                              for (BooleanSetting setting : list.getSettings()) {
                                 if (listObj.has(setting.name())) {
                                    setting.setState(listObj.get(setting.name()).getAsBoolean());
                                 }
                              }
                           }
                        }
                     } catch (Exception var20) {
                     }
                  }
               }
            }
         } catch (Exception var21) {
         }
      }

      for (Entry<Module, Boolean> entry : targetStates.entrySet()) {
         try {
            entry.getKey().setEnabled(entry.getValue());
         } catch (Exception var19) {
         }
      }
   }

   private JsonObject serializeHudState() {
      JsonObject hud = new JsonObject();
      Interface interfaceModule = ModuleClass.interfaceModule;
      if (interfaceModule == null) {
         return hud;
      } else {
         for (Entry<String, InterfaceProcessing> entry : interfaceModule.getConfigurableHudElements().entrySet()) {
            InterfaceProcessing element = entry.getValue();
            if (element != null) {
               JsonObject object = new JsonObject();
               object.add("unusualRectType", new JsonPrimitive(element.isUnusualRectType()));
               if (element instanceof WaterMark waterMark) {
                  object.add("showFps", new JsonPrimitive(waterMark.isShowFps()));
                  object.add("showMs", new JsonPrimitive(waterMark.isShowMs()));
                  object.add("showServer", new JsonPrimitive(waterMark.isShowServer()));
                  object.add("showTps", new JsonPrimitive(waterMark.isShowTps()));
               } else if (element instanceof TargetHud targetHud) {
                  object.add("headParticlesEnabled", new JsonPrimitive(targetHud.isHeadParticlesEnabled()));
               }

               hud.add(entry.getKey(), object);
            }
         }

         return hud;
      }
   }

   private void deserializeHudState(JsonObject hud) {
      Interface interfaceModule = ModuleClass.interfaceModule;
      if (interfaceModule != null) {
         for (Entry<String, InterfaceProcessing> entry : interfaceModule.getConfigurableHudElements().entrySet()) {
            if (hud.has(entry.getKey())) {
               try {
                  JsonObject object = hud.get(entry.getKey()).getAsJsonObject();
                  InterfaceProcessing element = entry.getValue();
                  if (object.has("unusualRectType")) {
                     element.setUnusualRectType(object.get("unusualRectType").getAsBoolean());
                  }

                  if (element instanceof WaterMark waterMark) {
                     if (object.has("showFps")) {
                        waterMark.setShowFps(object.get("showFps").getAsBoolean());
                     }

                     if (object.has("showMs")) {
                        waterMark.setShowMs(object.get("showMs").getAsBoolean());
                     }

                     if (object.has("showServer")) {
                        waterMark.setShowServer(object.get("showServer").getAsBoolean());
                     }

                     if (object.has("showTps")) {
                        waterMark.setShowTps(object.get("showTps").getAsBoolean());
                     }
                  } else if (element instanceof TargetHud targetHud && object.has("headParticlesEnabled")) {
                     targetHud.setHeadParticlesEnabled(object.get("headParticlesEnabled").getAsBoolean());
                  }
               } catch (Exception var9) {
               }
            }
         }
      }
   }

   private JsonObject serializeDraggables() {
      JsonObject draggables = new JsonObject();

      for (Draggable drag : DragStorage.draggables.values()) {
         JsonObject object = new JsonObject();
         object.add("x", new JsonPrimitive(drag.getX()));
         object.add("y", new JsonPrimitive(drag.getY()));
         draggables.add(drag.getName(), object);
      }

      return draggables;
   }

   private void deserializeDraggables(JsonObject draggables) {
      for (String name : draggables.keySet()) {
         Draggable drag = DragStorage.draggables.get(name);
         if (drag != null) {
            JsonObject object = draggables.get(name).getAsJsonObject();
            if (object.has("x")) {
               drag.setX(object.get("x").getAsFloat());
            }

            if (object.has("y")) {
               drag.setY(object.get("y").getAsFloat());
            }
         }
      }
   }
}
