package dev.ftb.mods.ftbteamdimensions.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccess {
    @Accessor
    BlockPos getPortalEntrancePos();
}
