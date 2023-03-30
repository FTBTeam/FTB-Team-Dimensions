package dev.ftb.mods.ftbteamdimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DynamicDimensionManager;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class VisitDimension extends BaseC2SMessage {
    private final ResourceLocation dimId;

    public VisitDimension(ResourceLocation dimId) {
        this.dimId = dimId;
    }

    public VisitDimension(FriendlyByteBuf buf) {
        dimId = buf.readResourceLocation();
    }

    @Override
    public MessageType getType() {
        return FTBDimensionsNet.VISIT_DIMENSION;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimId);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer sp && sp.hasPermissions(2)) {
            DynamicDimensionManager.teleport(sp, ResourceKey.create(Registry.DIMENSION_REGISTRY, dimId));
        }
    }
}
