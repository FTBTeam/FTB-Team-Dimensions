package dev.ftb.mods.ftbteamdimensions.dimensions;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.ftb.mods.ftbteamdimensions.FTBDimensionsConfig;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DimensionStorage;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbteamdimensions.net.SyncArchivedDimensions;
import dev.ftb.mods.ftbteamdimensions.net.SyncPrebuiltStructures;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamType;
import dev.ftb.mods.ftbteams.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.event.TeamEvent;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.ftb.mods.ftbteamdimensions.dimensions.DimensionUtils.locateSpawn;

@Mod.EventBusSubscriber
public class DimensionsMain {
    public static final int SIZE = 128;
    public static final int HEIGHT = SIZE * 2;

    public static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation OVERWORLD = new ResourceLocation("overworld");

    public static void setup() {
        TeamEvent.PLAYER_LEFT_PARTY.register(DimensionsMain::teamPlayerLeftParty);
        TeamEvent.PLAYER_JOINED_PARTY.register(DimensionsMain::teamPlayerJoin);
        LifecycleEvent.SERVER_STARTED.register(DimensionsMain::serverStart);

        PlayerEvent.PLAYER_JOIN.register(DimensionsMain::syncDimensions);
    }

    private static void syncDimensions(ServerPlayer player) {
        new SyncArchivedDimensions(DimensionStorage.get(player.server).getArchivedDimensions()).sendTo(player);
        new SyncPrebuiltStructures(PrebuiltStructureManager.getServerInstance()).sendTo(player);
    }

    private static void teamPlayerJoin(PlayerJoinedPartyTeamEvent event) {
        Team team = event.getTeam();
        if (team.getType() != TeamType.PARTY || team.getOwner() == event.getPlayer().getUUID()) {
            return;
        }

        ResourceKey<Level> dimension = DimensionsManager.INSTANCE.getDimension(team);
        if (dimension == null) {
            return;
        }

        BlockPos blockPos = DimensionStorage.get(event.getPlayer().server).getDimensionSpawnLocation(dimension.location());
        if (blockPos != null) {
            event.getPlayer().setRespawnPosition(dimension, blockPos, 0, true, false);
            DynamicDimensionManager.teleport(event.getPlayer(), dimension);
        }
    }

    private static void serverStart(MinecraftServer minecraftServer) {
        ServerLevel level = minecraftServer.getLevel(Level.OVERWORLD);
        if (level == null) {
            LOGGER.warn("Missed spawn reset event due to overworld being null");
            return;
        }

        if (DimensionStorage.get(minecraftServer).isLobbySpawned() && !level.getSharedSpawnPos().equals(DimensionStorage.get(minecraftServer).getLobbySpawnPos())) {
            level.setDefaultSpawnPos(DimensionStorage.get(minecraftServer).getLobbySpawnPos(), 180F);
            LOGGER.info("Updating shared spawn to the lobby location");
        }
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity().level.dimension().equals(Level.OVERWORLD) && event.getSource() != DamageSource.OUT_OF_WORLD) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        MinecraftServer server = serverLevel.getServer();
        DimensionStorage dimensionStorage = DimensionStorage.get(server);

        if (!serverLevel.dimension().location().equals(OVERWORLD) || dimensionStorage.isLobbySpawned()) {
            return;
        }

