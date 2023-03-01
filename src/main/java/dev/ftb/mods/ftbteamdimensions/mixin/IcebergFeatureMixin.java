package dev.ftb.mods.ftbteamdimensions.mixin;

import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionUtils;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IcebergFeature.class)
public class IcebergFeatureMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void onPlace(FeaturePlaceContext<BlockStateConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        // no sky icebergs, thanks!
        if (DimensionUtils.isVoidChunkGen(context.chunkGenerator())) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
