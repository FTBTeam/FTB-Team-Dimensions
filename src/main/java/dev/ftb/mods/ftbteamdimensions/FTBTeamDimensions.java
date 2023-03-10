package dev.ftb.mods.ftbteamdimensions;

import dev.ftb.mods.ftbteamdimensions.client.DimensionsClient;
import dev.ftb.mods.ftbteamdimensions.commands.FTBDimensionsCommands;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionUtils;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsMain;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import dev.ftb.mods.ftbteamdimensions.net.FTBDimensionsNet;
import dev.ftb.mods.ftbteamdimensions.net.VoidTeamDimension;
import dev.ftb.mods.ftbteamdimensions.registry.ModArgumentTypes;
import dev.ftb.mods.ftbteamdimensions.registry.ModBlocks;
import dev.ftb.mods.ftbteamdimensions.registry.ModWorldGen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
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
        FTBDimensionsConfig.init();

        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

        MOD_BUS.addListener(this::commonSetup);
        MOD_BUS.addListener(this::clientSetup);
        MOD_BUS.addListener(ModWorldGen::setup);

        ModBlocks.BLOCK_REGISTRY.register(MOD_BUS);
        ModBlocks.ITEM_REGISTRY.register(MOD_BUS);
        ModArgumentTypes.ARGUMENT_TYPES.register(MOD_BUS);
        ModWorldGen.STRUCTURE_TYPES.register(MOD_BUS);
        ModWorldGen.STRUCTURE_PIECE_TYPES.register(MOD_BUS);
        ModWorldGen.STRUCTURE_PLACEMENT_TYPES.register(MOD_BUS);

        MinecraftForge.EVENT_BUS.addListener(this::commandsSetup);
        MinecraftForge.EVENT_BUS.addListener(this::reloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::dimensionChanged);
        MinecraftForge.EVENT_BUS.addListener(this::entityJoinLevel);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onSleepFinished);

        FTBDimensionsNet.init();
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

    private void reloadListener(final AddReloadListenerEvent event) {
        event.addListener(new PrebuiltStructureManager.ReloadListener());
    }

    private void dimensionChanged(final EntityTravelToDimensionEvent event) {
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

    private void entityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && DimensionUtils.isVoidChunkGen(sp.getLevel().getChunkSource().getGenerator())) {
            VoidTeamDimension.INSTANCE.sendTo(sp);
        }
    }

    private void onSleepFinished(final SleepFinishedTimeEvent event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().location().getNamespace().equals(MOD_ID)) {
            // player has slept in a dynamic dimension
            // sleeping in dynamic dimensions doesn't work in general: https://bugs.mojang.com/browse/MC-188578
            // best we can do here is advance the overworld time
            ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                overworld.setDayTime(event.getNewTime());
                if (overworld.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && overworld.isRaining()) {
                    if (overworld.getLevelData() instanceof ServerLevelData data) {
                        data.setRainTime(0);
                        data.setRaining(false);
                        data.setThunderTime(0);
                        data.setThundering(false);
                    }
                }
            }
        }
    }
}
