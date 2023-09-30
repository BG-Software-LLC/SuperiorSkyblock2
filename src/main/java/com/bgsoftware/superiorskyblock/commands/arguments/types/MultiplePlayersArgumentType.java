package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandSuggestions;
import com.bgsoftware.superiorskyblock.commands.arguments.SuggestionsSelector;
import com.bgsoftware.superiorskyblock.commands.arguments.SuggestionsSelectors;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MultiplePlayersArgumentType extends AbstractPlayerArgumentType<List<SuperiorPlayer>> {

    public static final MultiplePlayersArgumentType ALL_PLAYERS = new MultiplePlayersArgumentType(null, null);
    public static final MultiplePlayersArgumentType ONLINE_PLAYERS = new MultiplePlayersArgumentType(
            SuperiorPlayer::isOnline, SuggestionsSelectors.ONLINE_PLAYERS);


    private MultiplePlayersArgumentType(@Nullable PlayerSelector selector, @Nullable SuggestionsSelector<SuperiorPlayer> suggestionsSelector) {
        super(selector, suggestionsSelector);
    }

    @Override
    public List<SuperiorPlayer> parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        if (name.equals("*")) {
            return new SequentialListBuilder<SuperiorPlayer>()
                    .filter(this.selector)
                    .build(plugin.getPlayers().getAllPlayers());
        }

        return Collections.singletonList(parsePlayer(plugin, context, name));
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        if ("*".contains(argument)) suggestions.add("*");

        return CommandSuggestions.getPlayerSuggestions(plugin, context, argument, this.suggestionsSelector, suggestions);
    }

}
