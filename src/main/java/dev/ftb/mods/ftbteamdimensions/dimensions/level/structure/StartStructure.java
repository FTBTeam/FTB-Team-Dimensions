package dev.ftb.mods.ftbteamdimensions.dimensions.level.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionUtils;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.PrebuiltStructureProvider;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbteamdimensions.registry.ModWorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class StartStructure extends Structure {
	public static final Codec<StartStructure> CODEC = RecordCodecBuilder.<StartStructure>mapCodec((x) -> x.group(
			settingsCodec(x),
			HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
			Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap)
	).apply(x, StartStructure::new)).codec();

	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;

	private StartStructure(StructureSettings settings, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap) {
		super(settings);
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!(context.chunkGenerator() instanceof PrebuiltStructureProvider provider)) {
			return Optional.empty();
		}

		PrebuiltStructureManager mgr = PrebuiltStructureManager.getServerInstance();
		var res = mgr.getStructure(provider.getPrebuiltStructureId()).map(start -> {
			StructureTemplate template = context.structureTemplateManager().getOrCreate(start.structureLocation());

			BlockPos spawnPos = DimensionUtils.locateSpawn(template);
			int x = -spawnPos.getX();
			int y = -spawnPos.getY();
			int z = -spawnPos.getZ();
			BlockPos blockPos = new BlockPos(x, y + start.height(), z);

			return Optional.of(new GenerationStub(blockPos, builder ->
					builder.addPiece(new StartStructurePiece(context.structureTemplateManager(), start.structureLocation(), blockPos, template)))
			);
		}).orElse(Optional.empty());

		if (res.isEmpty()) {
			FTBTeamDimensions.LOGGER.warn("Unable to find [{}] in the prebuilt structure list", provider.getPrebuiltStructureId());
		}

		return res;
	}

	@Override
	public StructureType<?> type() {
		return ModWorldGen.START_STRUCTURE.get();
	}
}
