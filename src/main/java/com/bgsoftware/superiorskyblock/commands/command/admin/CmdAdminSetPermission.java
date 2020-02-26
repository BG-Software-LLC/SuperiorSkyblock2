package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdAdminSetPermission implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setpermission";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setpermission <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_PERMISSION.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ISLAND_ROLE.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_PERMISSION.getMessage(locale);
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

        IslandPermission islandPermission;

        try{
            islandPermission = IslandPermission.valueOf(args[3].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ISLAND_PERMISSION.send(sender, args[3], StringUtils.getPermissionsString());
            return;
        }

        PlayerRole playerRole;

        try{
            playerRole = SPlayerRole.of(args[4]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ROLE.send(sender, args[4], SPlayerRole.getValuesString());
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setPermission(playerRole, islandPermission, true)));

        if(islands.size() > 1)
            Locale.PERMISSION_CHANGED_ALL.send(sender, StringUtils.format(islandPermission.name()));
        else if(targetPlayer == null)
            Locale.PERMISSION_CHANGED_NAME.send(sender, StringUtils.format(islandPermission.name()), islands.get(0).getName());
        else
            Locale.PERMISSION_CHANGED.send(sender, StringUtils.format(islandPermission.name()), targetPlayer.getName());
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
            list.addAll(Arrays.stream(IslandPermission.values())
                    .map(islandPermission -> islandPermission.toString().toLowerCase())
                    .filter(islandPermissionName -> islandPermissionName.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList())
            );
        }

        else if(args.length == 5){
            list.addAll(plugin.getPlayers().getRoles().stream()
                    .map(playerRole -> playerRole.toString().toLowerCase())
                    .filter(playerRoleName -> playerRoleName.startsWith(args[4].toLowerCase()))
                    .collect(Collectors.toList())
            );
        }

        return list;
    }
}
