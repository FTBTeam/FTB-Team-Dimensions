package dev.ftb.mods.ftbteamdimensions.registry;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen.MultiBiomeVoidChunkGenerator;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen.SimpleVoidChunkGenerator;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.placement.OneChunkOnlyPlacement;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.structure.StartStructure;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.structure.StartStructurePiece;
import dev.ftb.mods.ftbteamdimensions.dimensions.waterlogging.WaterLoggingFixProcessor;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModWorldGen {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES
            = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, FTBTeamDimensions.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES
            = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, FTBTeamDimensions.MOD_ID);
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES
            = DeferredRegister.create(Registry.STRUCTURE_PLACEMENT_TYPE_REGISTRY, FTBTeamDimensions.MOD_ID);
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS
            = DeferredRegister.create(Registry.STRUCTURE_PROCESSOR_REGISTRY, FTBTeamDimensions.MOD_ID);

    public static final RegistryObject<StructureType<StartStructure>> START_STRUCTURE
            = STRUCTURE_TYPES.register("start", () -> explicitStructureTypeTyping(StartStructure.CODEC));

    public static final RegistryObject<StructurePieceType.StructureTemplateType> START_STRUCTURE_PIECE
            = STRUCTURE_PIECE_TYPES.register("start", () -> StartStructurePiece::new);

    public static final RegistryObject<StructurePlacementType<OneChunkOnlyPlacement>> ZERO_ZERO_PLACEMENT
            = STRUCTURE_PLACEMENT_TYPES.register("one_chunk_only", () -> explicitStructurePlacementTypeTyping(OneChunkOnlyPlacement.CODEC));

    public static final RegistryObject<StructureProcessorType<WaterLoggingFixProcessor>> WATER_LOGGING_FIX_PROCESSOR
            = STRUCTURE_PROCESSORS.register("waterlogging_fix_processor", () -> () -> WaterLoggingFixProcessor.CODEC);

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> codec) {
        return () -> codec;
    }

    private static <T extends StructurePlacement> StructurePlacementType<T> explicitStructurePlacementTypeTyping(Codec<T> codec) {
        return () -> codec;
    }

    public static void setup(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registry.CHUNK_GENERATOR_REGISTRY)) {
            event.register(Registry.CHUNK_GENERATOR_REGISTRY, FTBTeamDimensions.rl("multi_biome_void"), () -> MultiBiomeVoidChunkGenerator.CODEC);
            event.register(Registry.CHUNK_GENERATOR_REGISTRY, FTBTeamDimensions.rl("simple_void"), () -> SimpleVoidChunkGenerator.CODEC);
        }
    }
}
