package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.List;
import java.util.Locale;

public abstract class AbstractMissionArgumentType<E> implements CommandArgumentType<E> {

    protected AbstractMissionArgumentType() {

    }

    protected final Mission<?> parseMission(SuperiorSkyblock plugin, CommandContext context, String name) throws CommandSyntaxException {
        Mission<?> mission = plugin.getMissions().getMission(name);

        if (mission == null) {
            Message.INVALID_MISSION.send(context.getDispatcher(), name);
            throw new CommandSyntaxException("Invalid mission: " + name);
        }

        return mission;
    }

    protected List<String> getMissionSuggestions(SuperiorSkyblock plugin, String argument, List<String> suggestions) {
        for (Mission<?> mission : plugin.getMissions().getAllMissions()) {
            String missionName = mission.getName().toLowerCase(Locale.ENGLISH);
            if (missionName.contains(argument))
                suggestions.add(missionName);
        }

        return suggestions;
    }

}
