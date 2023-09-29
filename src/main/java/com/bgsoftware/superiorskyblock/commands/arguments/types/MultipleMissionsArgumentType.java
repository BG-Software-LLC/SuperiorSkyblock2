package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;

public class MultipleMissionsArgumentType implements CommandArgumentType<List<Mission<?>>> {

    public static final MultipleMissionsArgumentType INSTANCE = new MultipleMissionsArgumentType();

    private MultipleMissionsArgumentType() {

    }

    @Override
    public List<Mission<?>> parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        if (argument.equals("*") || argument.equalsIgnoreCase("all")) {
            return plugin.getMissions().getAllMissions();
        }

        Mission<?> mission = plugin.getMissions().getMission(argument);

        if (mission == null) {
            Message.INVALID_MISSION.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid mission: " + argument);
        }

        return Collections.singletonList(mission);
    }

}
