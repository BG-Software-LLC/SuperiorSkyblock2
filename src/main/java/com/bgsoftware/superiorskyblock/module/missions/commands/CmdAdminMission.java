package com.bgsoftware.superiorskyblock.module.missions.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MissionArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PlayerCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminMission implements InternalPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("mission");
    }

    @Override
    public String getPermission() {
        return "superior.admin.mission";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_MISSION.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .add(CommandArgument.required("action", StringArgumentType.INSTANCE, "complete/reset"))
                .add(CommandArgument.required("mission", MissionArgumentType.INSTANCE, "all", Message.COMMAND_ARGUMENT_MISSION_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean requireIslandFromPlayer() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PlayerCommandContext context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        SuperiorPlayer targetPlayer = context.getSuperiorPlayer();
        List<Mission<?>> missions = context.getRequiredArgument("mission", List.class);
        String action = context.getRequiredArgument("action", String.class);

        if (action.equalsIgnoreCase("complete")) {
            missions.forEach(mission -> plugin.getMissions().rewardMission(mission, targetPlayer, false, true));
            if (missions.size() == 1)
                Message.MISSION_STATUS_COMPLETE.send(dispatcher, missions.get(0).getName(), targetPlayer.getName());
            else
                Message.MISSION_STATUS_COMPLETE_ALL.send(dispatcher, targetPlayer.getName());
            return;
        } else if (action.equalsIgnoreCase("reset")) {
            Island island = targetPlayer.getIsland();

            boolean anyIslandChanged = false;

            for (Mission<?> mission : missions) {
                IMissionsHolder missionsHolder = mission.getIslandMission() ? island : targetPlayer;
                if (missionsHolder != null && plugin.getEventsBus().callMissionResetEvent(dispatcher, missionsHolder, mission)) {
                    anyIslandChanged = true;
                    missionsHolder.resetMission(mission);
                }
            }

            if (!anyIslandChanged)
                return;

            if (missions.size() == 1)
                Message.MISSION_STATUS_RESET.send(dispatcher, missions.get(0).getName(), targetPlayer.getName());
            else
                Message.MISSION_STATUS_RESET_ALL.send(dispatcher, targetPlayer.getName());
            return;
        }

        throw new CommandSyntaxException("Invalid command syntax");
    }

}
