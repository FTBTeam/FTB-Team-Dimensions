package dev.ftb.mods.ftbteamdimensions.dimensions;

import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class Pregen {
    private static final Path PREGEN_PATH = Path.of(FTBTeamDimensions.MOD_ID, "pregen");

    public static void copyIfExists(MinecraftServer server, ResourceLocation prebuiltId, ResourceKey<Level> levelKey) {
        Path rootDir = server.getServerDirectory().toPath();
        Path resDir = Path.of(prebuiltId.getNamespace(), prebuiltId.getPath().split("/"));
        Path pregenDir = rootDir.resolve(PREGEN_PATH).resolve(resDir);

        if (!pregenDir.toFile().isDirectory() || !pregenDir.resolve("region").toFile().isDirectory()) {
            return;
        }

        Path destDir = server.getWorldPath(LevelResource.ROOT)
                .resolve("dimensions")
                .resolve(levelKey.location().getNamespace())
                .resolve(levelKey.location().getPath());

        try {
            FileUtils.copyDirectory(pregenDir.toFile(), destDir.toFile());
            FTBTeamDimensions.LOGGER.info("Copied pregen MCA files from {} to {}", pregenDir, destDir);
        } catch (IOException e) {
            FTBTeamDimensions.LOGGER.error("Failed to copy pregen MCA files from {} to {}: {}", pregenDir, destDir, e.getMessage());
            e.printStackTrace();
        }
    }
}
