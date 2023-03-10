package dev.ftb.mods.ftbteamdimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbteamdimensions.client.DimensionsClient;
import dev.ftb.mods.ftbteamdimensions.client.VoidTeamLevelData;
import net.minecraft.network.FriendlyByteBuf;

public class VoidTeamDimension extends BaseS2CMessage {
    public static final VoidTeamDimension INSTANCE = new VoidTeamDimension();

    private VoidTeamDimension() {
    }

    VoidTeamDimension(FriendlyByteBuf buf) {
    }

    @Override
    public MessageType getType() {
        return FTBDimensionsNet.VOID_TEAM_DIMENSION;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (DimensionsClient.clientLevel().getLevelData() instanceof VoidTeamLevelData vld) {
            vld.setVoidTeamDimension();
        }
    }
}
