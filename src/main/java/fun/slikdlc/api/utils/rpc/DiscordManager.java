package fun.slikdlc.api.utils.rpc;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.utils.rpc.utils.DiscordEventHandlers;
import fun.slikdlc.api.utils.rpc.utils.DiscordRPC;
import fun.slikdlc.api.utils.rpc.utils.DiscordRichPresence;
import fun.slikdlc.client.modules.impl.render.base.implement.WaterMark;
import lombok.Generated;

public class DiscordManager implements QClient {
   private DiscordManager.DiscordDaemonThread discordDaemonThread;
   private long APPLICATION_ID;
   private boolean running;
   private String image;
   private String site;
   private String telegram;
   String state = "";
   public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
   public static DiscordRPC discordRPC = DiscordRPC.INSTANCE;

   public DiscordManager() {
   }

   private void cppInit() {
      this.discordDaemonThread = new DiscordManager.DiscordDaemonThread();
      this.APPLICATION_ID = 1480864732553547786L;
      this.running = true;
      this.image = "https://raw.githubusercontent.com/dezolator1/discordrpc/main/Comp-1_42_30fps.gif";
      this.site = "https://slikdlcclient.ru/";
      this.telegram = "https://t.me/slikdlcclient";
   }

   public void init() {
      this.cppInit();
      DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();
      DiscordRPC.INSTANCE.Discord_Initialize(String.valueOf(this.APPLICATION_ID), handlers, true, "");
      discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
      discordRPC.Discord_UpdatePresence(discordRichPresence);
      new Thread(() -> {
         while (this.running) {
            try {
               discordRichPresence.details = "Name » " + WaterMark.getUsername();
               discordRichPresence.state = "UID » " + WaterMark.getUID();
               discordRichPresence.largeImageKey = this.image;
               discordRichPresence.button_label_1 = "Купить";
               discordRichPresence.button_url_1 = this.site;
               discordRichPresence.button_label_2 = "Телеграмм";
               discordRichPresence.button_url_2 = this.telegram;
               DiscordRPC.INSTANCE.Discord_UpdatePresence(discordRichPresence);
               Thread.sleep(2000L);
            } catch (InterruptedException var2) {
               Thread.currentThread().interrupt();
               return;
            }
         }
      }, "Discord-RPC-Updater").start();
      this.discordDaemonThread.start();
   }

   public DiscordManager start() {
      this.init();
      return this;
   }

   public void stopRPC() {
      this.running = false;
      DiscordRPC.INSTANCE.Discord_Shutdown();
      if (this.discordDaemonThread != null) {
         this.discordDaemonThread.interrupt();
      }
   }

   @Generated
   public DiscordManager.DiscordDaemonThread getDiscordDaemonThread() {
      return this.discordDaemonThread;
   }

   @Generated
   public long getAPPLICATION_ID() {
      return this.APPLICATION_ID;
   }

   @Generated
   public boolean isRunning() {
      return this.running;
   }

   @Generated
   public String getImage() {
      return this.image;
   }

   @Generated
   public String getSite() {
      return this.site;
   }

   @Generated
   public String getTelegram() {
      return this.telegram;
   }

   @Generated
   public String getState() {
      return this.state;
   }

   private class DiscordDaemonThread extends Thread {
      private DiscordDaemonThread() {
      }

      @Override
      public void run() {
         this.setName("Discord-RPC");

         try {
            while (DiscordManager.this.running) {
               DiscordRPC.INSTANCE.Discord_RunCallbacks();
               Thread.sleep(15000L);
            }
         } catch (Exception var2) {
            DiscordManager.this.stopRPC();
         }

         super.run();
      }
   }
}
