package dev.ftb.mods.ftbteamdimensions.mixin;

import dev.ftb.mods.ftbteamdimensions.FTBDimensionsConfig;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacedFeature.class)
public class PlacedFeatureMixin {
    @Inject(method = "placeWithContext", at = @At("HEAD"), cancellable = true)
    private void onPlaceWithContext(PlacementContext context, RandomSource source, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // no feature gen in a void world!
        if (!FTBDimensionsConfig.DIMENSIONS.allowVoidFeatureGen.get() && DimensionUtils.isVoidChunkGen(context.generator())) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
