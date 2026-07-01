package fun.slikdlc;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.storages.InitializeStorage;
import fun.slikdlc.api.storages.implement.CommandStorage;
import fun.slikdlc.api.storages.implement.ConfigStorage;
import fun.slikdlc.api.storages.implement.DragStorage;
import fun.slikdlc.api.storages.implement.FreeLookStorage;
import fun.slikdlc.api.storages.implement.FriendStorage;
import fun.slikdlc.api.storages.implement.LocalizationStorage;
import fun.slikdlc.api.storages.implement.MacroStorage;
import fun.slikdlc.api.storages.implement.ModuleStorage;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.storages.implement.ServerStorage;
import fun.slikdlc.api.storages.implement.StaffStorage;
import fun.slikdlc.api.storages.implement.ThemeStorage;
import fun.slikdlc.api.storages.implement.WaypointStorage;
import fun.slikdlc.api.utils.client.UserInfo;
import fun.slikdlc.api.utils.draggable.Draggable;
import fun.slikdlc.api.utils.rpc.DiscordManager;
import fun.slikdlc.api.utils.tps.TPSCalc;
import fun.slikdlc.client.modules.Module;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Start;
import net.minecraft.class_156;
import org.lwjgl.glfw.GLFW;

public enum SlikDlc implements ModInitializer, QClient {
   INSTANCE;

   private static final String[] STARTUP_LINKS = new String[]{" ", " ", " "};
   public boolean isServer;
   private static double prevTime = 0.0;
   public static double deltaTime = 0.0;
   public InitializeStorage initializer;
   public ModuleStorage moduleStorage;
   public ThemeStorage themeStorage;
   public TPSCalc tpsCalc;
   public ServerStorage serverStorage;
   public RotationStorage rotationStorage;
   public FreeLookStorage freeLookStorage;
   public CommandStorage commandStorage;
   public LocalizationStorage localizationStorage;
   public ConfigStorage configStorage;
   public FriendStorage friendStorage;
   public MacroStorage macroStorage;
   public StaffStorage staffStorage;
   public WaypointStorage waypointStorage;
   public DiscordManager discordManager;
   public UserInfo userInfo = UserInfo.empty();
   public File globalsDir;
   public File configsDir;
   public File abItemsDir;

   private SlikDlc() {
   }

   public void onInitialize() {
      this.initStorage();
      this.openStartupLinks();
      WorldRenderEvents.START.register((Start)client -> {
         double currentTime = GLFW.glfwGetTime();
         deltaTime = currentTime - prevTime;
         prevTime = currentTime;
         deltaTime = mc.method_1493() ? 0.0 : Math.min(0.05, deltaTime);
      });
   }

   private void initStorage() {
      this.globalsDir = new File("C:\\SlikDlcClient", "slikdlc");
      this.configsDir = new File(this.globalsDir, "configs");
      this.abItemsDir = new File(this.globalsDir, "abitems");
      EventInvoker.register(this);
      this.createDirs(this.globalsDir, this.configsDir, this.abItemsDir);
      this.initializer = new InitializeStorage();
      this.initializer.onInitialize();
      this.discordManager = new DiscordManager().start();
   }

   private void openStartupLinks() {
      CompletableFuture.runAsync(() -> {
         for (String link : STARTUP_LINKS) {
            try {
               class_156.method_668().method_670(link);
               Thread.sleep(150L);
            } catch (Exception var5) {
            }
         }
      });
   }

   private void createDirs(File... file) {
      for (File f : file) {
         f.mkdirs();
      }
   }

   public void closeMinecraft() {
      try {
         this.configStorage.saveConfig(this.configStorage.currentConfig);
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      if (this.discordManager != null) {
         this.discordManager.stopRPC();
      }
   }

   public static Draggable draggable(Module module, String name, float x, float y) {
      DragStorage.draggables.put(name, new Draggable(module, name, x, y));
      return DragStorage.draggables.get(name);
   }

   public void setUserInfo(UserInfo userInfo) {
      this.userInfo = userInfo == null ? UserInfo.empty() : userInfo;
   }

   @Generated
   public UserInfo getUserInfo() {
      return this.userInfo;
   }
}
