package dev.ftb.mods.ftbteamdimensions.mixin.client;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenSettingsComponent.class)
public interface WorldGenSettingsComponentAccess {
    @Accessor("typeButton")
    CycleButton<Holder<WorldPreset>> getTypeButton();
}
