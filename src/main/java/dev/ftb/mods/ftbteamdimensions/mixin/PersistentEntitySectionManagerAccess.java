package dev.ftb.mods.ftbteamdimensions.mixin;

import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.UUID;

@Mixin(PersistentEntitySectionManager.class)
public interface PersistentEntitySectionManagerAccess {
    @Accessor
    Set<UUID> getKnownUuids();
}
