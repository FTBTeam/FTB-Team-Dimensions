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

        public CategoryDimensions() {
            COMMON_BUILDER.push("hammers");

            this.clearPlayerInventory = COMMON_BUILDER
                    .comment("When set to true, the players inventory will be cleared when leaving a team")
                    .define("clearPlayerInventory", true);

            this.lobbyStructure = COMMON_BUILDER
                    .comment("Resource location of the structure NBT for the lobby")
                    .define("lobbyStructure", FTBTeamDimensions.MOD_ID + ":lobby");

            COMMON_BUILDER.pop();
        }

        public ResourceLocation lobbyLocation() {
            return new ResourceLocation(lobbyStructure.get());
        }
    }
}
