package dev.ftb.mods.ftbteamdimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbteamdimensions.client.DimensionsClient;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.ArchivedDimension;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class SyncArchivedDimensions extends BaseS2CMessage {
    List<ArchivedDimension> dimensions;

    public SyncArchivedDimensions(List<ArchivedDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public SyncArchivedDimensions(FriendlyByteBuf buf) {
        this.dimensions = buf.readList(bufItem -> new ArchivedDimension(
                bufItem.readUtf(), bufItem.readUUID(), bufItem.readUtf(), bufItem.readResourceLocation()
        ));
    }

    @Override
    public MessageType getType() {
        return FTBDimensionsNet.SYNC_ARCHIVED_DIMENSIONS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.dimensions, (buf1, archivedDimension) -> {
            buf1.writeUtf(archivedDimension.teamOwner());
            buf1.writeUUID(archivedDimension.teamOwnerUuid());
            buf1.writeUtf(archivedDimension.teamName());
            buf1.writeResourceLocation(archivedDimension.dimensionName());
        });
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            DimensionsClient.knownDimensions.clear();
            DimensionsClient.knownDimensions.addAll(this.dimensions);
        });
    }
}
