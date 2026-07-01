package fun.slikdlc.api.utils.player;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ViaProtocolUtils {
   private static final int MC_1_19_PROTOCOL = 759;
   private static final long CACHE_TIME_MS = 1500L;
   private static final Pattern VERSION_PATTERN = Pattern.compile("1\\.(\\d+)");
   private static long nextRefreshAt;
   private static boolean belowOneNineteen;

   private ViaProtocolUtils() {
   }

   public static boolean isTargetProtocolBelowOneNineteen() {
      long now = System.currentTimeMillis();
      if (now < nextRefreshAt) {
         return belowOneNineteen;
      } else {
         belowOneNineteen = resolveBelowOneNineteen();
         nextRefreshAt = now + 1500L;
         return belowOneNineteen;
      }
   }

   private static boolean resolveBelowOneNineteen() {
      try {
         Class<?> viaFabricPlusClass = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
         Object impl = viaFabricPlusClass.getMethod("getImpl").invoke(null);
         if (impl == null) {
            return false;
         } else {
            Object targetVersion = invokeNoArg(impl, "getTargetVersion");
            if (targetVersion == null) {
               return false;
            } else {
               Integer protocolId = readProtocolId(targetVersion);
               return protocolId != null && protocolId < 759;
            }
         }
      } catch (Throwable var4) {
         return false;
      }
   }

   private static Object invokeNoArg(Object instance, String methodName) {
      try {
         Method method = instance.getClass().getMethod(methodName);
         return Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0 ? method.invoke(instance) : null;
      } catch (Throwable var3) {
         return null;
      }
   }

   private static Integer readProtocolId(Object targetVersion) {
      try {
         Method getVersion = targetVersion.getClass().getMethod("getVersion");
         if (getVersion.invoke(targetVersion) instanceof Number number) {
            return number.intValue();
         }
      } catch (Throwable var9) {
      }

      try {
         for (Method method : targetVersion.getClass().getMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0) {
               Class<?> returnType = method.getReturnType();
               if (returnType == int.class || returnType == Integer.class) {
                  String name = method.getName().toLowerCase();
                  if ((name.contains("version") || name.contains("protocol") || name.contains("id")) && method.invoke(targetVersion) instanceof Number number) {
                     return number.intValue();
                  }
               }
            }
         }
      } catch (Throwable var10) {
      }

      Matcher matcher = VERSION_PATTERN.matcher(String.valueOf(targetVersion));
      if (matcher.find()) {
         int minor = Integer.parseInt(matcher.group(1));
         return minor >= 19 ? 759 : 758;
      } else {
         return null;
      }
   }
}
