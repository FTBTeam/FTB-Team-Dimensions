package dev.ftb.mods.ftbteamdimensions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.commands.arguments.DimensionCommandArgument;
import dev.ftb.mods.ftbteamdimensions.dimensions.DimensionsManager;
import dev.ftb.mods.ftbteamdimensions.dimensions.NetherPortalPlacement;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.ArchivedDimension;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DimensionStorage;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import dev.ftb.mods.ftbteams.data.TeamType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FTBDimensionsCommands {
    private static final DynamicCommandExceptionType NOT_PARTY_TEAM = new DynamicCommandExceptionType(
            object -> Component.translatable("ftbteamdimensions.message.not_a_party", object));
    private static final DynamicCommandExceptionType DIM_MISSING = new DynamicCommandExceptionType(
            object -> Component.translatable("ftbteamdimensions.message.missing_dimension", object));
    private static final DynamicCommandExceptionType NO_DIM = new DynamicCommandExceptionType(
            object -> Component.translatable("ftbteamdimensions.message.no_dim_for_team", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        FTBTeamDimensions.LOGGER.info("Registering FTB Dimensions Commands");

        LiteralCommandNode<CommandSourceStack> commands = commandDispatcher.register(Commands.literal(FTBTeamDimensions.MOD_ID)
                .then(Commands.literal("visit")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("team", TeamArgument.create()).executes(context -> visitDim(context.getSource(), TeamArgument.get(context, "team"))))
                )
                .then(Commands.literal("nether-visit")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("team", TeamArgument.create()).executes(context -> visitNetherDim(context.getSource(), TeamArgument.get(context, "team"))))
                )
                .then(Commands.literal("list-dimensions")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> listDimensions(context.getSource()))
                )
                .then(Commands.literal("list-archived")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> listArchived(context.getSource()))
                )
                .then(Commands.literal("prune-all")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> prune(context.getSource()))
                )
                .then(Commands.literal("prune")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("dimension", DimensionCommandArgument.create())
                                .executes(context -> prune(context.getSource(), DimensionCommandArgument.get(context, "dimension")))
                        )
                )
                .then(Commands.literal("restore")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("dimension", DimensionCommandArgument.create())
                                        .executes(context -> restore(context.getSource(), DimensionCommandArgument.get(context, "dimension"), EntityArgument.getPlayer(context, "player")))
                                )
                        )
                )
                .then(Commands.literal("lobby").executes(context -> lobby(context.getSource())))
                .then(Commands.literal("home").executes(context -> home(context.getSource())))
        );

        commandDispatcher.register(Commands.literal("ftbdim").redirect(commands));
    }

    public static PartyTeam createPartyTeam(ServerPlayer player) throws CommandSyntaxException {
        var o = FTBTeamsAPI.partyCreationOverride;
        FTBTeamsAPI.partyCreationOverride = null;
        PartyTeam party = FTBTeamsAPI.getManager().createParty(player, player.getName().getString() + " Party").getValue();
        FTBTeamsAPI.partyCreationOverride = o;
        return party;
    }

    private static int restore(CommandSourceStack source, ArchivedDimension dimension, ServerPlayer player) throws CommandSyntaxException {
        PartyTeam party = createPartyTeam(player);

        DimensionStorage storage = DimensionStorage.get(source.getServer());
        ResourceKey<Level> levelResourceKey = storage.putDimension(party, dimension.dimensionName());

        // Remove the dimension from the archived dims
        storage.getArchivedDimensions().remove(dimension);
        storage.setDirty();

        source.getServer().executeIfPossible(() -> DynamicDimensionManager.teleport(player, levelResourceKey));

        source.sendSuccess(Component.translatable("ftbteamdimensions.message.restored", dimension.dimensionName()).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static int prune(CommandSourceStack source, ArchivedDimension dimension) throws CommandSyntaxException {
        DimensionStorage storage = DimensionStorage.get(source.getServer());
        List<ArchivedDimension> archivedDimensions = storage.getArchivedDimensions();
        if (!archivedDimensions.contains(dimension)) {
            throw DIM_MISSING.create(dimension.dimensionName());
        }

        DynamicDimensionManager.destroy(source.getServer(), ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension.dimensionName()));

        archivedDimensions.remove(dimension);
        storage.setDirty();

        source.sendSuccess(Component.translatable("ftbteamdimensions.message.pruned_one", dimension.dimensionName()).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static int prune(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        DimensionStorage storage = DimensionStorage.get(source.getServer());
        List<ArchivedDimension> archivedDimensions = storage.getArchivedDimensions();
        int size = archivedDimensions.size();

        for (ArchivedDimension e : archivedDimensions) {
            DynamicDimensionManager.destroy(server, ResourceKey.create(Registry.DIMENSION_REGISTRY, e.dimensionName()));
        }

        storage.getArchivedDimensions().clear();
        storage.setDirty();

        source.sendSuccess(Component.translatable("ftbteamdimensions.message.pruned_all", size).withStyle(ChatFormatting.GREEN), false);

        return 0;
    }

    private static int listDimensions(CommandSourceStack source) {
        DimensionStorage storage = DimensionStorage.get(source.getServer());
        Map<UUID, ResourceLocation> map = storage.getTeamToDimension();

        if (map.isEmpty()) {
            source.sendFailure(Component.translatable("ftbteamdimensions.message.no_dimensions"));
            return -1;
        } else {
            Set<UUID> dimIds = storage.getTeamToDimension().keySet();
            source.sendSuccess(Component.translatable("ftbteamdimensions.message.dim_header", dimIds.size()).withStyle(ChatFormatting.GREEN), false);
            dimIds.forEach(id -> {
                Team team = FTBTeamsAPI.getManager().getTeamByID(id);
                if (team != null) {
                    ResourceKey<Level> key = storage.getDimensionId(team);
                    if (key != null) {
                        source.sendSuccess(Component.literal(key.location().toString()).withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(": [team=%s]".formatted(team.getStringID())).withStyle(ChatFormatting.WHITE)), false);
                    }
                }
            });
            return 0;
        }
    }

    private static int listArchived(CommandSourceStack source) {
        List<ArchivedDimension> archivedDimensions = DimensionStorage.get(source.getServer()).getArchivedDimensions();
        if (archivedDimensions.isEmpty()) {
            source.sendFailure(Component.translatable("ftbteamdimensions.message.no_archived"));
            return -1;
        }

        source.sendSuccess(Component.translatable("ftbteamdimensions.message.dim_header", archivedDimensions.size()).withStyle(ChatFormatting.DARK_GREEN), false);
        for (ArchivedDimension aDim : archivedDimensions) {
            source.sendSuccess(Component.literal(aDim.dimensionName().toString()).withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": [team=%s] [owner=%s]".formatted(aDim.teamName(), aDim.teamOwner())).withStyle(ChatFormatting.GRAY)),
                    false);
        }

        return 0;
    }

    private static int visitDim(CommandSourceStack source, Team team) throws CommandSyntaxException {
        if (team.getType() != TeamType.PARTY) {
            throw NOT_PARTY_TEAM.create(team.getName().getString());
        }

        ResourceKey<Level> dimension = DimensionsManager.INSTANCE.getDimension(team);
        if (dimension == null) {
            throw NO_DIM.create(team.getName().getString());
        }

        DynamicDimensionManager.teleport(source.getPlayerOrException(), dimension);
        return 0;
    }

    private static int visitNetherDim(CommandSourceStack source, Team team) throws CommandSyntaxException {
        if (team.getType() != TeamType.PARTY) {
            throw NOT_PARTY_TEAM.create(team.getName().getString());
        }

        ServerLevel nether = source.getServer().getLevel(Level.NETHER);
        if (nether == null) {
            throw DIM_MISSING.create(team.getName().getString());
        }
        ServerPlayer player = source.getPlayerOrException();
        PortalInfo portalInfo = NetherPortalPlacement.teamSpecificEntryPoint(nether, player, team);

        BlockPos pos = new BlockPos(portalInfo.pos.x(), portalInfo.pos.y(), portalInfo.pos.z());
        ChunkPos chunkpos = new ChunkPos(pos);
        nether.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getId());
        player.stopRiding();
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }
        player.teleportTo(nether, portalInfo.pos.x() + .5D, portalInfo.pos.y() + .01D, portalInfo.pos.z() + .5D, player.getYRot(), player.getXRot());
        player.setPortalCooldown();

        return 0;
    }

    private static int lobby(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (DynamicDimensionManager.teleport(player, Level.OVERWORLD)) {
            return 1;
        }

        return 0;
    }

    private static int home(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Team team = FTBTeamsAPI.getPlayerTeam(player);

        if (team.getType().isParty() && DynamicDimensionManager.teleport(player, DimensionsManager.INSTANCE.getDimension(player))) {
            return 1;
        }

        source.sendFailure(Component.translatable("ftbteamdimensions.message.cant_teleport"));
        return 0;
    }
}
