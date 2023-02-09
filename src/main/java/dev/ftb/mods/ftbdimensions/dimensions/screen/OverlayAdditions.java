package dev.ftb.mods.ftbdimensions.dimensions.screen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

// TODO needed anymore?

@Mod.EventBusSubscriber(Dist.CLIENT)
public class OverlayAdditions {
//    @SubscribeEvent
//    public static void overlay(RenderGameOverlayEvent event) {
//        if (!DimensionsClient.debugMode) return;
//        if (event.getType() != RenderGameOverlayEvent.ElementType.DEBUG) return;
//

//        LocalPlayer player = Minecraft.getInstance().player;
//        if (player == null) return;
//
//        int x = player.chunkPosition().getMinBlockX();
//        int z = player.chunkPosition().getMinBlockZ();
//
//        double v = DungeonStructureFeature.circularDistance(BlockPos.ZERO, new Vec3i(x, 0, z));
//        Window window = Minecraft.getInstance().getWindow();
//        Screen.drawCenteredString(event.getMatrixStack(), Minecraft.getInstance().font, "" + v, window.getGuiScaledWidth() / 2, window.getGuiScaledHeight() / 2, 0xFFFFFF);
//    }
}
