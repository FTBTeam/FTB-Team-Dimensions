package dev.ftb.mods.ftbteamdimensions.portal.content;

import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.net.ShowSelectionGui;
import dev.ftb.mods.ftbteamdimensions.portal.ForgeOnlyOverride;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A bit like the Nether Portal block, but not really.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FTBDimensionsPortalBlock extends NetherPortalBlock {
    public FTBDimensionsPortalBlock() {
        super(Properties.copy(Blocks.NETHER_PORTAL));
    }

    @Override
    public void randomTick(BlockState arg, ServerLevel arg2, BlockPos arg3, RandomSource random) {

    }

    @Override
    @ForgeOnlyOverride
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.isPassenger() || entity.isVehicle() || !entity.canChangeDimensions() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.isOnPortalCooldown()) {
            // vanilla functionality here: ensure portal creation/port logic only happens when stepping into the portal
            // and not when loitering around in a portal block
            player.setPortalCooldown();
        } else {
            ResourceKey<Level> dimension = DimensionsManager.INSTANCE.getDimension(player);
            if (dimension != null) {
                // player's team already has a dimension - just go!
                // note: needs to be deferred a tick, or things can go wrong (e.g. falling out of world on the other side)
                player.getServer().executeIfPossible(() -> DynamicDimensionManager.teleport(player, dimension));
            } else {
                // no team yet: bring up the island selection GUI
                player.setPortalCooldown();
                new ShowSelectionGui().sendTo(player);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState arg, Direction arg2, BlockState arg3, LevelAccessor arg4, BlockPos arg5, BlockPos arg6) {
        return arg;
    }
}
