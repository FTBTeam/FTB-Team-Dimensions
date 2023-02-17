package dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions.rl;

public record PrebuiltStructure(ResourceLocation id, ResourceLocation structureLocation, String name, String author,
                                ResourceLocation structureSetId, int height, ResourceLocation dimensionType, ResourceLocation previewImage)
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
            ResourceLocation.CODEC.optionalFieldOf("structure_set", DEFAULT_STRUCTURE_SET)
                    .forGetter(PrebuiltStructure::structureSetId),
            Codec.INT.optionalFieldOf("height", 64)
                    .forGetter(PrebuiltStructure::height),
            ResourceLocation.CODEC.optionalFieldOf("dimension_type", DEFAULT_DIMENSION_TYPE)
                    .forGetter(PrebuiltStructure::dimensionType),
            ResourceLocation.CODEC.optionalFieldOf("preview_image", DEFAULT_PREVIEW)
                    .forGetter(PrebuiltStructure::previewImage)
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
        int height = buf.readVarInt();
        ResourceLocation structureSetId = buf.readResourceLocation();
        ResourceLocation dimensionType = buf.readResourceLocation();
        ResourceLocation previewImage = buf.readResourceLocation();

        return new PrebuiltStructure(id, structureLocation, name, author, structureSetId, height, dimensionType, previewImage);
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
        buf.writeVarInt(height);
        buf.writeResourceLocation(structureSetId);
        buf.writeResourceLocation(dimensionType);
        buf.writeResourceLocation(previewImage);
    }
}
