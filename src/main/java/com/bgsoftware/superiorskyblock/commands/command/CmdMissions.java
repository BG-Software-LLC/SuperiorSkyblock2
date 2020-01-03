package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.menu.MenuIslandMissions;
import com.bgsoftware.superiorskyblock.menu.MenuMissions;
import com.bgsoftware.superiorskyblock.menu.MenuPlayerMissions;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CmdMissions implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("missions", "challenges");
    }

    @Override
    public String getPermission() {
        return "superior.island.missions";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "missions [island/player]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_MISSIONS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

        if(args.length == 2){
            if(args[1].equalsIgnoreCase("island")){
                MenuIslandMissions.openInventory(superiorPlayer, null);
            }
            else if(args[1].equalsIgnoreCase("player")){
                MenuPlayerMissions.openInventory(superiorPlayer, null);
            }
        }

        MenuMissions.openInventory(superiorPlayer, null);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 2){
            list.addAll(Stream.of("members", "visitors", "toggle")
                    .filter(value -> value.startsWith(args[1].toLowerCase())).collect(Collectors.toList()));
        }

        return list;
    }
}
