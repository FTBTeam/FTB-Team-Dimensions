package dev.ftb.mods.ftbteamdimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbteamdimensions.client.DimensionsClient;
import dev.ftb.mods.ftbteamdimensions.mixin.LevelAccess;
import dev.ftb.mods.ftbteamdimensions.mixin.PersistentEntitySectionManagerAccess;
import dev.ftb.mods.ftbteamdimensions.mixin.ServerLevelAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

import java.util.HashMap;
import java.util.Map;

public class OpenVisitGui extends BaseS2CMessage {
    private final Map<ResourceLocation, DimData> dimensionData;

    public OpenVisitGui(Map<ResourceLocation, DimData> dimensionData) {
        this.dimensionData = dimensionData;
    }

    public OpenVisitGui(FriendlyByteBuf buf) {
        dimensionData = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            dimensionData.put(buf.readResourceLocation(), DimData.fromNetwork(buf));
        }
    }

    @Override
    public MessageType getType() {
        return FTBDimensionsNet.OPEN_VISIT_GUI;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(dimensionData.size());
        dimensionData.forEach((id, data) -> {
            buf.writeResourceLocation(id);
            data.toNetwork(buf);
        });
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        DimensionsClient.openVisitScreen(dimensionData);
    }

    public record DimData(String teamName, double tickTime, boolean archived, int blockEntities, int entities, int loadedChunks) {
        public static DimData fromNetwork(FriendlyByteBuf buf) {
            String teamName = buf.readUtf(Short.MAX_VALUE);
            double tickTime = buf.readDouble();
            boolean archived = buf.readBoolean();
            int blockEntities = buf.readInt();
            int entities = buf.readInt();
            int loadedChunks = buf.readVarInt();

            return new DimData(teamName, tickTime, archived, blockEntities, entities, loadedChunks);
        }

        public static DimData create(ServerLevel level, String teamName, boolean archived) {
            int beCount = ((LevelAccess) level).getBlockEntityTickers().size();
            PersistentEntitySectionManager<Entity> m = ((ServerLevelAccess) level).getEntityManager();
            int eCount = ((PersistentEntitySectionManagerAccess) m).getKnownUuids().size();
            int lcCount = level.getChunkSource().getLoadedChunksCount();
            return new DimData(teamName, getTickTime(level.getServer(), level.dimension()), archived, beCount, eCount, lcCount);
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeUtf(teamName);
            buf.writeDouble(tickTime);
            buf.writeBoolean(archived);
            buf.writeInt(blockEntities);
            buf.writeInt(entities);
            buf.writeVarInt(loadedChunks);
        }

        private static double getTickTime(MinecraftServer server, ResourceKey<Level> key) {
            long[] times = server.getTickTime(key);
            if (times == null) times = new long[] { 0L };
            return (double) mean(times) * 1.0E-6;
        }

        private static long mean(long[] values) {
            long sum = 0L;
            for (long v : values) {
                sum += v;
            }
            return sum / (long)values.length;
        }
    }
}
