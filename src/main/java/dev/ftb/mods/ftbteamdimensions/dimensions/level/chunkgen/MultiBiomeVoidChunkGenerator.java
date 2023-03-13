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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * Basically the vanilla NoiseBasedChunkGenerator, with biome distribution but no actual blocks
 * and with the initial start structure.
 * Note: it is very important that this class extends NoiseBasedChunkGenerator and not just ChunkGenerator - vanilla does
 * specific instanceof checks during chunk gen which require this to be a type of NoiseBasedChunkGenerator
 */
public class MultiBiomeVoidChunkGenerator extends NoiseBasedChunkGenerator implements PrebuiltStructureProvider {
    public static final Codec<MultiBiomeVoidChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance -> commonCodec(instance)
                    .and(instance.group(
                            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(gen -> gen.noises),
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings),
                            ResourceLocation.CODEC.fieldOf("prebuilt_structure_id").forGetter(gen -> gen.prebuiltStructureId)
                    ))
                    .apply(instance, instance.stable(MultiBiomeVoidChunkGenerator::new)));

    protected final BlockState defaultBlock;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final Aquifer.FluidPicker globalFluidPicker;

    private final ResourceLocation prebuiltStructureId;
    private final HolderSet<StructureSet> startStructure;

    public static MultiBiomeVoidChunkGenerator multiBiomeVoidChunkGen(RegistryAccess registryAccess, ResourceLocation prebuiltStructureId) {
        Registry<StructureSet> structureSetRegistry = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);

        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        MultiNoiseBiomeSource biomeSource = MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomeRegistry);

        Registry<NormalNoise.NoiseParameters> noiseParams = registryAccess.registryOrThrow(Registry.NOISE_REGISTRY);

        Holder<NoiseGeneratorSettings> settings = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
                .getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);

        MultiBiomeVoidChunkGenerator gen = new MultiBiomeVoidChunkGenerator(structureSetRegistry, noiseParams, biomeSource, settings, prebuiltStructureId);

        if (!FTBDimensionsConfig.COMMON_GENERAL.allowVoidFeatureGen.get()) {
            //noinspection ConstantConditions
            ((ChunkGeneratorAccess) gen).setFeaturesPerStep(List::of);
        }

        return gen;
    }

    private MultiBiomeVoidChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, ResourceLocation prebuiltStructureId) {
        super(structureSets, noises, biomeSource, settings);

        this.noises = noises;
        this.prebuiltStructureId = prebuiltStructureId;
        NoiseGeneratorSettings genSettings = settings.value();
        this.defaultBlock = genSettings.defaultBlock();
        Aquifer.FluidStatus lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = genSettings.seaLevel();
        Aquifer.FluidStatus defaultFluid = new Aquifer.FluidStatus(i, genSettings.defaultFluid());
        this.globalFluidPicker = (x, y, z) -> y < Math.min(-54, i) ? lava : defaultFluid;

        ResourceLocation structureSetId = PrebuiltStructureManager.getServerInstance().getStructure(prebuiltStructureId)
                .map(PrebuiltStructure::structureSetId).orElse(PrebuiltStructure.DEFAULT_STRUCTURE_SET);
        startStructure = structureSets.getOrCreateTag(TagKey.create(Registry.STRUCTURE_SET_REGISTRY, structureSetId));
    }

    @Override
    public Stream<Holder<StructureSet>> possibleStructureSets() {
        return Stream.concat(startStructure.stream(), super.possibleStructureSets());
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        // no-op
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        // no-op
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        // no-op
    }

    @Override
    public int getGenDepth() {
        return settings.value().noiseSettings().height();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return settings.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return settings.value().noiseSettings().minY();
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        return new NoiseColumn(height.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public ResourceLocation getPrebuiltStructureId() {
        return prebuiltStructureId;
    }
}
