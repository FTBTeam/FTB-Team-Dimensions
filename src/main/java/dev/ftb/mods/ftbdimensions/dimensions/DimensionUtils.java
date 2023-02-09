package dev.ftb.mods.ftbdimensions.dimensions;

import com.google.common.collect.ImmutableList;
import dev.ftb.mods.ftbdimensions.FTBDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
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
                FTBDimensions.LOGGER.info("Found data block at [{}] with data [{}]", info.pos, info.nbt.getString("metadata"));

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
}
