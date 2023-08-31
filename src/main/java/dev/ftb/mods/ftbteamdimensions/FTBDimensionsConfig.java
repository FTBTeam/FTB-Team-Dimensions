package dev.ftb.mods.ftbteamdimensions;

import dev.ftb.mods.ftbteamdimensions.dimensions.level.chunkgen.ChunkGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
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
        public final ForgeConfigSpec.BooleanValue clearPlayerInventoryOnLeave;
        public final ForgeConfigSpec.BooleanValue clearPlayerInventoryOnJoin;
        public final ForgeConfigSpec.ConfigValue<String> lobbyStructure;
        public final ForgeConfigSpec.IntValue lobbyYposition;
        public final ForgeConfigSpec.BooleanValue allowNetherPortals;
        public final ForgeConfigSpec.EnumValue<FeatureGeneration> allowFeatureGen;
        public final ForgeConfigSpec.ConfigValue<String> singleBiomeId;
        public final ForgeConfigSpec.ConfigValue<String> noiseSettings;
        public final ForgeConfigSpec.EnumValue<ChunkGenerators> chunkGenerator;
        public final ForgeConfigSpec.BooleanValue teamSpecificNetherEntryPoint;
        public final ForgeConfigSpec.BooleanValue placeEntitiesInStartStructure;
        public final ForgeConfigSpec.IntValue replaceColdBiomesNearSpawn;
        public final ForgeConfigSpec.EnumValue<GameType> lobbyGameMode;
        public final ForgeConfigSpec.BooleanValue allowLobbyDamages;
        public final ForgeConfigSpec.ConfigValue<String> replaceColdBiomeId;

        public CategoryCommonGeneral() {
            COMMON_BUILDER.push("general");

            this.clearPlayerInventoryOnJoin = COMMON_BUILDER
                    .comment("When set to true, the player's inventory will be cleared when joining a team")
                    .define("clearPlayerInventoryOnJoin", false);

            this.clearPlayerInventoryOnLeave = COMMON_BUILDER
                    .comment("When set to true, the players inventory will be cleared when leaving a team")
                    .define("clearPlayerInventory", true);

            this.lobbyStructure = COMMON_BUILDER
                    .comment("Resource location of the structure NBT for the lobby")
                    .define("lobbyStructure", FTBTeamDimensions.MOD_ID + ":lobby");

            this.lobbyYposition = COMMON_BUILDER
                    .comment("Y position at which the lobby structure will be pasted into the overworld. Note: too near world min/max build height may result in parts of the structure being cut off, beware.")
                    .defineInRange("lobbyYposition", 0, -64, 256);

            COMMON_BUILDER.pop();

            COMMON_BUILDER.push("nether");

            this.allowNetherPortals = COMMON_BUILDER
                    .comment("When set to true, nether portals may be constructed in team dimensions")
                    .define("allowNetherPortals", true);

            this.teamSpecificNetherEntryPoint = COMMON_BUILDER
                    .comment("If true, then players going to the Nether via Nether Portal will be sent to a team-specific position in the Nether")
                    .define("teamSpecificNetherEntryPoint", true);

            COMMON_BUILDER.pop();

            COMMON_BUILDER.push("worldgen");

            this.chunkGenerator = COMMON_BUILDER
                    .comment("Resource location for the chunk generator to use. SIMPLE_VOID (void dim, one biome), MULTI_BIOME_VOID (void dim, overworld-like biome distribution) and CUSTOM (full worldgen, customisable biome source & noise settings)")
                    .defineEnum("chunkGenerator", ChunkGenerators.MULTI_BIOME_VOID);

            this.allowFeatureGen = COMMON_BUILDER
                    .comment("DEFAULT: generate features in non-void worlds, don't generate in void worlds; NEVER: never generate; ALWAYS: always generate")
                    .defineEnum("allowFeatureGen", FeatureGeneration.DEFAULT);

            this.singleBiomeId = COMMON_BUILDER
                    .comment("Only used by the CUSTOM and SIMPLE_VOID generators; if non-empty (e.g. 'minecraft:the_void'), the dimension will generate with only this biome. If empty, CUSTOM generator will use an overworld-like biome distribution, and SIMPLE_VOID will use 'minecraft:the_void'")
                    .define("singleBiomeId", "");

            this.noiseSettings = COMMON_BUILDER
                    .comment("Only used by the CUSTOM generator; resource location for the noise settings to use.")
                    .define("customNoiseSettings", "minecraft:overworld");

            this.placeEntitiesInStartStructure = COMMON_BUILDER
                    .comment("If true, then any entities saved in the starting structure NBT will be included when the structure is generated")
                    .define("placeEntitiesInStartStructure", true);

            this.replaceColdBiomesNearSpawn = COMMON_BUILDER
                    .comment("If > 0, any chunk closer than this distance from spawn, with a cold biome (i.e. water can freeze) in its X/Z midpoint, will have its biome replaced with the biome defined in 'replaceColdBiomeId'. Set to 0 to disable all replacement.")
                    .defineInRange("replaceColdBiomesNearSpawn", 64, 0, Integer.MAX_VALUE);

            this.lobbyGameMode = COMMON_BUILDER
                    .comment("Define the gamemode attributed to players when in the lobby")
                    .defineEnum("lobbyGameMode", GameType.ADVENTURE);

            this.allowLobbyDamages = COMMON_BUILDER
                    .comment("If true, living entities can deal damages in the lobby")
                            .define("allowLobbyDamages", false);

            this.replaceColdBiomeId = COMMON_BUILDER
                    .comment("Id of the biome which will be used to replace cold biomes near spawn (see 'replaceColdBiomesNearSpawn')")
                    .define("replaceColdBiomeId", "minecraft:plains");

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

    public enum FeatureGeneration {
        DEFAULT,
        NEVER,
        ALWAYS;

        public boolean shouldGenerate(boolean isVoid) {
            return this == ALWAYS || this == DEFAULT && !isVoid;
        }
    }

}
