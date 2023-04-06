package dev.ftb.mods.ftbteamdimensions.mixin.client;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreateWorldScreen.class)
public interface CreateWorldScreenAccess {
    @Accessor("worldGenSettingsComponent")
    WorldGenSettingsComponent getWorldGenSettingsComponent();
}
