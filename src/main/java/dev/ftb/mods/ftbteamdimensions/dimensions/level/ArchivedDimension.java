package dev.ftb.mods.ftbteamdimensions.dimensions.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ArchivedDimension(
        String teamOwner,
        UUID teamOwnerUuid,
        String teamName,
        ResourceLocation dimensionName
) {
    public static ArchivedDimension read(CompoundTag tag) {
        return new ArchivedDimension(
                tag.getString("teamOwner"),
                tag.getUUID("teamOwnerUuid"),
                tag.getString("teamName"),
                new ResourceLocation(tag.getString("dimensionName"))
        );
    }

    public CompoundTag write() {
        var compound = new CompoundTag();
        compound.putString("teamOwner", this.teamOwner);
        compound.putUUID("teamOwnerUuid", this.teamOwnerUuid);
        compound.putString("teamName", this.teamName);
        compound.putString("dimensionName", this.dimensionName.toString());

        return compound;
    }
}
