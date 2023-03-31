package dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions.rl;

public record PrebuiltStructure(ResourceLocation id, ResourceLocation structureLocation, String name, String author, Optional<BlockPos> spawnOverride,
                                ResourceLocation structureSetId, int height, ResourceLocation dimensionType, ResourceLocation previewImage, int displayOrder)
{
    public static final ResourceLocation DEFAULT_PREVIEW = rl("default");
    public static final ResourceLocation FALLBACK_IMAGE = rl("textures/fallback.png");
    public static final ResourceLocation DEFAULT_DIMENSION_TYPE = rl("default");
    public static final ResourceLocation DEFAULT_STRUCTURE_SET = rl( "default");

    public static final Codec<PrebuiltStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id")
                    .forGetter(PrebuiltStructure::id),
            ResourceLocation.CODEC.fieldOf("structure")
                    .forGetter(PrebuiltStructure::structureLocation),
            Codec.STRING.fieldOf("name")
                    .forGetter(PrebuiltStructure::name),
            Codec.STRING.optionalFieldOf("author", "FTB Team")
                    .forGetter(PrebuiltStructure::author),
            BlockPos.CODEC.optionalFieldOf("spawn_override")
                    .forGetter(PrebuiltStructure::spawnOverride),
            ResourceLocation.CODEC.optionalFieldOf("structure_set", DEFAULT_STRUCTURE_SET)
                    .forGetter(PrebuiltStructure::structureSetId),
            Codec.INT.optionalFieldOf("height", 64)
                    .forGetter(PrebuiltStructure::height),
            ResourceLocation.CODEC.optionalFieldOf("dimension_type", DEFAULT_DIMENSION_TYPE)
                    .forGetter(PrebuiltStructure::dimensionType),
            ResourceLocation.CODEC.optionalFieldOf("preview_image", DEFAULT_PREVIEW)
                    .forGetter(PrebuiltStructure::previewImage),
            Codec.INT.optionalFieldOf("display_order", 0)
                    .forGetter(PrebuiltStructure::displayOrder)
    ).apply(instance, PrebuiltStructure::new));

    public static Optional<PrebuiltStructure> fromJson(JsonElement element) {
        return CODEC.decode(JsonOps.INSTANCE, element)
                .resultOrPartial(error -> FTBTeamDimensions.LOGGER.error("JSON parse failure: {}", error))
                .map(Pair::getFirst);
    }

    public static PrebuiltStructure fromBytes(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        ResourceLocation structureLocation = buf.readResourceLocation();
        String name = buf.readUtf(256);
        String author = buf.readUtf(256);
        Optional<BlockPos> spawnOverride = buf.readBoolean() ? Optional.of(buf.readBlockPos()) : Optional.empty();
        int height = buf.readVarInt();
        ResourceLocation structureSetId = buf.readResourceLocation();
        ResourceLocation dimensionType = buf.readResourceLocation();
        ResourceLocation previewImage = buf.readResourceLocation();
        int displayOrder = buf.readVarInt();

        return new PrebuiltStructure(id, structureLocation, name, author, spawnOverride, structureSetId, height, dimensionType, previewImage, displayOrder);
    }

    public ResourceLocation previewImage() {
        return previewImage.equals(DEFAULT_PREVIEW) ?
                new ResourceLocation(id.getNamespace(), "textures/spawn/" + id.getPath() + ".png") :
                previewImage;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeResourceLocation(structureLocation);
        buf.writeUtf(name);
        buf.writeUtf(author);
        buf.writeBoolean(spawnOverride.isPresent());
        spawnOverride.ifPresent(buf::writeBlockPos);
        buf.writeVarInt(height);
        buf.writeResourceLocation(structureSetId);
        buf.writeResourceLocation(dimensionType);
        buf.writeResourceLocation(previewImage);
        buf.writeVarInt(displayOrder);
    }

    public boolean matchesName(String match) {
        return match.isEmpty() || name.toLowerCase().contains(match.toLowerCase());
    }
}
