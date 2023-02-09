package dev.ftb.mods.ftbdimensions.dimensions.level;

import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class DimensionCreatedEvent extends Event {
    public final String dimName;
    public final Team team;
    public final ServerPlayer player;
    @Nullable
    public final ServerLevel level;

    public DimensionCreatedEvent(String dimName, Team team, ServerPlayer player, @Nullable ServerLevel level) {
        this.dimName = dimName;
        this.team = team;
        this.player = player;
        this.level = level;
    }
}
