package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdName implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("name", "setname", "rename");
    }

    @Override
    public String getPermission() {
        return "superior.island.name";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "name <" + Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_NAME.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
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
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPrivileges.CHANGE_NAME)){
            Locale.NO_NAME_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.CHANGE_NAME));
            return;
        }

        String islandName = args[1];
        String strippedName = plugin.getSettings().islandNamesColorSupport ?
                ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', islandName)) : islandName;

        if(strippedName.length() > plugin.getSettings().islandNamesMaxLength){
            Locale.NAME_TOO_LONG.send(superiorPlayer);
            return;
        }

        if(strippedName.length() < plugin.getSettings().islandNamesMinLength){
            Locale.NAME_TOO_SHORT.send(superiorPlayer);
            return;
        }

        if(plugin.getSettings().filteredIslandNames.stream().anyMatch(name -> islandName.toLowerCase().contains(name.toLowerCase()))){
            Locale.NAME_BLACKLISTED.send(superiorPlayer);
            return;
        }

        if(island.getName().equals(islandName)){
            Locale.SAME_NAME_CHANGE.send(superiorPlayer);
            return;
        }

        if(!island.getName().equalsIgnoreCase(islandName) && plugin.getGrid().getIsland(islandName) != null){
            Locale.ISLAND_ALREADY_EXIST.send(superiorPlayer);
            return;
        }

        island.setName(islandName);

        String coloredName = plugin.getSettings().islandNamesColorSupport ?
                ChatColor.translateAlternateColorCodes('&', islandName) : islandName;

        for(Player player : Bukkit.getOnlinePlayers())
            Locale.NAME_ANNOUNCEMENT.send(player, superiorPlayer.getName(), coloredName);

        Locale.CHANGED_NAME.send(superiorPlayer, coloredName);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
