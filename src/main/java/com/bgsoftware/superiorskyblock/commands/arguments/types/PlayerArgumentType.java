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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class PlayerArgumentType extends AbstractPlayerArgumentType<SuperiorPlayer> {

    public static final PlayerArgumentType ALL_PLAYERS = new PlayerArgumentType(null, null);
    public static final PlayerArgumentType ONLINE_PLAYERS = new PlayerArgumentType(SuperiorPlayer::isOnline, SuggestionsSelectors.ONLINE_PLAYERS);

    public static PlayerArgumentType allOf(@Nullable SuggestionsSelector<SuperiorPlayer> selector) {
        return new PlayerArgumentType(null, selector);
    }

    private PlayerArgumentType(@Nullable PlayerSelector selector, @Nullable SuggestionsSelector<SuperiorPlayer> suggestionsSelector) {
        super(selector, suggestionsSelector);
    }

    @Override
    public SuperiorPlayer parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();
        return parsePlayer(plugin, context, name);
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);
        List<String> suggestions = new LinkedList<>();
        return CommandSuggestions.getPlayerSuggestions(plugin, context, argument, this.suggestionsSelector, suggestions);
    }

}
