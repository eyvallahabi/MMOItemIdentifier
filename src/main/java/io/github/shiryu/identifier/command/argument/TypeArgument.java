package io.github.shiryu.identifier.command.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.command.CommandSender;

public class TypeArgument extends ArgumentResolver<CommandSender, Type> {

    @Override
    protected ParseResult<Type> parse(Invocation<CommandSender> invocation, Argument<Type> context, String argument) {
        final Type type = MMOItems.plugin.getTypes().get(argument);

        if (type == null) {
            return ParseResult.failure("Type '" + argument + "' not found.");
        }

        return ParseResult.success(type);
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Type> argument, SuggestionContext context) {
        return SuggestionResult.from(
                Suggestion.from(
                        MMOItems.plugin.getTypes()
                                .getAllTypeNames()
                )
        );
    }
}
