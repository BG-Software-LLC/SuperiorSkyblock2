package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;

import java.util.Arrays;
import java.util.List;

public class StringArrayArgumentType implements CommandArgumentType<String[]> {

    private final SuggestionsCollector suggestionsCollector;

    public static StringArrayArgumentType withSuggestions(SuggestionsCollector suggestionsCollector) {
        return new StringArrayArgumentType(suggestionsCollector);
    }

    private StringArrayArgumentType(SuggestionsCollector suggestionsCollector) {
        this.suggestionsCollector = suggestionsCollector;
    }

    @Override
    public String[] parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String[] args = reader.getRaw();
        int startCursor = reader.getCursor();
        reader.setCursor(args.length);
        return startCursor == 0 ? args : Arrays.copyOfRange(args, startCursor, args.length);
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        return this.suggestionsCollector.collect(plugin, context, reader);
    }

    public interface SuggestionsCollector {

        List<String> collect(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException;

    }

}
