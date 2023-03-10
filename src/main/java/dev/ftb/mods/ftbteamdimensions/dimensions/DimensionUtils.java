package dev.ftb.mods.ftbteamdimensions.dimensions;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftbteamdimensions.FTBDimensionsConfig;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen.MultiBiomeVoidChunkGenerator;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen.SimpleVoidChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DimensionUtils {
    private static final BlockIgnoreProcessor IGNORE_PROCESSOR = new BlockIgnoreProcessor(ImmutableList.of(Blocks.STRUCTURE_VOID, Blocks.STRUCTURE_BLOCK));

    public static BlockPos locateSpawn(StructureTemplate template) {
        StructurePlaceSettings placeSettings = makePlacementSettings(template);
        BlockPos spawnPos = BlockPos.ZERO;

        for (var info : template.filterBlocks(BlockPos.ZERO, placeSettings, Blocks.STRUCTURE_BLOCK)) {
            if (info.nbt != null && StructureMode.valueOf(info.nbt.getString("mode")) == StructureMode.DATA) {
                FTBTeamDimensions.LOGGER.info("Found data block at [{}] with data [{}]", info.pos, info.nbt.getString("metadata"));

                if (info.nbt.getString("metadata").equalsIgnoreCase("spawn_point")) {
                    spawnPos = info.pos;
                }
            }
        }

        return spawnPos;
    }

    public static StructurePlaceSettings makePlacementSettings(StructureTemplate template) {
        Vec3i size = template.getSize();
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setIgnoreEntities(true);
        settings.addProcessor(IGNORE_PROCESSOR);
        settings.setRotationPivot(new BlockPos(size.getX() / 2, size.getY() / 2, size.getZ() / 2));
        settings.setRotation(Rotation.NONE);
        return settings;
    }

    public static boolean isPortalDimension(Level level) {
        return FTBDimensionsConfig.COMMON_GENERAL.allowNetherPortals.get()
                && level.dimension().location().getNamespace().equals(FTBTeamDimensions.MOD_ID);
    }

    public static boolean isVoidChunkGen(ChunkGenerator chunkGenerator) {
        return chunkGenerator instanceof SimpleVoidChunkGenerator
                || chunkGenerator instanceof MultiBiomeVoidChunkGenerator;
    }
}
