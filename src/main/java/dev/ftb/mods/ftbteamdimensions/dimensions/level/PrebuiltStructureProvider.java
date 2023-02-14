package dev.ftb.mods.ftbteamdimensions.dimensions.level;

import net.minecraft.resources.ResourceLocation;

/**
 * Custom chunk generators which support a prebuilt structure should implement this
 */
@FunctionalInterface
public interface PrebuiltStructureProvider {
    /**
     * Get the unique ID of the prebuilt structure; structures are loaded from datapack JSON:
     * {@code data/<namespace>/ftbdim_prebuilt_structures/<path>}
     * @return a resource location for the prebuilt structure
     */
    ResourceLocation getPrebuiltStructureId();
}
