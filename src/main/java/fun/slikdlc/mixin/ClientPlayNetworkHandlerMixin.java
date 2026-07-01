package fun.slikdlc.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fun.slikdlc.SlikDlc;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.utils.baritone.BaritoneAntiStuck;
import fun.slikdlc.api.utils.bot.BotSessionManager;
import fun.slikdlc.api.utils.chat.ChatUtils;
import net.minecraft.class_10264;
import net.minecraft.class_124;
import net.minecraft.class_2664;
import net.minecraft.class_2678;
import net.minecraft.class_2743;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_7439;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_634.class})
public abstract class ClientPlayNetworkHandlerMixin {
   @Shadow
   private class_638 field_3699;

   public ClientPlayNetworkHandlerMixin() {
   }

   @Inject(
      method = {"method_45729"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void sendChatMessage(@NotNull String message, CallbackInfo ci) {
      if (message.startsWith(SlikDlc.INSTANCE.commandStorage.getPrefix())) {
         try {
            SlikDlc.INSTANCE
               .commandStorage
               .getDispatcher()
               .execute(message.substring(SlikDlc.INSTANCE.commandStorage.getPrefix().length()), SlikDlc.INSTANCE.commandStorage.getSource());
         } catch (CommandSyntaxException var4) {
            ChatUtils.sendMessage(class_124.field_1061 + "Ошибка в использовании!");
         }

         ci.cancel();
      }
   }

   @Inject(
      method = {"method_11132"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onVelocityUpdate(class_2743 packet, CallbackInfo ci) {
      EventPacket event = new EventPacket(packet, EventPacket.Type.RECEIVE);
      event.call();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_11124"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onExplosion(class_2664 packet, CallbackInfo ci) {
      EventPacket event = new EventPacket(packet, EventPacket.Type.RECEIVE);
      event.call();
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_64553"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onEntityPositionSync(class_10264 packet, CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (this.field_3699 == null || mc.field_1724 == null || mc.field_1687 == null) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_43596"},
      at = {@At("HEAD")}
   )
   private void onGameMessage(class_7439 packet, CallbackInfo ci) {
      BaritoneAntiStuck.onGameMessage(packet.comp_763().getString());
   }

   @Inject(
      method = {"method_11120"},
      at = {@At("HEAD")}
   )
   private void onGameJoin(class_2678 packet, CallbackInfo ci) {
      BotSessionManager.finishBotConnectStage();
   }
}
