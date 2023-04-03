package dev.ftb.mods.ftbteamdimensions.dimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.function.Predicate;

public class BiomeReplacementUtils {
    public static void replaceBiome(ServerLevel level, ChunkAccess chunk, BlockPos pFrom, BlockPos pTo, Holder<Biome> replacement) {
        BlockPos minPos = quantize(pFrom);
        BlockPos maxPos = quantize(pTo);
        BoundingBox boundingbox = BoundingBox.fromCorners(minPos, maxPos);
        chunk.fillBiomesFromNoise(makeResolver(chunk, boundingbox, replacement, holder -> true), level.getChunkSource().randomState().sampler());
        chunk.setUnsaved(true);
        syncChunkToClients(level, chunk);
    }

    private static BiomeResolver makeResolver(ChunkAccess pChunk, BoundingBox pTargetRegion, Holder<Biome> pReplacementBiome, Predicate<Holder<Biome>> pFilter) {
        return (x, y, z, sampler) -> {
            int i = QuartPos.toBlock(x);
            int j = QuartPos.toBlock(y);
            int k = QuartPos.toBlock(z);
            Holder<Biome> holder = pChunk.getNoiseBiome(x, y, z);
            return pTargetRegion.isInside(new BlockPos(i, j, k)) && pFilter.test(holder) ? pReplacementBiome : holder;
        };
    }

    private static void syncChunkToClients(ServerLevel serverLevel, ChunkAccess chunk) {
        ChunkPos chunkpos = chunk.getPos();
        LevelChunk levelchunk = chunk instanceof LevelChunk lc ? lc : serverLevel.getChunk(chunkpos.x, chunkpos.z);

        // inefficient but there's no mechanism until 1.19.3 to sync just the biome data; need to sync the whole chunk data
        serverLevel.getChunkSource().chunkMap.getPlayers(chunkpos, false).forEach(player ->
                player.connection.send(new ClientboundLevelChunkWithLightPacket(levelchunk, serverLevel.getLightEngine(), null, null, true))
        );
    }

    private static int quantize(int pValue) {
        return QuartPos.toBlock(QuartPos.fromBlock(pValue));
    }

    private static BlockPos quantize(BlockPos pPos) {
        return new BlockPos(quantize(pPos.getX()), quantize(pPos.getY()), quantize(pPos.getZ()));
    }
}
