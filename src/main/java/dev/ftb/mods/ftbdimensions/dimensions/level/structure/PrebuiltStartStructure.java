package dev.ftb.mods.ftbdimensions.dimensions.level.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbdimensions.FTBDimensions;
import dev.ftb.mods.ftbdimensions.dimensions.DimensionUtils;
import dev.ftb.mods.ftbdimensions.dimensions.level.PrebuiltStructureProvider;
import dev.ftb.mods.ftbdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbdimensions.registry.ModWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class PrebuiltStartStructure extends Structure {
	//	public static final Codec<PrebuiltStartStructure> CODEC = simpleCodec(PrebuiltStartStructure::new);
	public static final Codec<PrebuiltStartStructure> CODEC = RecordCodecBuilder.<PrebuiltStartStructure>mapCodec((x) -> x.group(
			settingsCodec(x),
			HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
			Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap)
	).apply(x, PrebuiltStartStructure::new)).codec();

	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;

	private PrebuiltStartStructure(StructureSettings settings, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap) {
		super(settings);
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!(context.chunkGenerator() instanceof PrebuiltStructureProvider provider)
				|| context.chunkPos().getMinBlockX() != 0 || context.chunkPos().getMinBlockZ() != 0)
		{
			return Optional.empty();
		}

		PrebuiltStructureManager mgr = PrebuiltStructureManager.getServerInstance();
		var res = mgr.getStructure(provider.getPrebuiltStructure()).map(start -> {
			StructureTemplate template = context.structureTemplateManager().getOrCreate(start.structureLocation());

			BlockPos spawnPos = DimensionUtils.locateSpawn(template);
			int x = -spawnPos.getX();
			int y = -spawnPos.getY();
			int z = -spawnPos.getZ();
			BlockPos blockPos = new BlockPos(x, y + start.height(), z);

			return Optional.of(new GenerationStub(blockPos, builder ->
					builder.addPiece(new PrebuiltStartStructurePiece(context.structureTemplateManager(), start.structureLocation(), blockPos, template)))
			);
		}).orElse(Optional.empty());

		if (res.isEmpty()) {
			FTBDimensions.LOGGER.warn("Unable to find [{}] in the prebuilt structure list", provider.getPrebuiltStructure());
		}

		return res;
	}

	@Override
	public StructureType<?> type() {
		return ModWorldGen.START_STRUCTURE.get();
	}
}
