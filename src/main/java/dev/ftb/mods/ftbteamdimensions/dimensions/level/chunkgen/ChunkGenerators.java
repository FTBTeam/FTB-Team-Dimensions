package dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.BiFunction;

public enum ChunkGenerators {
    SIMPLE_VOID("simple_void", SimpleVoidChunkGenerator::simpleVoidChunkGen, SimpleVoidChunkGenerator.CODEC),
    MULTI_BIOME_VOID("multi_biome_void", MultiBiomeVoidChunkGenerator::multiBiomeVoidChunkGen, MultiBiomeVoidChunkGenerator.CODEC),
    CUSTOM("custom", CustomChunkGenerator::customChunkgen, CustomChunkGenerator.CODEC);

    private final ResourceLocation id;
    private final BiFunction<RegistryAccess, ResourceLocation, ChunkGenerator> factory;
    private final Codec<? extends ChunkGenerator> codec;

    ChunkGenerators(String id, BiFunction<RegistryAccess, ResourceLocation, ChunkGenerator> factory, Codec<? extends ChunkGenerator> codec) {
        this.id = FTBTeamDimensions.rl(id);
        this.factory = factory;
        this.codec = codec;
    }

    public ChunkGenerator makeGenerator(RegistryAccess registryAccess, ResourceLocation prebuiltStructureId) {
        return factory.apply(registryAccess, prebuiltStructureId);
    }

    public static void registerGenerators(RegisterEvent event) {
        for (ChunkGenerators gen : ChunkGenerators.values()) {
            event.register(Registry.CHUNK_GENERATOR_REGISTRY, gen.id, () -> gen.codec);
        }
    }
}
