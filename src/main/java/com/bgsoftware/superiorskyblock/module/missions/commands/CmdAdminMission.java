package com.bgsoftware.superiorskyblock.module.missions.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminMission implements IAdminPlayerCommand {

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
        return "admin mission <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> <complete/reset> <" + Locale.COMMAND_ARGUMENT_MISSION_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_MISSION.getMessage(locale);
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

        if(missions.isEmpty())
            return;

        if(args[3].equalsIgnoreCase("complete")){
            missions.forEach(mission -> plugin.getMissions().rewardMission(mission, targetPlayer, false, true));
            if(missions.size() == 1)
                Locale.MISSION_STATUS_COMPLETE.send(sender, missions.get(0).getName(), targetPlayer.getName());
            else
                Locale.MISSION_STATUS_COMPLETE_ALL.send(sender, targetPlayer.getName());
            return;
        }
        else if(args[3].equalsIgnoreCase("reset")){
            Island island = targetPlayer.getIsland();

            missions.forEach(mission -> {
                if(mission.getIslandMission()){
                    if(island != null)
                        island.resetMission(mission);
                }
                else{
                    targetPlayer.resetMission(mission);
                }
            });

            if(missions.size() == 1)
                Locale.MISSION_STATUS_RESET.send(sender, missions.get(0).getName(), targetPlayer.getName());
            else
                Locale.MISSION_STATUS_RESET_ALL.send(sender, targetPlayer.getName());
            return;
        }

        Locale.COMMAND_USAGE.send(sender,plugin.getCommands().getLabel() + " " + getUsage(LocaleUtils.getLocale(sender)));
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "complete", "reset") :
                args.length == 5 && args[3].equalsIgnoreCase("complete") || args[3].equalsIgnoreCase("reset") ?
                args[4].equals("*") ? CommandTabCompletes.getAllMissions(plugin) : CommandTabCompletes.getMissions(plugin, args[4]) :
                new ArrayList<>();
    }

}
