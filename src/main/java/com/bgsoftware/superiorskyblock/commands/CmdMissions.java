package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuIslandMissions;
import com.bgsoftware.superiorskyblock.menu.MenuMissions;
import com.bgsoftware.superiorskyblock.menu.MenuPlayerMissions;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CmdMissions implements ISuperiorCommand {

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
        return "missions [" + Locale.COMMAND_ARGUMENT_ISLAND.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_PLAYER.getMessage(locale) + "]";
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
            if(args[1].equalsIgnoreCase(Locale.COMMAND_ARGUMENT_ISLAND.getMessage(superiorPlayer.getUserLocale()))){
                MenuIslandMissions.openInventory(superiorPlayer, null);
                return;
            }
            else if(args[1].equalsIgnoreCase(Locale.COMMAND_ARGUMENT_PLAYER.getMessage(superiorPlayer.getUserLocale()))){
                MenuPlayerMissions.openInventory(superiorPlayer, null);
                return;
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
