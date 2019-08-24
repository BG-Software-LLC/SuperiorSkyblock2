package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CmdAdminDisband implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("disband");
    }

    @Override
    public String getPermission() {
        return "superior.admin.disband";
    }

    @Override
    public String getUsage() {
        return "island admin disband <player-name/island-name>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_DISBAND.getMessage();
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

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.DISBAND_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), sender.getName());
            }
        }

        if(targetPlayer == null)
            Locale.DISBANDED_ISLAND_OTHER_NAME.send(sender, island.getName());
        else
            Locale.DISBANDED_ISLAND_OTHER.send(sender, targetPlayer.getName());

        island.disbandIsland();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                if (onlinePlayer.getIsland() != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if (onlinePlayer.getIsland() != null && onlinePlayer.getIsland().getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(onlinePlayer.getIsland().getName());
                }
            }
        }

        return list;
    }
}
