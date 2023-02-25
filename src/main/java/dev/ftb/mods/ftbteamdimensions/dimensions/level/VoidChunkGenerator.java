package dev.ftb.mods.ftbteamdimensions.dimensions.level;

import dev.ftb.mods.ftbteamdimensions.FTBDimensionsConfig;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsMain;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructure;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;

/**
 * Simple-minded void chunk generator (single biome only)
 */
public class VoidChunkGenerator extends FlatLevelSource implements PrebuiltStructureProvider {
    private final ResourceLocation prebuiltStructureId;

    private VoidChunkGenerator(Registry<StructureSet> structureSets, FlatLevelGeneratorSettings settings, ResourceLocation prebuiltStructureId) {
        super(structureSets, settings);
        this.prebuiltStructureId = prebuiltStructureId;
    }

    @Override
    public ResourceLocation getPrebuiltStructureId() {
        return prebuiltStructureId;
    }

    static ChunkGenerator simpleVoidChunkGen(RegistryAccess registryAccess, ResourceLocation prebuiltStructureId) {
        Registry<StructureSet> structureSetRegistry = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        ResourceLocation structureSetId = PrebuiltStructureManager.getServerInstance().getStructure(prebuiltStructureId)
                .map(PrebuiltStructure::structureSetId).orElse(PrebuiltStructure.DEFAULT_STRUCTURE_SET);
        HolderSet<StructureSet> structures = structureSetRegistry.getOrCreateTag(TagKey.create(Registry.STRUCTURE_SET_REGISTRY, structureSetId));

        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        ResourceKey<Biome> biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(FTBDimensionsConfig.DIMENSIONS.singleBiomeName.get()));

        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biomeRegistry)
                .withLayers(List.of(new FlatLayerInfo(1, Blocks.AIR)), Optional.of(structures));
        settings.setBiome(biomeRegistry.getOrCreateHolder(biomeKey).result().orElseThrow());

        return new VoidChunkGenerator(structureSetRegistry, settings, prebuiltStructureId);
    }

    @Override
    public int getGenDepth() {
        return DimensionsMain.HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return -DimensionsMain.SIZE - 1;
    }

    @Override
    public int getMinY() {
        return -DimensionsMain.SIZE;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return DimensionsMain.SIZE - 1;
    }
}
