package dev.ftb.mods.ftbteamdimensions.mixin.client;

import dev.ftb.mods.ftbteamdimensions.FTBDimensionsConfig;
import dev.ftb.mods.ftbteamdimensions.client.VoidTeamLevelData;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelDataMixin implements VoidTeamLevelData {
    private boolean voidTeamDimension;

    @Override
    public boolean isVoidTeamDimension() {
        return voidTeamDimension;
    }

    @Override
    public void setVoidTeamDimension() {
        this.voidTeamDimension = true;
    }

    @Inject(at = @At("HEAD"), method = "getHorizonHeight", cancellable = true)
    private void onGetHorizonHeight(CallbackInfoReturnable<Double> cir) {
        if (voidTeamDimension) {
            cir.setReturnValue(FTBDimensionsConfig.CLIENT_GENERAL.voidBiomeHorizon.get());
        }
    }

    @Inject(at = @At("HEAD"), method = "getClearColorScale", cancellable = true)
    private void onGetClearColorScale(CallbackInfoReturnable<Float> cir) {
        if (voidTeamDimension && FTBDimensionsConfig.CLIENT_GENERAL.hideVoidFog.get()) {
            cir.setReturnValue(1.0F);
        }
    }
}
