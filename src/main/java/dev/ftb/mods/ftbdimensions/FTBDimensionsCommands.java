package dev.ftb.mods.ftbdimensions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.ftb.mods.ftbdimensions.dimensions.DimensionsClient;
import dev.ftb.mods.ftbdimensions.dimensions.DimensionsManager;
import dev.ftb.mods.ftbdimensions.dimensions.arguments.DimensionCommandArgument;
import dev.ftb.mods.ftbdimensions.dimensions.level.ArchivedDimension;
import dev.ftb.mods.ftbdimensions.dimensions.level.DimensionStorage;
import dev.ftb.mods.ftbdimensions.dimensions.level.DynamicDimensionManager;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import dev.ftb.mods.ftbteams.data.TeamType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.util.List;

public class FTBDimensionsCommands {
    private static final LevelResource EXPORT_PATH = new LevelResource("stoneblock-export.png");

    public static final DynamicCommandExceptionType NOT_PARTY_TEAM = new DynamicCommandExceptionType((object) -> Component.literal("[%s] is not a party team...".formatted(object)));
    public static final DynamicCommandExceptionType DIM_MISSING = new DynamicCommandExceptionType((object) -> Component.literal("[%s] can not be found".formatted(object)));
    public static final DynamicCommandExceptionType NO_DIM = new DynamicCommandExceptionType((object) -> Component.literal("No dimension found for %s".formatted(object)));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        FTBDimensions.LOGGER.info("Registering FTB Dimensions Commands");

        LiteralCommandNode<CommandSourceStack> commands = commandDispatcher.register(Commands.literal("ftbdimensions")
                .then(Commands.literal("visit")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("team", TeamArgument.create()).executes(context -> visitDim(context.getSource(), TeamArgument.get(context, "team"))))
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

        commandDispatcher.register(Commands.literal("ftbstoneblock").redirect(commands));
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

        ResourceKey<Level> levelResourceKey = DimensionStorage.get(source.getServer()).putDimension(party, dimension.dimensionName());

        // Remove the dimension from the archived dims
        DimensionStorage.get(source.getServer()).getArchivedDimensions().remove(dimension);
        DimensionStorage.get(source.getServer()).setDirty();

        DynamicDimensionManager.teleport(player, levelResourceKey);

        source.sendSuccess(Component.literal("Successfully restored dimension").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static int prune(CommandSourceStack source, ArchivedDimension dimension) throws CommandSyntaxException {
        List<ArchivedDimension> archivedDimensions = DimensionStorage.get(source.getServer()).getArchivedDimensions();
        if (!archivedDimensions.contains(dimension)) {
            throw DIM_MISSING.create(dimension.dimensionName());
        }

        DynamicDimensionManager.destroy(source.getServer(), ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension.dimensionName()));

        archivedDimensions.remove(dimension);
        DimensionStorage.get(source.getServer()).setDirty();

        source.sendSuccess(Component.literal("Successfully pruned %s".formatted(dimension.dimensionName())).withStyle(ChatFormatting.GREEN), false);
        return 0;
    }

    private static int prune(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        List<ArchivedDimension> archivedDimensions = DimensionStorage.get(source.getServer()).getArchivedDimensions();
        int size = archivedDimensions.size();

        for (ArchivedDimension e : archivedDimensions) {
            DynamicDimensionManager.destroy(server, ResourceKey.create(Registry.DIMENSION_REGISTRY, e.dimensionName()));
        }

        DimensionStorage.get(source.getServer()).getArchivedDimensions().clear();
        DimensionStorage.get(source.getServer()).setDirty();

        source.sendSuccess(Component.literal("Successfully pruned %s dimensions".formatted(size)).withStyle(ChatFormatting.GREEN), false);

        return 0;
    }

    private static int listArchived(CommandSourceStack source) {
        List<ArchivedDimension> archivedDimensions = DimensionStorage.get(source.getServer()).getArchivedDimensions();
        if (archivedDimensions.isEmpty()) {
            source.sendFailure(Component.literal("No archived dimensions available"));
            return -1;
        }

        for (ArchivedDimension archivedDimension : archivedDimensions) {
            source.sendSuccess(Component.literal("%s: [team=%s] [owner=%s]".formatted(
                    archivedDimension.dimensionName(),
                    archivedDimension.teamName(),
                    archivedDimension.teamOwner()
            )), false);
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

        source.sendFailure(Component.literal("Go to the lobby and jump through the portal!"));
        return 0;
    }

    private static int exportBiomes(CommandSourceStack source, int radius) {
        if (radius <= 0) {
            source.sendFailure(Component.literal("Empty image!"));
            return 0;
        }

        source.sendSuccess(Component.literal("Exporting " + (radius * 2 + 1) + "x" + (radius * 2 + 1) + " image..."), false);
        DimensionsClient.exportBiomes(source.getLevel(), source.getServer().getWorldPath(EXPORT_PATH), radius);
        source.sendSuccess(Component.literal("Done!"), false);
        return 1;
    }
}
