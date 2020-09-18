package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
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

        List<Mission<?>> missions = new ArrayList<>();

        if(args[4].equals("*")){
            missions.addAll(plugin.getMissions().getAllMissions());
        }
        else{
            Mission<?> mission = plugin.getMissions().getMission(args[4]);

            if(mission == null){
                Locale.INVALID_MISSION.send(sender, args[4]);
                return;
            }

            missions.add(mission);
        }

        if(args[3].equalsIgnoreCase("complete")){
            missions.forEach(mission -> plugin.getMissions().rewardMission(mission, targetPlayer, false, true));
            if(missions.size() == 1)
                Locale.MISSION_STATUS_COMPLETE.send(sender, missions.get(0).getName(), targetPlayer.getName());
            else
                Locale.MISSION_STATUS_COMPLETE_ALL.send(sender, targetPlayer.getName());
            return;
        }
        else if(args[3].equalsIgnoreCase("reset")){
            missions.forEach(targetPlayer::resetMission);
            if(missions.size() == 1)
                Locale.MISSION_STATUS_RESET.send(sender, missions.get(0).getName(), targetPlayer.getName());
            else
                Locale.MISSION_STATUS_RESET_ALL.send(sender, targetPlayer.getName());
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
                    if (player.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }
        else if(args.length == 4){
            list.addAll(Stream.of("complete", "reset").filter(subCommand -> subCommand.contains(args[3].toLowerCase())).collect(Collectors.toList()));
        }
        else if(args.length == 5 && (args[3].equalsIgnoreCase("complete") || args[3].equalsIgnoreCase("reset"))){
            for(Mission<?> mission : plugin.getMissions().getAllMissions()){
                if (args[4].equals("*") || mission.getName().toLowerCase().contains(args[4].toLowerCase()))
                    list.add(mission.getName());
            }
        }

        return list;
    }
}
