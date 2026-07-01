package fun.slikdlc.api.utils.namespaced;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Generated;

public final class FileUtils {
   public static void reset(String str) throws IOException {
      Path path = Paths.get(str);
      if (Files.exists(path)) {
         new File(str).delete();
      }

      Files.createFile(path);
   }

   public static boolean exists(String str) {
      return Files.exists(Paths.get(str));
   }

   @Generated
   private FileUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
