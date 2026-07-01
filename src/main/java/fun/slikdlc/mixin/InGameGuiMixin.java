package fun.slikdlc.mixin;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventRender;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.SidebarEntry;
import fun.slikdlc.api.utils.render.blur.BlurProgram;
import fun.slikdlc.client.modules.impl.misc.NameProtect;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_5251;
import net.minecraft.class_9011;
import net.minecraft.class_9022;
import net.minecraft.class_9025;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public class InGameGuiMixin implements QClient {
   private static final int DOMAIN_COLOR = 15557921;
   @Shadow
   @Final
   private class_310 field_2035;

   public InGameGuiMixin() {
   }

   @Inject(
      method = {"method_1735"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderVignetteOverlay(class_332 context, class_1297 entity, CallbackInfo ci) {
      if (ModuleClass.noVignette.isEnable()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1753"},
      at = {@At("HEAD")}
   )
   private void render(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      BlurProgram.getInstance().beginFrame();
      if (EventInvoker.hasListeners(EventRender.Default.class)) {
         new EventRender.Default(context, tickCounter.method_60637(true)).call();
      }
   }

   @Shadow
   private class_1657 method_1737() {
      return null;
   }

   @Inject(
      method = {"method_1757"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void slikdlc$renderPatchedScoreboard(class_332 drawContext, class_266 objective, CallbackInfo ci) {
      if (this.slikdlc$shouldPatchScoreboard()) {
         class_269 scoreboard = objective.method_1117();
         class_9022 numberFormat = objective.method_55380(class_9025.field_47567);
         List<SidebarEntry> lines = scoreboard.method_1184(objective)
            .stream()
            .filter(entry -> !entry.method_55385())
            .sorted(Comparator.comparing(class_9011::comp_2128).reversed().thenComparing(class_9011::comp_2127, String.CASE_INSENSITIVE_ORDER))
            .limit(15L)
            .map(entry -> {
               class_268 team = scoreboard.method_1164(entry.comp_2127());
               class_2561 name = this.slikdlc$patchText(class_268.method_1142(team, entry.method_55387()));
               class_2561 score = entry.method_55386(numberFormat);
               int scoreWidth = this.field_2035.field_1772.method_27525(score);
               return new SidebarEntry(name, score, scoreWidth);
            })
            .toList();
         class_2561 title = this.slikdlc$patchText(objective.method_1114());
         int titleWidth = this.field_2035.field_1772.method_27525(title);
         int maxWidth = titleWidth;
         int separatorWidth = this.field_2035.field_1772.method_1727(": ");

         for (SidebarEntry line : lines) {
            maxWidth = Math.max(maxWidth, this.field_2035.field_1772.method_27525(line.name) + (line.scoreWidth > 0 ? separatorWidth + line.scoreWidth : 0));
         }

         int lineCount = lines.size();
         int totalHeight = lineCount * 9;
         int bottom = drawContext.method_51443() / 2 + totalHeight / 3;
         int left = drawContext.method_51421() - maxWidth - 3;
         int right = drawContext.method_51421() - 1;
         int bodyColor = this.field_2035.field_1690.method_19345(0.3F);
         int headerColor = this.field_2035.field_1690.method_19345(0.4F);
         int top = bottom - lineCount * 9;
         drawContext.method_25294(left - 2, top - 10, right, top - 1, headerColor);
         drawContext.method_25294(left - 2, top - 1, right, bottom, bodyColor);
         drawContext.method_51439(this.field_2035.field_1772, title, left + maxWidth / 2 - titleWidth / 2, top - 9, -1, false);

         for (int index = 0; index < lineCount; index++) {
            SidebarEntry line = lines.get(index);
            int y = bottom - (lineCount - index) * 9;
            drawContext.method_51439(this.field_2035.field_1772, line.name, left, y, -1, false);
            drawContext.method_51439(this.field_2035.field_1772, line.score, right - line.scoreWidth, y, -1, false);
         }

         ci.cancel();
      }
   }

   private boolean slikdlc$shouldPatchScoreboard() {
      return ModuleClass.INSTANCE != null && ModuleClass.nameProtect != null && ModuleClass.nameProtect.isEnable();
   }

   private class_2561 slikdlc$patchText(class_2561 text) {
      NameProtect nameProtect = ModuleClass.nameProtect;
      class_2561 patched = nameProtect.patchText(text);
      String patchedString = patched.getString();
      if (nameProtect.shouldHideGrief()) {
         if (patchedString.contains("Анархия-")) {
            patchedString = patchedString.replaceAll("Анархия-\\d+", "slikdlcclient.fun");
         }

         if (patchedString.contains("ГРИФ #")) {
            patchedString = patchedString.replaceAll("ГРИФ #\\d+", "slikdlcclient.fun");
         }
      }

      return (class_2561)(patchedString.equals(patched.getString())
         ? patched
         : class_2561.method_43470(patchedString).method_10862(patched.method_10866().method_27703(class_5251.method_27717(15557921))));
   }
}
