package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CmdAdminSetSettings implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setsettings");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setsettings";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setsettings <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_SETTINGS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_VALUE.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_SETTINGS.getMessage(locale);
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
        List<Island> islands = new ArrayList<>();

        if(args[2].equalsIgnoreCase("*")){
            islands.addAll(plugin.getGrid().getIslands());
        }

        else {
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if (island == null) {
                if (args[2].equalsIgnoreCase(sender.getName()))
                    Locale.INVALID_ISLAND.send(sender);
                else if (targetPlayer == null)
                    Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
                else
                    Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            islands.add(island);
        }

        IslandSettings islandSettings;

        try{
            islandSettings = IslandSettings.valueOf(args[3].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_SETTINGS.send(sender, args[3], StringUtils.getSettingsString());
            return;
        }

        boolean value = args[4].equalsIgnoreCase("true");

        Executor.data(() -> islands.forEach(island -> {
            if(value)
                island.enableSettings(islandSettings);
            else
                island.disableSettings(islandSettings);
        }));

        if(islands.size() != 1)
            Locale.SETTINGS_UPDATED_ALL.send(sender, StringUtils.format(islandSettings.name()));
        else if(targetPlayer == null)
            Locale.SETTINGS_UPDATED_NAME.send(sender, StringUtils.format(islandSettings.name()), islands.get(0).getName());
        else
            Locale.SETTINGS_UPDATED.send(sender, StringUtils.format(islandSettings.name()), targetPlayer.getName());
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
            list.addAll(Arrays.stream(IslandSettings.values())
                    .map(islandSettings -> islandSettings.toString().toLowerCase())
                    .filter(islandSettingsName -> islandSettingsName.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList())
            );
        }
        else if(args.length == 5){
            list.addAll(Stream.of("true", "false").filter(value -> value.startsWith(args[4].toLowerCase())).collect(Collectors.toList()));
        }

        return list;
    }
}
