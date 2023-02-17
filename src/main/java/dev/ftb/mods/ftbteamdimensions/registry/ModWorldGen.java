package dev.ftb.mods.ftbteamdimensions.registry;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.structure.StartStructure;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.structure.StartStructurePiece;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModWorldGen {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES
            = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, FTBTeamDimensions.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES
            = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, FTBTeamDimensions.MOD_ID);

    public static final RegistryObject<StructureType<StartStructure>> START_STRUCTURE
            = STRUCTURE_TYPES.register("start", () -> explicitStructureTypeTyping(StartStructure.CODEC));

    public static final RegistryObject<StructurePieceType.StructureTemplateType> START_STRUCTURE_PIECE
            = STRUCTURE_PIECE_TYPES.register("start", () -> StartStructurePiece::new);

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }
}
