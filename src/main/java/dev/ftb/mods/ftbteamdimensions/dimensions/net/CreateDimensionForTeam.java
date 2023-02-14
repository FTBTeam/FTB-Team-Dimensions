package dev.ftb.mods.ftbteamdimensions.dimensions.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CreateDimensionForTeam extends BaseC2SMessage {
	private final ResourceLocation prebuiltId;

	public CreateDimensionForTeam(ResourceLocation id) {
		prebuiltId = id;
	}

	public CreateDimensionForTeam(FriendlyByteBuf buf) {
		this.prebuiltId = buf.readResourceLocation();
	}

	@Override
	public MessageType getType() {
		return FTBDimensionsNet.CREATE_DIMENSION_FOR_TEAM;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(prebuiltId);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(() -> DimensionsManager.INSTANCE.createDimForTeam((ServerPlayer) context.getPlayer(), prebuiltId));
	}
}
