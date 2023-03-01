package dev.ftb.mods.ftbteamdimensions.dimensions.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.math.Vector3d;
import com.mojang.serialization.Lifecycle;
import dev.ftb.mods.ftbteamdimensions.FTBDimensionsConfig;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen.MultiBiomeVoidChunkGenerator;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen.SimpleVoidChunkGenerator;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructure;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbteamdimensions.net.UpdateDimensionsList;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Thanks to McJty and Commoble for providing this code.
 * See original DynamicDimensionManager in RF Tools Dimensions for comments and more generic example.
 */
public class DynamicDimensionManager {
	public static ServerLevel create(MinecraftServer server, ResourceKey<Level> key, ResourceLocation prebuiltStructureId) {
		@SuppressWarnings("deprecation")
		Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();

		ServerLevel existingLevel = map.get(key);

		if (existingLevel != null) {
			return existingLevel;
		}

		RegistryAccess registryAccess = server.registryAccess();

		ServerLevel overworld = Objects.requireNonNull(server.getLevel(Level.OVERWORLD));

		ResourceLocation dimensionTypeId = PrebuiltStructureManager.getServerInstance().getStructure(prebuiltStructureId)
				.map(PrebuiltStructure::dimensionType)
				.orElse(PrebuiltStructure.DEFAULT_DIMENSION_TYPE);

		ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, key.location());
		Holder<DimensionType> typeHolder = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(
				ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, dimensionTypeId)
		);

		// TODO allow specification of other generation types
		ChunkGenerator chunkGenerator = FTBDimensionsConfig.DIMENSIONS.singleBiomeDimension.get() ?
				SimpleVoidChunkGenerator.simpleVoidChunkGen(registryAccess, prebuiltStructureId) :
				MultiBiomeVoidChunkGenerator.multiBiomeVoidChunkGen(registryAccess, prebuiltStructureId);

		LevelStem dimension = new LevelStem(typeHolder, chunkGenerator);

		ChunkProgressListener chunkProgressListener = server.progressListenerFactory.create(11);
		WorldData worldData = server.getWorldData();
		WorldGenSettings worldGenSettings = worldData.worldGenSettings();
		DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

		((MappedRegistry<LevelStem>) worldGenSettings.dimensions()).register(dimensionKey, dimension, Lifecycle.stable());

		ServerLevel newWorld = new ServerLevel(
				server,
				server.executor,
				server.storageSource,
				derivedLevelData,
				key,
				dimension,
				chunkProgressListener,
				worldGenSettings.isDebug(),
				BiomeManager.obfuscateSeed(worldGenSettings.seed()),
				ImmutableList.of(),
				false
		);

		overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newWorld.getWorldBorder()));
		map.put(key, newWorld);
		server.markWorldsDirty();
		MinecraftForge.EVENT_BUS.post(new LevelEvent(newWorld));
		new UpdateDimensionsList(key, true).sendToAll(server);
		return newWorld;
	}

	public static ServerLevel destroy(MinecraftServer server, ResourceKey<Level> key) {
		WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
		ServerLevel overworld = Objects.requireNonNull(server.getLevel(Level.OVERWORLD));
		Path dimensionPath = server.storageSource.getDimensionPath(key);

		// I know, we shouldn't mess with this, but...
		ServerLevel removedLevel = server.forgeGetWorldMap().remove(key);
		if (removedLevel == null) {
			return null;
		}

		// move all players in the dimension back to overworld
		for (ServerPlayer player : Lists.newArrayList(removedLevel.players())) {
			BlockPos destinationPos = player.getRespawnPosition();
			if (destinationPos == null) {
				destinationPos = overworld.getSharedSpawnPos();
			}

			float respawnAngle = player.getRespawnAngle();
			player.teleportTo(overworld, destinationPos.getX(), destinationPos.getY(), destinationPos.getZ(), respawnAngle, 0F);
		}

		removedLevel.save(null, false, removedLevel.noSave());
		MinecraftForge.EVENT_BUS.post(new LevelEvent(removedLevel));

		// reset world border listeners if needed
		WorldBorder overworldBorder = overworld.getWorldBorder();
		WorldBorder removedWorldBorder = removedLevel.getWorldBorder();
		overworldBorder.listeners.stream()
				.filter(listener -> listener instanceof BorderChangeListener.DelegateBorderChangeListener l && removedWorldBorder == l.worldBorder)
				.findFirst()
				.ifPresent(overworldBorder::removeListener);

		// remove the dimension from the dimensions (level-stem) registry
		// needs to be done by copying the registry to a new one without the deleted dimension
		Registry<LevelStem> oldRegistry = worldGenSettings.dimensions();
		MappedRegistry<LevelStem> newRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, oldRegistry.elementsLifecycle(), null);
		for (var entry : oldRegistry.entrySet()) {
			ResourceKey<LevelStem> oldKey = entry.getKey();
			ResourceKey<Level> oldLevelKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, oldKey.location());
			LevelStem dimension = entry.getValue();
			if (dimension != null && oldLevelKey != key) {
				newRegistry.register(oldKey, dimension, oldRegistry.lifecycle(dimension));
			}
		}
		worldGenSettings.dimensions = newRegistry;

		// try to delete the world from disk
		if (Files.exists(dimensionPath)) {
			try {
				FileUtils.deleteDirectory(dimensionPath.toFile());
				FTBTeamDimensions.LOGGER.info("Deleted archived dimension {} from disk", dimensionPath);
			} catch (IOException e) {
				FTBTeamDimensions.LOGGER.error("Failed to delete dimension file for {} at {}", key, dimensionPath, e);
			}
		}

		server.markWorldsDirty();
		new UpdateDimensionsList(key, false).sendToAll(server);

		return removedLevel;
	}

	public static boolean teleport(ServerPlayer player, ResourceKey<Level> key) {
		ServerLevel level = player.server.getLevel(key);

		if (level != null) {
			if (key.equals(Level.OVERWORLD)) {
				BlockPos lobbySpawnPos = DimensionStorage.get(player.server).getLobbySpawnPos();
				player.teleportTo(level, lobbySpawnPos.getX() + .5D, lobbySpawnPos.getY() + .01D, lobbySpawnPos.getZ() + .5D, player.getYRot(), player.getXRot());
			} else {
				Vector3d vec = new Vector3d(0.5D, 1.1D, 0.5D);
				BlockPos respawnPosition = player.getRespawnPosition();
				if (player.getRespawnDimension().equals(key) && respawnPosition != null) {
					vec.add(new Vector3d(respawnPosition.getX(), respawnPosition.getY(), respawnPosition.getZ()));
				} else {
					BlockPos levelSharedSpawn = DimensionStorage.get(player.server).getDimensionSpawnLocation(level.dimension().location());
					if (levelSharedSpawn == null) {
						levelSharedSpawn = BlockPos.ZERO;
					}

					vec.add(new Vector3d(levelSharedSpawn.getX(), levelSharedSpawn.getY(), levelSharedSpawn.getZ()));
				}

				FTBTeamDimensions.LOGGER.debug("teleport {} to {} in {}", player.getGameProfile().getName(), vec, key.location());
				ChunkPos chunkpos = new ChunkPos(new BlockPos(vec.x, vec.y, vec.z));
				level.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getId());
				player.stopRiding();
				player.teleportTo(level, vec.x, vec.y, vec.z, player.getYRot(), player.getXRot());
			}
			return true;
		} else {
			FTBTeamDimensions.LOGGER.error("Failed to teleport {} to {} (bad level key)", player.getScoreboardName(), key.location());
			return false;
		}
	}
}
