package dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbteamdimensions.FTBDimensionsConfig;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.PrebuiltStructureProvider;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructure;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbteamdimensions.mixin.ChunkGeneratorAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;
import java.util.stream.Stream;

/**
 * Basically the vanilla NoiseBasedChunkGenerator, with the initial start structure.  Can be configured to use standard
 * overworld biome noise source, or a single biome.
 * Note: it is very important that this class extends NoiseBasedChunkGenerator and not just ChunkGenerator - vanilla does
 * specific instanceof checks during chunk gen which require this to be a type of NoiseBasedChunkGenerator
 */
public class CustomChunkGenerator extends NoiseBasedChunkGenerator implements PrebuiltStructureProvider {
    public static final Codec<CustomChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance -> commonCodec(instance)
                    .and(instance.group(
                            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(gen -> gen.noises),
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings),
                            ResourceLocation.CODEC.fieldOf("prebuilt_structure_id").forGetter(gen -> gen.prebuiltStructureId)
                    ))
                    .apply(instance, instance.stable(CustomChunkGenerator::new)));

    private final Registry<NormalNoise.NoiseParameters> noises;

    private final ResourceLocation prebuiltStructureId;
    private final HolderSet<StructureSet> startStructure;

    public static CustomChunkGenerator customChunkgen(RegistryAccess registryAccess, ResourceLocation prebuiltStructureId) {
        Registry<StructureSet> structureSetRegistry = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);

        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        BiomeSource biomeSource;
        if (!FTBDimensionsConfig.COMMON_GENERAL.singleBiomeId.get().isEmpty()) {
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY,
                    new ResourceLocation(FTBDimensionsConfig.COMMON_GENERAL.singleBiomeId.get()));
            biomeSource = new FixedBiomeSource(biomeRegistry.getHolderOrThrow(biomeKey));
        } else {
            biomeSource = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry);
        }

        Registry<NormalNoise.NoiseParameters> noiseRegistry = registryAccess.registryOrThrow(Registry.NOISE_REGISTRY);

        ResourceKey<NoiseGeneratorSettings> noiseSettingsKey = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY,
                new ResourceLocation(FTBDimensionsConfig.COMMON_GENERAL.noiseSettings.get()));
        Holder<NoiseGeneratorSettings> noiseSettings = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
                .getHolderOrThrow(noiseSettingsKey);

        CustomChunkGenerator gen = new CustomChunkGenerator(structureSetRegistry, noiseRegistry, biomeSource, noiseSettings, prebuiltStructureId);

        if (!FTBDimensionsConfig.COMMON_GENERAL.allowFeatureGen.get().shouldGenerate(false)) {
            //noinspection ConstantConditions
            ((ChunkGeneratorAccess) gen).setFeaturesPerStep(List::of);
        }

        return gen;
    }

    private CustomChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, ResourceLocation prebuiltStructureId) {
        super(structureSets, noises, biomeSource, settings);

        this.prebuiltStructureId = prebuiltStructureId;

        this.noises = noises;

        ResourceLocation structureSetId = PrebuiltStructureManager.getServerInstance().getStructure(prebuiltStructureId)
                .map(PrebuiltStructure::structureSetId)
                .orElse(PrebuiltStructure.DEFAULT_STRUCTURE_SET);
        startStructure = structureSets.getOrCreateTag(TagKey.create(Registry.STRUCTURE_SET_REGISTRY, structureSetId));
    }

    @Override
    public ResourceLocation getPrebuiltStructureId() {
        return prebuiltStructureId;
    }

    @Override
    public Stream<Holder<StructureSet>> possibleStructureSets() {
        return Stream.concat(startStructure.stream(), super.possibleStructureSets());
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
