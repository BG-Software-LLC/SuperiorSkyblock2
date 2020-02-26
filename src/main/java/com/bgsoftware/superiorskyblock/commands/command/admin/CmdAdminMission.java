package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CmdAdminMission implements ISuperiorCommand {

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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Mission mission = plugin.getMissions().getMission(args[4]);

        if(mission == null){
            Locale.INVALID_MISSION.send(sender, args[4]);
            return;
        }

        if(args[3].equalsIgnoreCase("complete")){
            plugin.getMissions().rewardMission(mission, targetPlayer, false, true);
            Locale.MISSION_STATUS_COMPLETE.send(sender, mission.getName(), targetPlayer.getName());
            return;
        }
        else if(args[3].equalsIgnoreCase("reset")){
            targetPlayer.resetMission(mission);
            mission.clearData(targetPlayer);
            Locale.MISSION_STATUS_RESET.send(sender, mission.getName(), targetPlayer.getName());
            return;
        }

        Locale.COMMAND_USAGE.send(sender,plugin.getCommands().getLabel() + " " + getUsage(LocaleUtils.getLocale(sender)));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }
        else if(args.length == 4){
            list.addAll(Stream.of("complete", "reset").filter(subCommand -> subCommand.startsWith(args[3].toLowerCase())).collect(Collectors.toList()));
        }
        else if(args.length == 5 && (args[3].equalsIgnoreCase("complete") || args[3].equalsIgnoreCase("reset"))){
            for(Mission mission : plugin.getMissions().getAllMissions()){
                if (mission.getName().toLowerCase().startsWith(args[4].toLowerCase()))
                    list.add(mission.getName());
            }
        }

        return list;
    }
}
