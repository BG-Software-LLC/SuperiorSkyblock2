package com.bgsoftware.superiorskyblock.module.missions.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CmdAdminMission implements IAdminPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("mission");
    }

    @Override
    public String getPermission() {
        return "superior.admin.mission";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin mission <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <complete/reset> <" +
                Message.COMMAND_ARGUMENT_MISSION_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_MISSIONS.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_MISSION.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultiplePlayers() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        List<Mission<?>> missions = CommandArguments.getMultipleMissions(plugin, sender, args[4]);

        if (missions.isEmpty())
            return;

        if (args[3].equalsIgnoreCase("complete")) {
            missions.forEach(mission -> plugin.getMissions().rewardMission(mission, targetPlayer, false, true));
            if (missions.size() == 1)
                Message.MISSION_STATUS_COMPLETE.send(sender, missions.get(0).getName(), targetPlayer.getName());
            else
                Message.MISSION_STATUS_COMPLETE_ALL.send(sender, targetPlayer.getName());
            return;
        } else if (args[3].equalsIgnoreCase("reset")) {
            Island island = targetPlayer.getIsland();

            int islandsChangedCount = 0;

            for (Mission<?> mission : missions) {
                IMissionsHolder missionsHolder = mission.getIslandMission() ? island : targetPlayer;
                if (missionsHolder != null && PluginEventsFactory.callMissionResetEvent(sender, missionsHolder, mission)) {
                    ++islandsChangedCount;
                    missionsHolder.resetMission(mission);
                }
            }

            if (islandsChangedCount <= 0)
                return;

            if (missions.size() == 1)
                Message.MISSION_STATUS_RESET.send(sender, missions.get(0).getName(), targetPlayer.getName());
            else
                Message.MISSION_STATUS_RESET_ALL.send(sender, targetPlayer.getName());
            return;
        }

        Message.COMMAND_USAGE.send(sender, plugin.getCommands().getLabel() + " " + getUsage(PlayerLocales.getLocale(sender)));
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        switch (args.length) {
            case 4:
                return CommandTabCompletes.getCustomComplete(args[3], "complete", "reset");
            case 5: {
                List<String> list = new LinkedList<>();

                if (args[3].equalsIgnoreCase("complete"))
                    list.addAll(CommandTabCompletes.getMissions(plugin, args[4], mission ->
                            plugin.getMissions().canCompleteAgain(targetPlayer, mission)));
                else if (args[3].equalsIgnoreCase("reset"))
                    list.addAll(CommandTabCompletes.getMissions(plugin, args[4], mission ->
                            !plugin.getMissions().canCompleteAgain(targetPlayer, mission)));

                if ("*".contains(args[4]) && !list.isEmpty())
                    list.add("*");

                return Collections.unmodifiableList(list);
            }
        }

        return Collections.emptyList();
    }

}
