package dev.ftb.mods.ftbteamdimensions.mixin;

import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorMixin {
    @Accessor("featuresPerStep")
    @Mutable
    void setFeaturesPerStep(Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep);
}
