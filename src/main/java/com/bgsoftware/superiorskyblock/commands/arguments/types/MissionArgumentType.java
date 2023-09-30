package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.missions.Mission;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MissionArgumentType extends AbstractMissionArgumentType<Mission<?>> {

    public static final MissionArgumentType INSTANCE = new MissionArgumentType();

    private MissionArgumentType() {

    }

    @Override
    public Mission<?> parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();
        return parseMission(plugin, context, argument);
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);
        List<String> suggestions = new LinkedList<>();
        return getMissionSuggestions(plugin, argument, suggestions);
    }

}
