package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.missions.Mission;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MultipleMissionsArgumentType extends AbstractMissionArgumentType<List<Mission<?>>> {

    public static final MultipleMissionsArgumentType INSTANCE = new MultipleMissionsArgumentType();

    private MultipleMissionsArgumentType() {

    }

    @Override
    public List<Mission<?>> parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        if (argument.equals("*") || argument.equalsIgnoreCase("all")) {
            return plugin.getMissions().getAllMissions();
        }

        return Collections.singletonList(parseMission(plugin, context, argument));
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        if ("*".contains(argument)) suggestions.add("*");
        if ("all".contains(argument)) suggestions.add("all");

        return getMissionSuggestions(plugin, argument, suggestions);
    }

}
