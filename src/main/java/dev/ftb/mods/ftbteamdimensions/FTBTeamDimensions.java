package dev.ftb.mods.ftbteamdimensions;

import dev.ftb.mods.ftbteamdimensions.client.DimensionsClient;
import dev.ftb.mods.ftbteamdimensions.commands.FTBDimensionsCommands;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsMain;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbteamdimensions.registry.ModArgumentTypes;
import dev.ftb.mods.ftbteamdimensions.registry.ModBlocks;
import dev.ftb.mods.ftbteamdimensions.registry.ModWorldGen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FTBTeamDimensions.MOD_ID)
public class FTBTeamDimensions {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "ftbteamdimensions";

    public FTBTeamDimensions() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FTBDimensionsConfig.COMMON_CONFIG);

        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

        MOD_BUS.addListener(this::commonSetup);
        MOD_BUS.addListener(this::clientSetup);

        ModBlocks.BLOCK_REGISTRY.register(MOD_BUS);
        ModBlocks.ITEM_REGISTRY.register(MOD_BUS);
        ModArgumentTypes.ARGUMENT_TYPES.register(MOD_BUS);
        ModWorldGen.STRUCTURE_TYPES.register(MOD_BUS);
        ModWorldGen.STRUCTURE_PIECE_TYPES.register(MOD_BUS);

        MinecraftForge.EVENT_BUS.addListener(this::commandsSetup);
        MinecraftForge.EVENT_BUS.addListener(this::reloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::dimensionChanged);
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        DimensionsMain.setup();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DimensionsClient::init);
    }

    private void commandsSetup(final RegisterCommandsEvent event) {
        FTBDimensionsCommands.register(event.getDispatcher());
    }

    private void reloadListener(AddReloadListenerEvent event) {
        event.addListener(new PrebuiltStructureManager.ReloadListener());
    }

    private void dimensionChanged(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getLevel().dimension().equals(Level.NETHER) && event.getDimension().equals(Level.OVERWORLD)) {
                // returning from a Nether portal: intercept this and send the player to their island spawnpoint instead
                var dim = DimensionsManager.INSTANCE.getDimension(player);
                if (dim != null) {
                    event.setCanceled(true);
                    player.server.executeIfPossible(() -> DynamicDimensionManager.teleport(player, dim));
                }
            }
        }
    }
}
