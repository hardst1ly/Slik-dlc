package fun.slikdlc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import fun.slikdlc.SlikDlc;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_2172;
import net.minecraft.class_342;
import net.minecraft.class_4717;
import net.minecraft.class_4717.class_464;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_4717.class})
public abstract class ChatInputSuggestorMixin {
   @Final
   @Shadow
   class_342 field_21599;
   @Shadow
   boolean field_21614;
   @Shadow
   private ParseResults<class_2172> field_21610;
   @Shadow
   private CompletableFuture<Suggestions> field_21611;
   @Shadow
   private class_464 field_21612;

   public ChatInputSuggestorMixin() {
   }

   @Shadow
   public abstract void method_23920(boolean var1);

   @Inject(
      method = {"method_23934"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/brigadier/StringReader;canRead()Z",
         remap = false
      )},
      cancellable = true
   )
   public void refresh(CallbackInfo ci, @Local StringReader reader) {
      String prefix = SlikDlc.INSTANCE.commandStorage.getPrefix();
      if (reader.canRead(prefix.length()) && reader.getString().startsWith(prefix, reader.getCursor())) {
         reader.setCursor(reader.getCursor() + prefix.length());
         CommandDispatcher<class_2172> dispatcher = SlikDlc.INSTANCE.commandStorage.getDispatcher();
         if (this.field_21610 == null) {
            this.field_21610 = dispatcher.parse(reader, SlikDlc.INSTANCE.commandStorage.getSource());
         }

         int cursor;
         if ((cursor = this.field_21599.method_1881()) >= 1 && (this.field_21612 == null || !this.field_21614)) {
            this.field_21611 = dispatcher.getCompletionSuggestions(this.field_21610, cursor);
            this.field_21611.thenRun(() -> {
               if (this.field_21611.isDone()) {
                  this.method_23920(false);
               }
            });
         }

         ci.cancel();
      }
   }
}
