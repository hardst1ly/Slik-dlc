package fun.slikdlc.mixin;

import java.util.Locale;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_2960.class})
public abstract class IdentifierMixin {
   private static final String SAFE_JOIN_PATH = "invalid_join_id";

   public IdentifierMixin() {
   }

   @Inject(
      method = {"method_45137"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void slikdlc$sanitizeJoinPath(String namespace, String path, CallbackInfoReturnable<String> cir) {
      if (shouldSanitizePath(path)) {
         cir.setReturnValue(sanitizePath(path));
      }
   }

   @Inject(
      method = {"method_45135"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void slikdlc$sanitizeJoinNamespace(String namespace, String path, CallbackInfoReturnable<String> cir) {
      if (shouldSanitizeNamespace(namespace)) {
         cir.setReturnValue(sanitizeNamespace(namespace));
      }
   }

   private static boolean shouldSanitizeNamespace(String value) {
      if (value != null && !value.isEmpty()) {
         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!isAllowedNamespaceChar(c)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static boolean shouldSanitizePath(String value) {
      if (value != null && !value.isEmpty()) {
         for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!isAllowedPathChar(c)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static boolean isAllowedNamespaceChar(char c) {
      return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '-';
   }

   private static boolean isAllowedPathChar(char c) {
      return isAllowedNamespaceChar(c) || c == '/';
   }

   private static String sanitizeNamespace(String namespace) {
      StringBuilder builder = new StringBuilder(namespace.length());
      String lower = namespace.toLowerCase(Locale.ROOT);

      for (int i = 0; i < lower.length(); i++) {
         char c = lower.charAt(i);
         if (isAllowedNamespaceChar(c)) {
            builder.append(c);
         } else {
            builder.append('_');
         }
      }

      String sanitized = builder.toString();
      return sanitized.isBlank() ? "minecraft" : sanitized;
   }

   private static String sanitizePath(String path) {
      StringBuilder builder = new StringBuilder(path.length());
      String lower = path.toLowerCase(Locale.ROOT);

      for (int i = 0; i < lower.length(); i++) {
         char c = lower.charAt(i);
         if (isAllowedPathChar(c)) {
            builder.append(c);
         } else {
            builder.append('_');
         }
      }

      String sanitized = builder.toString();
      return sanitized.isBlank() ? "invalid_join_id" : sanitized;
   }
}
