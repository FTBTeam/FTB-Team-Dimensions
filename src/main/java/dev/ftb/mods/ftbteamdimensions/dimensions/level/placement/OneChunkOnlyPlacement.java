package dev.ftb.mods.ftbteamdimensions.dimensions.level.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbteamdimensions.registry.ModWorldGen;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

/**
 * Ensures the start structure generates only in a specific chunk
 */
public class OneChunkOnlyPlacement extends StructurePlacement {
    public static final Codec<OneChunkOnlyPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("cx").forGetter(z -> z.cx),
            Codec.INT.fieldOf("cz").forGetter(z -> z.cz)
    ).apply(instance, OneChunkOnlyPlacement::new));

    private final int cx, cz;

    protected OneChunkOnlyPlacement(int cx, int cz) {
        super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0f, 0, Optional.empty());

        this.cx = cx;
        this.cz = cz;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGenerator generator, RandomState randomState, long seed, int x, int z) {
        return x == cx && z == cz;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModWorldGen.ZERO_ZERO_PLACEMENT.get();
    }
}