        try {
            ResourceLocation lobbyLocation = FTBDimensionsConfig.COMMON_GENERAL.lobbyLocation();

            if (!serverLevel.dimension().location().equals(OVERWORLD)) {
                BlockPos dimSpawn = dimensionStorage.getDimensionSpawnLocation(serverLevel.dimension().location());
                if (!serverLevel.getSharedSpawnPos().equals(dimSpawn)) {
                    serverLevel.setDefaultSpawnPos(dimSpawn == null ? BlockPos.ZERO.above(1) : dimSpawn, 0f);
                }
            }

            // Spawn the dim
            StructureTemplate lobby = serverLevel.getStructureManager().getOrCreate(lobbyLocation);
            StructurePlaceSettings placeSettings = DimensionUtils.makePlacementSettings(lobby);
            BlockPos spawnPos = locateSpawn(lobby);
            BlockPos lobbyLoc = BlockPos.ZERO.offset(-(lobby.getSize().getX() / 2), FTBDimensionsConfig.COMMON_GENERAL.lobbyYposition.get(), -(lobby.getSize().getZ() / 2));
            BlockPos playerSpawn = spawnPos.offset(lobbyLoc.getX(), lobbyLoc.getY(), lobbyLoc.getZ());

            lobby.placeInWorld(serverLevel, lobbyLoc, lobbyLoc, placeSettings, serverLevel.random, Block.UPDATE_ALL);

            dimensionStorage.setLobbySpawnPos(playerSpawn);
            dimensionStorage.setLobbySpawned(true);

            serverLevel.removeBlock(playerSpawn, false);
            LOGGER.info("Spawned lobby structure @ {}", lobbyLoc);
        } catch (ResourceLocationException e) {
            LOGGER.error("invalid lobby resource location: " + e.getMessage());
        }
    }

    private static void teamPlayerLeftParty(PlayerLeftPartyTeamEvent event) {
        ServerPlayer serverPlayer = event.getPlayer();
        if (serverPlayer != null) {
            ResourceKey<Level> dimensionId = DimensionStorage.get(event.getPlayer().server).getDimensionId(event.getTeam());
            if (dimensionId == null) {
                return;
            }

            if (FTBDimensionsConfig.COMMON_GENERAL.clearPlayerInventory.get()) {
                serverPlayer.getInventory().clearOrCountMatchingItems(arg -> true, -1, serverPlayer.inventoryMenu.getCraftSlots());
                serverPlayer.containerMenu.broadcastChanges();
                serverPlayer.inventoryMenu.slotsChanged(serverPlayer.getInventory());
            }

            if (event.getTeamDeleted()) {
                DimensionStorage dimensionStorage = DimensionStorage.get(event.getPlayer().server);
                dimensionStorage.archiveDimension(event.getTeam());
                new SyncArchivedDimensions(dimensionStorage.getArchivedDimensions()).sendToAll(serverPlayer.server);
            }

            DynamicDimensionManager.teleport(serverPlayer, Level.OVERWORLD);
            serverPlayer.setRespawnPosition(Level.OVERWORLD, DimensionStorage.get(serverPlayer.server).getLobbySpawnPos(), 180F, true, false);
        }
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
             return;
        }

        if (level.dimension().location().equals(OVERWORLD) && player.getRespawnDimension().location().equals(OVERWORLD)) {
            // Assume this is their first time joining the world as otherwise their respawn dimension would be their own dimension
            BlockPos lobbySpawnPos = DimensionStorage.get(player.server).getLobbySpawnPos();
            if (player.getRespawnPosition() == null || !player.getRespawnPosition().equals(lobbySpawnPos)) {
                player.setRespawnPosition(level.dimension(), lobbySpawnPos, -180, true, false);
                player.teleportTo((ServerLevel) level, lobbySpawnPos.getX(), lobbySpawnPos.getY(), lobbySpawnPos.getZ(), -180F, -10F);
            }
        }

        swapGameMode(level.dimension(), player);
    }

    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        swapGameMode(event.getLevel().dimension(), player);
    }

    /**
     * When entering the overworld (lobby) the player will be switch to Adventure mode as long as they're not in creative mode,
     * upon the overworld whilst the game mode is set to adventure, we'll switch back to survival
     *
     * @param dimension dimension entering from or leaving
     * @param player    the player leaving or entering a dimension.
     */
    private static void swapGameMode(ResourceKey<Level> dimension, ServerPlayer player) {
        if (dimension.location().equals(OVERWORLD) && player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE && player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
            player.setGameMode(GameType.ADVENTURE);
        }

        if (!dimension.location().equals(OVERWORLD) && player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            player.setGameMode(GameType.SURVIVAL);
        }
    }
}
