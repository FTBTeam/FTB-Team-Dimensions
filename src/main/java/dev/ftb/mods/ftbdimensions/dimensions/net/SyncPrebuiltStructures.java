package dev.ftb.mods.ftbdimensions.dimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbdimensions.dimensions.prebuilt.PrebuiltStructure;
import dev.ftb.mods.ftbdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;

public class SyncPrebuiltStructures extends BaseS2CMessage {
    private final Collection<PrebuiltStructure> structures;

    public SyncPrebuiltStructures(PrebuiltStructureManager manager) {
        this.structures = manager.getStructures();
    }

    public SyncPrebuiltStructures(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        structures = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            structures.add(PrebuiltStructure.fromBytes(buf));
        }
    }

    @Override
    public MessageType getType() {
        return FTBDimensionsNet.SYNC_PREBUILT_STRUCTURES;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(structures.size());
        structures.forEach(s -> s.toBytes(buf));
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        PrebuiltStructureManager.getClientInstance().syncFromServer(structures);
    }
}
