package dev.ftb.mods.ftbteamdimensions.mixin;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccess {
    @Invoker("getExitPortal")
    Optional<BlockUtil.FoundRectangle> invokeGetExitPortal(ServerLevel destination, BlockPos findFrom, boolean isToNether, WorldBorder worldBorder);
}
