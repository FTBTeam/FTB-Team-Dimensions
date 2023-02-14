package dev.ftb.mods.ftbteamdimensions.dimensions.net;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;

public class FTBDimensionsNet {
    public static final SimpleNetworkManager NET = SimpleNetworkManager.create(FTBTeamDimensions.MOD_ID);

    public static final MessageType SYNC_ARCHIVED_DIMENSIONS = NET.registerS2C("sync_archived_dimensions", SyncArchivedDimensions::new);
    public static final MessageType SHOW_SELECTION_GUI = NET.registerS2C("show_start_selection", ShowSelectionGui::new);
    public static final MessageType CREATE_DIMENSION_FOR_TEAM = NET.registerC2S("create_dimension_for_team", CreateDimensionForTeam::new);
    public static final MessageType UPDATE_DIMENSION_LIST = NET.registerS2C("update_dimensions_list", UpdateDimensionsList::new);
    public static final MessageType SYNC_PREBUILT_STRUCTURES = NET.registerS2C("sync_prebuilt_structures", SyncPrebuiltStructures::new);
}
