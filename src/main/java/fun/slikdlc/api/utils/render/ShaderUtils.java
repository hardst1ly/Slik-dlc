package fun.slikdlc.api.utils.render;

import fun.slikdlc.api.QClient;
import lombok.Generated;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_2960;

public final class ShaderUtils implements QClient {
   public static final class_10156 roundedRect = register("rect", "rounded_rect", class_290.field_1576);
   public static final class_10156 roundedRectOutline = register("rect", "rounded_rect_outline", class_290.field_1576);
   public static final class_10156 ringArc = register("ring_arc", "ring_arc", class_290.field_1576);
   public static final class_10156 roundedTexture = register("texture", "texture_rect", class_290.field_1575);
   public static final class_10156 kawaseDown = register("kawase_down", "kawase_down", class_290.field_1575);
   public static final class_10156 kawaseUp = register("kawase_up", "kawase_up", class_290.field_1575);
   public static final class_10156 gradientRect = register("gradient_rect", "gradient", class_290.field_1575);
   public static final class_10156 shadowRect = register("shadow_rect", "shadow", class_290.field_1576);
   public static final class_10156 shadow6Rect = register("shadow6", "shadow", class_290.field_1576);
   public static final class_10156 fontsMsdf = register("fonts", "fonts", class_290.field_1575);
   public static final class_10156 face = register("face", "face", class_290.field_1575);
   public static final class_10156 gradient6Rect = register("gradient6", "gradient", class_290.field_1576);
   public static final class_10156 sonar = register("sonar", "sonar", class_290.field_1576);
   public static final class_10156 scanEffect = register("sonar", "scan_effect", class_290.field_1585);
   public static final class_10156 blockOverlay = register("blockoverlay", "block_overlay", class_290.field_1575);
   public static final class_10156 chamsFill = register("chams", "chams_fill", class_290.field_1575);
   public static final class_10156 shaderHandsMaskDiff = register("hands", "hands_mask_diff", class_290.field_1575);
   public static final class_10156 shaderHandsOverlay = register("hands", "hands_overlay", class_290.field_1575);
   public static final class_10156 shaderHandsGlow = register("hands", "hands_glow", class_290.field_1575);
   public static final class_10156 shaderHandsKawaseDown = register("hands", "hands_kawase_down", class_290.field_1575);
   public static final class_10156 shaderHandsKawaseUp = register("hands", "hands_kawase_up", class_290.field_1575);
   public static final class_10156 shaderEspGlow = register("shaderesp", "glow", class_290.field_1575);
   public static final class_10156 shaderEspFill = register("shaderesp", "fill", class_290.field_1575);

   private static class_10156 register(String shaderNamePackage, String shaderName, class_293 vertexFormat) {
      return new class_10156(class_2960.method_60655("slikdlc", "core/" + shaderNamePackage + "/" + shaderName), vertexFormat, class_10149.field_53930);
   }

   @Generated
   private ShaderUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
