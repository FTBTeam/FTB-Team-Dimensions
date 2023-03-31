package dev.ftb.mods.ftbteamdimensions.dimensions.level;

import com.google.common.collect.ImmutableMap;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DimensionStorage extends SavedData {
    private static final String SAVE_NAME = FTBTeamDimensions.MOD_ID + "_dimension_store";

    private final HashMap<UUID, ResourceLocation> teamToDimension = new HashMap<>();
    private final HashMap<ResourceLocation, BlockPos> dimensionSpawnLocations = new HashMap<>();
    private final List<ArchivedDimension> archivedDimensions = new ArrayList<>();
    private final HashMap<UUID,BlockPos> playerNetherPortalLocs = new HashMap<>();

    private boolean lobbySpawned = false;
    private BlockPos lobbySpawnPos = BlockPos.ZERO;

    @Nullable
    public static DimensionStorage get() {
        var level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return null;
        }

        DimensionDataStorage dataStorage = level.getDataStorage();

        return dataStorage.computeIfAbsent(DimensionStorage::load, DimensionStorage::new, SAVE_NAME);
    }

    public static DimensionStorage get(MinecraftServer server) {
        DimensionDataStorage dataStorage = server.getLevel(Level.OVERWORLD).getDataStorage();

        return dataStorage.computeIfAbsent(DimensionStorage::load, DimensionStorage::new, SAVE_NAME);
    }

    @Nullable
    public ResourceKey<Level> getDimensionId(Team team) {
        ResourceLocation dimLocation = teamToDimension.get(team.getId());
        if (dimLocation == null) {
            return null;
        }

        return ResourceKey.create(Registry.DIMENSION_REGISTRY, dimLocation);
    }

    public ResourceKey<Level> putDimension(Team playerTeam, String generateDimensionName) {
        return putDimension(playerTeam, new ResourceLocation(FTBTeamDimensions.MOD_ID, "team/%s".formatted(generateDimensionName)));
    }

    public ResourceKey<Level> putDimension(Team playerTeam, ResourceLocation generateDimensionName) {
        teamToDimension.put(playerTeam.getId(), generateDimensionName);
        this.setDirty();

        return getDimensionId(playerTeam);
    }

    public void archiveDimension(Team oldTeam) {
        ResourceKey<Level> dimensionId = getDimensionId(oldTeam);
        if (dimensionId == null) {
            return;
        }

        teamToDimension.remove(oldTeam.getId());

        ServerPlayer player = oldTeam.manager.server.getPlayerList().getPlayer(oldTeam.getOwner());
        String playerName = player != null ? player.getDisplayName().getString() : "unknown";

        archivedDimensions.add(new ArchivedDimension(playerName, oldTeam.getOwner(), oldTeam.getDisplayName(), dimensionId.location()));
        this.setDirty();
    }

    public List<ArchivedDimension> getArchivedDimensions() {
        return archivedDimensions;
    }

    public Optional<ArchivedDimension> getArchivedDimension(ResourceLocation location) {
        return archivedDimensions.stream().filter(e -> e.dimensionName().equals(location)).findFirst();
    }

    public ImmutableMap<UUID, ResourceLocation> getTeamToDimension() {
        return ImmutableMap.copyOf(teamToDimension);
    }

    @Nullable
    public BlockPos getDimensionSpawnLocation(ResourceLocation dimKeyLocation) {
        return dimensionSpawnLocations.get(dimKeyLocation);
    }

    public void addDimensionSpawn(ResourceLocation location, BlockPos pos) {
        dimensionSpawnLocations.put(location, pos);
        this.setDirty();
    }

    public boolean isLobbySpawned() {
        return lobbySpawned;
    }

    public void setLobbySpawned(boolean lobbySpawned) {
        this.lobbySpawned = lobbySpawned;
        this.setDirty();
    }

    public BlockPos getLobbySpawnPos() {
        return lobbySpawnPos;
    }

    public void setLobbySpawnPos(BlockPos lobbySpawnPos) {
        this.lobbySpawnPos = lobbySpawnPos;
        this.setDirty();
    }

    /**
     * Note the location of the nether portal where the player last left their team dimension to go to the Nether.
     * This will be used when player returns to their team dimension from the Nether; we need to do this since normal
     * vanilla portal location logic (x/8, z/8) doesn't work because we do custom nether portal locations for teams.
     *
     * @param player the player about to go to the nether
     * @param blockPosition the position they leave their team dimension from, which should normally be a nether portal
     */
    public void setPlayerNetherPortalLoc(ServerPlayer player, BlockPos blockPosition) {
        if (blockPosition == null) {
            if (playerNetherPortalLocs.remove(player.getUUID()) != null) {
                setDirty();
            }
        } else {
            playerNetherPortalLocs.put(player.getUUID(), blockPosition);
            setDirty();
        }
    }

    public Optional<BlockPos> getPlayerNetherPortalLoc(ServerPlayer player) {
        return Optional.ofNullable(playerNetherPortalLocs.get(player.getUUID()));
    }

    private static DimensionStorage load(CompoundTag compoundTag) {
        var storage = new DimensionStorage();
        storage.read(compoundTag);
        return storage;
    }

    private void read(CompoundTag tag) {
        if (!tag.contains("team_dimensions")) {
            return;
        }

        teamToDimension.putAll(hashMapReader(UUID::fromString,
                (tag1, key) -> new ResourceLocation(tag1.getString(key)), tag.getCompound("team_dimensions")));
        dimensionSpawnLocations.putAll(hashMapReader(ResourceLocation::new,
                (tag1, key) -> BlockPos.of(tag1.getLong(key)), tag.getCompound("dimension_spawns")));
        playerNetherPortalLocs.putAll(hashMapReader(UUID::fromString,
                (tag1, key) -> BlockPos.of(tag1.getLong(key)), tag.getCompound("player_nether_portal_locs")));

        ListTag dimensionsArchive = tag.getList("dimensions_archive", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < dimensionsArchive.size(); i++) {
            this.archivedDimensions.add(ArchivedDimension.read(dimensionsArchive.getCompound(i)));
        }

        this.lobbySpawned = tag.getBoolean("lobby_spawned");
        if (tag.contains("lobby_spawn_pos")) {
            this.lobbySpawnPos = NbtUtils.readBlockPos(tag.getCompound("lobby_spawn_pos"));
        }

    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("team_dimensions", hashMapWriter(teamToDimension,
                (tag, key, value) -> tag.putString(key.toString(), value.toString())));
        nbt.put("dimension_spawns", hashMapWriter(dimensionSpawnLocations,
                (tag, key, value) -> tag.putLong(key.toString(), value.asLong())));
        nbt.put("player_nether_portal_locs", hashMapWriter(playerNetherPortalLocs,
                (tag, key, value) -> tag.putLong(key.toString(), value.asLong())));

        ListTag archivedList = new ListTag();
        this.archivedDimensions.forEach(e -> archivedList.add(e.write()));
        nbt.put("dimensions_archive", archivedList);

        nbt.putBoolean("lobby_spawned", lobbySpawned);
        nbt.put("lobby_spawn_pos", NbtUtils.writeBlockPos(lobbySpawnPos));

        this.setDirty(false);
        return nbt;
    }

    private <K, V> HashMap<K, V> hashMapReader(Function<String, K> keyReader, BiFunction<CompoundTag, String, V> valueReader, CompoundTag tag) {
        HashMap<K, V> hashMap = new HashMap<>();

        for (String key : tag.getAllKeys()) {
            hashMap.put(keyReader.apply(key), valueReader.apply(tag, key));
        }

        return hashMap;
    }

    private <K, V> CompoundTag hashMapWriter(HashMap<K, V> map, TriConsumer<CompoundTag, K, V> writer) {
        var tag = new CompoundTag();
        for (Map.Entry<K, V> kvEntry : map.entrySet()) {
            writer.accept(tag, kvEntry.getKey(), kvEntry.getValue());
        }

        return tag;
    }

}
