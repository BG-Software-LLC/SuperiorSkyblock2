package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public class MissionArgumentType implements CommandArgumentType<Mission<?>> {

    public static final MissionArgumentType INSTANCE = new MissionArgumentType();

    private MissionArgumentType() {

    }

    @Override
    public Mission<?> parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        Mission<?> mission = plugin.getMissions().getMission(argument);

        if (mission == null) {
            Message.INVALID_MISSION.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid mission: " + argument);
        }

        return mission;
    }

}
