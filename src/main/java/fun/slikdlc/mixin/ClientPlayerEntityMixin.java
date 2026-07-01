package fun.slikdlc.mixin;

import com.mojang.authlib.GameProfile;
import fun.slikdlc.api.QClient;
import fun.slikdlc.api.events.EventInvoker;
import fun.slikdlc.api.events.implement.EventCloseInv;
import fun.slikdlc.api.events.implement.EventMove;
import fun.slikdlc.api.events.implement.EventSlowWalking;
import fun.slikdlc.api.events.implement.EventSprint;
import fun.slikdlc.api.events.implement.EventUpdate;
import fun.slikdlc.api.events.implement.EventUpdatePost;
import fun.slikdlc.api.storages.implement.RotationStorage;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.slikdlc.api.utils.player.ViaProtocolUtils;
import fun.slikdlc.client.modules.impl.combat.Aura;
import net.minecraft.class_1313;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2815;
import net.minecraft.class_304;
import net.minecraft.class_634;
import net.minecraft.class_746;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_746.class})
public abstract class ClientPlayerEntityMixin extends class_1657 implements QClient {
   @Shadow
   @Final
   public class_634 field_3944;

   public ClientPlayerEntityMixin(class_1937 world, class_2338 pos, float yaw, GameProfile gameProfile) {
      super(world, pos, yaw, gameProfile);
   }

   @Shadow
   public abstract void method_3137();

   @Inject(
      method = {"method_5773"},
      at = {@At(
         value = "HEAD",
         target = "Lnet/minecraft/class_742;method_5773()V"
      )}
   )
   private void onTick(CallbackInfo ci) {
      if (EventInvoker.hasListeners(EventUpdate.class)) {
         new EventUpdate().call();
      }
   }

   @Inject(
      method = {"method_5773"},
      at = {@At(
         value = "TAIL",
         target = "Lnet/minecraft/class_742;method_5773()V"
      )}
   )
   private void onTickPost(CallbackInfo ci) {
      if (EventInvoker.hasListeners(EventUpdatePost.class)) {
         new EventUpdatePost().call();
      }

      if (this.shouldSyncRotation()) {
         this.field_6241 = this.method_36454();
         this.field_6259 = this.method_36454();
         this.field_6283 = this.method_36454();
         this.field_6220 = this.method_36454();
      }
   }

   @Unique
   private boolean shouldSyncRotation() {
      return ModuleClass.aura.isEnable() && Aura.clientLook.isState() && RotationStorage.instance.isRotating();
   }

   @Redirect(
      method = {"method_6007"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_304;method_1434()Z",
         ordinal = 1
      ),
      require = 0
   )
   private boolean onSprintKeyPressed(class_304 instance) {
      if (!ViaProtocolUtils.isTargetProtocolBelowOneNineteen() || !this.field_5976 && !this.field_34927) {
         EventSprint event = new EventSprint();
         event.call();
         return event.isCancelled() ? false : instance.method_1434();
      } else {
         return false;
      }
   }

   @Redirect(
      method = {"method_6007"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_6115()Z"
      ),
      require = 0
   )
   private boolean onSlowDownRedirect(class_746 player) {
      if (player.method_6115()) {
         EventSlowWalking event = new EventSlowWalking();
         event.call();
         return player.method_6115() && player.method_5854() == null && !event.isCancelled();
      } else {
         return player.method_6115() && player.method_5854() == null;
      }
   }

   @Inject(
      method = {"method_30673"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
      if (ModuleClass.noPush.isEnable() && ModuleClass.noPush.getCollisionList().is("Блоки")) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_5784"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMoveHook(class_1313 movementType, class_243 movement, @NotNull CallbackInfo ci) {
      EventMove event = new EventMove(movement);
      event.call();
      if (event.isCancelled() || !event.getMovePos().equals(movement)) {
         if (event.isCancelled()) {
            ci.cancel();
         } else {
            double d = this.method_23317();
            double e = this.method_23321();
            super.method_5784(movementType, event.getMovePos());
            float f = (float)Math.sqrt(Math.pow(this.method_23317() - d, 2.0) + Math.pow(this.method_23321() - e, 2.0));
            this.method_48565(f);
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"method_7346"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCloseHandledScreen(CallbackInfo ci) {
      int syncId = this.field_7512.field_7763;
      EventCloseInv event = new EventCloseInv(syncId);
      event.call();
      if (!event.isCancelled()) {
         this.field_3944.method_52787(new class_2815(syncId));
      }

      this.method_3137();
      ci.cancel();
   }

   @Inject(
      method = {"method_7290"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      if (ModuleClass.lockSlot != null && ModuleClass.lockSlot.isCurrentSlotLockedForDrop()) {
         cir.setReturnValue(false);
      }
   }
}
