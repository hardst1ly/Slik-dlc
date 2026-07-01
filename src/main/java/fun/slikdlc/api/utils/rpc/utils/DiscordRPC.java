package fun.slikdlc.api.utils.rpc.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public interface DiscordRPC extends Library {
   DiscordRPC INSTANCE = DiscordRPC.Holder.INSTANCE;

   void Discord_UpdateHandlers(DiscordEventHandlers var1);

   void Discord_UpdatePresence(DiscordRichPresence var1);

   void Discord_Respond(String var1, int var2);

   void Discord_Register(String var1, String var2);

   void Discord_Shutdown();

   void Discord_UpdateConnection();

   void Discord_RegisterSteamGame(String var1, String var2);

   void Discord_RunCallbacks();

   void Discord_Initialize(String var1, DiscordEventHandlers var2, boolean var3, String var4);

   void Discord_ClearPresence();

   public static enum DiscordReply {
      NO(0),
      IGNORE(2),
      YES(1);

      public final int reply;

      private DiscordReply(int reply) {
         this.reply = reply;
      }

      private static DiscordRPC.DiscordReply[] getReplies() {
         return new DiscordRPC.DiscordReply[]{NO, YES, IGNORE};
      }
   }

   final class Holder {
      static final DiscordRPC INSTANCE;

      static {
         loadNativeLibrary();
         INSTANCE = Native.loadLibrary("discord-rpc", DiscordRPC.class);
      }

      private static void loadNativeLibrary() {
         String osArch = System.getProperty("os.arch", "");
         String resourcePath = osArch.contains("aarch64") || osArch.contains("arm") ? "/win32-aarch64/discord-rpc.dll" : "/win32-x86-64/discord-rpc.dll";

         try (InputStream input = DiscordRPC.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
               return;
            }

            Path directory = Files.createTempDirectory("slikdlc-discord-rpc");
            directory.toFile().deleteOnExit();
            Path library = directory.resolve("discord-rpc.dll");
            Files.copy(input, library, StandardCopyOption.REPLACE_EXISTING);
            library.toFile().deleteOnExit();
            System.setProperty("jna.library.path", directory.toString());
            System.setProperty("jna.platform.library.path", directory.toString());
         } catch (IOException ignored) {
         }
      }
   }
}
