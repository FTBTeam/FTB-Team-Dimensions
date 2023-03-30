package dev.ftb.mods.ftbteamdimensions.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.client.gui.StartSelectScreen;
import dev.ftb.mods.ftbteamdimensions.client.gui.VisitScreen;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.ArchivedDimension;
import dev.ftb.mods.ftbteamdimensions.net.CreateDimensionForTeam;
import dev.ftb.mods.ftbteamdimensions.net.OpenVisitGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class DimensionsClient {
    public static boolean debugMode = false;
    public static final List<ArchivedDimension> knownDimensions = new ArrayList<>();

    @SubscribeEvent
    public static void registerClientCommand(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ftbstoneblock-client").then(Commands.literal("debug")
                .executes(context -> DimensionsClient.toggleDebug()))
        );
    }

    public static void init() {
        DimensionSpecialEffects.EFFECTS.put(new ResourceLocation(FTBTeamDimensions.MOD_ID, "stoneblock"), new StoneblockDimensionSpecialEffects());
    }

    public static void exportBiomes(ServerLevel level, Path path, int radius) {
        Player player = Minecraft.getInstance().player;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        try (NativeImage image = new NativeImage(radius * 2 + 1, radius * 2 + 1, true)) {
            for (int x = 0; x <= radius * 2; x++) {
                for (int z = 0; z <= radius * 2; z++) {
                    pos.set(x - radius + Mth.floor(player.getX()), 0, z - radius + Mth.floor(player.getZ()));
//                    image.setPixelRGBA(x, z, StoneBlockDataKjs.getColor(level, pos));
                    // TODO do we need this anymore?
                    image.setPixelRGBA(x, z, 0);
                }
            }

            if (Files.exists(path)) {
                Files.delete(path);
            }

            image.writeToFile(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void openSelectionScreen() {
        Minecraft.getInstance().setScreen(new StartSelectScreen(prebuiltStructure -> {
            new CreateDimensionForTeam(prebuiltStructure.id()).sendToServer();
        }));
    }

    public static void openVisitScreen(Map<ResourceLocation, OpenVisitGui.DimData> dim2name) {
        Minecraft.getInstance().setScreen(new VisitScreen(dim2name));
    }

    public static int toggleDebug() {
        debugMode = !debugMode;
        return 0;
    }

    public static Set<ResourceKey<Level>> playerLevels(Player player) {
        return ((LocalPlayer) player).connection.levels();
    }

    private static class StoneblockDimensionSpecialEffects extends DimensionSpecialEffects {
        public StoneblockDimensionSpecialEffects() {
            super(Float.NaN, false, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 arg, float f) {
            return arg.scale(0.15D);
        }

        @Override
        public boolean isFoggyAt(int i, int j) {
            return false;
        }

        @Override
        public float[] getSunriseColor(float f, float g) {
            return null;
        }
    }

    @Nonnull
    public static Level clientLevel() {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }
}
