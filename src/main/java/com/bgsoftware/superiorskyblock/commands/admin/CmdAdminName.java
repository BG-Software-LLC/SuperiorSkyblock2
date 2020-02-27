package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminName implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("name", "setname", "rename");
    }

    @Override
    public String getPermission() {
        return "superior.admin.name";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin name <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_NAME.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

        if(island == null){
            if(args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        String islandName = args[3];

        if(islandName.length() > plugin.getSettings().islandNamesMaxLength){
            Locale.NAME_TOO_LONG.send(sender);
            return;
        }

        if(islandName.length() < plugin.getSettings().islandNamesMinLength){
            Locale.NAME_TOO_SHORT.send(sender);
            return;
        }

        if(plugin.getSettings().filteredIslandNames.stream().anyMatch(name -> islandName.toLowerCase().contains(name.toLowerCase()))){
            Locale.NAME_BLACKLISTED.send(sender);
            return;
        }

        if(island.getName().equals(islandName)){
            Locale.SAME_NAME_CHANGE.send(sender);
            return;
        }

        if(!island.getName().equalsIgnoreCase(islandName) && plugin.getGrid().getIsland(islandName) != null){
            Locale.ISLAND_ALREADY_EXIST.send(sender);
            return;
        }

        String oldName = island.getName();
        island.setName(islandName);

        String coloredName = plugin.getSettings().islandNamesColorSupport ?
                ChatColor.translateAlternateColorCodes('&', islandName) : islandName;

        for(Player player : Bukkit.getOnlinePlayers())
            Locale.NAME_ANNOUNCEMENT.send(player, sender.getName(), coloredName);

        if(targetPlayer == null)
            Locale.CHANGED_NAME_OTHER_NAME.send(sender, oldName, coloredName);
        else
            Locale.CHANGED_NAME_OTHER.send(sender, targetPlayer.getName(), coloredName);
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

        return list;
    }
}
