package dev.ftb.mods.ftbteamdimensions.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbteamdimensions.client.DimensionsClient;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.ArchivedDimension;
import dev.ftb.mods.ftbteamdimensions.dimensions.level.DimensionStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DimensionCommandArgument implements ArgumentType<ArchivedDimension> {
    private static final DynamicCommandExceptionType DIMENSION_NAME_NOT_FOUND = new DynamicCommandExceptionType(
            object -> Component.translatable("ftbteamdimensions.message.missing_dimension", object));

    public static DimensionCommandArgument create() {
        return new DimensionCommandArgument();
    }

    public static ArchivedDimension get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ArchivedDimension.class);
    }

    private DimensionCommandArgument() {
    }

    @Override
    public ArchivedDimension parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(i, reader.getCursor());
        DimensionStorage dimensionStorage = DimensionStorage.get();
        if (dimensionStorage == null) {
            throw DIMENSION_NAME_NOT_FOUND.createWithContext(reader, s);
        }

        Optional<ArchivedDimension> instance = dimensionStorage.getArchivedDimension(new ResourceLocation(s));

        if (instance.isPresent()) {
            return instance.get();
        }

        throw DIMENSION_NAME_NOT_FOUND.createWithContext(reader, s);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder builder) {
        if (commandContext.getSource() instanceof SharedSuggestionProvider) {
            return SharedSuggestionProvider.suggest(DimensionsClient.knownDimensions.stream().map(e -> e.dimensionName().toString()), builder);
        }

        return Suggestions.empty();
    }
}
