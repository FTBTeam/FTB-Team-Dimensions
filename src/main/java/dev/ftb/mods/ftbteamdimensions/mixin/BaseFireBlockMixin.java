package dev.ftb.mods.ftbteamdimensions.mixin;

import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    @Inject(method="inPortalDimension", at = @At("RETURN"), cancellable = true)
    private static void onInPortalDimension(Level pLevel, CallbackInfoReturnable<Boolean> cir) {
        // vanilla only allows nether portals to be created in the overworld and nether
        // we also want to allow them to be created in our dynamic dimensions
        if (DimensionUtils.isPortalDimension(pLevel)) {
            cir.setReturnValue(true);
        }
    }
}
