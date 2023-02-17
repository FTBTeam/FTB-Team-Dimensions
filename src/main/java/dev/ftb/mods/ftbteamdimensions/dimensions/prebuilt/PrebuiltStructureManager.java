package dev.ftb.mods.ftbteamdimensions.dimensions.prebuilt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev.ftb.mods.ftbteamdimensions.net.SyncPrebuiltStructures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PrebuiltStructureManager {
    private static final PrebuiltStructureManager CLIENT_INSTANCE = new PrebuiltStructureManager();
    private static final PrebuiltStructureManager SERVER_INSTANCE = new PrebuiltStructureManager();

    private final Map<ResourceLocation, PrebuiltStructure> STRUCTURES = new ConcurrentHashMap<>();

    public static PrebuiltStructureManager getClientInstance() {
        return CLIENT_INSTANCE;
    }

    public static PrebuiltStructureManager getServerInstance() {
        return SERVER_INSTANCE;
    }

    public Optional<PrebuiltStructure> getStructure(ResourceLocation id) {
        return Optional.ofNullable(STRUCTURES.get(id));
    }

    public Collection<ResourceLocation> getStructureIds() {
        return STRUCTURES.keySet();
    }

    public Collection<PrebuiltStructure> getStructures() {
        return STRUCTURES.values();
    }

    public void syncFromServer(Collection<PrebuiltStructure> structures) {
        STRUCTURES.clear();
        structures.forEach(s -> STRUCTURES.put(s.id(), s));
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

        public ReloadListener() {
            super(GSON, "ftbdim_prebuilt_structures");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
            getServerInstance().STRUCTURES.clear();

            object.forEach((id, json) -> PrebuiltStructure.fromJson(json).ifPresent(s -> getServerInstance().STRUCTURES.put(id, s)));

            if (ServerLifecycleHooks.getCurrentServer() != null) {
                new SyncPrebuiltStructures(PrebuiltStructureManager.getServerInstance()).sendToAll(ServerLifecycleHooks.getCurrentServer());
            }
        }
    }
}
