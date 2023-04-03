package dev.ftb.mods.ftbteamdimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class FTBDimensionsConfig {
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec CLIENT_CONFIG;

    public static final CategoryCommonGeneral COMMON_GENERAL = new CategoryCommonGeneral();
    public static final CategoryClientGeneral CLIENT_GENERAL = new CategoryClientGeneral();

    static {
        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
    }

    public static class CategoryCommonGeneral {
        public final ForgeConfigSpec.BooleanValue clearPlayerInventory;
        public final ForgeConfigSpec.ConfigValue<String> lobbyStructure;
        public final ForgeConfigSpec.BooleanValue allowNetherPortals;
        public final ForgeConfigSpec.BooleanValue singleBiomeDimension;
        public final ForgeConfigSpec.BooleanValue allowVoidFeatureGen;
        public final ForgeConfigSpec.ConfigValue<String> singleBiomeName;
        public final ForgeConfigSpec.BooleanValue teamSpecificNetherEntryPoint;
        public final ForgeConfigSpec.BooleanValue placeEntitiesInStartStructure;
        public final ForgeConfigSpec.IntValue replaceColdBiomesNearSpawn;

        public CategoryCommonGeneral() {
            COMMON_BUILDER.push("general");

            this.clearPlayerInventory = COMMON_BUILDER
                    .comment("When set to true, the players inventory will be cleared when leaving a team")
                    .define("clearPlayerInventory", true);

            this.lobbyStructure = COMMON_BUILDER
                    .comment("Resource location of the structure NBT for the lobby")
                    .define("lobbyStructure", FTBTeamDimensions.MOD_ID + ":lobby");

            this.allowNetherPortals = COMMON_BUILDER
                    .comment("When set to true, nether portals may be constructed in team dimensions")
                    .define("allowNetherPortals", true);

            this.allowVoidFeatureGen = COMMON_BUILDER
                    .comment("When set to false, no features may generate in void dimensions. If this set to true, some features (e.g. icebergs) will generate in applicable biomes without any checks for surrounding blocks (like water). Changing this will *not* affect any already-generated chunks.")
                    .define("allowVoidFeatureGen", false);

            this.singleBiomeDimension = COMMON_BUILDER
                    .comment("If true, generate a void dimension with only a single biome. Otherwise, generate a void dimension with overworld-like biome distribution")
                    .define("singleBiomeDimension", false);

            this.singleBiomeName = COMMON_BUILDER
                    .comment("If 'singleBiomeDimension' is true, this is the ID of the biome to generate")
                    .define("singleBiomeName", "minecraft:the_void");

            this.teamSpecificNetherEntryPoint = COMMON_BUILDER
                    .comment("If true, then players going to the Nether via Nether Portal will be sent to a team-specific position in the Nether")
                    .define("teamSpecificNetherEntryPoint", true);

            this.placeEntitiesInStartStructure = COMMON_BUILDER
                    .comment("If true, then any entities saved in the starting structure NBT will be included when the structure is generated")
                    .define("placeEntitiesInStartStructure", true);

            this.replaceColdBiomesNearSpawn = COMMON_BUILDER
                    .comment("If > 0, any chunk closer than this to spawn with a cold biome (i.e. water can freeze) in its X/Z midpoint will have its biome replaced with 'minecraft:plains'. Set to 0 to disable all replacement.")
                    .defineInRange("replaceColdBiomesNearSpawn", 64, 0, Integer.MAX_VALUE);

            COMMON_BUILDER.pop();
        }

        public ResourceLocation lobbyLocation() {
            return new ResourceLocation(lobbyStructure.get());
        }
    }

    public static class CategoryClientGeneral {
        public final ForgeConfigSpec.DoubleValue voidBiomeHorizon;
        public final ForgeConfigSpec.BooleanValue hideVoidFog;

        public CategoryClientGeneral() {
            CLIENT_BUILDER.push("general");

            this.voidBiomeHorizon = CLIENT_BUILDER
                    .comment("In void team dimensions, the Y level of the horizon; the lower sky turns black if the player's eye position is below this level")
                    .defineInRange("voidBiomeHorizon", 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

            this.hideVoidFog = CLIENT_BUILDER
                    .comment("If true, suppress the void fog effect that appears at low Y levels while in void team dimensions")
                    .define("hideVoidFog", true);

            CLIENT_BUILDER.pop();
        }
    }
}
