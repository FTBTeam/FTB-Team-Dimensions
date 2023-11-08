package dev.ftb.mods.ftbteamdimensions.dimensions.waterlogging;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbteamdimensions.registry.ModWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WaterLoggingFixProcessor extends StructureProcessor {
    public static final WaterLoggingFixProcessor INSTANCE = new WaterLoggingFixProcessor();
    public static final Codec<WaterLoggingFixProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo after, StructurePlaceSettings structurePlaceSettings, @Nullable StructureTemplate template) {
        // NOTE: process() method is a Forge extension. If we ever port this to Fabric, use processBlock() instead

        if (!after.state.isAir()) {
            getWriter(levelReader).ifPresent(writer -> {
                // Is the block meant to be a fluid? No then lets make sure the world doesn't already have a fluid there
                if (after.state.getFluidState().isEmpty()) {
                    // Is the block water? No, is the current block in the world water? Yes, Cool, remove it before placement
                    if (after.state.getBlock() != Blocks.WATER && levelReader.hasChunkAt(after.pos) && levelReader.getBlockState(after.pos).getFluidState().is(Fluids.WATER)) {
                        writer.setBlock(after.pos, after.state, Block.UPDATE_ALL);
                    }
                }
            });
        }

        return after;
    }

    private Optional<LevelWriter> getWriter(LevelReader reader) {
        if (reader instanceof WorldGenRegion wg) {
            return Optional.of(wg);
        } else if (reader instanceof ServerLevel sl) {
            return Optional.of(sl);
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModWorldGen.WATER_LOGGING_FIX_PROCESSOR.get();
    }
}
