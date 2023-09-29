package com.bgsoftware.superiorskyblock.module.missions.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MissionArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Arrays;
import java.util.List;

public class CmdMission implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("mission", "challenge");
    }

    @Override
    public String getPermission() {
        return "superior.island.mission";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_MISSION.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("action", StringArgumentType.INSTANCE))
                .add(CommandArgument.required("mission", MissionArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_MISSION_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) throws CommandSyntaxException {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        String action = context.getRequiredArgument("action", String.class);

        if (!action.equalsIgnoreCase("complete"))
            throw new CommandSyntaxException("Invalid action: " + action);

        Mission<?> mission = context.getRequiredArgument("mission", Mission.class);

        List<String> requiredMissions = mission.getRequiredMissions();

        if (!requiredMissions.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            requiredMissions.forEach(requiredMission -> {
                Mission<?> _mission = plugin.getMissions().getMission(requiredMission);
                if (_mission != null && plugin.getMissions().canCompleteAgain(superiorPlayer, _mission))
                    stringBuilder.append(_mission.getName()).append(", ");
            });
            if (stringBuilder.length() != 0) {
                Message.MISSION_NOT_COMPLETE_REQUIRED_MISSIONS.send(superiorPlayer, stringBuilder.substring(0, stringBuilder.length() - 2));
                return;
            }
        }

        if (!plugin.getMissions().canComplete(superiorPlayer, mission)) {
            Message.MISSION_CANNOT_COMPLETE.send(superiorPlayer);
            return;
        }

        try {
            plugin.getMissions().rewardMission(mission, superiorPlayer, false);
        } catch (IllegalStateException ex) {
            Message.INVALID_MISSION.send(superiorPlayer, context.getInputArgument("mission"));
        }
    }

}
