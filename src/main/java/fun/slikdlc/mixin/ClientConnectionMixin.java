package fun.slikdlc.mixin;

import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventPacket;
import fun.slikdlc.api.utils.network.NetworkUtils;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2535.class})
public abstract class ClientConnectionMixin {
   public ClientConnectionMixin() {
   }

   @Inject(
      method = {"method_10770"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void channelRead0(ChannelHandlerContext channelHandlerContext, class_2596<?> packet, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      EventPacket eventReceive = new EventPacket(packet, EventPacket.Type.RECEIVE);
      EventInvoker.invoke(eventReceive);
      if (eventReceive.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_10743"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void send(class_2596<?> packet, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (NetworkUtils.getSilentPackets().contains(packet)) {
         NetworkUtils.getSilentPackets().remove(packet);
      } else {
         EventPacket eventSend = new EventPacket(packet, EventPacket.Type.SEND);
         EventInvoker.invoke(eventSend);
         if (eventSend.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
