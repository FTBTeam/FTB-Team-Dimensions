package dev.ftb.mods.ftbdimensions.dimensions.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbdimensions.dimensions.prebuilt.PrebuiltStructure;
import dev.ftb.mods.ftbdimensions.dimensions.prebuilt.PrebuiltStructureManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class PrebuiltCommandArgument implements ArgumentType<PrebuiltStructure> {
    private static final DynamicCommandExceptionType START_NOT_FOUND
            = new DynamicCommandExceptionType(object -> Component.literal("Prebuilt Structure '" + object + "' not found!"));

    public static PrebuiltCommandArgument create() {
        return new PrebuiltCommandArgument();
    }

    public static PrebuiltStructure get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, PrebuiltStructure.class);
    }

    private PrebuiltCommandArgument() {
    }

    @Override
    public PrebuiltStructure parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(i, reader.getCursor());

        return PrebuiltStructureManager.getServerInstance().getStructure(new ResourceLocation(s))
                .orElseThrow(() -> START_NOT_FOUND.createWithContext(reader, s));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder builder) {
        if (commandContext.getSource() instanceof SharedSuggestionProvider) {
            var ids = PrebuiltStructureManager.getServerInstance().getStructureIds();
            return SharedSuggestionProvider.suggest(ids.stream().map(ResourceLocation::toString), builder);
        }

        return Suggestions.empty();
    }
}
