package dev.ftb.mods.ftbteamdimensions;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class FTBDimensionsTags {
    public static final TagKey<Biome> HAS_START_STRUCTURE = TagKey.create(Registry.BIOME_REGISTRY, FTBTeamDimensions.rl("start"));
}
