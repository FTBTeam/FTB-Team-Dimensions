package dev.ftb.mods.ftbteamdimensions.mixin;

import dev.ftb.mods.ftbteamdimensions.portal.FTBDimServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements FTBDimServerPlayer {
    private int portalCoolDown = 0;
    private boolean isInPortal = false;

    @Inject(method = "Lnet/minecraft/server/level/ServerPlayer;tick()V", at = @At("TAIL"))
    public void onTick(CallbackInfo info) {
        this.tickPortalChecks();
    }

    private void tickPortalChecks() {
        if (this.isOnCoolDown()) {
            this.portalCoolDown --;
        }

        if (this.isInPortal) {
            this.isInPortal = false;
        }
    }

    private boolean isOnCoolDown() {
        return this.portalCoolDown > 0;
    }

    @Override
    public void handleStoneBlockPortal(Runnable teleport) {
        this.isInPortal = true;
        if (this.isOnCoolDown())
            return;

        teleport.run();
        this.isInPortal = false;
        this.portalCoolDown = 60;
    }
}
