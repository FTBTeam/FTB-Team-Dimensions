package dev.ftb.mods.ftbteamdimensions.dimensions;

import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Pregen {
    private static final Path PREGEN_PATH = Path.of(FTBTeamDimensions.MOD_ID, "pregen");
    private static final Path PREGEN_INITIAL_PATH = Path.of(FTBTeamDimensions.MOD_ID, "pregen_initial");

    private static final List<String> INITIAL_SUBDIRS = List.of("region", "entities", "poi", "DIM1", "DIM-1");

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

    public static void maybeDoInitialPregen(MinecraftServer server) {
        Path initialPath = server.getServerDirectory().toPath().resolve(PREGEN_INITIAL_PATH);
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        if (Files.isDirectory(initialPath) && !Files.isDirectory(worldPath.resolve("region"))) {
            // looks like a brand-new world, just created - copy over any pregen MCA files for overworld/nether/end if they exist
            for (String subDir : INITIAL_SUBDIRS) {
                Path srcDir = initialPath.resolve(subDir);
                Path destDir = worldPath.resolve(subDir);
                if (Files.isDirectory(srcDir) && !Files.isDirectory(destDir)) {
                    try {
                        FileUtils.copyDirectory(srcDir.toFile(), destDir.toFile());
                        FTBTeamDimensions.LOGGER.info("Copied initial pregen MCA files from {} to {}", srcDir, destDir);
                    } catch (IOException e) {
                        FTBTeamDimensions.LOGGER.error("Failed to copy initial MCA files from {} to {}: {}", srcDir, destDir, e.getMessage());
                    }
                }
            }
        }
    }
}
