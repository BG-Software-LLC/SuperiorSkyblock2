package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminIgnore implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ignore");
    }

    @Override
    public String getPermission() {
        return "superior.admin.ignore";
    }

    @Override
    public String getUsage() {
        return "island admin ignore <player-name/island-name>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_IGNORE.getMessage();
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

        island.setIgnored(true);

        if(targetPlayer == null)
            Locale.IGNORED_ISLAND_NAME.send(sender, island.getName());
        else
            Locale.IGNORED_ISLAND.send(sender, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island island = onlinePlayer.getIsland();
                if (island != null && !island.isIgnored()) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if (island.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(island.getName());
                }
            }
        }

        return list;
    }
}
