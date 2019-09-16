package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdMission implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("mission", "challenge");
    }

    @Override
    public String getPermission() {
        return "superior.island.mission";
    }

    @Override
    public String getUsage() {
        return "island mission complete <mission-name>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_MISSION.getMessage();
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

        if(!args[1].equalsIgnoreCase("complete")){
            Locale.sendMessage(sender, getDescription());
            return;
        }

        Mission mission = plugin.getMissions().getMission(args[2]);

        if(mission == null){
            Locale.INVALID_MISSION.send(superiorPlayer, args[2]);
            return;
        }

        if(!mission.canComplete(superiorPlayer)){
            Locale.MISSION_CANNOT_COMPLETE.send(superiorPlayer);
            return;
        }

        try {
            plugin.getMissions().rewardMission(mission, superiorPlayer, false);
        }catch(IllegalStateException ex){
            Locale.INVALID_MISSION.send(superiorPlayer, args[2]);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
