package dev.ftb.mods.ftbteamdimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class FTBDimensionsConfig {
    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static final CategoryDimensions DIMENSIONS = new CategoryDimensions();

    static {
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static class CategoryDimensions {
        public final ForgeConfigSpec.BooleanValue clearPlayerInventory;
        public final ForgeConfigSpec.ConfigValue<String> lobbyStructure;
        public final ForgeConfigSpec.BooleanValue allowNetherPortals;
        public final ForgeConfigSpec.BooleanValue singleBiomeDimension;
        public final ForgeConfigSpec.ConfigValue<String> singleBiomeName;

        public CategoryDimensions() {
            COMMON_BUILDER.push("hammers");

            this.clearPlayerInventory = COMMON_BUILDER
                    .comment("When set to true, the players inventory will be cleared when leaving a team")
                    .define("clearPlayerInventory", true);

            this.lobbyStructure = COMMON_BUILDER
                    .comment("Resource location of the structure NBT for the lobby")
                    .define("lobbyStructure", FTBTeamDimensions.MOD_ID + ":lobby");

            this.allowNetherPortals = COMMON_BUILDER
                    .comment("When set to true, nether portals may be constructed in team dimensions")
                    .define("allowNetherPortals", true);

            this.singleBiomeDimension = COMMON_BUILDER
                    .comment("If true, generate a void dimension with only a single biome. Otherwise, generate a void dimension with overworld-like biome distribution")
                    .define("singleBiomeDimension", false);

            this.singleBiomeName = COMMON_BUILDER
                    .comment("If 'singleBiomeDimension' is true, this is the ID of the biome to generate")
                    .define("singleBiomeName", "minecraft:the_void");

            COMMON_BUILDER.pop();
        }

        public ResourceLocation lobbyLocation() {
            return new ResourceLocation(lobbyStructure.get());
        }
    }
}
