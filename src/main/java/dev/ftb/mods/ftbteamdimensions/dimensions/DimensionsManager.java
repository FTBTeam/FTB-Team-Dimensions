package dev.ftb.mods.ftbteamdimensions.dimensions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.commands.FTBDimensionsCommands;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DimensionStorage;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbteamdimensions.event.DimensionCreatedEvent;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public enum DimensionsManager {
    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    public ResourceKey<Level> getDimension(Player player) {
        Team playerTeam = FTBTeamsAPI.getPlayerTeam((ServerPlayer) player);
        return getDimension(playerTeam);
    }

    @Nullable
    public ResourceKey<Level> getDimension(Team team) {
        if (team.getType() != TeamType.PARTY) {
            return null;
        }

        return DimensionStorage.get(team.manager.server).getDimensionId(team);
    }

    public void createDimForTeam(ServerPlayer player, ResourceLocation prebuiltId) {
        // Validate the player is in the right team, they very likely aren't so let's sort that for them.
        Team playerTeam = FTBTeamsAPI.getPlayerTeam(player);
        if (playerTeam.getType() != TeamType.PARTY) {
            try {
                playerTeam = FTBDimensionsCommands.createPartyTeam(player);
            } catch (CommandSyntaxException e) {
                // This likely can't happen
                LOGGER.error("Unable to create player team due to " + e);
                return;
            }
        }

        // if the player has somehow returned to the lobby and back to the portal, just port them straight back
        ResourceKey<Level> dimensionId = DimensionStorage.get(player.server).getDimensionId(playerTeam);
        if (dimensionId != null) {
            DynamicDimensionManager.teleport(player, dimensionId);
            return;
        }

        // Create the dim and store the key
        String dimensionName = playerTeam.getId().toString();

        ResourceKey<Level> key = DimensionStorage.get(player.server).putDimension(playerTeam, dimensionName);

        ServerLevel serverLevel = DynamicDimensionManager.create(player.server, key, prebuiltId);

        // Attempt to load the structure and get the spawn location of the island / structure
        var spawnPoint = PrebuiltStructureManager.getServerInstance().getStructure(prebuiltId)
                .map(prebuilt -> player.server.getStructureManager().get(prebuilt.structureLocation()).map(structure -> {
                            BlockPos blockPos = prebuilt.spawnOverride().orElse(new BlockPos(0, prebuilt.height(), 0));
                            ResourceLocation dimLoc = serverLevel.getLevel().dimension().location();
                            BlockPos spawnPos = DimensionStorage.get(player.server).getDimensionSpawnLocation(dimLoc);
                            if (spawnPos == null) {
                                DimensionStorage.get(player.server).addDimensionSpawn(dimLoc, blockPos);
                                FTBTeamDimensions.LOGGER.info("Adding spawnpoint {} to dim storage for {}", blockPos, dimLoc);
                            }
                            return blockPos;
                        }).orElse(BlockPos.ZERO)
                ).orElse(BlockPos.ZERO);

        player.setRespawnPosition(key, spawnPoint, 0, true, false);
        DynamicDimensionManager.teleport(player, key);

        player.getInventory().clearContent();
        player.heal(player.getMaxHealth());
        FoodData foodData = player.getFoodData();
        foodData.setExhaustion(0);
        foodData.setFoodLevel(20);
        foodData.setSaturation(5.0f);

        MinecraftForge.EVENT_BUS.post(new DimensionCreatedEvent(dimensionName, playerTeam, player, player.server.getLevel(key)));
    }

}
