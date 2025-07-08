package org.jmod.elytraswap.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.jmod.elytraswap.swapper.SwapManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerMixin {

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;checkGliding()Z", shift = At.Shift.AFTER))
    public void enableSwap(CallbackInfo callbackInfo) {
        ClientPlayerEntity that = ((ClientPlayerEntity) (Object) this);
        if (!SwapManager.clientPlayer.equals(that)) return;
        if (!that.isOnGround() && !that.isGliding() && !that.isTouchingWater() && !that.hasStatusEffect(StatusEffects.LEVITATION)) {
            SwapManager.enableSwap();
            SwapManager.tick();
        }
    }
}
