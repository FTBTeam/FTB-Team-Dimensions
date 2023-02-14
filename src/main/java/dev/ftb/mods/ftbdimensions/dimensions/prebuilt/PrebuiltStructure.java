package dev.ftb.mods.ftbdimensions.dimensions.prebuilt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.ftb.mods.ftbdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbdimensions.dimensions.level.DynamicDimensionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record PrebuiltStructure(ResourceLocation id, ResourceLocation structureLocation, Component name, String author, ResourceLocation structureSetId, int height, ResourceLocation dimensionType) {
    public static final ResourceLocation DEFAULT_IMAGE = new ResourceLocation(FTBTeamDimensions.MOD_ID, "textures/default_start.png");

    public static PrebuiltStructure fromJson(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        if (!json.has("id") || !json.has("name")) {
            throw new JsonSyntaxException("missing 'id' or 'name' field!");
        }

        ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
        ResourceLocation structureLocation = new ResourceLocation(json.get("structure").getAsString());
        Component name = Component.translatable(json.get("name").getAsString());
        String author = json.has("author") ? json.get("author").getAsString() : "FTB Team";
        ResourceLocation structureSetId = json.has("structure_set") ?
                new ResourceLocation(json.get("structure_set").getAsString()) :
                DynamicDimensionManager.DEFAULT_STRUCTURE_SET;
        int height = json.has("height") ? json.get("height").getAsInt() : 0;
        ResourceLocation dimensionType = json.has("dimension_type") ?
                new ResourceLocation(json.get("dimension_type").getAsString()) :
                DynamicDimensionManager.DEFAULT_DIMENSION_TYPE;

        return new PrebuiltStructure(id, structureLocation, name, author, structureSetId, height, dimensionType);
    }

    public static PrebuiltStructure fromBytes(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        ResourceLocation structureLocation = buf.readResourceLocation();
        Component name = buf.readComponent();
        String author = buf.readUtf(256);
        ResourceLocation structureSetId = buf.readResourceLocation();
        int height = buf.readVarInt();
        ResourceLocation dimensionType = buf.readResourceLocation();

        return new PrebuiltStructure(id, structureLocation, name, author, structureSetId, height, dimensionType);
    }

    public ResourceLocation getImage() {
        return new ResourceLocation(id.getNamespace(), "textures/spawn/" + id.getPath() + ".png");
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeResourceLocation(structureLocation);
        buf.writeComponent(name);
        buf.writeUtf(author);
        buf.writeResourceLocation(structureSetId);
        buf.writeVarInt(height);
    }

    @Override
    public String toString() {
        return "PrebuiltStructure{" +
                "id=" + id +
                ", structure=" + structureLocation +
                ", name=" + name +
                ", author='" + author + '\'' +
                ", image=" + getImage() +
                '}';
    }
}
