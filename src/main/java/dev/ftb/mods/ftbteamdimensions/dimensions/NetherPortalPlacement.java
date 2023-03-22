package dev.ftb.mods.ftbteamdimensions.dimensions;

import dev.ftb.mods.ftbteamdimensions.mixin.EntityAccess;
import dev.ftb.mods.ftbteamdimensions.mixin.ServerPlayerAccess;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class NetherPortalPlacement {
    /**
     * Find a team-specific entry point to the Nether, based on the team. Teams get their own distinct locations to
     * prevent everyone arriving at the same place and potential grief from chunk claiming, portal blocking etc.
     *
     * @param netherLevel the Nether
     * @param serverPlayer the player who is travelling
     * @param team the team being visited; if null, then the player's own team
     * @return portal info for the entry point in the Nether
     */
    public static PortalInfo teamSpecificEntryPoint(ServerLevel netherLevel, ServerPlayer serverPlayer, @Nullable Team team) {
        // this code is adapted from Entity#findDimensionEntryPoint, specifically for going to the Nether

        WorldBorder worldborder = netherLevel.getWorldBorder();

        BlockPos pos0 = getBasePos(serverPlayer, team);
        BlockPos blockpos1 = worldborder.clampToBounds(pos0.getX(), pos0.getY(), pos0.getZ());

        // if null, probably coming from the nether-visit command
        BlockPos entrancePos = Objects.requireNonNullElseGet(((EntityAccess) serverPlayer).getPortalEntrancePos(), serverPlayer::blockPosition);

        return ((ServerPlayerAccess) serverPlayer).invokeGetExitPortal(netherLevel, blockpos1, true, worldborder).map((foundRect) -> {
            BlockState blockstate = serverPlayer.level.getBlockState(entrancePos);
            Direction.Axis axis;
            Vec3 vec3;
            if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                BlockUtil.FoundRectangle rect2 = BlockUtil.getLargestRectangleAround(entrancePos, axis, 21, Direction.Axis.Y, 21,
                        (pos) -> serverPlayer.level.getBlockState(pos) == blockstate);
                vec3 = getRelativePortalPos(serverPlayer, axis, rect2);
            } else {
                axis = Direction.Axis.X;
                vec3 = new Vec3(0.5D, 0.0D, 0.0D);
            }

            return PortalShape.createPortalInfo(netherLevel, foundRect, axis, vec3, serverPlayer.getDimensions(serverPlayer.getPose()),
                    serverPlayer.getDeltaMovement(), serverPlayer.getYRot(), serverPlayer.getXRot());
        }).orElse(null);
    }

    public static PortalInfo teamSpecificEntryPoint(ServerLevel netherLevel, ServerPlayer serverPlayer) {
        return teamSpecificEntryPoint(netherLevel, serverPlayer, null);
    }

    private static BlockPos getBasePos(ServerPlayer serverPlayer, @Nullable Team team) {
        // seed the random generator based on the UUID of team being visited
        // this *should* give a deterministic and distinct location, based on the team ID

        UUID id = team == null ? FTBTeamsAPI.getPlayerTeamID(serverPlayer.getUUID()) : team.getId();
        Random rand = new Random(id.getLeastSignificantBits() ^ id.getMostSignificantBits());

        double angle = rand.nextDouble(Math.PI * 2);
        int dist = rand.nextInt(25000);

        return new BlockPos(Math.cos(angle) * dist, serverPlayer.blockPosition().getY(), Math.sin(angle) * dist);
    }

    private static Vec3 getRelativePortalPos(ServerPlayer player, Direction.Axis axis, BlockUtil.FoundRectangle rect) {
        Vec3 pos = PortalShape.getRelativePosition(rect, axis, player.position(), player.getDimensions(player.getPose()));
        return new Vec3(pos.x, pos.y, 0.0);
    }
}
